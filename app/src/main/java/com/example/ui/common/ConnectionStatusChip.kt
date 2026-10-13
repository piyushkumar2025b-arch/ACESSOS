package com.example.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HardwareConnectionState
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ConnectionStatusChip(
    connectionState: HardwareConnectionState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (label, iconColor, bgColor, borderColor) = when (connectionState) {
        is HardwareConnectionState.Connected -> Quad(
            "ESP32 Connected",
            AccessSuccess,
            AccessSuccess.copy(alpha = 0.12f),
            AccessSuccess.copy(alpha = 0.4f)
        )
        is HardwareConnectionState.Scanning -> Quad(
            "Scanning (${connectionState.secondsRemaining}s)...",
            AccessPrimary,
            AccessPrimary.copy(alpha = 0.12f),
            AccessPrimary.copy(alpha = 0.4f)
        )
        is HardwareConnectionState.Connecting -> Quad(
            "Connecting...",
            AccessPrimary,
            AccessPrimary.copy(alpha = 0.12f),
            AccessPrimary.copy(alpha = 0.4f)
        )
        is HardwareConnectionState.BluetoothOff -> Quad(
            "Bluetooth Off",
            AccessDanger,
            AccessDanger.copy(alpha = 0.12f),
            AccessDanger.copy(alpha = 0.4f)
        )
        is HardwareConnectionState.PermissionRequired -> Quad(
            "Permission Needed",
            Color(0xFFE65100),
            Color(0xFFFFF3E0),
            Color(0xFFFFB74D)
        )
        is HardwareConnectionState.Unavailable -> Quad(
            "BT Unavailable",
            TextMuted,
            Color.LightGray.copy(alpha = 0.15f),
            AccessSurfaceBorder
        )
        is HardwareConnectionState.Error -> Quad(
            "Connection Error",
            AccessDanger,
            AccessDanger.copy(alpha = 0.12f),
            AccessDanger.copy(alpha = 0.4f)
        )
        HardwareConnectionState.Disconnected -> Quad(
            "Hardware Offline",
            TextMuted,
            Color.LightGray.copy(alpha = 0.15f),
            AccessSurfaceBorder
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (connectionState) {
                is HardwareConnectionState.Scanning, is HardwareConnectionState.Connecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = iconColor
                    )
                }
                is HardwareConnectionState.Connected -> {
                    Icon(
                        imageVector = Icons.Default.BluetoothConnected,
                        contentDescription = "Connected",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                is HardwareConnectionState.BluetoothOff -> {
                    Icon(
                        imageVector = Icons.Default.BluetoothDisabled,
                        contentDescription = "Bluetooth Off",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                is HardwareConnectionState.PermissionRequired, is HardwareConnectionState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (iconColor == TextMuted) TextPrimary else iconColor
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
