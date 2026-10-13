package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessDangerContainer
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSecondary
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSuccessContainer
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessSurfaceVariant
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DataSyncManager
import com.example.util.SyncEventType
import com.example.util.SyncVerificationEvent
import com.example.util.VibrationHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataSyncConfirmationBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentEvent by DataSyncManager.currentBannerEvent.collectAsState()
    val activeReceipt by DataSyncManager.activeReceipt.collectAsState()
    val showAuditDialog by DataSyncManager.showAuditDialog.collectAsState()

    // Auto-dismiss confirmation banner after 7 seconds, while leaving in audit log
    LaunchedEffect(currentEvent) {
        if (currentEvent != null) {
            delay(7500)
            DataSyncManager.dismissBanner()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = currentEvent != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            currentEvent?.let { event ->
                val isUpload = event.isUpload
                val isLocalVault = event.type == SyncEventType.LOCAL_VAULT_SAVE ||
                        event.type == SyncEventType.LOCAL_VAULT_UPDATE ||
                        event.type == SyncEventType.LOCAL_VAULT_DELETE

                val headerBg = if (isLocalVault) {
                    AccessPrimary.copy(alpha = 0.95f)
                } else if (isUpload) {
                    AccessSuccessContainer
                } else {
                    AccessSurfaceVariant
                }

                val borderAccent = if (isLocalVault) {
                    AccessTertiary
                } else if (isUpload) {
                    AccessSuccess
                } else {
                    AccessSecondary
                }

                val badgeText = if (isLocalVault) {
                    "🔒 ENCRYPTED IN LOCAL DATABASE"
                } else if (isUpload) {
                    "✅ UPLOAD VERIFIED IN DATABASE"
                } else {
                    "📥 DATA RECEIVED FROM CLOUD / FAMILY"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("data_sync_confirmation_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = headerBg),
                    border = BorderStroke(2.dp, borderAccent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
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
                                    shape = CircleShape,
                                    color = borderAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, borderAccent),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isLocalVault) Icons.Default.Lock
                                            else if (isUpload) Icons.Default.CloudDone
                                            else Icons.Default.CloudDownload,
                                            contentDescription = "Sync Verification",
                                            tint = if (isLocalVault) AccessTertiary else if (isUpload) AccessSuccess else AccessSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = badgeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isLocalVault) AccessTertiary else if (isUpload) Color(0xFFA7F3D0) else Color(0xFFA5D6A7),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = event.title,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { DataSyncManager.dismissBanner() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.detail,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 48.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified at ${event.formattedTime} • ${event.storageTarget.take(24)}",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = {
                                    VibrationHelper.vibrateShort(context)
                                    DataSyncManager.openReceipt(event)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = "Receipt",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RECEIPT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Detail Dialog
    activeReceipt?.let { receipt ->
        SyncReceiptDialog(
            event = receipt,
            onDismiss = { DataSyncManager.closeReceipt() }
        )
    }

    // Full Database Sync Audit Dialog
    if (showAuditDialog) {
        DatabaseSyncAuditDialog(
            onDismiss = { DataSyncManager.closeAuditDialog() }
        )
    }
}

@Composable
fun SyncReceiptDialog(
    event: SyncVerificationEvent,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AccessSurface,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = "Receipt",
                    tint = AccessTertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Database Verification Receipt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccessSuccessContainer,
                    border = BorderStroke(1.dp, AccessSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = AccessSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STATUS: VERIFIED & CONFIRMED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccessSurfaceVariant,
                    border = BorderStroke(1.dp, AccessSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceiptRow(label = "VERIFICATION ID", value = event.receiptId, isMonospace = true)
                        ReceiptRow(label = "ACTION TYPE", value = if (event.isUpload) "Upload / Save Data" else "Retrieve / Receive Data")
                        ReceiptRow(label = "SUBJECT", value = event.title)
                        ReceiptRow(label = "STORAGE TARGET", value = event.storageTarget)
                        ReceiptRow(label = "TIMESTAMP", value = event.formattedDate)
                        ReceiptRow(label = "DETAILS", value = event.detail)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CLOSE RECEIPT", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
fun DatabaseSyncAuditDialog(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isChecking by DataSyncManager.isCheckingDb.collectAsState()
    val dbStatus by DataSyncManager.dbStatus.collectAsState()
    val auditLog by DataSyncManager.syncAuditLog.collectAsState()
    val lastCheckTime by DataSyncManager.lastVerifiedCheckTime.collectAsState()

    val timeFormatted = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(lastCheckTime))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AccessSurface,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Database Verification",
                        tint = AccessTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Database & Sync Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Live Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccessSurfaceVariant),
                    border = BorderStroke(1.5.dp, AccessSuccess)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AccessSuccess,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE DATABASE STATUS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccessSuccess
                            )
                        }

                        Text(
                            text = dbStatus,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Last verified: $timeFormatted",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                DataSyncManager.verifyDatabaseNow(coroutineScope)
                            },
                            enabled = !isChecking,
                            colors = ButtonDefaults.buttonColors(containerColor = AccessTertiary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TESTING DATABASE...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Test DB",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TEST DATABASE CONNECTION NOW",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Recent Verified Database Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (auditLog.isEmpty()) {
                    Text(
                        text = "No transactions logged in this session yet.",
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLog, key = { it.receiptId + it.timestamp }) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccessBackground,
                                border = BorderStroke(1.dp, AccessSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isUpload) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = if (item.isUpload) AccessSuccess else AccessSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${item.formattedTime} • ${item.receiptId}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AccessSuccessContainer
                                    ) {
                                        Text(
                                            text = "VERIFIED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFA7F3D0),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DONE", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}
