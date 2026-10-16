package com.homeapp.data.cloud

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import com.homeapp.core.time.currentTimeEpochSeconds

object CloudRuntime {
    var url = ""; private set
    var key = ""; private set
    var preview = false; private set
    var loadSession: () -> String? = { null }
    var saveSession: (String?) -> Unit = {}
    val enabled get() = url.isNotBlank() && key.isNotBlank() && !preview
    fun configure(url: String, key: String, preview: Boolean = false) {
        require(url.isBlank() || url.startsWith("https://")) { "Supabase requires HTTPS" }
        require(!key.startsWith("sb_secret_")) { "Never ship a Supabase secret key" }
        this.url = url.trimEnd('/'); this.key = key; this.preview = preview
    }
}
internal fun json(vararg values: Pair<String, Any?>): JsonObject = JsonObject(values.associate { (k,v) -> k to when(v) {
    null -> JsonNull; is JsonElement -> v; is Boolean -> JsonPrimitive(v); is Number -> JsonPrimitive(v); else -> JsonPrimitive(v.toString())
} })
internal fun JsonObject.string(k: String, default: String = "") = (get(k) as? JsonPrimitive)?.contentOrNull ?: default
internal fun JsonObject.long(k: String, default: Long = 0) = (get(k) as? JsonPrimitive)?.longOrNull ?: default
internal fun JsonObject.double(k: String, default: Double = 0.0) = (get(k) as? JsonPrimitive)?.doubleOrNull ?: default
internal fun JsonObject.bool(k: String) = (get(k) as? JsonPrimitive)?.booleanOrNull == true
internal fun JsonObject.epoch(k: String): Long = runCatching { kotlin.time.Instant.parse(string(k)).epochSeconds }.getOrDefault(0)
internal fun JsonElement.rows(): List<JsonObject> = (this as? JsonArray)?.map { it.jsonObject } ?: emptyList()

object SupabaseGateway {
    private val client by lazy { HttpClient { install(io.ktor.client.plugins.HttpTimeout) { requestTimeoutMillis=20000; connectTimeoutMillis=15000 } } }
    val identity = MutableStateFlow<String?>(null)
    val error = MutableStateFlow<String?>(null)
    private var access = ""
    private var refresh = ""
    private var expires = 0L
    private val refreshLock = Mutex()
    fun acceptSession(session: JsonObject) {
        access=session.string("access_token"); refresh=session.string("refresh_token")
        expires=session.long("expires_at",currentTimeEpochSeconds()+session.long("expires_in",3600))
        identity.value=(session["user"] as? JsonObject)?.string("id")?.takeIf {it.isNotBlank()}
        require(access.isNotBlank() && identity.value!=null) { "Confirm your email, then sign in." }
        CloudRuntime.saveSession(JsonObject(session + ("expires_at" to JsonPrimitive(expires))).toString()); error.value=null
    }
    suspend fun restore(): Boolean {
        val saved=CloudRuntime.loadSession() ?: return false
        return try { acceptSession(Json.parseToJsonElement(saved).jsonObject); token(); true }
        catch(e: Exception) { if(e is CancellationException) throw e; clear(); false }
    }
    fun clear() { access="";refresh="";expires=0;identity.value=null;CloudRuntime.saveSession(null) }
    private suspend fun token(): String = refreshLock.withLock {
        require(access.isNotBlank()) { "Sign in to your cloud account" }
        if(expires < currentTimeEpochSeconds()+60) acceptSession(auth("token?grant_type=refresh_token",json("refresh_token" to refresh)).jsonObject)
        access
    }
    internal suspend fun accessToken(): String = token()
    suspend fun auth(path: String, payload: JsonObject): JsonElement = request("auth/v1/$path",HttpMethod.Post,payload,false)
    suspend fun logout() { request("auth/v1/logout",HttpMethod.Post) }
    suspend fun rpc(name: String,payload: JsonObject = json()): JsonElement = request("rest/v1/rpc/$name",HttpMethod.Post,payload)
    suspend fun read(path: String): JsonElement = request("rest/v1/$path",HttpMethod.Get)
    suspend fun write(path: String,payload: JsonElement,method: HttpMethod=HttpMethod.Post): JsonElement = request("rest/v1/$path",method,payload)
    private suspend fun request(path: String,method: HttpMethod,payload: JsonElement?=null,authenticated: Boolean=true): JsonElement {
        val bearer=if(authenticated) token() else null
        val response=client.request("${CloudRuntime.url}/$path") {
            this.method=method; header("apikey",CloudRuntime.key)
            if(bearer!=null) bearerAuth(bearer)
            contentType(ContentType.Application.Json); header("Prefer","return=representation,resolution=merge-duplicates")
            if(payload!=null) setBody(payload.toString())
        }
        val body=response.bodyAsText()
        if(response.status.value !in 200..299) {
            val message=runCatching {Json.parseToJsonElement(body).jsonObject.let {it.string("msg",it.string("message",it.string("error_description","Cloud request failed (${response.status.value})")))}}.getOrDefault("Cloud request failed (${response.status.value})")
            error.value=message; throw IllegalStateException(message)
        }
        error.value=null
        return if(body.isBlank()) JsonNull else Json.parseToJsonElement(body)
    }
    suspend fun uploadPhoto(bookingId: Long, photo: ServicePhoto): String {
        require(photo.bytes.size in 1..5*1024*1024 && photo.mime in listOf("image/jpeg","image/png","image/webp")) { "Choose JPEG, PNG or WebP up to 5 MB" }
        val name=rpc("prepare_booking_photo",json("p_booking" to bookingId,"p_mime" to photo.mime,"p_client_key" to photo.key)).jsonPrimitive.content
        val response=client.post("${CloudRuntime.url}/storage/v1/object/service-photos/$name") {
            header("apikey",CloudRuntime.key); bearerAuth(token()); contentType(ContentType.parse(photo.mime)); setBody(photo.bytes)
        }
        if(response.status.value !in 200..299 && response.status.value != 409) throw IllegalStateException("Photo upload failed. Open the booking to retry.")
        rpc("confirm_booking_photo",json("p_path" to name)); return name
    }
    suspend fun photoUrl(path: String): String {
        val response=request("storage/v1/object/sign/service-photos/$path",HttpMethod.Post,json("expiresIn" to 300)).jsonObject
        return CloudRuntime.url+"/storage/v1"+response.string("signedURL",response.string("signedUrl"))
    }
}
data class ServicePhoto(val name: String,val mime: String,val bytes: ByteArray,val key: String = kotlin.uuid.Uuid.random().toString())
object ServicePhotoPicker {
    var saveLocal: (Long, ServicePhoto) -> String = { _, _ -> error("Photo storage requires Android") }
    var pick: ((List<ServicePhoto>,String?) -> Unit) -> Unit = { callback -> callback(emptyList(),"Photo selection is supported in the Android app.") }
}
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> cloudFlow(empty: T, realtimeTables: Set<String> = emptySet(), block: suspend () -> T): Flow<T> = SupabaseGateway.identity.flatMapLatest { id ->
    if(id==null) flowOf(empty) else flow {
        val fallback = flow { emit(Unit); while(currentCoroutineContext().isActive) {delay(8000);emit(Unit)} }
        val triggers = if(realtimeTables.isEmpty()) fallback else merge(fallback,
            SupabaseRealtime.invalidations.filter {it=="*" || it in realtimeTables}.map {Unit})
        triggers.conflate().collect {
            try {emit(block())} catch(e: Exception) {if(e is CancellationException) throw e;SupabaseGateway.error.value=e.message;emit(empty)}
        }
    }
}
