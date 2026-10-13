package com.example.hardware.ble

import com.example.domain.model.HardwareConnectionState
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AccessOsBleRepository {
    val connectionState: StateFlow<HardwareConnectionState>
    val events: SharedFlow<HardwareEvent>
    val latestEvent: StateFlow<HardwareEvent?>
    val deliveryStatus: StateFlow<HardwareDeliveryStatus>

    fun startScan()
    fun stopScan()
    fun disconnect()
    fun updatePermissions()
    fun setDeliveryStatus(status: HardwareDeliveryStatus)
    fun simulateTestSosPress(deviceId: String = "AccessOS-ESP32 (Test Mode)")
    fun simulateTestPing()
}

class AccessOsBleRepositoryImpl(
    private val bleManager: AccessOsBleManager
) : AccessOsBleRepository {
    override val connectionState: StateFlow<HardwareConnectionState> = bleManager.connectionState
    override val events: SharedFlow<HardwareEvent> = bleManager.events
    override val latestEvent: StateFlow<HardwareEvent?> = bleManager.latestEvent
    override val deliveryStatus: StateFlow<HardwareDeliveryStatus> = bleManager.deliveryStatus

    override fun startScan() = bleManager.startScan()
    override fun stopScan() = bleManager.stopScan()
    override fun disconnect() = bleManager.disconnect()
    override fun updatePermissions() = bleManager.updateInitialStatus()
    override fun setDeliveryStatus(status: HardwareDeliveryStatus) = bleManager.setDeliveryStatus(status)
    override fun simulateTestSosPress(deviceId: String) = bleManager.simulateTestSosPress(deviceId)
    override fun simulateTestPing() = bleManager.simulateTestPing()
}

open class FakeBleRepository : AccessOsBleRepository {
    override val connectionState: StateFlow<HardwareConnectionState> =
        MutableStateFlow(HardwareConnectionState.Disconnected)
    override val events: SharedFlow<HardwareEvent> =
        MutableSharedFlow()
    override val latestEvent: StateFlow<HardwareEvent?> =
        MutableStateFlow(null)
    override val deliveryStatus: StateFlow<HardwareDeliveryStatus> =
        MutableStateFlow(HardwareDeliveryStatus.IDLE)

    override fun startScan() {}
    override fun stopScan() {}
    override fun disconnect() {}
    override fun updatePermissions() {}
    override fun setDeliveryStatus(status: HardwareDeliveryStatus) {}
    override fun simulateTestSosPress(deviceId: String) {}
    override fun simulateTestPing() {}
}
