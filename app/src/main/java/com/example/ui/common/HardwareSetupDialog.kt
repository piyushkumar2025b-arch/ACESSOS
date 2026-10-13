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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.HardwareConnectionState
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessSurfaceVariant
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HardwareSetupDialog(
    connectionState: HardwareConnectionState,
    onDismiss: () -> Unit,
    onTestPing: () -> Unit,
    onTestSos: () -> Unit
) {
    val isConnected = connectionState is HardwareConnectionState.Connected

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("hardware_setup_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = AccessSurface,
            border = BorderStroke(1.dp, AccessSurfaceBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccessPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperBoard,
                                    contentDescription = "ESP32 Board",
                                    tint = AccessPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hardware Wiring Guide",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "ESP32 • OLED • Button • Buzzer • LED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_hardware_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AccessSurfaceBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Link Status Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isConnected) AccessSuccess.copy(alpha = 0.12f) else AccessSurfaceVariant,
                        border = BorderStroke(1.dp, if (isConnected) AccessSuccess.copy(alpha = 0.4f) else AccessSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Memory,
                                contentDescription = null,
                                tint = if (isConnected) AccessSuccess else AccessTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isConnected) "ESP32 Connected via BLE" else "BLE Hardware Disconnected",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) AccessSuccess else TextPrimary
                                )
                                Text(
                                    text = if (isConnected)
                                        "Software is paired and ready. You can test the buzzer or press the SOS button."
                                    else
                                        "Upload the Arduino sketch, power the ESP32, and tap 'Scan & Connect'.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // 1. Bill of Materials from User Kit
                    Text(
                        text = "1. Kit Components (From Invoice)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HardwareItemRow(
                            icon = Icons.Default.DeveloperBoard,
                            name = "ESP32 Dev Board (38-pin)",
                            detail = "Microcontroller running AccessOS BLE firmware"
                        )
                        HardwareItemRow(
                            icon = Icons.Default.Tv,
                            name = "1.3\" I2C OLED Display",
                            detail = "SDA to GPIO 21, SCL to GPIO 22, VCC (3.3V), GND"
                        )
                        HardwareItemRow(
                            icon = Icons.Default.RadioButtonChecked,
                            name = "4-Pin Push Button",
                            detail = "Emergency SOS button on GPIO 27 (Active LOW to GND)"
                        )
                        HardwareItemRow(
                            icon = Icons.Default.Lightbulb,
                            name = "5mm Red LED + 330 Ω Resistor",
                            detail = "Anode via 330Ω to GPIO 26, Cathode to GND"
                        )
                        HardwareItemRow(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            name = "Buzzer (5V-12V)",
                            detail = "Positive pin to GPIO 25, Negative to GND"
                        )
                        HardwareItemRow(
                            icon = Icons.Default.Memory,
                            name = "GL12 Breadboard & Jumpers",
                            detail = "840 points solderless prototyping board"
                        )
                    }

                    // 2. Pinout Table
                    Text(
                        text = "2. Quick Wiring / Pinout Table",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.04f),
                        border = BorderStroke(1.dp, AccessSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            PinoutRow("Component", "ESP32 Pin", "Note", isHeader = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = AccessSurfaceBorder)
                            PinoutRow("SOS Button", "GPIO 27", "Internal Pullup to GND")
                            PinoutRow("Red LED (+)", "GPIO 26", "Via 330 Ω Resistor")
                            PinoutRow("Buzzer (+)", "GPIO 25", "Alarm chime drive")
                            PinoutRow("OLED SDA", "GPIO 21", "I2C Data line")
                            PinoutRow("OLED SCL", "GPIO 22", "I2C Clock line")
                            PinoutRow("OLED VCC", "3.3V Pin", "Power rail")
                            PinoutRow("All GNDs", "GND Rail", "Common Ground")
                        }
                    }

                    // 3. Firmware Flashing Reference
                    Text(
                        text = "3. Firmware File Location",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccessPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AccessPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Arduino Sketch: hardware/firmware/AccessOs_ESP32_Firmware.ino",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccessPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• In Arduino IDE, select 'ESP32 Dev Module'.\n• Install Adafruit SSD1306 and Adafruit GFX libraries.\n• Click Upload and open Serial Monitor at 115200 baud.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // 4. Quick Testing Controls
                    Text(
                        text = "4. Test Hardware Integration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTestPing,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_test_ping_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccessTertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chirp Buzzer & LED", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onTestSos,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_test_sos_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccessDanger),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Test SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_hardware_guide_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "GOT IT • BACK TO APP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareItemRow(
    icon: ImageVector,
    name: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = AccessPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccessPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PinoutRow(
    col1: String,
    col2: String,
    col3: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            fontSize = if (isHeader) 12.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHeader) TextPrimary else AccessPrimaryLight,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = col2,
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) TextPrimary else TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = col3,
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1.5f)
        )
    }
}
