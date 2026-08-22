package org.muslim.app.core.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the optional home-automation bearer token encrypted with an Android
 * Keystore AES key. The endpoint remains a normal user-visible preference,
 * while the secret is never placed in DataStore, logs, notifications or events.
 */
@Singleton
class SmartHomeBridgeSecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun save(token: String) {
        val normalized = token.trim()
        val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (normalized.isEmpty()) {
            prefs.edit().remove(TOKEN_KEY).apply()
            return
        }
        prefs.edit().putString(TOKEN_KEY, encrypt(normalized)).apply()
    }

    fun read(): String? {
        val encrypted = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .getString(TOKEN_KEY, null)
            ?: return null
        return runCatching { decrypt(encrypted) }.getOrNull()
    }

    fun clear() {
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .edit()
            .remove(TOKEN_KEY)
            .apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = ByteBuffer.allocate(cipher.iv.size + encrypted.size)
            .put(cipher.iv)
            .put(encrypted)
            .array()
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > IV_LENGTH_BYTES)
        val buffer = ByteBuffer.wrap(payload)
        val iv = ByteArray(IV_LENGTH_BYTES).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), javax.crypto.spec.GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "muslim_smart_home_bridge_token_v1"
        const val PREFERENCES_FILE = "smart_home_bridge_secret"
        const val TOKEN_KEY = "encrypted_token"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
        const val TAG_LENGTH_BITS = 128
    }
}
