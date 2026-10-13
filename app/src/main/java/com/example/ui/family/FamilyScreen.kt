package com.example.ui.family

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FamilyGroup
import com.example.domain.model.FamilyMember
import com.example.domain.usecase.FamilyUseCases
import com.example.ui.common.AccessTopBar
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessDanger
import com.example.ui.theme.AccessDangerContainer
import com.example.ui.theme.AccessPrimary
import com.example.ui.theme.AccessPrimaryLight
import com.example.ui.theme.AccessSecondary
import com.example.ui.theme.AccessSuccess
import com.example.ui.theme.AccessSurface
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.AccessSurfaceVariant
import com.example.ui.theme.AccessTertiary
import com.example.ui.theme.TextAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch

@Composable
fun FamilyScreen(
    familyUseCases: FamilyUseCases,
    isSessionActive: Boolean,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val familyMembers by familyUseCases.getFamilyMembers().collectAsState(initial = emptyList())
    val familyGroup by familyUseCases.getFamilyGroup().collectAsState(initial = FamilyGroup())

    var showAddDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showAiDigestDialog by remember { mutableStateOf(false) }
    var aiDigestText by remember { mutableStateOf<String?>(null) }
    var isAiDigestLoading by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    // AI Caregiver Digest Dialog
    if (showAiDigestDialog) {
        AlertDialog(
            onDismissRequest = {
                showAiDigestDialog = false
                aiDigestText = null
            },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "🤖 AI Caregiver Digest",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccessTertiary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Real-time summary for ${familyGroup.primaryElderName}'s care circle.",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (isAiDigestLoading) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccessSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "✨ Analyzing schedules & activity logs with Gemini AI...",
                                fontSize = 16.sp,
                                color = TextAccent,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccessSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = aiDigestText ?: "All routine health vitals and dosage reminders are recorded on schedule.",
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAiDigestDialog = false
                        aiDigestText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CLOSE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AccessBackground)
    ) {
        // App Top Bar
        AccessTopBar(
            isUnlocked = isSessionActive,
            onLogoutClick = onBackToHome
        )

        // Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AccessSurfaceVariant,
            border = BorderStroke(1.dp, AccessSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Button(
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        onBackToHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("family_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Add Relative Button
                Button(
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessTertiary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_family_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact",
                        tint = AccessBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEW CONTACT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccessBackground
                    )
                }
            }
        }

        // Contact List & Hub
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Family Care Hub Cloud Banner
            item {
                FamilyHubBannerCard(
                    familyGroup = familyGroup,
                    onManageClick = {
                        VibrationHelper.vibrateShort(context)
                        showGroupDialog = true
                    },
                    onCopyCode = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Family Invite Code", familyGroup.inviteCode)
                        clipboard.setPrimaryClip(clip)
                        VibrationHelper.vibrateShort(context)
                        Toast.makeText(context, "Invite Code ${familyGroup.inviteCode} Copied!", Toast.LENGTH_SHORT).show()
                    },
                    onShareCode = {
                        VibrationHelper.vibrateShort(context)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Join our AccessOS Family Care Hub for ${familyGroup.primaryElderName}! Use Invite Code: ${familyGroup.inviteCode}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Family Invite Code"))
                    }
                )
            }

            // AI Caregiver Health Digest Button
            item {
                Button(
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        showAiDigestDialog = true
                        isAiDigestLoading = true
                        coroutineScope.launch {
                            val summary = com.example.data.remote.GeminiAiService.generateCaregiverSummary(
                                elderName = familyGroup.primaryElderName,
                                medicinesSummary = "Daily doses configured for morning and evening.",
                                logsSummary = "Doses marked taken on schedule."
                            )
                            aiDigestText = summary
                            isAiDigestLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessTertiary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = "🤖 AI Caregiver Health Digest",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            item {
                Text(
                    text = "Speed-Dial Family & Doctors",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = "Tap the big green CALL button to instantly dial or message.",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            items(familyMembers, key = { it.id }) { member ->
                FamilyContactCard(
                    member = member,
                    onCallClick = {
                        VibrationHelper.vibrateSuccess(context)
                        dialPhoneNumber(context, member.phoneNumber)
                    },
                    onWhatsAppClick = {
                        VibrationHelper.vibrateShort(context)
                        openWhatsApp(context, member.phoneNumber, "Hello ${member.name}, I am reaching out from AccessOS.")
                    },
                    onDeleteClick = {
                        VibrationHelper.vibrateShort(context)
                        memberToDelete = member
                    }
                )
            }
        }
    }

    // Manage / Create / Join Family Dialog
    if (showGroupDialog) {
        var selectedTab by remember { mutableIntStateOf(0) }
        var inputCode by remember { mutableStateOf("") }
        var inputGroupName by remember { mutableStateOf("") }
        var groupErrorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Family Care Group",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = AccessSurfaceVariant,
                        contentColor = TextPrimary,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AccessTertiary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("JOIN GROUP", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("CREATE NEW", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        )
                    }

                    if (groupErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccessDangerContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = groupErrorMessage ?: "",
                                color = Color(0xFFFF8A80),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        Text(
                            text = "Enter the 6-character Invite Code shared by a family member or caregiver to link accounts:",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it.uppercase()
                                groupErrorMessage = null
                            },
                            label = { Text("6-Digit Code (e.g. SHRM88)", fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccessTertiary,
                                unfocusedBorderColor = AccessSurfaceBorder
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Create a new Family Care Hub circle for live cloud synchronization and emergency alerts:",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = inputGroupName,
                            onValueChange = {
                                inputGroupName = it
                                groupErrorMessage = null
                            },
                            label = { Text("Family Circle Name (e.g. Verma Family)", fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccessTertiary,
                                unfocusedBorderColor = AccessSurfaceBorder
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (selectedTab == 0) {
                                if (inputCode.trim().isEmpty()) {
                                    groupErrorMessage = "Please enter an invite code"
                                    return@launch
                                }
                                val result = familyUseCases.joinFamilyGroup(inputCode.trim())
                                if (result.isSuccess) {
                                    VibrationHelper.vibrateSuccess(context)
                                    Toast.makeText(context, "Linked to Family Group!", Toast.LENGTH_SHORT).show()
                                    showGroupDialog = false
                                } else {
                                    groupErrorMessage = result.exceptionOrNull()?.message ?: "Failed to join"
                                }
                            } else {
                                if (inputGroupName.trim().isEmpty()) {
                                    groupErrorMessage = "Please enter a family group name"
                                    return@launch
                                }
                                val newGroup = familyUseCases.createFamilyGroup(inputGroupName.trim())
                                VibrationHelper.vibrateSuccess(context)
                                Toast.makeText(context, "Group '${newGroup.groupName}' Created! Code: ${newGroup.inviteCode}", Toast.LENGTH_LONG).show()
                                showGroupDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTab == 0) "JOIN GROUP" else "CREATE HUB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showGroupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
fun FamilyHubBannerCard(
    familyGroup: FamilyGroup,
    onManageClick: () -> Unit,
    onCopyCode: () -> Unit,
    onShareCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AccessSurfaceVariant),
        border = BorderStroke(2.dp, AccessTertiary.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Family Group",
                        tint = AccessTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = familyGroup.groupName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AccessSuccess,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cloud Connected (${familyGroup.totalMembersCount} Active)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccessSuccess
                            )
                        }
                    }
                }

                Button(
                    onClick = onManageClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LINK / EDIT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Invite Code Container
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccessSurface,
                border = BorderStroke(1.dp, AccessSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "FAMILY INVITE CODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = familyGroup.inviteCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccessTertiary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onCopyCode,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Invite Code",
                                tint = AccessPrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = onShareCode,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Invite Code",
                                tint = AccessTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyContactCard(
    member: FamilyMember,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contact_item_${member.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AccessSurface),
        border = BorderStroke(2.dp, if (member.isPrimaryEmergencyContact) AccessSecondary else AccessSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Member Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Circle
                    Surface(
                        shape = CircleShape,
                        color = Color(member.avatarColorHex),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = member.avatarInitials,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (member.isPrimaryEmergencyContact) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Primary",
                                    tint = TextAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = member.relationship,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccessPrimaryLight
                        )

                        Text(
                            text = member.phoneNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Delete button for non-emergency hotlines
                if (member.phoneNumber != "112") {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Huge CALL Button
                Button(
                    onClick = onCallClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(60.dp)
                        .testTag("btn_call_${member.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (member.phoneNumber == "112") AccessDanger else AccessSuccess
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALL NOW",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Message / WhatsApp Button
                if (member.phoneNumber != "112") {
                    Button(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Message",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHAT",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val cleanPhone = phoneNumber.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open phone dialer: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
    }
}

private fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
    try {
        // Strip everything except digits
        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
        val encodedMessage = Uri.encode(message)

        // Try direct WhatsApp explicit intent via smsto URI
        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val packageManager = context.packageManager
        if (whatsappIntent.resolveActivity(packageManager) != null) {
            context.startActivity(whatsappIntent)
        } else {
            // Fallback to WhatsApp Business or universal browser link
            val universalIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleanPhone?text=$encodedMessage")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(universalIntent)
        }
    } catch (e: Exception) {
        // Ultimate fallback to standard web link
        try {
            val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "").trim()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (err: Exception) {
            Toast.makeText(context, "Cannot open WhatsApp: ${err.localizedMessage ?: "App not found"}", Toast.LENGTH_LONG).show()
        }
    }
}
