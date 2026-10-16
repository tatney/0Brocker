package com.homeapp.data.cloud

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

internal enum class RealtimeState { DISCONNECTED, CONNECTING, LIVE, RECONNECTING }

/** Phoenix v1 frames. Records are invalidation hints only; authenticated REST rechecks RLS. */
internal object BookingRealtimeProtocol {
    val tables = setOf("marketplace_bookings", "quotes", "booking_workflow", "booking_events", "booking_photos", "reviews")
    fun frame(topic: String,event: String,payload: JsonObject,ref: String,joinRef: String? = "1") =
        json("topic" to topic,"event" to event,"payload" to payload,"ref" to ref,"join_ref" to joinRef).toString()
    fun join(topic: String,token: String): String = frame(topic,"phx_join",json(
        "access_token" to token,
        "config" to json("private" to false,"broadcast" to json("ack" to false,"self" to false),"presence" to json("enabled" to false),
            "postgres_changes" to JsonArray(tables.map {json("event" to "*","schema" to "public","table" to it)}))
    ),"1")
    fun parse(text: String): JsonObject? = runCatching {Json.parseToJsonElement(text).jsonObject}.getOrNull()
    fun joined(message: JsonObject): Boolean {
        if(message.string("event")!="phx_reply" || message.string("ref")!="1") return false
        val payload=message["payload"] as? JsonObject ?: return false
        if(payload.string("status")!="ok") error("Realtime subscription rejected")
        val response=payload["response"] as? JsonObject ?: error("Missing realtime subscription acknowledgement")
        val bindings=(response["postgres_changes"] as? JsonArray)?.mapNotNull {(it as? JsonObject)?.string("table")}?.toSet()
        require(bindings==tables) {"Booking realtime bindings are incomplete"}
        return true
    }
    fun postgresReady(message: JsonObject): Boolean {
        val payload=message["payload"] as? JsonObject ?: return false
        return message.string("event")=="system" && payload.string("extension")=="postgres_changes" && payload.string("status")=="ok"
    }
    fun changedTable(message: JsonObject): String? {
        if(message.string("event")!="postgres_changes") return null
        val data=((message["payload"] as? JsonObject)?.get("data") as? JsonObject) ?: return null
        return data.string("table").takeIf {data.string("schema")=="public" && it in tables}
    }
    fun requireHealthy(message: JsonObject) {
        require(message.string("event") !in setOf("phx_error","phx_close")) {"Booking realtime connection closed"}
        if(message.string("event")=="system") {
            val payload=message["payload"] as? JsonObject
            require(payload?.string("status")!="error") {"Booking realtime subscription unavailable"}
        }
    }
}

/** One authenticated socket shared by visible booking observers; stops on logout/no observers. */
@OptIn(ExperimentalCoroutinesApi::class)
internal object SupabaseRealtime {
    private val client by lazy {HttpClient {install(WebSockets)}}
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    val state=MutableStateFlow(RealtimeState.DISCONNECTED)
    internal val diagnostics=MutableStateFlow(emptyList<String>())
    private fun note(message: JsonObject) {
        val payload=message["payload"] as? JsonObject
        val data=payload?.get("data") as? JsonObject
        val summary=message.string("event")+": schema="+(data?.string("schema")?:"")+" table="+(data?.string("table")?:"")+" status="+(payload?.string("status")?:"")+" keys="+(payload?.keys?.joinToString(",")?:"")
        diagnostics.value=(diagnostics.value+summary).takeLast(16)
    }
    val invalidations: SharedFlow<String> = SupabaseGateway.identity.flatMapLatest { identity ->
        if(identity==null) emptyFlow() else channelFlow {
            val events=this
            var retryMs=1000L
            while(currentCoroutineContext().isActive) {
                state.value=if(retryMs==1000L) RealtimeState.CONNECTING else RealtimeState.RECONNECTING
                try {
                    val topic="realtime:mobile-bookings-$identity"
                    val endpoint=CloudRuntime.url.replaceFirst("https://","wss://")+"/realtime/v1/websocket?apikey="+CloudRuntime.key.encodeURLParameter()+"&vsn=1.0.0"
                    client.webSocket(endpoint) {
                        var jwt=SupabaseGateway.accessToken()
                        send(Frame.Text(BookingRealtimeProtocol.join(topic,jwt)))
                        withTimeout(15000) {
                            var acknowledged=false
                            var postgresReady=false
                            while(!acknowledged || !postgresReady) {
                                val frame=incoming.receive() as? Frame.Text ?: continue
                                val message=BookingRealtimeProtocol.parse(frame.readText()) ?: continue
                                note(message)
                                BookingRealtimeProtocol.requireHealthy(message)
                                if(BookingRealtimeProtocol.joined(message)) acknowledged=true
                                if(BookingRealtimeProtocol.postgresReady(message)) postgresReady=true
                            }
                        }
                        retryMs=1000L
                        state.value=RealtimeState.LIVE
                        events.send("*") // Re-fetch after every join/reconnect to close snapshot races.
                        coroutineScope {
                            val acknowledgements=Channel<String>(Channel.BUFFERED)
                            val heartbeat=launch {
                                var seq=2L
                                while(isActive) {
                                    delay(20000)
                                    val fresh=SupabaseGateway.accessToken()
                                    if(fresh!=jwt) {jwt=fresh;send(Frame.Text(BookingRealtimeProtocol.frame(topic,"access_token",json("access_token" to jwt),(seq++).toString())))}
                                    val ref=(seq++).toString()
                                    send(Frame.Text(BookingRealtimeProtocol.frame("phoenix","heartbeat",json(),ref,null)))
                                    try {withTimeout(10000) {while(acknowledgements.receive()!=ref) { /* Ignore other replies. */ }}}
                                    catch(e: TimeoutCancellationException) {error("Realtime heartbeat timed out")}
                                }
                            }
                            try {
                                for(frame in incoming) {
                                    if(frame !is Frame.Text) continue
                                    val message=BookingRealtimeProtocol.parse(frame.readText()) ?: continue
                                    BookingRealtimeProtocol.requireHealthy(message)
                                    if(message.string("event")=="phx_reply") acknowledgements.trySend(message.string("ref"))
                                    BookingRealtimeProtocol.changedTable(message)?.let {events.send(it)}
                                }
                            } finally {heartbeat.cancel();acknowledgements.close()}
                        }
                    }
                } catch(e: Exception) {
                    if(e is CancellationException) throw e
                    diagnostics.value=(diagnostics.value+(e.message?:"Realtime connection error")).takeLast(16)
                    state.value=RealtimeState.RECONNECTING
                }
                delay(retryMs);retryMs=(retryMs*2).coerceAtMost(30000)
            }
        }.onCompletion {state.value=RealtimeState.DISCONNECTED}
    }.shareIn(scope,SharingStarted.WhileSubscribed(stopTimeoutMillis=5000,replayExpirationMillis=0),replay=1)
}
