package com.example.ui.sos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SosAlert
import com.example.domain.usecase.FamilyUseCases
import com.example.ui.common.AccessTopBar
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessDangerContainer
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSuccessContainer
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessSurfaceVariant
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SosScreen(
    familyUseCases: FamilyUseCases,
    isSessionActive: Boolean,
    onBack: () -> Unit,
    userName: String = "Authorized Elder",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val sosHistory by familyUseCases.getSosAlertHistory().collectAsState(initial = emptyList())
    val familyMembers by familyUseCases.getFamilyMembers().collectAsState(initial = emptyList())
    val primaryContact = familyMembers.firstOrNull { it.isPrimaryEmergencyContact && it.phoneNumber != "112" } ?: familyMembers.firstOrNull()

    var lastSentAlert by remember { mutableStateOf<SosAlert?>(null) }
    var isLocatingAndSending by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(LocationHelper.hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Pulsing animation for the big SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AccessBackground)
    ) {
        // App Top Bar
        AccessTopBar(
            isUnlocked = isSessionActive,
            onLogoutClick = onBack
        )

        // Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AccessDangerContainer,
            border = BorderStroke(1.dp, AccessDanger)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("sos_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BACK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert Mode",
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EMERGENCY DISPATCH",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8A80)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Emergency Trigger Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = AccessSurface),
                    border = BorderStroke(3.dp, AccessDanger),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NEED IMMEDIATE HELP?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF8A80),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Tap the red button below to obtain real GPS coordinates and send instant emergency broadcasts to your family.",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )

                        // GIANT SOS BUTTON (Min height 90dp, pulsing, oversized)
                        Button(
                            onClick = {
                                VibrationHelper.vibrateSuccess(context)
                                isLocatingAndSending = true
                                coroutineScope.launch {
                                    val locationResult = LocationHelper.getCurrentLocation(context)
                                    val alert = familyUseCases.triggerEmergencySos(
                                        triggeredBy = userName,
                                        lat = locationResult.latitude,
                                        lng = locationResult.longitude,
                                        address = locationResult.address,
                                        isRealGps = locationResult.isReal
                                    )
                                    lastSentAlert = alert
                                    isLocatingAndSending = false

                                    NotificationHelper.showSosDispatchedNotification(
                                        context,
                                        userName,
                                        locationResult.address
                                    )

                                    val toastMsg = when (locationResult.status) {
                                        com.example.util.LocationStatus.REAL_LOCATION -> "🚨 SOS BROADCASTED WITH CURRENT GPS LOCATION!"
                                        com.example.util.LocationStatus.LAST_KNOWN_LOCATION -> "🚨 SOS BROADCASTED WITH LAST KNOWN LOCATION!"
                                        com.example.util.LocationStatus.LOCATION_UNAVAILABLE -> "🚨 SOS BROADCASTED (GPS UNAVAILABLE)!"
                                    }
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isLocatingAndSending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .scale(if (isLocatingAndSending) 1f else pulseScale)
                                .testTag("btn_trigger_emergency_sos"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccessDanger),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
                        ) {
                            if (isLocatingAndSending) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "ACQUIRING GPS LOCATION...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = "SOS",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "TRIGGER SOS ALERT",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "1-Touch Real GPS & WhatsApp Broadcast",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Call Ambulance 112
            item {
                Button(
                    onClick = {
                        VibrationHelper.vibrateSuccess(context)
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("btn_call_ambulance_112"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color(0xFFFF8A80))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Ambulance",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CALL AMBULANCE (112)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Status Card if Alert Triggered with Live Maps & WhatsApp Actions
            lastSentAlert?.let { alert ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AccessSuccessContainer),
                        border = BorderStroke(2.dp, AccessSuccess)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Dispatched",
                                    tint = AccessSuccess,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "SOS Broadcast Sent with GPS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS",
                                    tint = AccessTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = alert.address,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Lat: ${"%.5f".format(Locale.US, alert.latitude)}, Lng: ${"%.5f".format(Locale.US, alert.longitude)}",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons for WhatsApp broadcast & Map view
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(alert.googleMapsUrl)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Map",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VIEW MAP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }

                                primaryContact?.let { contact ->
                                    Button(
                                        onClick = {
                                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
                                            val message = "🚨 EMERGENCY SOS ALERT!\nMrs. Sharma needs immediate assistance.\n📍 Location: ${alert.address}\n🗺️ Google Maps: ${alert.googleMapsUrl}\n⏰ Time: $timeStr"
                                            sendWhatsAppMessage(context, contact.phoneNumber, message)
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "WhatsApp",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WHATSAPP SOS", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Alert Dispatch History
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Recent SOS Dispatches",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "History of emergency pings synced across family cloud",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            if (sosHistory.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AccessSurface,
                        border = BorderStroke(1.dp, AccessSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No emergency alerts triggered recently.",
                            fontSize = 16.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(sosHistory, key = { it.id }) { item ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())
                    val formatted = dateFormat.format(Date(item.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AccessSurface),
                        border = BorderStroke(1.dp, AccessSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccessDangerContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Alert",
                                        tint = Color(0xFFFF8A80),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SOS Broadcast Triggered",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A80)
                                )
                                Text(
                                    text = item.address,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                                Text(
                                    text = formatted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccessPrimaryLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
    try {
        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
        val encodedMessage = Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
