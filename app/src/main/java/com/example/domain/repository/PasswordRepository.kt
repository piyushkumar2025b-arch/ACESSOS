package com.example.domain.repository

import com.example.domain.model.PasswordEntry
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAllPasswords(): Flow<List<PasswordEntry>>
    suspend fun addPassword(password: PasswordEntry): Long
    suspend fun updatePassword(password: PasswordEntry)
    suspend fun deletePassword(id: Long)
}
