package com.example.domain.usecase

import com.example.domain.model.UserSession
import com.example.domain.repository.SessionRepository
import kotlinx.coroutines.flow.StateFlow

class SessionUseCases(
    private val sessionRepository: SessionRepository
) {
    val sessionState: StateFlow<UserSession> = sessionRepository.sessionState

    fun validateAndUnlockWithCard(uid: String, userName: String = "Authorized User"): Result<Boolean> {
        return sessionRepository.validateAndUnlockWithCard(uid, userName)
    }

    fun unlockWithRfid(uid: String = "04A3B2C1", userName: String = "Authorized User") {
        sessionRepository.validateAndUnlockWithCard(uid, userName)
    }

    fun recordActivity() {
        sessionRepository.recordActivity()
    }

    fun enrollCard(uid: String): Boolean {
        return sessionRepository.enrollCard(uid)
    }

    fun logout() {
        sessionRepository.endSession()
    }

    fun isUnlocked(): Boolean = sessionRepository.isSessionActive()
}
