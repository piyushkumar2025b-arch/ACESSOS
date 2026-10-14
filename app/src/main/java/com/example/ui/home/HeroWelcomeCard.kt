package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.common.AccessDs
import com.example.ui.common.ConnectionStatusChip
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeroWelcomeCard(
    userName: String,
    liveDateString: String,
    liveTimeString: String,
    timeZoneLabel: String,
    connectionState: HardwareConnectionState,
    onClockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = userName.substringBefore(" ").ifEmpty { userName }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AccessDs.CardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, AccessDs.BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(AccessDs.HeroBrush).padding(AccessDs.HeroPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Good day,", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        Text(text = firstName, fontSize = 30.sp, fontWeight = FontWeight.Black, color = TextPrimary, lineHeight = 36.sp)
                        Text(text = liveDateString, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = 0.35f), border = BorderStroke(1.dp, AccessPrimaryLight.copy(alpha = 0.45f)), modifier = Modifier.clickable(onClick = onClockClick)) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = "Clock", tint = AccessPrimaryLight, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = liveTimeString.ifEmpty { "--:--" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = timeZoneLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccessPrimaryLight)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = AccessSuccess.copy(alpha = 0.18f), modifier = Modifier.size(30.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = "Protected", tint = AccessSuccess, modifier = Modifier.size(17.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Care Circle Active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Offline vault locked", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        }
                    }
                    ConnectionStatusChip(connectionState = connectionState)
                }
            }
        }
    }
}
