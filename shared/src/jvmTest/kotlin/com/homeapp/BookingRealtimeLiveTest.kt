package com.homeapp

import com.homeapp.data.cloud.*
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.junit.Assume.assumeTrue
import kotlin.test.*

/** Opt-in acceptance against an explicitly created, unassigned and later removed QA booking. */
class BookingRealtimeLiveTest {
    @Test fun authenticatedSocketInvalidatesBookingImmediately() = runBlocking {
        val root=System.getenv("HOMEAPP_LIVE_REALTIME_ROOT")
        assumeTrue("Explicit live QA fixture required",!root.isNullOrBlank())
        val fixture=Json.parseToJsonElement(File(root,".scratch/realtime-fixture.json").readText()).jsonObject
        val accounts=Json.parseToJsonElement(File(root,".scratch/live-test-accounts.json").readText()).jsonObject["accounts"]!!.jsonArray
        val customer=accounts.map {it.jsonObject}.first {it.string("role")=="CUSTOMER"}
        val id=fixture.long("bookingId")
        CloudRuntime.configure(fixture.string("url"),fixture.string("anonKey"))
        try {
            SupabaseGateway.acceptSession(SupabaseGateway.auth("token?grant_type=password",json("email" to customer.string("email"),"password" to customer.string("password"))).jsonObject)
            val event=async(start=CoroutineStart.UNDISPATCHED) {SupabaseRealtime.invalidations.first {it=="marketplace_bookings"}}
            withTimeout(45000) {SupabaseRealtime.state.first {it==RealtimeState.LIVE}}
            SupabaseGateway.rpc("set_booking_payment_method",json("p_booking" to id,"p_method" to "Cash"))
            try {assertEquals("marketplace_bookings",withTimeout(5000) {event.await()})} catch(e: TimeoutCancellationException) {error("No native event. State="+SupabaseRealtime.state.value+" diagnostics="+SupabaseRealtime.diagnostics.value)}
            val snapshots=Channel<String>(Channel.CONFLATED)
            val observer=launch {cloudFlow("",setOf("marketplace_bookings")) {
                SupabaseGateway.read("marketplace_bookings?select=payment_method&id=eq.$id").rows().single().string("payment_method")
            }.collect {snapshots.send(it)}}
            try {
                withTimeout(10000) {while(snapshots.receive()!="Cash") {}}
                SupabaseGateway.rpc("set_booking_payment_method",json("p_booking" to id,"p_method" to "Mobile Money"))
                // This deadline is shorter than the eight-second fallback poll.
                withTimeout(5000) {while(snapshots.receive()!="Mobile Money") {}}
            } finally {observer.cancelAndJoin();snapshots.close()}
            println("Realtime metadata proof: "+SupabaseRealtime.diagnostics.value)
        } finally {SupabaseGateway.clear();CloudRuntime.configure("","",false)}
        Unit
    }
}
