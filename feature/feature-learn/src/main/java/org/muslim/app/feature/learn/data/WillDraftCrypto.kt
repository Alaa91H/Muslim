package org.muslim.app.feature.learn.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts the private will draft with an app-specific AES key held by
 * Android Keystore. The key material never leaves the system keystore.
 *
 * This protects the persisted draft at rest. It is intentionally not tied to
 * biometric authentication so background migration and normal draft loading
 * remain reliable; an optional biometric gate can be layered above it later.
 */
@Singleton
class WillDraftCrypto @Inject constructor() {
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val iv = Base64.getEncoder().encodeToString(cipher.iv)
        val cipherText = Base64.getEncoder().encodeToString(encrypted)
        return "$PAYLOAD_VERSION:$iv:$cipherText"
    }

    fun decrypt(payload: String): String? = runCatching {
        val parts = payload.split(':', limit = 3)
        require(parts.size == 3 && parts[0] == PAYLOAD_VERSION)

        val iv = Base64.getDecoder().decode(parts[1])
        val cipherText = Base64.getDecoder().decode(parts[2])
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getExistingKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
        )

        String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
    }.getOrNull()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = loadKeyStore()
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) {
            return existing
        }

        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(KEY_SIZE_BITS)
                        .build(),
                )
            }
            .generateKey()
    }

    private fun getExistingKey(): SecretKey {
        val key = loadKeyStore().getKey(KEY_ALIAS, null) as? SecretKey
        requireNotNull(key) { "Will draft encryption key is unavailable" }
        return key
    }

    private fun loadKeyStore(): KeyStore = KeyStore
        .getInstance(ANDROID_KEYSTORE)
        .apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "muslim_will_draft_aes_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PAYLOAD_VERSION = "v1"
        const val KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
