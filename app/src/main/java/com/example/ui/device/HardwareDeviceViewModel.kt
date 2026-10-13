package com.example.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.HardwareConnectionState
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import com.example.hardware.ble.AccessOsBleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HardwareDeviceViewModel(
    private val repository: AccessOsBleRepository
) : ViewModel() {

    val connectionState: StateFlow<HardwareConnectionState> = repository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HardwareConnectionState.Disconnected)

    val latestEvent: StateFlow<HardwareEvent?> = repository.latestEvent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val deliveryStatus: StateFlow<HardwareDeliveryStatus> = repository.deliveryStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HardwareDeliveryStatus.IDLE)

    private val _testFeedback = MutableStateFlow<String?>(null)
    val testFeedback: StateFlow<String?> = _testFeedback.asStateFlow()

    init {
        viewModelScope.launch {
            repository.events.collect { event ->
                when (event) {
                    is HardwareEvent.DevicePing -> {
                        _testFeedback.value = "🟢 Hardware Ping OK: OLED, LED, and Buzzer acknowledged."
                    }
                    is HardwareEvent.DeviceReady -> {
                        _testFeedback.value = "🟢 Hardware Ready (Firmware v${event.firmwareVersion})"
                    }
                    is HardwareEvent.DeviceFault -> {
                        _testFeedback.value = "⚠️ Hardware Fault: ${event.errorMessage}"
                    }
                    is HardwareEvent.SosButtonPressed -> {
                        _testFeedback.value = "🚨 SOS Button Pressed on ${event.deviceId} [${event.eventId}]"
                    }
                }
            }
        }
    }

    fun scanAndConnect() {
        _testFeedback.value = null
        repository.startScan()
    }

    fun disconnect() {
        repository.disconnect()
        _testFeedback.value = "Disconnected from AccessOS hardware"
    }

    fun testHardwarePing() {
        repository.simulateTestPing()
    }

    fun simulateTestSosPress() {
        repository.simulateTestSosPress()
    }

    fun clearFeedback() {
        _testFeedback.value = null
    }

    fun updatePermissions() {
        repository.updatePermissions()
    }
}
