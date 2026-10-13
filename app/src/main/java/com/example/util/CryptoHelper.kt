package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore backed AES-GCM-256 encryption.
 * - Random IV per encryption (12 bytes)
 * - Authenticated ciphertext with GCM authentication tag
 * - No hardcoded keys or static IVs
 * - Hard failure on encryption errors (NEVER returns plaintext)
 */
object CryptoHelper {
    private const val TAG = "CryptoHelper"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "access_os_vault_master_key_v2"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PREFIX = "GCM_V2:"

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        // Generate a new 256-bit AES key inside Android Keystore
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns Result.success(ciphertext) or Result.failure(exception).
     * NEVER falls back to storing or returning plaintext!
     */
    fun encrypt(context: Context, plainText: String): Result<String> {
        if (plainText.isEmpty()) return Result.success("")
        if (plainText.startsWith(PREFIX)) return Result.success(plainText)

        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val iv = cipher.iv
            val cipherTextBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherTextBytes, Base64.NO_WRAP)

            Result.success("$PREFIX$ivBase64:$cipherBase64")
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed securely: ${e.message}", e)
            Result.failure(SecurityException("Vault encryption failed securely. Plaintext will not be stored.", e))
        }
    }

    /**
     * Decrypts ciphertext using AES-256-GCM.
     * Returns Result.success(plainText) or Result.failure(exception).
     */
    fun decrypt(context: Context, cipherText: String): Result<String> {
        if (cipherText.isEmpty()) return Result.success("")
        if (!cipherText.startsWith(PREFIX)) {
            // Unencrypted or legacy format - return directly for migration if valid
            return Result.success(cipherText)
        }

        return try {
            val payload = cipherText.removePrefix(PREFIX)
            val parts = payload.split(":")
            if (parts.size != 2) {
                return Result.failure(IllegalArgumentException("Invalid encrypted payload format"))
            }

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            Result.success(String(decryptedBytes, StandardCharsets.UTF_8))
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}", e)
            Result.failure(SecurityException("Vault decryption failed: Authentication tag mismatch or corrupted data", e))
        }
    }
}
