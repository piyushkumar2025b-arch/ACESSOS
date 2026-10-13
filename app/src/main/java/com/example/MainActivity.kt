package com.example

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AccessOsDatabase
import com.example.data.repository.FamilyRepositoryImpl
import com.example.data.repository.MedicineRepositoryImpl
import com.example.data.repository.PasswordRepositoryImpl
import com.example.data.repository.SessionRepositoryImpl
import com.example.domain.model.HardwareDeliveryStatus
import com.example.domain.model.HardwareEvent
import com.example.domain.model.Screen
import com.example.domain.usecase.FamilyUseCases
import com.example.domain.usecase.MedicineUseCases
import com.example.domain.usecase.PasswordUseCases
import com.example.domain.usecase.SessionUseCases
import com.example.hardware.ble.AccessOsBleManager
import com.example.hardware.ble.AccessOsBleRepository
import com.example.hardware.ble.AccessOsBleRepositoryImpl
import com.example.ui.common.DataSyncConfirmationBanner
import com.example.ui.device.HardwareDeviceViewModel
import com.example.ui.family.FamilyScreen
import com.example.ui.home.HomeScreen
import com.example.ui.lock.LockScreen
import com.example.ui.medicine.MedicineScreen
import com.example.ui.password.PasswordScreen
import com.example.ui.sos.SosScreen
import com.example.ui.theme.AccessBackground
import com.example.ui.theme.AccessOsTheme
import com.example.util.LocationHelper
import com.example.util.NotificationHelper
import com.example.util.VibrationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AccessOsDatabase
    private lateinit var sessionRepository: SessionRepositoryImpl
    private lateinit var sessionUseCases: SessionUseCases
    private lateinit var passwordRepository: PasswordRepositoryImpl
    private lateinit var passwordUseCases: PasswordUseCases
    private lateinit var medicineRepository: MedicineRepositoryImpl
    private lateinit var medicineUseCases: MedicineUseCases
    private lateinit var familyRepository: FamilyRepositoryImpl
    private lateinit var familyUseCases: FamilyUseCases

    // Hardware BLE Integration
    private lateinit var bleManager: AccessOsBleManager
    private lateinit var bleRepository: AccessOsBleRepository
    private lateinit var hardwareViewModel: HardwareDeviceViewModel
    private val externalSosRequestedState = mutableStateOf(false)

    private var nfcAdapter: NfcAdapter? = null
    private val isNfcSupportedState = mutableStateOf(false)
    private val isNfcEnabledState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels
        NotificationHelper.initNotificationChannels(this)

        // Initialize Firestore with configured database ID
        com.example.data.remote.FirestoreProvider.initialize(applicationContext)

        // Initialize Room Database & Clean Architecture Repositories with ApplicationContext for secure encryption and persistence
        database = AccessOsDatabase.getDatabase(this, lifecycleScope)
        sessionRepository = SessionRepositoryImpl(context = applicationContext)
        sessionUseCases = SessionUseCases(sessionRepository)

        passwordRepository = PasswordRepositoryImpl(
            passwordDao = database.passwordDao(),
            context = applicationContext
        )
        passwordUseCases = PasswordUseCases(passwordRepository, sessionRepository)

        medicineRepository = MedicineRepositoryImpl()
        medicineUseCases = MedicineUseCases(medicineRepository)

        familyRepository = FamilyRepositoryImpl(
            context = applicationContext,
            onFamilyChanged = { newFamilyId ->
                medicineRepository.updateFamilyId(newFamilyId)
            }
        )
        familyUseCases = FamilyUseCases(familyRepository)

        // Initialize Hardware BLE Subsystem
        bleManager = AccessOsBleManager(applicationContext, lifecycleScope)
        bleRepository = AccessOsBleRepositoryImpl(bleManager)
        hardwareViewModel = HardwareDeviceViewModel(bleRepository)

        // Hardware SOS Event Pipeline Listener
        lifecycleScope.launch {
            bleRepository.events.collect { event ->
                if (event is HardwareEvent.SosButtonPressed) {
                    Log.d("AccessOsHardware", "Hardware SOS button event received from ${event.deviceId} [${event.eventId}]")
                    externalSosRequestedState.value = true
                    VibrationHelper.vibrateSos(applicationContext)

                    Toast.makeText(
                        this@MainActivity,
                        "🚨 PHYSICAL ESP32 SOS PRESSED! Dispatching Emergency Alert...",
                        Toast.LENGTH_LONG
                    ).show()

                    bleRepository.setDeliveryStatus(HardwareDeliveryStatus.RECEIVED_BY_PHONE)

                    val loc = LocationHelper.getCurrentLocation(applicationContext)
                    bleRepository.setDeliveryStatus(HardwareDeliveryStatus.DISPATCHING_SOS)

                    try {
                        familyUseCases.triggerEmergencySos(
                            triggeredBy = "AccessOS Hardware (${event.deviceId})",
                            lat = loc.latitude,
                            lng = loc.longitude,
                            address = loc.address,
                            isRealGps = loc.isReal
                        )
                        bleRepository.setDeliveryStatus(HardwareDeliveryStatus.CLOUD_CONFIRMED)
                    } catch (e: Exception) {
                        bleRepository.setDeliveryStatus(HardwareDeliveryStatus.LOCAL_ONLY_OFFLINE)
                        Log.w("AccessOsHardware", "Cloud delivery failed, saved locally: ${e.message}")
                    }
                }
            }
        }

        // Setup Real Physical NFC Reader Hardware
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            val supported = nfcAdapter != null
            val enabled = nfcAdapter?.isEnabled == true
            isNfcSupportedState.value = supported
            isNfcEnabledState.value = enabled
            Log.d("AccessOsNFC", "NFC Supported: $supported, Enabled: $enabled")
        } catch (e: Exception) {
            Log.w("AccessOsNFC", "NFC check error: ${e.message}")
        }

        // Check if launched from an NFC tag tap
        handleNfcIntent(intent)

        // Disable automatic FCM background token requests when running in emulator or unprovisioned push environments
        try {
            val fcm = FirebaseMessaging.getInstance()
            fcm.isAutoInitEnabled = false
            Log.d("AccessOsFCM", "Firebase Messaging initialized with auto-init disabled. Real-time Firestore sync active.")
        } catch (e: Exception) {
            Log.d("AccessOsFCM", "FCM auto-init check: ${e.message}")
        }

        setContent {
            AccessOsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AccessBackground
                ) {
                    AccessOsApp(
                        sessionUseCases = sessionUseCases,
                        passwordUseCases = passwordUseCases,
                        medicineUseCases = medicineUseCases,
                        familyUseCases = familyUseCases,
                        hardwareViewModel = hardwareViewModel,
                        externalSosRequested = externalSosRequestedState.value,
                        onResetExternalSos = { externalSosRequestedState.value = false },
                        isNfcSupported = isNfcSupportedState.value,
                        isNfcEnabled = isNfcEnabledState.value
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
        hardwareViewModel.updatePermissions()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val adapter = nfcAdapter ?: return
            if (!adapter.isEnabled) return
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            adapter.enableForegroundDispatch(this, pendingIntent, null, null)
        } catch (e: Exception) {
            Log.w("AccessOsNFC", "Foreground dispatch enable error: ${e.message}")
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(this)
        } catch (e: Exception) {
            Log.w("AccessOsNFC", "Foreground dispatch disable error: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action
        ) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                val idBytes = tag.id
                val uidHex = idBytes.joinToString("") { "%02X".format(it) }

                val unlockResult = sessionUseCases.validateAndUnlockWithCard(uidHex, "Authorized Elder")
                if (unlockResult.isSuccess) {
                    VibrationHelper.vibrateSuccess(this)
                    Toast.makeText(
                        this,
                        "🟢 Authorized Care Card Recognized! Welcome.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    VibrationHelper.vibrateShort(this)
                    Toast.makeText(
                        this,
                        "⚠️ Unrecognized Card: This card is not enrolled in your Care Circle.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
fun AccessOsApp(
    sessionUseCases: SessionUseCases,
    passwordUseCases: PasswordUseCases,
    medicineUseCases: MedicineUseCases,
    familyUseCases: FamilyUseCases,
    hardwareViewModel: HardwareDeviceViewModel = remember { HardwareDeviceViewModel(com.example.hardware.ble.FakeBleRepository()) },
    externalSosRequested: Boolean = false,
    onResetExternalSos: () -> Unit = {},
    isNfcSupported: Boolean = false,
    isNfcEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val session by sessionUseCases.sessionState.collectAsState()
    val hardwareConnectionState by hardwareViewModel.connectionState.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Lock) }

    LaunchedEffect(externalSosRequested) {
        if (externalSosRequested) {
            currentScreen = Screen.Sos
            onResetExternalSos()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // If session is locked, automatically ensure we are on the Lock Screen (or SOS if triggered from lock)
        if (!session.isUnlocked && currentScreen != Screen.Sos) {
            LockScreen(
                isNfcSupported = isNfcSupported,
                isNfcEnabled = isNfcEnabled,
                connectionState = hardwareConnectionState,
                onSimulateRfidScan = { uid ->
                    val result = sessionUseCases.validateAndUnlockWithCard(uid, "Authorized Elder")
                    if (result.isSuccess) {
                        currentScreen = Screen.Home
                    }
                },
                onEmergencySosClick = {
                    currentScreen = Screen.Sos
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Active Navigation
            when (currentScreen) {
                Screen.Password -> {
                    PasswordScreen(
                        passwordUseCases = passwordUseCases,
                        isSessionActive = session.isUnlocked,
                        onBackToHome = {
                            currentScreen = Screen.Home
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.Medicine -> {
                    MedicineScreen(
                        medicineUseCases = medicineUseCases,
                        isSessionActive = session.isUnlocked,
                        onBackToHome = {
                            currentScreen = Screen.Home
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.Family -> {
                    FamilyScreen(
                        familyUseCases = familyUseCases,
                        isSessionActive = session.isUnlocked,
                        onBackToHome = {
                            currentScreen = Screen.Home
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.Sos -> {
                    SosScreen(
                        familyUseCases = familyUseCases,
                        isSessionActive = session.isUnlocked,
                        userName = session.userName,
                        onBack = {
                            currentScreen = if (session.isUnlocked) Screen.Home else Screen.Lock
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    HomeScreen(
                        session = session,
                        hardwareViewModel = hardwareViewModel,
                        onLogoutClick = {
                            sessionUseCases.logout()
                            currentScreen = Screen.Lock
                        },
                        onMedicineClick = {
                            currentScreen = Screen.Medicine
                        },
                        onPasswordClick = {
                            currentScreen = Screen.Password
                        },
                        onCallFamilyClick = {
                            currentScreen = Screen.Family
                        },
                        onSosClick = {
                            currentScreen = Screen.Sos
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Global Data Sync & Upload/Download Verification Banner & Receipts
        DataSyncConfirmationBanner(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
        )
    }
}
