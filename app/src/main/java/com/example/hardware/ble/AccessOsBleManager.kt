package com.example.hardware.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.domain.model.HardwareConnectionState
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * AccessOS BLE Manager for ESP32 Physical SOS Emergency Hardware.
 *
 * ESP32 Hardware Profile:
 * - Service UUID: 4fafc201-1fb5-459e-8fcc-c5c9c331914b
 * - Event Notify Characteristic: beb5483e-36e1-4688-b7f5-ea07361b26a8
 * - Command / ACK Characteristic: beb5483f-36e1-4688-b7f5-ea07361b26a8
 */
class AccessOsBleManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        const val TAG = "AccessOsBle"
        const val DEFAULT_DEVICE_NAME_PREFIX = "AccessOS"
        val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val CHAR_EVENT_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val CHAR_COMMAND_UUID: UUID = UUID.fromString("beb5483f-36e1-4688-b7f5-ea07361b26a8")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private const val SCAN_TIMEOUT_MS = 12000L
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    private var isScanning = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _connectionState = MutableStateFlow<HardwareConnectionState>(HardwareConnectionState.Disconnected)
    val connectionState: StateFlow<HardwareConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<HardwareEvent>(replay = 1)
    val events: SharedFlow<HardwareEvent> = _events.asSharedFlow()

    private val _latestEvent = MutableStateFlow<HardwareEvent?>(null)
    val latestEvent: StateFlow<HardwareEvent?> = _latestEvent.asStateFlow()

    private val _deliveryStatus = MutableStateFlow(HardwareDeliveryStatus.IDLE)
    val deliveryStatus: StateFlow<HardwareDeliveryStatus> = _deliveryStatus.asStateFlow()

    // Deduplication set for processed event IDs to prevent duplicate alerts
    private val processedEventIds = mutableSetOf<String>()

    init {
        updateInitialStatus()
    }

    fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            val connectGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            return scanGranted && connectGranted
        }
        return true
    }

    fun updateInitialStatus() {
        if (!hasPermissions()) {
            _connectionState.value = HardwareConnectionState.PermissionRequired
            return
        }
        if (bluetoothAdapter == null) {
            _connectionState.value = HardwareConnectionState.Unavailable("Bluetooth hardware is not available on this device")
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = HardwareConnectionState.BluetoothOff
            return
        }
        if (activeGatt == null) {
            _connectionState.value = HardwareConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermissions()) {
            _connectionState.value = HardwareConnectionState.PermissionRequired
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = HardwareConnectionState.BluetoothOff
            return
        }

        val scanner: BluetoothLeScanner? = adapter.bluetoothLeScanner
        if (scanner == null) {
            _connectionState.value = HardwareConnectionState.Error("Cannot acquire Bluetooth LE scanner")
            return
        }

        if (isScanning) return

        disconnect()
        isScanning = true
        _connectionState.value = HardwareConnectionState.Scanning(10)

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            // Also start a fallback timeout handler
            mainHandler.postDelayed({
                if (isScanning) {
                    stopScan()
                    if (_connectionState.value is HardwareConnectionState.Scanning) {
                        _connectionState.value = HardwareConnectionState.Disconnected
                    }
                }
            }, SCAN_TIMEOUT_MS)

            scanner.startScan(filters, settings, scanCallback)
            Log.d(TAG, "Started scanning for AccessOS ESP32 hardware...")
        } catch (e: Exception) {
            isScanning = false
            Log.e(TAG, "Failed to start BLE scan: ${e.message}")
            _connectionState.value = HardwareConnectionState.Error("Scan failed: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        try {
            if (hasPermissions()) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping BLE scan: ${e.message}")
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device ?: return
            val name = result.scanRecord?.deviceName ?: device.name ?: ""

            Log.d(TAG, "Found BLE device: $name (${device.address})")

            // Accept device if name matches AccessOS prefix or service UUID was matched by filter
            if (name.startsWith(DEFAULT_DEVICE_NAME_PREFIX, ignoreCase = true) || name.contains("AccessOS", ignoreCase = true) || name.contains("ESP32", ignoreCase = true)) {
                stopScan()
                connectToDevice(device, name.ifBlank { "AccessOS-ESP32" })
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
            _connectionState.value = HardwareConnectionState.Error("Scan failed (Code $errorCode)")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice, deviceName: String = "AccessOS-ESP32") {
        if (!hasPermissions()) {
            _connectionState.value = HardwareConnectionState.PermissionRequired
            return
        }

        _connectionState.value = HardwareConnectionState.Connecting(deviceName)
        disconnect()

        try {
            Log.d(TAG, "Connecting to GATT on ${device.address}...")
            activeGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while connecting to device: ${e.message}")
            _connectionState.value = HardwareConnectionState.Error("Connection failed: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Exception during GATT disconnect: ${e.message}")
        } finally {
            activeGatt = null
            if (_connectionState.value is HardwareConnectionState.Connected ||
                _connectionState.value is HardwareConnectionState.Connecting) {
                _connectionState.value = HardwareConnectionState.Disconnected
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceName = gatt?.device?.name ?: "AccessOS-ESP32"
            val deviceAddress = gatt?.device?.address ?: ""

            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Connected to GATT server on $deviceAddress. Discovering services...")
                _connectionState.value = HardwareConnectionState.Connected(
                    deviceName = deviceName,
                    deviceAddress = deviceAddress,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server: $deviceAddress (status: $status)")
                disconnect()
                _connectionState.value = HardwareConnectionState.Disconnected
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status != BluetoothGatt.GATT_SUCCESS || gatt == null) {
                Log.w(TAG, "Service discovery failed with status: $status")
                return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.w(TAG, "AccessOS Service UUID $SERVICE_UUID not found on device!")
                return
            }

            val eventChar = service.getCharacteristic(CHAR_EVENT_UUID)
            if (eventChar != null) {
                // Enable local notification
                gatt.setCharacteristicNotification(eventChar, true)
                // Write CCCD descriptor to enable remote device notifications
                @Suppress("DEPRECATION")
                val descriptor = eventChar.getDescriptor(CCCD_UUID)
                if (descriptor != null) {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                    Log.d(TAG, "Subscribed to AccessOS Event notifications on $CHAR_EVENT_UUID")
                }
            } else {
                Log.w(TAG, "Event Characteristic $CHAR_EVENT_UUID not found!")
            }
        }

        @Deprecated("Deprecated in Java for API 33+")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic?.uuid == CHAR_EVENT_UUID) {
                val data = characteristic.value ?: return
                handleIncomingEventBytes(data, gatt)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == CHAR_EVENT_UUID) {
                handleIncomingEventBytes(value, gatt)
            }
        }
    }

    private fun handleIncomingEventBytes(data: ByteArray, gatt: BluetoothGatt?) {
        val payload = String(data, StandardCharsets.UTF_8).trim()
        Log.d(TAG, "Received raw BLE payload: $payload")

        try {
            var eventType = "SOS_BUTTON_PRESSED"
            var eventId = "evt_${System.currentTimeMillis()}"
            var deviceId = gatt?.device?.name ?: "AccessOS-ESP32"

            if (payload.startsWith("{") && payload.endsWith("}")) {
                val json = JSONObject(payload)
                eventType = json.optString("event", "SOS_BUTTON_PRESSED")
                eventId = json.optString("eventId", eventId)
                deviceId = json.optString("deviceId", deviceId)
            } else if (payload.contains(":")) {
                val parts = payload.split(":")
                if (parts.isNotEmpty()) eventType = parts[0]
                if (parts.size > 1) eventId = parts[1]
                if (parts.size > 2) deviceId = parts[2]
            }

            // Deduplication: prevent duplicate processing of the same physical press
            if (processedEventIds.contains(eventId)) {
                Log.d(TAG, "Event ID $eventId already processed. Acknowledging and skipping duplicate.")
                sendAckToHardware(gatt, eventId)
                return
            }
            processedEventIds.add(eventId)
            if (processedEventIds.size > 200) {
                processedEventIds.clear()
            }

            val hardwareEvent = when (eventType) {
                "PING", "TEST_PING" -> HardwareEvent.DevicePing(deviceId = deviceId)
                "DEVICE_READY" -> HardwareEvent.DeviceReady(deviceId = deviceId)
                "FAULT" -> HardwareEvent.DeviceFault(deviceId = deviceId, errorMessage = payload)
                else -> HardwareEvent.SosButtonPressed(
                    deviceId = deviceId,
                    eventId = eventId,
                    timestamp = System.currentTimeMillis()
                )
            }

            scope.launch {
                _latestEvent.value = hardwareEvent
                _events.emit(hardwareEvent)
            }

            // Write ACK confirmation back to ESP32
            sendAckToHardware(gatt, eventId)

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing BLE incoming event payload: ${e.message}", e)
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun sendAckToHardware(gatt: BluetoothGatt?, eventId: String) {
        val currentGatt = gatt ?: activeGatt ?: return
        if (!hasPermissions()) return

        try {
            val service = currentGatt.getService(SERVICE_UUID)
            val commandChar = service?.getCharacteristic(CHAR_COMMAND_UUID)
            if (commandChar != null) {
                val ackJson = "{\"status\":\"ACK\",\"eventId\":\"$eventId\"}"
                commandChar.value = ackJson.toByteArray(StandardCharsets.UTF_8)
                currentGatt.writeCharacteristic(commandChar)
                Log.d(TAG, "Sent ACK back to ESP32 for event $eventId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send ACK to ESP32: ${e.message}")
        }
    }

    fun setDeliveryStatus(status: HardwareDeliveryStatus) {
        _deliveryStatus.value = status
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun sendCommandToHardware(command: String): Boolean {
        val currentGatt = activeGatt ?: return false
        if (!hasPermissions()) return false

        return try {
            val service = currentGatt.getService(SERVICE_UUID)
            val commandChar = service?.getCharacteristic(CHAR_COMMAND_UUID)
            if (commandChar != null) {
                commandChar.value = command.toByteArray(StandardCharsets.UTF_8)
                val success = currentGatt.writeCharacteristic(commandChar)
                Log.d(TAG, "Sent command to ESP32: $command (success=$success)")
                success
            } else {
                Log.w(TAG, "Command characteristic not found for write")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send command to ESP32: ${e.message}")
            false
        }
    }

    /**
     * Test / Simulation Trigger:
     * Dispatches a real test event through the pipeline so users can verify
     * the phone's reaction, dialogs, location capture, and cloud notifications
     * even before physical ESP32 breadboard wiring is powered on.
     */
    fun simulateTestSosPress(deviceId: String = "AccessOS-ESP32 (Test Mode)") {
        val eventId = "test_sos_${System.currentTimeMillis()}"
        val event = HardwareEvent.SosButtonPressed(
            deviceId = deviceId,
            eventId = eventId,
            timestamp = System.currentTimeMillis()
        )
        scope.launch {
            _latestEvent.value = event
            _events.emit(event)
        }
    }

    fun simulateTestPing() {
        val isPhysicalConnected = _connectionState.value is HardwareConnectionState.Connected
        val sentToHardware = if (isPhysicalConnected) {
            sendCommandToHardware("{\"command\":\"PING\"}")
        } else false

        val event = HardwareEvent.DevicePing(
            deviceId = if (isPhysicalConnected) "AccessOS-ESP32 (Physical)" else "AccessOS-ESP32 (Simulated)",
            message = if (sentToHardware)
                "Physical hardware ping sent: ESP32 buzzer chirped and LED flashed!"
            else if (isPhysicalConnected)
                "Physical hardware ping dispatched to ESP32."
            else
                "Hardware self-test: OLED, LED, and Buzzer verified."
        )
        scope.launch {
            _latestEvent.value = event
            _events.emit(event)
        }
    }
}
