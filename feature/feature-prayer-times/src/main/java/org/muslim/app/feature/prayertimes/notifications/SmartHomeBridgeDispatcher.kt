package org.muslim.app.feature.prayertimes.notifications

import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.SmartHomeBridgeSecretStore
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sends an explicitly configured, minimal adhan-start event to a user's HTTPS
 * automation endpoint. It never retries, queues, logs, or blocks local adhan
 * playback; the receiver treats it as best-effort supplementary automation.
 */
@Singleton
class SmartHomeBridgeDispatcher @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val secretStore: SmartHomeBridgeSecretStore,
    private val client: OkHttpClient,
) {
    suspend fun dispatchAdhanStarted(prayer: Prayer): Boolean {
        val preferences = appPreferencesRepository.preferences.first()
        val endpoint = SmartHomeEndpointValidator.normalizedHttps(preferences.smartHomeBridgeEndpoint)
            ?: return false
        if (!preferences.smartHomeBridgeEnabled) return false

        val token = runCatching { secretStore.read() }.getOrNull()
        val body = """{"schemaVersion":1,"event":"adhan_started","prayer":"${prayer.name.lowercase()}","occurredAtEpochMillis":${System.currentTimeMillis()},"source":"muslim_android"}"""
            .toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .header("Accept", "application/json")
            .apply {
                token?.takeIf(String::isNotBlank)?.let { bearerToken ->
                    header("Authorization", "Bearer $bearerToken")
                }
            }
            .build()

        return runCatching {
            client.newBuilder()
                .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
                .newCall(request)
                .execute()
                .use { response -> response.isSuccessful }
        }.getOrDefault(false)
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val CALL_TIMEOUT_SECONDS = 5L
    }
}

/** Validates the opt-in endpoint before any network call is made. */
object SmartHomeEndpointValidator {
    fun normalizedHttps(raw: String): String? {
        val endpoint = raw.trim()
        val uri = runCatching { URI(endpoint) }.getOrNull() ?: return null
        return endpoint.takeIf {
            uri.scheme.equals("https", ignoreCase = true) &&
                !uri.host.isNullOrBlank() &&
                uri.userInfo.isNullOrBlank() &&
                uri.fragment.isNullOrBlank()
        }
    }
}
