package com.example.hardware

import com.example.domain.model.HardwareConnectionState
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HardwareBleEventTest {

    @Test
    fun `test parse valid json sos event`() {
        val jsonString = """{"version":1,"event":"SOS_BUTTON_PRESSED","deviceId":"AccessOS-ESP32","eventId":"evt_12345","timestamp":1790000000}"""
        val json = JSONObject(jsonString)

        assertEquals("SOS_BUTTON_PRESSED", json.getString("event"))
        assertEquals("AccessOS-ESP32", json.getString("deviceId"))
        assertEquals("evt_12345", json.getString("eventId"))

        val event = HardwareEvent.SosButtonPressed(
            deviceId = json.getString("deviceId"),
            eventId = json.getString("eventId"),
            timestamp = json.getLong("timestamp")
        )

        assertEquals("AccessOS-ESP32", event.deviceId)
        assertEquals("evt_12345", event.eventId)
    }

    @Test
    fun `test hardware connection states`() {
        val connectedState = HardwareConnectionState.Connected(
            deviceName = "AccessOS-ESP32",
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            lastSeenTimestamp = 1000L
        )

        assertEquals("AccessOS-ESP32", connectedState.deviceName)
        assertEquals("AA:BB:CC:DD:EE:FF", connectedState.deviceAddress)
    }

    @Test
    fun `test hardware delivery status enum`() {
        assertEquals(HardwareDeliveryStatus.IDLE, HardwareDeliveryStatus.valueOf("IDLE"))
        assertEquals(HardwareDeliveryStatus.RECEIVED_BY_PHONE, HardwareDeliveryStatus.valueOf("RECEIVED_BY_PHONE"))
        assertEquals(HardwareDeliveryStatus.DISPATCHING_SOS, HardwareDeliveryStatus.valueOf("DISPATCHING_SOS"))
        assertEquals(HardwareDeliveryStatus.CLOUD_CONFIRMED, HardwareDeliveryStatus.valueOf("CLOUD_CONFIRMED"))
        assertEquals(HardwareDeliveryStatus.LOCAL_ONLY_OFFLINE, HardwareDeliveryStatus.valueOf("LOCAL_ONLY_OFFLINE"))
    }
}
