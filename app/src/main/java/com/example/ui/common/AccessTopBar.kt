package com.example.ui.common

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccessBackgroundDeep
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeManager
import com.example.util.VibrationHelper
import androidx.compose.foundation.clickable

@Composable
fun AccessTopBar(
    isUnlocked: Boolean,
    userName: String = "Mrs. Sharma",
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTime by TimeManager.currentTime.collectAsState()
    val currentDate by TimeManager.currentDate.collectAsState()
    val timeZoneLabel by TimeManager.timeZoneLabel.collectAsState()
    var showTimeZoneDialog by remember { mutableStateOf(false) }

    val networkMonitor = remember { com.example.util.NetworkMonitor(context.applicationContext) }
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)

    if (showTimeZoneDialog) {
        TimeZoneSelectorDialog(onDismiss = { showTimeZoneDialog = false })
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AccessBackgroundDeep,
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        border = BorderStroke(1.dp, AccessSurfaceBorder.copy(alpha = 0.6f)),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(AccessPrimary.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Offline Status Strip
            if (!isOnline) {
                Surface(
                    color = Color(0xFFE65100).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 Offline Mode • Changes saved locally & will sync when internet returns",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFCC80)
                        )
                    }
                }
            }

            // Top row: App brand + Live Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AccessPrimaryLight.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "AccessOS Shield",
                            tint = AccessPrimaryLight,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AccessOS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isUnlocked) "User: $userName" else "Simplified Assistant",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Live Clock Pill (Clickable to change Time Zone)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccessBackgroundDeep.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, AccessSurfaceBorder),
                    modifier = Modifier.clickable {
                        VibrationHelper.vibrateShort(context)
                        showTimeZoneDialog = true
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (currentTime.isEmpty()) "12:00 PM" else currentTime,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextAccent
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = timeZoneLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccessPrimaryLight
                            )
                            Text(
                                text = " • $currentDate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Bottom row: Status badge + DB Sync Indicator + Logout action if unlocked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUnlocked) AccessSuccess.copy(alpha = 0.2f) else AccessDanger.copy(alpha = 0.2f),
                        border = BorderStroke(
                            1.dp,
                            if (isUnlocked) AccessSuccess else AccessDanger
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (isUnlocked) "Session Active" else "Locked",
                                tint = if (isUnlocked) AccessSuccess else AccessDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isUnlocked) "ACTIVE" else "LOCKED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFEF9A9A)
                            )
                        }
                    }

                    // Database Sync Pill (Clickable)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccessTertiary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AccessTertiary),
                        modifier = Modifier.clickable {
                            VibrationHelper.vibrateShort(context)
                            com.example.util.DataSyncManager.openAuditDialog()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccessSuccess,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "DB VERIFIED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccessTertiary
                            )
                        }
                    }
                }

                // Logout Button
                if (isUnlocked && onLogoutClick != null) {
                    OutlinedButton(
                        onClick = {
                            VibrationHelper.vibrateShort(context)
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AccessDanger.copy(alpha = 0.15f),
                            contentColor = Color(0xFFFF8A80)
                        ),
                        border = BorderStroke(1.dp, AccessDanger),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOCK & EXIT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
