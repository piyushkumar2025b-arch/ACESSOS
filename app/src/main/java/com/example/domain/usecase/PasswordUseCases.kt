package com.example.domain.usecase

import com.example.domain.model.PasswordEntry
import com.example.domain.repository.PasswordRepository
import com.example.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PasswordUseCases(
    private val passwordRepository: PasswordRepository,
    private val sessionRepository: SessionRepository
) {
    fun getAllPasswords(): Flow<List<PasswordEntry>> {
        // Enforce Session Security: If session is locked, never emit passwords
        return if (sessionRepository.isSessionActive()) {
            passwordRepository.getAllPasswords()
        } else {
            emptyFlow()
        }
    }

    suspend fun addPassword(entry: PasswordEntry): Long {
        if (!sessionRepository.isSessionActive()) {
            throw IllegalStateException("Cannot add password when session is locked")
        }
        return passwordRepository.addPassword(entry)
    }

    suspend fun updatePassword(entry: PasswordEntry) {
        if (!sessionRepository.isSessionActive()) {
            throw IllegalStateException("Cannot update password when session is locked")
        }
        passwordRepository.updatePassword(entry)
    }

    suspend fun deletePassword(id: Long) {
        if (!sessionRepository.isSessionActive()) {
            throw IllegalStateException("Cannot delete password when session is locked")
        }
        passwordRepository.deletePassword(id)
    }
}
