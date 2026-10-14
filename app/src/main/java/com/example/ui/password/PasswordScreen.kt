package com.example.ui.password

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PasswordEntry
import com.example.domain.usecase.PasswordUseCases
import com.example.ui.common.AccessTopBar
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch

@Composable
fun PasswordScreen(
    passwordUseCases: PasswordUseCases,
    isSessionActive: Boolean,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val passwords by passwordUseCases.getAllPasswords().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var passwordToEdit by remember { mutableStateOf<PasswordEntry?>(null) }
    var passwordToDelete by remember { mutableStateOf<PasswordEntry?>(null) }
    val passwordVisibilityMap = remember { mutableStateMapOf<Long, Boolean>() }

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
        AccessTopBar(
            isUnlocked = isSessionActive,
            onLogoutClick = onBackToHome
        )

        // Professional screen header
        com.example.ui.common.ScreenHeader(
            title = "Password Vault",
            subtitle = "Offline only • never leaves this phone",
            icon = Icons.Default.Security,
            accent = AccessTertiary,
            onBack = {
                VibrationHelper.vibrateShort(context)
                onBackToHome()
            },
            action = {
                androidx.compose.material3.Button(
                    onClick = {
                        VibrationHelper.vibrateShort(context)
                        showAddDialog = true
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = AccessTertiary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_password_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp, vertical = 10.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Password",
                        tint = AccessBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccessBackground
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        // Main Vault Content
        if (!isSessionActive) {
            // Security Lock View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AccessDangerContainer,
                    border = BorderStroke(2.dp, AccessDanger),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Password Vault Locked",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please scan your RFID card to unlock and view your passwords.",
                            fontSize = 18.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info notice card
                item {
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
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Private & Secure",
                                tint = AccessTertiary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Private On-Device Vault",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Passwords are stored locally in Room DB and never uploaded to cloud.",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (passwords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = AccessSurface),
                            border = BorderStroke(1.dp, AccessSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "No passwords",
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Passwords Saved Yet",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap '+ NEW PASSWORD' above to add your Wi-Fi, accounts, or bank cards.",
                                    fontSize = 16.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(passwords, key = { it.id }) { item ->
                        val isVisible = passwordVisibilityMap[item.id] == true

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_item_${item.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AccessSurface,
                                contentColor = TextPrimary
                            ),
                            border = BorderStroke(2.dp, AccessSurfaceBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                // Header: Title + Category Tag + Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextAccent
                                        )
                                        if (item.category.isNotEmpty()) {
                                            Text(
                                                text = item.category,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AccessPrimaryLight
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                VibrationHelper.vibrateShort(context)
                                                passwordToEdit = item
                                            },
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Password",
                                                tint = AccessPrimaryLight,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                VibrationHelper.vibrateShort(context)
                                                passwordToDelete = item
                                            },
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Password",
                                                tint = Color(0xFFFF8A80),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Username field container
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccessSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "USERNAME / ID",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = item.username.ifEmpty { "(None)" },
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        }

                                        if (item.username.isNotEmpty()) {
                                            OutlinedButton(
                                                onClick = {
                                                    VibrationHelper.vibrateShort(context)
                                                    copyToClipboard(context, "Username", item.username)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, AccessPrimaryLight)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Username",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = AccessPrimaryLight
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "COPY",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccessPrimaryLight
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Password field container
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AccessSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "PASSWORD",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = if (isVisible) item.password else "••••••••••••",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = if (isVisible) FontFamily.Monospace else FontFamily.Default,
                                                color = if (isVisible) Color(0xFF80D8FF) else TextPrimary
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Toggle Show/Hide
                                            OutlinedButton(
                                                onClick = {
                                                    VibrationHelper.vibrateShort(context)
                                                    passwordVisibilityMap[item.id] = !isVisible
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, AccessTertiary)
                                            ) {
                                                Icon(
                                                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = if (isVisible) "Hide" else "Show",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = AccessTertiary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isVisible) "HIDE" else "SHOW",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccessTertiary
                                                )
                                            }

                                            // Copy Password
                                            Button(
                                                onClick = {
                                                    VibrationHelper.vibrateSuccess(context)
                                                    copyToClipboard(context, "Password", item.password)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Password",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = TextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "COPY",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                if (item.notes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Note: ${item.notes}",
                                        fontSize = 14.sp,
                                        color = TextSecondary,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add New Password Dialog
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("General") }
        var newNotes by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Add New Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccessDangerContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFF8A80),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = {
                            newTitle = it
                            errorMessage = null
                        },
                        label = { Text("Account Name (e.g. Wi-Fi, Bank, TV)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password_title")
                    )

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Username or Email (Optional)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessPrimaryLight,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password_username")
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password *", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password_value")
                    )

                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes (e.g. In living room) (Optional)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessPrimaryLight,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.trim().isEmpty()) {
                            errorMessage = "Please enter an account name"
                            return@Button
                        }
                        if (newPassword.trim().isEmpty()) {
                            errorMessage = "Please enter a password"
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                passwordUseCases.addPassword(
                                    PasswordEntry(
                                        title = newTitle.trim(),
                                        username = newUsername.trim(),
                                        password = newPassword.trim(),
                                        category = newCategory,
                                        notes = newNotes.trim()
                                    )
                                )
                                VibrationHelper.vibrateSuccess(context)
                                Toast.makeText(context, "Password Saved to Vault", Toast.LENGTH_SHORT).show()
                                showAddDialog = false
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to save"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_password_confirm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE PASSWORD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    passwordToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { passwordToDelete = null },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Delete Password?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${entry.title}' from your vault?",
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            passwordUseCases.deletePassword(entry.id)
                            VibrationHelper.vibrateShort(context)
                            Toast.makeText(context, "Password deleted", Toast.LENGTH_SHORT).show()
                            passwordToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "DELETE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { passwordToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        )
    }

    // Edit Password Dialog
    passwordToEdit?.let { entry ->
        var editTitle by remember(entry) { mutableStateOf(entry.title) }
        var editUsername by remember(entry) { mutableStateOf(entry.username) }
        var editPassword by remember(entry) { mutableStateOf(entry.password) }
        var editCategory by remember(entry) { mutableStateOf(entry.category) }
        var editNotes by remember(entry) { mutableStateOf(entry.notes) }
        var editErrorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { passwordToEdit = null },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Edit Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (editErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccessDangerContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = editErrorMessage ?: "",
                                color = Color(0xFFFF8A80),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            editErrorMessage = null
                        },
                        label = { Text("Account Name", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username or Email (Optional)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessPrimaryLight,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = {
                            editPassword = it
                            editErrorMessage = null
                        },
                        label = { Text("Password *", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes (Optional)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessPrimaryLight,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.trim().isEmpty()) {
                            editErrorMessage = "Please enter an account name"
                            return@Button
                        }
                        if (editPassword.trim().isEmpty()) {
                            editErrorMessage = "Please enter a password"
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                passwordUseCases.updatePassword(
                                    entry.copy(
                                        title = editTitle.trim(),
                                        username = editUsername.trim(),
                                        password = editPassword.trim(),
                                        category = editCategory,
                                        notes = editNotes.trim()
                                    )
                                )
                                VibrationHelper.vibrateSuccess(context)
                                Toast.makeText(context, "Password Updated", Toast.LENGTH_SHORT).show()
                                passwordToEdit = null
                            } catch (e: Exception) {
                                editErrorMessage = e.message ?: "Failed to update"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Changes",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE CHANGES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { passwordToEdit = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
