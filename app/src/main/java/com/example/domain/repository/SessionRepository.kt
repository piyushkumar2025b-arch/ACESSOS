package com.example.domain.repository

import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val sessionState: StateFlow<UserSession>
    fun startSession(rfidUid: String, userName: String = "Authorized User", familyId: String = "FAM-CARE-01")
    fun validateAndUnlockWithCard(rfidUid: String, userName: String = "Authorized User"): Result<Boolean>
    fun enrollCard(rfidUid: String): Boolean
    fun recordActivity()
    fun endSession()
    fun isSessionActive(): Boolean
}
