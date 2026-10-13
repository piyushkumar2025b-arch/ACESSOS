package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.domain.model.UserSession
import com.example.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepositoryImpl(
    private val context: Context? = null
) : SessionRepository {

    private val tag = "SessionRepo"
    private val prefs: SharedPreferences? = context?.getSharedPreferences("access_os_session_prefs", Context.MODE_PRIVATE)

    // Configurable 30-minute inactivity timeout for secure session
    private val sessionInactivityTimeoutMs = 30 * 60 * 1000L

    // Enrolled authorized tags whitelist
    private val defaultAuthorizedUids = setOf("04A3B2C1", "04F18B92", "04D9C8A1")

    private val _sessionState = MutableStateFlow(loadInitialSession())
    override val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private fun loadInitialSession(): UserSession {
        val sp = prefs ?: return UserSession(isUnlocked = false)
        val isUnlocked = sp.getBoolean("is_unlocked", false)
        val unlockTime = sp.getLong("unlocked_timestamp", 0L)
        val lastActivity = sp.getLong("last_activity_timestamp", unlockTime)
        val rfidUid = sp.getString("rfid_uid", null)
        val userName = sp.getString("user_name", "Authorized Elder") ?: "Authorized Elder"
        val familyId = sp.getString("family_id", "FAM-CARE-01") ?: "FAM-CARE-01"

        val now = System.currentTimeMillis()
        return if (isUnlocked && (now - lastActivity < sessionInactivityTimeoutMs)) {
            UserSession(
                isUnlocked = true,
                rfidUid = rfidUid,
                userName = userName,
                unlockedTimestamp = unlockTime,
                lastActivityTimestamp = lastActivity,
                activeFamilyId = familyId
            )
        } else {
            // Expired session
            sp.edit().clear().apply()
            UserSession(isUnlocked = false)
        }
    }

    private fun getEnrolledUids(): Set<String> {
        val custom = prefs?.getStringSet("enrolled_uids", emptySet()) ?: emptySet()
        return defaultAuthorizedUids + custom
    }

    override fun enrollCard(rfidUid: String): Boolean {
        if (rfidUid.isBlank()) return false
        val cleanUid = rfidUid.uppercase().trim()
        val current = getEnrolledUids().toMutableSet()
        current.add(cleanUid)
        prefs?.edit()?.putStringSet("enrolled_uids", current)?.apply()
        Log.i(tag, "Enrolled authorized care card successfully (UID hash: ${cleanUid.hashCode()})")
        return true
    }

    override fun validateAndUnlockWithCard(rfidUid: String, userName: String): Result<Boolean> {
        val cleanUid = rfidUid.uppercase().trim()
        val enrolled = getEnrolledUids()

        if (!enrolled.contains(cleanUid)) {
            Log.w(tag, "Rejected unauthorized NFC card scan attempt (hash: ${cleanUid.hashCode()})")
            return Result.failure(SecurityException("Unauthorized card: This RFID/NFC card is not enrolled in your Care Circle."))
        }

        startSession(cleanUid, userName)
        return Result.success(true)
    }

    override fun startSession(rfidUid: String, userName: String, familyId: String) {
        val now = System.currentTimeMillis()
        prefs?.edit()?.apply {
            putBoolean("is_unlocked", true)
            putString("rfid_uid", rfidUid)
            putString("user_name", userName)
            putString("family_id", familyId)
            putLong("unlocked_timestamp", now)
            putLong("last_activity_timestamp", now)
            apply()
        }

        _sessionState.value = UserSession(
            isUnlocked = true,
            rfidUid = rfidUid,
            userName = userName,
            unlockedTimestamp = now,
            lastActivityTimestamp = now,
            activeFamilyId = familyId
        )
    }

    override fun recordActivity() {
        val current = _sessionState.value
        if (!current.isUnlocked) return
        val now = System.currentTimeMillis()
        prefs?.edit()?.putLong("last_activity_timestamp", now)?.apply()
        _sessionState.value = current.copy(lastActivityTimestamp = now)
    }

    override fun endSession() {
        prefs?.edit()?.apply {
            remove("is_unlocked")
            remove("unlocked_timestamp")
            remove("last_activity_timestamp")
            remove("rfid_uid")
            apply()
        }
        _sessionState.value = UserSession(
            isUnlocked = false,
            rfidUid = null,
            unlockedTimestamp = 0L,
            lastActivityTimestamp = 0L
        )
    }

    override fun isSessionActive(): Boolean {
        val current = _sessionState.value
        if (!current.isUnlocked) return false
        val now = System.currentTimeMillis()
        if (now - current.lastActivityTimestamp > sessionInactivityTimeoutMs) {
            endSession()
            return false
        }
        return true
    }
}
