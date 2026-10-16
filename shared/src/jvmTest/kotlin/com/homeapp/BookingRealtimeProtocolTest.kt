package com.homeapp
import com.homeapp.data.cloud.*
import kotlinx.serialization.json.*
import kotlin.test.*

class BookingRealtimeProtocolTest {
    @Test fun joinCarriesJwtAndOnlyBookingTableBindings() {
        val frame=BookingRealtimeProtocol.parse(BookingRealtimeProtocol.join("realtime:bookings","test-token"))!!
        assertEquals("phx_join",frame.string("event"))
        val payload=frame["payload"]!!.jsonObject
        assertEquals("test-token",payload.string("access_token"))
        val config=payload["config"]!!.jsonObject
        assertEquals(BookingRealtimeProtocol.tables,config["postgres_changes"]!!.jsonArray.map {it.jsonObject.string("table")}.toSet())
    }
    @Test fun onlyAcknowledgedCompleteBindingsEstablishSubscription() {
        val payload=json("status" to "ok","response" to json("postgres_changes" to JsonArray(BookingRealtimeProtocol.tables.map {json("table" to it)})))
        val reply=json("event" to "phx_reply","ref" to "1","payload" to payload)
        assertTrue(BookingRealtimeProtocol.joined(reply))
        assertFailsWith<IllegalArgumentException> {BookingRealtimeProtocol.joined(json("event" to "phx_reply","ref" to "1","payload" to json("status" to "ok","response" to json())))}
        assertFalse(BookingRealtimeProtocol.joined(json("event" to "phx_reply","ref" to "42","payload" to payload)))
    }
    @Test fun postgresReadinessIsSeparateFromChannelAcknowledgement() {
        assertTrue(BookingRealtimeProtocol.postgresReady(json("event" to "system","payload" to json("extension" to "postgres_changes","status" to "ok"))))
        assertFalse(BookingRealtimeProtocol.postgresReady(json("event" to "phx_reply","payload" to json("status" to "ok"))))
        assertFalse(BookingRealtimeProtocol.postgresReady(json("event" to "system","payload" to json("extension" to "postgres_changes","status" to "error"))))
    }
    @Test fun onlyKnownPublicChangesInvalidateSnapshots() {
        fun event(schema: String,table: String)=json("event" to "postgres_changes","payload" to json("data" to json("schema" to schema,"table" to table)))
        assertEquals("marketplace_bookings",BookingRealtimeProtocol.changedTable(event("public","marketplace_bookings")))
        assertNull(BookingRealtimeProtocol.changedTable(event("private","marketplace_bookings")))
        assertNull(BookingRealtimeProtocol.changedTable(event("public","users")))
        assertNull(BookingRealtimeProtocol.parse("malformed"))
    }
    @Test fun channelAndSubscriptionFailuresRequireReconnect() {
        assertFailsWith<IllegalArgumentException> {BookingRealtimeProtocol.requireHealthy(json("event" to "phx_close"))}
        assertFailsWith<IllegalArgumentException> {BookingRealtimeProtocol.requireHealthy(json("event" to "system","payload" to json("status" to "error")))}
        BookingRealtimeProtocol.requireHealthy(json("event" to "phx_reply"))
    }
}
