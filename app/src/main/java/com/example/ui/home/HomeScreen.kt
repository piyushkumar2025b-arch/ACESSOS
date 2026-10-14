package com.example.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserSession
import com.example.ui.common.AccessDs
import com.example.ui.common.AccessTopBar
import com.example.ui.common.BigButton
import com.example.ui.common.SectionHeader
import com.example.ui.common.DeviceStatusCard
import com.example.ui.common.TimeZoneSelectorDialog
import com.example.ui.device.HardwareDeviceViewModel
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessBackgroundDeep
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessDangerContainer
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSecondary
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
fun HomeScreen(
    session: UserSession,
    hardwareViewModel: HardwareDeviceViewModel = remember { HardwareDeviceViewModel(com.example.hardware.ble.FakeBleRepository()) },
    onLogoutClick: () -> Unit,
    onMedicineClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onCallFamilyClick: () -> Unit,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    val liveTimeString by TimeManager.currentTime.collectAsState()
    val liveDateString by TimeManager.currentDate.collectAsState()
    val timeZoneLabel by TimeManager.timeZoneLabel.collectAsState()

    val connectionState by hardwareViewModel.connectionState.collectAsState()
    val latestEvent by hardwareViewModel.latestEvent.collectAsState()
    val deliveryStatus by hardwareViewModel.deliveryStatus.collectAsState()
    val testFeedback by hardwareViewModel.testFeedback.collectAsState()

    if (showTimeZoneDialog) {
        TimeZoneSelectorDialog(onDismiss = { showTimeZoneDialog = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AccessBackgroundDeep, AccessBackground)
                )
            )
    ) {
        // High-Contrast Accessible Top Bar
        AccessTopBar(
            isUnlocked = session.isUnlocked,
            userName = session.userName,
            onLogoutClick = { showLogoutConfirmation = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── HERO: Welcome + live clock + status ──
            HeroWelcomeCard(
                userName = session.userName,
                liveDateString = liveDateString,
                liveTimeString = liveTimeString,
                timeZoneLabel = timeZoneLabel,
                connectionState = connectionState,
                onClockClick = {
                    VibrationHelper.vibrateShort(context)
                    showTimeZoneDialog = true
                }
            )

            // ── HARDWARE ──
            SectionHeader(
                eyebrow = "Connected care hardware",
                title = "Smart Safety Device"
            )
            // DEDICATED ESP32 PHYSICAL HARDWARE CARD
            DeviceStatusCard(
                connectionState = connectionState,
                latestEvent = latestEvent,
                deliveryStatus = deliveryStatus,
                testFeedback = testFeedback,
                onScanAndConnect = {
                    VibrationHelper.vibrateShort(context)
                    hardwareViewModel.scanAndConnect()
                },
                onDisconnect = {
                    VibrationHelper.vibrateShort(context)
                    hardwareViewModel.disconnect()
                },
                onTestPing = {
                    VibrationHelper.vibrateShort(context)
                    hardwareViewModel.testHardwarePing()
                },
                onSimulateSos = {
                    VibrationHelper.vibrateSos(context)
                    hardwareViewModel.simulateTestSosPress()
                }
            )

            // ── EMERGENCY ──
            SectionHeader(
                eyebrow = "Always within reach",
                title = "Emergency"
            )
            // PROMINENT EMERGENCY (SOS) BUTTON
            BigButton(
                title = "Emergency (SOS)",
                subtitle = "Send GPS alert & notify family contacts",
                icon = Icons.Default.CrisisAlert,
                onClick = onSosClick,
                containerColor = AccessDangerContainer,
                contentColor = Color(0xFFFFCDD2),
                borderColor = AccessDanger,
                badgeText = "PRESS FOR HELP",
                badgeColor = AccessDanger,
                badgeTextColor = TextPrimary,
                testTag = "home_sos_button"
            )

            // ── DAILY CARE ──
            SectionHeader(
                eyebrow = "Daily care",
                title = "Your Services"
            )
            // CORE DASHBOARD SECTIONS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Medicine Reminders Button
                BigButton(
                    title = "Medicine Reminders",
                    subtitle = "View today's doses & mark taken",
                    icon = Icons.Default.Medication,
                    onClick = onMedicineClick,
                    containerColor = AccessSurface,
                    contentColor = TextPrimary,
                    borderColor = AccessPrimaryLight,
                    badgeText = "2 MEDICINES TODAY",
                    badgeColor = AccessPrimaryLight.copy(alpha = 0.2f),
                    badgeTextColor = TextAccent,
                    testTag = "home_medicine_button"
                )

                // 2. Password Manager Button (Protected: Active Session Only)
                BigButton(
                    title = "Password Manager",
                    subtitle = "Secure on-device local vault",
                    icon = Icons.Default.Key,
                    onClick = onPasswordClick,
                    containerColor = AccessSurface,
                    contentColor = TextPrimary,
                    borderColor = AccessTertiary,
                    badgeText = "PROTECTED • OFFLINE ONLY",
                    badgeColor = AccessTertiary.copy(alpha = 0.2f),
                    badgeTextColor = AccessTertiary,
                    testTag = "home_password_button"
                )

                // 3. Call Family Button
                BigButton(
                    title = "Call Family",
                    subtitle = "One-tap direct calling to relatives",
                    icon = Icons.Default.Call,
                    onClick = onCallFamilyClick,
                    containerColor = AccessSurface,
                    contentColor = TextPrimary,
                    borderColor = AccessSecondary,
                    badgeText = "FAMILY LINKED",
                    badgeColor = AccessSecondary.copy(alpha = 0.2f),
                    badgeTextColor = Color(0xFFA5D6A7),
                    testTag = "home_family_button"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Logout confirmation dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Lock AccessOS?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will lock the screen and protect your passwords. You will need to scan your RFID card to open it again. Note: The physical ESP32 SOS button remains active.",
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        VibrationHelper.vibrateShort(context)
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "YES, LOCK NOW",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        )
    }
}
