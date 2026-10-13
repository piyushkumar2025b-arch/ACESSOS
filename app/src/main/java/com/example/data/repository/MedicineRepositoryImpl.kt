package com.example.data.repository

import android.util.Log
import com.example.data.remote.FirestoreProvider
import com.example.domain.model.Medicine
import com.example.domain.model.MedicineLog
import com.example.domain.repository.MedicineRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class MedicineRepositoryImpl(
    private var currentFamilyId: String = "FAM-CARE-01"
) : MedicineRepository {

    private val tag = "MedicineRepoFirestore"
    private val firestore: FirebaseFirestore? = FirestoreProvider.instance

    private fun isTakenTodayByTimestamp(timestamp: Long?): Boolean {
        if (timestamp == null || timestamp <= 0) return false
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        return timestamp >= startOfToday
    }

    private val defaultMedicines = listOf(
        Medicine(
            id = "med_1",
            familyId = currentFamilyId,
            name = "Metformin (BP & Sugar)",
            time = "08:30 AM",
            dosage = "1 Tablet (500mg)",
            instructions = "Take after breakfast with a full glass of water",
            pillIconType = "tablet",
            isTakenToday = true,
            lastTakenTimestamp = System.currentTimeMillis() - 3600000 * 3
        ),
        Medicine(
            id = "med_2",
            familyId = currentFamilyId,
            name = "Calcium & Vitamin D3",
            time = "01:30 PM",
            dosage = "1 Capsule",
            instructions = "Take right after lunch with warm milk or water",
            pillIconType = "capsule",
            isTakenToday = false
        ),
        Medicine(
            id = "med_3",
            familyId = currentFamilyId,
            name = "Amlodipine (Blood Pressure)",
            time = "08:00 PM",
            dosage = "1 Tablet (5mg)",
            instructions = "Take after dinner before sleeping",
            pillIconType = "tablet",
            isTakenToday = false
        ),
        Medicine(
            id = "med_4",
            familyId = currentFamilyId,
            name = "Eye Refresh Drops",
            time = "09:30 PM",
            dosage = "2 Drops each eye",
            instructions = "Instill 2 drops in both eyes before bedtime",
            pillIconType = "drops",
            isTakenToday = false
        )
    )

    private val defaultLogs = listOf(
        MedicineLog(
            id = "log_1",
            medicineId = "med_1",
            medicineName = "Metformin (BP & Sugar)",
            dosage = "1 Tablet (500mg)",
            timestamp = System.currentTimeMillis() - 3600000 * 3,
            status = "taken",
            loggedBy = "Mrs. Sharma"
        )
    )

    private val _medicines = MutableStateFlow<List<Medicine>>(
        defaultMedicines.map { it.copy(isTakenToday = isTakenTodayByTimestamp(it.lastTakenTimestamp)) }
    )
    private val _logs = MutableStateFlow<List<MedicineLog>>(defaultLogs)

    private var medicinesListener: ListenerRegistration? = null
    private var logsListener: ListenerRegistration? = null

    init {
        try {
            startFirestoreListeners(currentFamilyId)
        } catch (e: Exception) {
            Log.w(tag, "Firestore listeners initialization error: ${e.message}")
        }
    }

    fun updateFamilyId(newFamilyId: String) {
        currentFamilyId = newFamilyId
        startFirestoreListeners(newFamilyId)
    }

    private fun startFirestoreListeners(familyId: String) {
        val db = firestore ?: return
        try {
            medicinesListener?.remove()
            logsListener?.remove()

            // 1. Listen to medicines collection under familyId
            medicinesListener = db.collection("families")
                .document(familyId)
                .collection("medicines")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Medicines snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val med = doc.toObject(Medicine::class.java)?.copy(id = doc.id)
                                med?.let {
                                    it.copy(isTakenToday = isTakenTodayByTimestamp(it.lastTakenTimestamp))
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _medicines.value = list
                            com.example.util.DataSyncManager.recordReceivedVerified(
                                type = com.example.util.SyncEventType.RETRIEVE_MEDICINE,
                                title = "Medicine Schedule Synchronized",
                                detail = "Received ${list.size} active medicine reminders from Family Care Database",
                                count = list.size
                            )
                        }
                    } else if (snapshot != null && snapshot.isEmpty) {
                        // Seed default medicines to Firestore on first run
                        seedDefaultMedicines(familyId)
                    }
                }

            // 2. Listen to logs collection under familyId
            logsListener = db.collection("families")
                .document(familyId)
                .collection("medicine_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Logs snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(MedicineLog::class.java)?.copy(id = doc.id)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _logs.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Error setting up snapshot listeners: ${e.message}")
        }
    }

    private fun seedDefaultMedicines(familyId: String) {
        val db = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val batch = db.batch()
                defaultMedicines.forEach { med ->
                    val docRef = db.collection("families")
                        .document(familyId)
                        .collection("medicines")
                        .document(med.id)
                    batch.set(docRef, med.copy(familyId = familyId))
                }
                defaultLogs.forEach { log ->
                    val docRef = db.collection("families")
                        .document(familyId)
                        .collection("medicine_logs")
                        .document(log.id)
                    batch.set(docRef, log)
                }
                batch.commit()
            } catch (e: Exception) {
                Log.w(tag, "Failed to seed default medicines: ${e.message}")
            }
        }
    }

    override fun getMedicines(): Flow<List<Medicine>> = _medicines.asStateFlow()

    override fun getMedicineLogs(): Flow<List<MedicineLog>> = _logs.asStateFlow()

    override suspend fun markAsTaken(medicineId: String, takenBy: String): Boolean {
        val now = System.currentTimeMillis()
        var foundMed: Medicine? = null

        // 1. Optimistic Local update
        _medicines.update { list ->
            list.map { med ->
                if (med.id == medicineId) {
                    foundMed = med
                    med.copy(
                        isTakenToday = true,
                        lastTakenTimestamp = now
                    )
                } else {
                    med
                }
            }
        }

        val logId = "log_${UUID.randomUUID().toString().take(8)}"
        foundMed?.let { med ->
            val newLog = MedicineLog(
                id = logId,
                medicineId = med.id,
                medicineName = med.name,
                dosage = med.dosage,
                timestamp = now,
                status = "taken",
                loggedBy = takenBy
            )
            _logs.update { currentLogs -> listOf(newLog) + currentLogs }

            // 2. Real Firestore update
            var isCloudSynced = false
            firestore?.let { db ->
                try {
                    db.collection("families")
                        .document(currentFamilyId)
                        .collection("medicines")
                        .document(medicineId)
                        .update(
                            mapOf(
                                "isTakenToday" to true,
                                "lastTakenTimestamp" to now
                            )
                        )
                        .await()

                    db.collection("families")
                        .document(currentFamilyId)
                        .collection("medicine_logs")
                        .document(logId)
                        .set(newLog)
                        .await()
                    isCloudSynced = true
                } catch (e: Exception) {
                    Log.w(tag, "Failed to sync markAsTaken to Firestore: ${e.message}")
                }
            }

            com.example.util.DataSyncManager.recordUploadVerified(
                type = com.example.util.SyncEventType.MARK_MEDICINE_TAKEN,
                title = if (isCloudSynced) "Dose Confirmed & Cloud Synced" else "Dose Confirmed (Offline Saved)",
                detail = "${med.name} marked as taken by $takenBy."
            )
        }
        return true
    }

    override suspend fun markAsUntaken(medicineId: String): Boolean {
        var foundMed: Medicine? = null
        // 1. Optimistic Local update
        _medicines.update { list ->
            list.map { med ->
                if (med.id == medicineId) {
                    foundMed = med
                    med.copy(
                        isTakenToday = false,
                        lastTakenTimestamp = null
                    )
                } else {
                    med
                }
            }
        }

        // 2. Real Firestore update
        var isCloudSynced = false
        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("medicines")
                    .document(medicineId)
                    .update(
                        mapOf(
                            "isTakenToday" to false,
                            "lastTakenTimestamp" to null
                        )
                    )
                    .await()
                isCloudSynced = true
            } catch (e: Exception) {
                Log.w(tag, "Failed to sync markAsUntaken to Firestore: ${e.message}")
            }
        }

        foundMed?.let { med ->
            com.example.util.DataSyncManager.recordUploadVerified(
                type = com.example.util.SyncEventType.MARK_MEDICINE_TAKEN,
                title = if (isCloudSynced) "Medicine Status Reset & Synced" else "Medicine Status Reset (Offline Saved)",
                detail = "${med.name} marked as not taken."
            )
        }
        return true
    }

    override suspend fun addMedicine(medicine: Medicine): Boolean {
        val medToSave = if (medicine.id.isBlank()) {
            medicine.copy(id = "med_${UUID.randomUUID().toString().take(8)}", familyId = currentFamilyId)
        } else {
            medicine.copy(familyId = currentFamilyId)
        }

        // 1. Optimistic Local update
        _medicines.update { it + medToSave }

        // 2. Real Firestore write
        var isCloudSynced = false
        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("medicines")
                    .document(medToSave.id)
                    .set(medToSave)
                    .await()
                isCloudSynced = true
            } catch (e: Exception) {
                Log.w(tag, "Failed to add medicine to Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.UPLOAD_MEDICINE,
            title = if (isCloudSynced) "New Medicine Saved & Cloud Synced" else "New Medicine Saved (Offline)",
            detail = "${medToSave.name} (${medToSave.dosage} at ${medToSave.time}) recorded."
        )
        return true
    }

    override suspend fun deleteMedicine(medicineId: String): Boolean {
        val medName = _medicines.value.firstOrNull { it.id == medicineId }?.name ?: "Medicine"
        // 1. Optimistic Local update
        _medicines.update { list -> list.filterNot { it.id == medicineId } }

        // 2. Real Firestore write
        var isCloudSynced = false
        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("medicines")
                    .document(medicineId)
                    .delete()
                    .await()
                isCloudSynced = true
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete medicine in Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.UPLOAD_MEDICINE,
            title = if (isCloudSynced) "Medicine Deleted & Synced" else "Medicine Deleted (Offline)",
            detail = "$medName was removed from active reminders."
        )
        return true
    }
}
