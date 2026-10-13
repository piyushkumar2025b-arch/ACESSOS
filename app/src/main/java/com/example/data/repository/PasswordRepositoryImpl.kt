package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.PasswordDao
import com.example.data.local.entity.PasswordEntity
import com.example.domain.model.PasswordEntry
import com.example.domain.repository.PasswordRepository
import com.example.util.CryptoHelper
import com.example.util.DataSyncManager
import com.example.util.SyncEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepositoryImpl(
    private val passwordDao: PasswordDao,
    private val context: Context
) : PasswordRepository {

    override fun getAllPasswords(): Flow<List<PasswordEntry>> {
        return passwordDao.getAllPasswords().map { list ->
            list.map { entity -> entity.toDomainModel() }
        }
    }

    override suspend fun addPassword(password: PasswordEntry): Long {
        val encryptResult = CryptoHelper.encrypt(context, password.password)
        if (encryptResult.isFailure) {
            throw SecurityException("Cannot save password: Encrypted vault encryption failed.", encryptResult.exceptionOrNull())
        }

        val encryptedPassword = encryptResult.getOrThrow()
        val entity = password.toEntity().copy(password = encryptedPassword)
        val insertedId = passwordDao.insertPassword(entity)

        DataSyncManager.recordUploadVerified(
            type = SyncEventType.LOCAL_VAULT_SAVE,
            title = "Password Encrypted & Saved",
            detail = "'${password.title}' encrypted with Android Keystore AES-GCM and stored in local database.",
            storageTarget = "Local Room SQLite Database (Encrypted)"
        )
        return insertedId
    }

    override suspend fun updatePassword(password: PasswordEntry) {
        val encryptResult = CryptoHelper.encrypt(context, password.password)
        if (encryptResult.isFailure) {
            throw SecurityException("Cannot update password: Encrypted vault encryption failed.", encryptResult.exceptionOrNull())
        }

        val encryptedPassword = encryptResult.getOrThrow()
        val entity = password.toEntity().copy(password = encryptedPassword)
        passwordDao.updatePassword(entity)

        DataSyncManager.recordUploadVerified(
            type = SyncEventType.LOCAL_VAULT_UPDATE,
            title = "Password Updated",
            detail = "'${password.title}' re-encrypted and updated in local database.",
            storageTarget = "Local Room SQLite Database (Encrypted)"
        )
    }

    override suspend fun deletePassword(id: Long) {
        passwordDao.deleteById(id)
        DataSyncManager.recordUploadVerified(
            type = SyncEventType.LOCAL_VAULT_DELETE,
            title = "Password Deleted",
            detail = "Password entry #$id securely erased from local encrypted vault.",
            storageTarget = "Local Room SQLite Database (Encrypted)"
        )
    }

    private fun PasswordEntity.toDomainModel(): PasswordEntry {
        val decryptedPass = CryptoHelper.decrypt(context, password).getOrDefault(password)
        return PasswordEntry(
            id = id,
            title = title,
            username = username,
            password = decryptedPass,
            category = category,
            notes = notes,
            createdAt = createdAt
        )
    }

    private fun PasswordEntry.toEntity(): PasswordEntity {
        return PasswordEntity(
            id = id,
            title = title,
            username = username,
            password = password,
            category = category,
            notes = notes,
            createdAt = createdAt
        )
    }
}
