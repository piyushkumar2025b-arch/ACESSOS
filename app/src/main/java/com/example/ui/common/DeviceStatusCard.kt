package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareConnectionState
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceStatusCard(
    connectionState: HardwareConnectionState,
    latestEvent: HardwareEvent?,
    deliveryStatus: HardwareDeliveryStatus,
    testFeedback: String?,
    onScanAndConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTestPing: () -> Unit,
    onSimulateSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHardwareGuideDialog by remember { mutableStateOf(false) }

    if (showHardwareGuideDialog) {
        HardwareSetupDialog(
            connectionState = connectionState,
            onDismiss = { showHardwareGuideDialog = false },
            onTestPing = {
                showHardwareGuideDialog = false
                onTestPing()
            },
            onTestSos = {
                showHardwareGuideDialog = false
                onSimulateSos()
            }
        )
    }

    val isConnected = connectionState is HardwareConnectionState.Connected
    val isScanning = connectionState is HardwareConnectionState.Scanning
    val isConnecting = connectionState is HardwareConnectionState.Connecting

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_status_card"),
        shape = RoundedCornerShape(AccessDs.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = AccessSurface,
            contentColor = TextPrimary
        ),
        border = BorderStroke(
            width = if (isConnected) 2.dp else 1.dp,
            color = if (isConnected) AccessSuccess.copy(alpha = 0.65f) else AccessDs.BorderSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.04f), Color.Transparent)
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Hardware Title & Connection Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(AccessDs.CardRadiusSmall),
                        color = if (isConnected) AccessSuccess.copy(alpha = 0.16f) else AccessPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(
                            1.dp,
                            if (isConnected) AccessSuccess.copy(alpha = 0.4f) else AccessPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.DeveloperBoard,
                                contentDescription = "Hardware Board",
                                tint = if (isConnected) AccessSuccess else AccessPrimaryLight,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isConnected) "Hardware Linked" else "AccessOS Smart Hardware",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ESP32 · SOS Button · OLED",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                ConnectionStatusChip(connectionState = connectionState)
            }

            // Sub-status and Details Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, AccessSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    when (connectionState) {
                        is HardwareConnectionState.Connected -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccessSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ready: ${connectionState.deviceName} (${connectionState.deviceAddress})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccessSuccess
                                )
                            }
                            Text(
                                text = "Last seen: ${
                                    SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(
                                        Date(connectionState.lastSeenTimestamp)
                                    )
                                }",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        is HardwareConnectionState.Scanning -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AccessPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Searching for AccessOS-ESP32 nearby...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccessPrimary
                                )
                            }
                        }
                        is HardwareConnectionState.Connecting -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AccessPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pairing with ${connectionState.deviceName}...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccessPrimary
                                )
                            }
                        }
                        is HardwareConnectionState.BluetoothOff -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AccessDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Phone Bluetooth is switched OFF. Turn on to link button.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccessDanger
                                )
                            }
                        }
                        is HardwareConnectionState.PermissionRequired -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nearby Devices permission is required for BLE.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                        else -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hardware button is offline. Tap 'Scan & Connect' to pair.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Disconnected safety notice
                    if (!isConnected && !isScanning && !isConnecting) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Notice: Physical SOS button cannot notify phone while disconnected.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccessDanger
                        )
                    }

                    // Latest Event Display
                    if (latestEvent != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val eventText = when (latestEvent) {
                            is HardwareEvent.SosButtonPressed -> "🚨 Last Event: Button pressed [${latestEvent.eventId}]"
                            is HardwareEvent.DevicePing -> "📶 Last Event: Self-test ping OK"
                            is HardwareEvent.DeviceReady -> "🟢 Last Event: Hardware ready (v${latestEvent.firmwareVersion})"
                            is HardwareEvent.DeviceFault -> "⚠️ Fault: ${latestEvent.errorMessage}"
                        }
                        Text(
                            text = eventText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (latestEvent is HardwareEvent.SosButtonPressed) AccessDanger else AccessPrimary
                        )
                    }
                }
            }

            // Feedback Banner
            AnimatedVisibility(visible = testFeedback != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccessPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, AccessPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = testFeedback ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AccessPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hardware Wiring & Pinout Guide Button
            OutlinedButton(
                onClick = { showHardwareGuideDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_wiring_guide_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccessPrimary.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccessPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperBoard,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View ESP32 Wiring & Pinout Guide",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isConnected) {
                    Button(
                        onClick = onScanAndConnect,
                        enabled = !isScanning,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_connect_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Refresh else Icons.AutoMirrored.Filled.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScanning) "Scanning..." else "Scan & Connect",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("disconnect_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccessDanger.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccessDanger)
                    ) {
                        Text(
                            text = "Disconnect",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onTestPing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_ping_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccessTertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Ping",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Simulate Test SOS Button (allows validating app response without physical hardware)
                OutlinedButton(
                    onClick = onSimulateSos,
                    modifier = Modifier.testTag("simulate_sos_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccessDanger.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccessDanger)
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Test SOS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
