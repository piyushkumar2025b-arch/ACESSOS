package com.example.ui.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareConnectionState
import com.example.ui.common.AccessTopBar
import com.example.ui.common.BigButton
import com.example.ui.common.ConnectionStatusChip
import com.example.ui.common.TimeZoneSelectorDialog
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessDangerContainer
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessSurfaceVariant
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeManager
import com.example.util.VibrationHelper

@Composable
fun LockScreen(
    onSimulateRfidScan: (uid: String) -> Unit,
    onEmergencySosClick: () -> Unit,
    isNfcSupported: Boolean = false,
    isNfcEnabled: Boolean = false,
    connectionState: HardwareConnectionState = HardwareConnectionState.Disconnected,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scanFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    val liveTimeString by TimeManager.currentTime.collectAsState()
    val liveDateString by TimeManager.currentDate.collectAsState()
    val timeZoneLabel by TimeManager.timeZoneLabel.collectAsState()

    if (showTimeZoneDialog) {
        TimeZoneSelectorDialog(onDismiss = { showTimeZoneDialog = false })
    }

    // Pulsing animation for scan target ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        com.example.ui.theme.AccessBackgroundDeep,
                        AccessBackground
                    )
                )
            )
    ) {
        // App Top Bar
        AccessTopBar(isUnlocked = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Real-Time Clock & Date Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable {
                        VibrationHelper.vibrateShort(context)
                        showTimeZoneDialog = true
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccessSurface),
                border = BorderStroke(1.dp, AccessSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = liveTimeString,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = TextAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = liveDateString,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = " • $timeZoneLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccessPrimaryLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ConnectionStatusChip(connectionState = connectionState)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Locked Card / Visual Scanner Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccessSurface,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(2.dp, AccessSurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Sensor Indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccessPrimaryLight.copy(alpha = 0.15f),
                            modifier = Modifier.size(110.dp)
                        ) {}
                        Surface(
                            shape = CircleShape,
                            color = AccessPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp),
                            border = BorderStroke(2.dp, AccessPrimaryLight)
                        ) {}
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Scan Card",
                            tint = TextAccent,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Please Scan Your Care Card",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isNfcSupported && isNfcEnabled) {
                            "Hold your authorized NFC card / Care tag against the back of this phone to unlock your passwords and dashboard."
                        } else {
                            "Hold your RFID card to the reader, or tap the demo card unlock button below."
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )

                    // Hardware Status Pill
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (connectionState is HardwareConnectionState.Connected) AccessSuccess.copy(alpha = 0.12f) else AccessSurfaceVariant,
                        border = BorderStroke(1.dp, if (connectionState is HardwareConnectionState.Connected) AccessSuccess.copy(alpha = 0.4f) else AccessSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (connectionState is HardwareConnectionState.Connected) Icons.Default.CheckCircle else Icons.Default.DeveloperBoard,
                                contentDescription = "Hardware Status",
                                tint = if (connectionState is HardwareConnectionState.Connected) AccessSuccess else AccessTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (connectionState is HardwareConnectionState.Connected) {
                                    "ESP32 SOS Button Connected & Guarding in Background"
                                } else {
                                    "ESP32 Hardware: Ready for pairing via Bluetooth"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (connectionState is HardwareConnectionState.Connected) AccessSuccess else AccessTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // BIG SIMULATE SCAN BUTTON (Primary Action)
                BigButton(
                    title = "DEMO CARD TAP (SIMULATE)",
                    subtitle = "Tap to test authorized care card unlock",
                    icon = Icons.Default.CreditCard,
                    onClick = {
                        VibrationHelper.vibrateSuccess(context)
                        scanFeedbackMessage = "Authorized Card Recognized! Unlocking..."
                        onSimulateRfidScan("04A3B2C1")
                    },
                    containerColor = AccessPrimary,
                    contentColor = TextPrimary,
                    borderColor = AccessPrimaryLight,
                    badgeText = "ENROLLED CARD UNLOCK",
                    badgeColor = AccessPrimaryLight.copy(alpha = 0.25f),
                    badgeTextColor = TextAccent,
                    testTag = "simulate_rfid_button"
                )

                // EMERGENCY SOS BUTTON (Directly available even on lock screen)
                BigButton(
                    title = "EMERGENCY (SOS)",
                    subtitle = "Instant family alert with GPS location",
                    icon = Icons.Default.CrisisAlert,
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        onEmergencySosClick()
                    },
                    containerColor = AccessDangerContainer,
                    contentColor = Color(0xFFFFCDD2),
                    borderColor = AccessDanger,
                    badgeText = "PHYSICAL OR SCREEN SOS",
                    badgeColor = AccessDanger,
                    badgeTextColor = TextPrimary,
                    testTag = "lock_screen_sos_button"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer info
            Text(
                text = "AccessOS • Hardware-Integrated Eldercare & SOS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
