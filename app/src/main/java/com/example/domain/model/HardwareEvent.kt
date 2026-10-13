package com.example.domain.model

sealed class HardwareEvent {
    abstract val deviceId: String
    abstract val timestamp: Long

    data class SosButtonPressed(
        override val deviceId: String,
        val eventId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : HardwareEvent()

    data class DevicePing(
        override val deviceId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val message: String = "Ping response from hardware"
    ) : HardwareEvent()

    data class DeviceReady(
        override val deviceId: String,
        val firmwareVersion: String = "1.0.0",
        override val timestamp: Long = System.currentTimeMillis()
    ) : HardwareEvent()

    data class DeviceFault(
        override val deviceId: String,
        val errorMessage: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : HardwareEvent()
}

sealed class HardwareConnectionState {
    object Disconnected : HardwareConnectionState()
    object BluetoothOff : HardwareConnectionState()
    object PermissionRequired : HardwareConnectionState()
    data class Scanning(val secondsRemaining: Int = 10) : HardwareConnectionState()
    data class Connecting(val deviceName: String) : HardwareConnectionState()
    data class Connected(
        val deviceName: String,
        val deviceAddress: String,
        val lastSeenTimestamp: Long = System.currentTimeMillis()
    ) : HardwareConnectionState()
    data class Error(val message: String) : HardwareConnectionState()
    data class Unavailable(val reason: String) : HardwareConnectionState()
}

enum class HardwareDeliveryStatus {
    IDLE,
    RECEIVED_BY_PHONE,
    DISPATCHING_SOS,
    CLOUD_CONFIRMED,
    LOCAL_ONLY_OFFLINE,
    FAILED
}
