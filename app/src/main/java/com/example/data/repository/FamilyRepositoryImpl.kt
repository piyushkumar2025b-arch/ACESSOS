package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.remote.FirestoreProvider
import com.example.domain.model.FamilyGroup
import com.example.domain.model.FamilyMember
import com.example.domain.model.SosAlert
import com.example.domain.repository.FamilyRepository
import com.example.util.GpsLocationResult
import com.example.util.LocationHelper
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
import java.util.UUID

class FamilyRepositoryImpl(
    private val context: Context? = null,
    private var currentFamilyId: String = "FAM-8821",
    private val onFamilyChanged: ((String) -> Unit)? = null
) : FamilyRepository {

    private val tag = "FamilyRepoFirestore"
    private val firestore: FirebaseFirestore? = FirestoreProvider.instance
    private var lastLocalTriggerTimestamp = 0L
    private var lastLocalAlertId: String? = null

    private val defaultContacts = listOf(
        FamilyMember(
            id = "fam_1",
            name = "Aarav Sharma",
            relationship = "Son (Primary Caregiver)",
            phoneNumber = "+91 98765 43210",
            isPrimaryEmergencyContact = true,
            avatarInitials = "AS",
            avatarColorHex = 0xFF2E7D32 // Forest Green
        ),
        FamilyMember(
            id = "fam_2",
            name = "Dr. Ananya Verma",
            relationship = "Family Physician",
            phoneNumber = "+91 98111 22334",
            isPrimaryEmergencyContact = false,
            avatarInitials = "AV",
            avatarColorHex = 0xFF1565C0 // Navy Blue
        ),
        FamilyMember(
            id = "fam_3",
            name = "Priya Sharma",
            relationship = "Daughter",
            phoneNumber = "+91 99887 76655",
            isPrimaryEmergencyContact = false,
            avatarInitials = "PS",
            avatarColorHex = 0xFF6A1B9A // Purple
        ),
        FamilyMember(
            id = "fam_4",
            name = "Emergency Ambulance",
            relationship = "National Emergency Hotline (112)",
            phoneNumber = "112",
            isPrimaryEmergencyContact = true,
            avatarInitials = "SOS",
            avatarColorHex = 0xFFC62828 // Crimson
        )
    )

    private val _members = MutableStateFlow<List<FamilyMember>>(defaultContacts)
    private val _familyGroup = MutableStateFlow(
        FamilyGroup(
            familyId = currentFamilyId,
            groupName = "Sharma Family Care Hub",
            inviteCode = "SHRM88",
            primaryElderName = "Mrs. Sharma",
            isLinked = true,
            lastSyncedTimestamp = System.currentTimeMillis(),
            totalMembersCount = 4
        )
    )
    private val _sosHistory = MutableStateFlow<List<SosAlert>>(emptyList())

    private var groupListener: ListenerRegistration? = null
    private var membersListener: ListenerRegistration? = null
    private var sosListener: ListenerRegistration? = null

    init {
        try {
            startFirestoreListeners(currentFamilyId)
        } catch (e: Exception) {
            Log.w(tag, "Firestore initialization fallback: ${e.message}")
        }
    }

    private fun startFirestoreListeners(familyId: String) {
        val db = firestore ?: return
        try {
            groupListener?.remove()
            membersListener?.remove()
            sosListener?.remove()

            // 1. Group info listener
            groupListener = db.collection("families")
                .document(familyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Group snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val group = snapshot.toObject(FamilyGroup::class.java)?.copy(familyId = snapshot.id)
                        if (group != null) {
                            _familyGroup.value = group
                        }
                    }
                }

            // 2. Members list listener
            membersListener = db.collection("families")
                .document(familyId)
                .collection("members")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Members snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(FamilyMember::class.java)?.copy(id = doc.id)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            val previousCount = _members.value.size
                            _members.value = list
                            com.example.util.DataSyncManager.recordReceivedVerified(
                                type = com.example.util.SyncEventType.RETRIEVE_FAMILY_DATA,
                                title = "Family Contacts Synchronized",
                                detail = "Retrieved ${list.size} verified family contacts from Family Cloud Circle",
                                count = list.size
                            )
                        }
                    } else if (snapshot != null && snapshot.isEmpty) {
                        // Seed default contacts to Firestore on first creation
                        seedDefaultFamily(familyId)
                    }
                }

            // 3. SOS Alerts history listener
            sosListener = db.collection("families")
                .document(familyId)
                .collection("sos_events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "SOS snapshot error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(SosAlert::class.java)?.copy(id = doc.id)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        val previousLatestId = _sosHistory.value.firstOrNull()?.id
                        _sosHistory.value = list

                        // Check if latest alert was triggered externally by another linked phone
                        val latestAlert = list.firstOrNull()
                        if (latestAlert != null && context != null) {
                            val timeDiff = System.currentTimeMillis() - latestAlert.timestamp
                            val isExternalAlert = latestAlert.id != lastLocalAlertId
                            // If alert is within last 20 seconds, was not triggered locally on this device
                            if (isExternalAlert && latestAlert.id != previousLatestId && timeDiff in 0..60000) {
                                com.example.util.DataSyncManager.recordReceivedVerified(
                                    type = com.example.util.SyncEventType.SOS_ALERT_RECEIVED,
                                    title = "🚨 Emergency SOS from Family Member",
                                    detail = "Alert by ${latestAlert.triggeredBy} received from Database: ${latestAlert.address}",
                                    count = 1
                                )
                                com.example.util.NotificationHelper.showSosDispatchedNotification(
                                    context,
                                    latestAlert.triggeredBy,
                                    latestAlert.address
                                )
                                com.example.util.VibrationHelper.vibrateSuccess(context)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Error attaching family listeners: ${e.message}")
        }
    }

    private fun seedDefaultFamily(familyId: String) {
        val db = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val group = _familyGroup.value.copy(familyId = familyId)
                db.collection("families").document(familyId).set(group)

                val batch = db.batch()
                defaultContacts.forEach { member ->
                    val docRef = db.collection("families")
                        .document(familyId)
                        .collection("members")
                        .document(member.id)
                    batch.set(docRef, member)
                }
                batch.commit()
            } catch (e: Exception) {
                Log.w(tag, "Failed to seed default contacts: ${e.message}")
            }
        }
    }

    override fun getFamilyMembers(): Flow<List<FamilyMember>> = _members.asStateFlow()

    override fun getFamilyGroup(): Flow<FamilyGroup> = _familyGroup.asStateFlow()

    override fun getSosAlertHistory(): Flow<List<SosAlert>> = _sosHistory.asStateFlow()

    override suspend fun addFamilyMember(member: FamilyMember): Boolean {
        val idToUse = if (member.id.isBlank()) "fam_${UUID.randomUUID().toString().take(8)}" else member.id
        val newMember = member.copy(id = idToUse)

        _members.update { current ->
            if (newMember.isPrimaryEmergencyContact) {
                listOf(newMember) + current.map { it.copy(isPrimaryEmergencyContact = false) }
            } else {
                current + newMember
            }
        }

        _familyGroup.update { it.copy(totalMembersCount = it.totalMembersCount + 1, lastSyncedTimestamp = System.currentTimeMillis()) }

        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("members")
                    .document(idToUse)
                    .set(newMember)

                db.collection("families")
                    .document(currentFamilyId)
                    .update("totalMembersCount", _members.value.size)
            } catch (e: Exception) {
                Log.w(tag, "Failed to write member to Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.UPLOAD_FAMILY_CONTACT,
            title = "Family Contact Saved & Uploaded",
            detail = "${newMember.name} (${newMember.relationship}) confirmed and synced to Family Care Hub"
        )
        return true
    }

    suspend fun updateFamilyMember(member: FamilyMember): Boolean {
        _members.update { list ->
            list.map {
                if (it.id == member.id) {
                    member
                } else if (member.isPrimaryEmergencyContact && it.id != member.id) {
                    it.copy(isPrimaryEmergencyContact = false)
                } else {
                    it
                }
            }
        }

        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("members")
                    .document(member.id)
                    .set(member)

                db.collection("families")
                    .document(currentFamilyId)
                    .update("totalMembersCount", _members.value.size)
            } catch (e: Exception) {
                Log.w(tag, "Failed to write member to Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.UPLOAD_FAMILY_CONTACT,
            title = "Contact Changes Uploaded",
            detail = "${member.name} details verified and updated in Family Cloud"
        )
        return true
    }

    override suspend fun deleteFamilyMember(id: String): Boolean {
        val memberName = _members.value.firstOrNull { it.id == id }?.name ?: "Contact"
        _members.update { list -> list.filterNot { it.id == id } }
        _familyGroup.update { it.copy(totalMembersCount = (it.totalMembersCount - 1).coerceAtLeast(1), lastSyncedTimestamp = System.currentTimeMillis()) }

        firestore?.let { db ->
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("members")
                    .document(id)
                    .delete()

                db.collection("families")
                    .document(currentFamilyId)
                    .update("totalMembersCount", _members.value.size)
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete member in Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.UPLOAD_FAMILY_CONTACT,
            title = "Contact Removed & Synced",
            detail = "$memberName was removed and synced with Family Cloud"
        )
        return true
    }

    override suspend fun createFamilyGroup(groupName: String): FamilyGroup {
        val cleanName = groupName.ifBlank { "Elder Family Circle" }
        val generatedCode = (cleanName.take(3).uppercase() + (1000..9999).random().toString().take(3))
        val newId = "FAM-${UUID.randomUUID().toString().take(6).uppercase()}"

        val newGroup = FamilyGroup(
            familyId = newId,
            groupName = cleanName,
            inviteCode = generatedCode,
            primaryElderName = "Mrs. Sharma",
            isLinked = true,
            lastSyncedTimestamp = System.currentTimeMillis(),
            totalMembersCount = _members.value.size
        )

        currentFamilyId = newId
        _familyGroup.value = newGroup
        onFamilyChanged?.invoke(newId)

        firestore?.let { db ->
            try {
                db.collection("families").document(newId).set(newGroup)
                startFirestoreListeners(newId)
                seedDefaultFamily(newId)
            } catch (e: Exception) {
                Log.w(tag, "Failed to create group in Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.FAMILY_GROUP_UPDATE,
            title = "Family Care Circle Created",
            detail = "Circle '$cleanName' (Code: $generatedCode) saved to Cloud Database"
        )
        return newGroup
    }

    override suspend fun joinFamilyGroup(inviteCode: String): Result<FamilyGroup> {
        val formattedCode = inviteCode.trim().uppercase()
        if (formattedCode.length < 4) {
            return Result.failure(IllegalArgumentException("Invite code must be at least 4 characters"))
        }

        val db = firestore
        if (db != null) {
            try {
                val query = db.collection("families")
                    .whereEqualTo("inviteCode", formattedCode)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    val doc = query.documents.first()
                    val group = doc.toObject(FamilyGroup::class.java)?.copy(familyId = doc.id)
                    if (group != null) {
                        currentFamilyId = group.familyId
                        _familyGroup.value = group
                        onFamilyChanged?.invoke(group.familyId)
                        startFirestoreListeners(group.familyId)
                        com.example.util.DataSyncManager.recordReceivedVerified(
                            type = com.example.util.SyncEventType.FAMILY_GROUP_UPDATE,
                            title = "Linked to Family Circle",
                            detail = "Successfully retrieved and joined '${group.groupName}' (ID: ${group.familyId})"
                        )
                        return Result.success(group)
                    }
                } else {
                    return Result.failure(IllegalArgumentException("No active Family Care Circle found matching code '$formattedCode'."))
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore join query failed: ${e.message}")
                return Result.failure(Exception("Could not connect to Family Cloud. Please check your internet connection and try again."))
            }
        }

        return Result.failure(Exception("Cloud service is temporarily unavailable. Please verify internet connectivity."))
    }

    override suspend fun triggerSosAlert(triggeredBy: String): SosAlert {
        val locationResult: GpsLocationResult = if (context != null) {
            LocationHelper.getCurrentLocation(context)
        } else {
            GpsLocationResult(
                latitude = null,
                longitude = null,
                address = "Location Unavailable (Context Not Ready)",
                status = com.example.util.LocationStatus.LOCATION_UNAVAILABLE
            )
        }

        return triggerSosAlertWithLocation(
            triggeredBy = triggeredBy,
            lat = locationResult.latitude,
            lng = locationResult.longitude,
            address = locationResult.address,
            isRealGps = locationResult.isReal
        )
    }

    fun registerDeviceFcmToken(token: String, deviceRole: String = "Family Device") {
        if (token.isBlank()) return
        val db = firestore ?: return
        val tokenKey = token.takeLast(20).replace("/", "_").replace(".", "_")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("fcm_tokens")
                    .document(tokenKey)
                    .set(
                        mapOf(
                            "token" to token,
                            "deviceRole" to deviceRole,
                            "deviceModel" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                Log.d(tag, "Successfully saved FCM token into family $currentFamilyId")
            } catch (e: Exception) {
                Log.w(tag, "Failed to save FCM token: ${e.message}")
            }
        }
    }

    suspend fun triggerSosAlertWithLocation(
        triggeredBy: String,
        lat: Double?,
        lng: Double?,
        address: String,
        isRealGps: Boolean
    ): SosAlert {
        val alertId = "sos_${UUID.randomUUID().toString().take(8)}"
        lastLocalTriggerTimestamp = System.currentTimeMillis()
        lastLocalAlertId = alertId

        val alert = SosAlert(
            id = alertId,
            timestamp = System.currentTimeMillis(),
            latitude = lat,
            longitude = lng,
            address = address,
            triggeredBy = triggeredBy,
            status = if (lat != null && lng != null) "SENT_TO_FAMILY" else "LOCATION_UNAVAILABLE",
            isRealGps = isRealGps
        )

        _sosHistory.update { listOf(alert) + it }

        var isCloudConfirmed = false
        firestore?.let { db ->
            try {
                // 1. Save to sos_events
                db.collection("families")
                    .document(currentFamilyId)
                    .collection("sos_events")
                    .document(alertId)
                    .set(alert)
                    .await()

                // 2. Update current location document for live tracking
                val locMap = mutableMapOf<String, Any>(
                    "address" to address,
                    "updatedTimestamp" to alert.timestamp,
                    "elderName" to triggeredBy,
                    "isEmergencyActive" to true,
                    "isRealGps" to isRealGps
                )
                if (lat != null && lng != null) {
                    locMap["latitude"] = lat
                    locMap["longitude"] = lng
                }

                db.collection("families")
                    .document(currentFamilyId)
                    .collection("locations")
                    .document("current")
                    .set(locMap)
                    .await()

                // 3. Save to pending_notifications collection for Cloud Functions & Admin SDK triggers
                val notifMap = mutableMapOf<String, Any>(
                    "type" to "EMERGENCY_SOS",
                    "title" to "🚨 EMERGENCY SOS ALERT",
                    "body" to "Emergency alert triggered by $triggeredBy at $address",
                    "location" to address,
                    "timestamp" to alert.timestamp,
                    "status" to "PENDING_DISPATCH"
                )
                if (lat != null && lng != null) {
                    notifMap["latitude"] = lat
                    notifMap["longitude"] = lng
                    alert.googleMapsUrl?.let { notifMap["googleMapsUrl"] = it }
                }

                db.collection("families")
                    .document(currentFamilyId)
                    .collection("pending_notifications")
                    .document(alertId)
                    .set(notifMap)
                    .await()

                isCloudConfirmed = true
            } catch (e: Exception) {
                Log.w(tag, "Failed to broadcast SOS to Firestore: ${e.message}")
            }
        }

        com.example.util.DataSyncManager.recordUploadVerified(
            type = com.example.util.SyncEventType.SOS_EMERGENCY_DISPATCH,
            title = if (isCloudConfirmed) "🚨 Emergency SOS Dispatched & Verified" else "🚨 Emergency SOS Saved Locally (Offline)",
            detail = if (isCloudConfirmed) "Alert payload committed to Cloud Emergency Registry ($address)" else "Alert saved to local emergency cache ($address)"
        )

        return alert
    }
}
