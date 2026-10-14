package com.example.ui.medicine

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Medicine
import com.example.domain.model.MedicineLog
import com.example.domain.usecase.MedicineUseCases
import com.example.ui.common.AccessTopBar
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
import com.example.util.VibrationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MedicineScreen(
    medicineUseCases: MedicineUseCases,
    isSessionActive: Boolean,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val medicines by medicineUseCases.getMedicines().collectAsState(initial = emptyList())

    val takenCount = medicines.count { it.isTakenToday }
    val totalCount = medicines.size
    val logs by medicineUseCases.getMedicineLogs().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAiMedicine by remember { mutableStateOf<Medicine?>(null) }
    var aiExplanationText by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    // AI Medicine Guide Dialog
    if (selectedAiMedicine != null) {
        val med = selectedAiMedicine!!
        AlertDialog(
            onDismissRequest = {
                selectedAiMedicine = null
                aiExplanationText = null
            },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🤖 AI Medicine Guide",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccessTertiary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = med.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Dosage: ${med.dosage}",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    if (isAiLoading) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccessSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "✨ Generating easy-to-read explanation with Gemini AI...",
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
                                text = aiExplanationText ?: "Follow your doctor's dosage instructions.",
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
                        selectedAiMedicine = null
                        aiExplanationText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("GOT IT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

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
            title = "Medicine",
            subtitle = "$takenCount of $totalCount doses taken today",
            icon = Icons.Default.Medication,
            accent = com.example.ui.theme.AccessSuccess,
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
                        containerColor = AccessSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_medicine_button"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp, vertical = 10.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Dose",
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

        // Today's Progress Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            color = if (takenCount == totalCount && totalCount > 0) AccessSuccessContainer else AccessSurface,
            border = BorderStroke(2.dp, if (takenCount == totalCount && totalCount > 0) AccessSuccess else AccessSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (takenCount == totalCount && totalCount > 0) "🎉 All Today's Doses Completed!" else "Today's Schedule Progress",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (takenCount == totalCount && totalCount > 0) Color(0xFFA7F3D0) else TextPrimary
                    )
                    Text(
                        text = "$takenCount of $totalCount medicines logged taken",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (takenCount == totalCount && totalCount > 0) AccessSuccess else AccessPrimary,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$takenCount/$totalCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Subtabs: Today's Schedule vs History Log
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = AccessSurface,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AccessTertiary,
                    height = 4.dp
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = {
                    VibrationHelper.vibrateShort(context)
                    selectedTabIndex = 0
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Today",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTabIndex == 0) AccessTertiary else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TODAY'S DOSES",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 0) AccessTertiary else TextSecondary
                        )
                    }
                }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = {
                    VibrationHelper.vibrateShort(context)
                    selectedTabIndex = 1
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTabIndex == 1) AccessTertiary else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HISTORY LOG",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == 1) AccessTertiary else TextSecondary
                        )
                    }
                }
            )
        }

        // Screen Body
        if (selectedTabIndex == 0) {
            // Schedule List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (medicines.isEmpty()) {
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
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = "No medicines",
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Medicines Scheduled",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap '+ NEW MEDICINE' above to add your daily doses.",
                                    fontSize = 16.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(medicines, key = { it.id }) { item ->
                        MedicineDoseCard(
                            medicine = item,
                            onToggleTaken = {
                                coroutineScope.launch {
                                    if (item.isTakenToday) {
                                        medicineUseCases.markAsUntaken(item.id)
                                        VibrationHelper.vibrateShort(context)
                                        Toast.makeText(context, "Dose marked un-taken", Toast.LENGTH_SHORT).show()
                                    } else {
                                        medicineUseCases.markAsTaken(item.id)
                                        VibrationHelper.vibrateSuccess(context)
                                        Toast.makeText(context, "✅ Dose Logged! Family notified.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onAskAi = {
                                VibrationHelper.vibrateShort(context)
                                selectedAiMedicine = item
                                isAiLoading = true
                                coroutineScope.launch {
                                    val explanation = com.example.data.remote.GeminiAiService.explainMedicine(
                                        medicineName = item.name,
                                        dosage = item.dosage,
                                        instructions = item.instructions
                                    )
                                    aiExplanationText = explanation
                                    isAiLoading = false
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // History Log List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (logs.isEmpty()) {
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
                                    imageVector = Icons.Default.History,
                                    contentDescription = "No logs",
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No History Recorded Yet",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        MedicineLogCard(log = log)
                    }
                }
            }
        }
    }

    // Add New Medicine Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("08:00 AM") }
        var dosage by remember { mutableStateOf("1 Tablet") }
        var instructions by remember { mutableStateOf("After meals with water") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AccessSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Add Daily Prescription",
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
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("Medicine Name (e.g. Amlodipine)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_name")
                    )

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Schedule Time (e.g. 09:00 AM / 08:30 PM)", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccessTertiary,
                            unfocusedBorderColor = AccessSurfaceBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_time")
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 1 Tablet, 5ml, 2 Drops)", fontSize = 14.sp) },
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
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions (e.g. After breakfast)", fontSize = 14.sp) },
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
                        if (name.trim().isEmpty()) {
                            errorMessage = "Please enter medicine name"
                            return@Button
                        }
                        if (time.trim().isEmpty()) {
                            errorMessage = "Please enter time"
                            return@Button
                        }

                        coroutineScope.launch {
                            medicineUseCases.addMedicine(
                                Medicine(
                                    id = "med_${UUID.randomUUID()}",
                                    name = name.trim(),
                                    time = time.trim(),
                                    dosage = dosage.trim(),
                                    instructions = instructions.trim(),
                                    isTakenToday = false
                                )
                            )
                            VibrationHelper.vibrateSuccess(context)
                            Toast.makeText(context, "Medicine Schedule Added", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_medicine_confirm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD MEDICINE",
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
}

@Composable
fun MedicineDoseCard(
    medicine: Medicine,
    onToggleTaken: () -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground by animateColorAsState(
        targetValue = if (medicine.isTakenToday) AccessSuccessContainer.copy(alpha = 0.5f) else AccessSurface,
        label = "card_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (medicine.isTakenToday) AccessSuccess else AccessSurfaceBorder,
        label = "card_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medicine_item_${medicine.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Time Badge + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccessPrimaryLight.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, AccessPrimaryLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = TextAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = medicine.time,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextAccent
                        )
                    }
                }

                if (medicine.isTakenToday) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccessSuccess,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Taken",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAKEN TODAY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccessDangerContainer,
                        border = BorderStroke(1.dp, AccessDanger)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Pending",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PENDING DOSE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8A80)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Medicine Name & Dosage
            Text(
                text = medicine.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Dosage: ${medicine.dosage}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccessTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (medicine.instructions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccessSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "💡 Instructions: ${medicine.instructions}",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Ask AI Button
            OutlinedButton(
                onClick = onAskAi,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccessTertiary),
                border = BorderStroke(1.dp, AccessTertiary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "🤖 AI Medicine Guide • What is this for?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccessTertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Action Button
            if (!medicine.isTakenToday) {
                Button(
                    onClick = onToggleTaken,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("btn_take_medicine_${medicine.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccessSuccess),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Taken",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MARK AS TAKEN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val takenTimeStr = medicine.lastTakenTimestamp?.let { timeFormat.format(Date(it)) } ?: "Today"

                    Text(
                        text = "Logged at $takenTimeStr",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA7F3D0)
                    )

                    OutlinedButton(
                        onClick = onToggleTaken,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = BorderStroke(1.dp, TextMuted),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(18.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNDO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineLogCard(
    log: MedicineLog,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
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
                color = AccessSuccessContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Taken",
                        tint = AccessSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.medicineName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Dose: ${log.dosage} • By ${log.loggedBy}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = formattedDate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccessPrimaryLight
                )
            }
        }
    }
}
