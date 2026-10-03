package org.muslim.app.feature.quran.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.muslim.app.feature.quran.domain.FullSurahRecitation
import org.muslim.app.feature.quran.domain.VerifiedMp3QuranAudio
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** On-demand client for MP3Quran's multilingual full-surah recitation catalogue. */
@Singleton
class Mp3QuranCatalogClient @Inject constructor(
    private val httpClient: OkHttpClient,
) {
    suspend fun fetch(languageCode: String = "ar"): List<FullSurahRecitation> = withContext(Dispatchers.IO) {
        require(LANGUAGE_CODE.matches(languageCode)) { "Unsupported language code" }
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("language", languageCode)
            .build()
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("MP3Quran catalogue request failed: HTTP ${response.code}")
            val contentType = response.header("Content-Type").orEmpty()
            val mediaType = contentType.substringBefore(';').trim()
            if (
                !mediaType.equals("application/json", ignoreCase = true) &&
                !mediaType.endsWith("+json", ignoreCase = true)
            ) {
                throw IOException("MP3Quran catalogue returned a non-JSON response")
            }
            val body = response.body.string()
            Mp3QuranCatalogParser.parse(body).takeIf { it.isNotEmpty() }
                ?: throw IOException("MP3Quran catalogue response contained no valid recordings")
        }
    }

    /** Probe one advertised URL without downloading audio bytes or inferring an unavailable bitrate. */
    suspend fun verifyAudioSource(
        recording: FullSurahRecitation,
        surahNumber: Int,
    ): VerifiedMp3QuranAudio? = withContext(Dispatchers.IO) {
        val audioUrl = recording.audioUrl(surahNumber) ?: return@withContext null
        val request = Request.Builder().url(audioUrl).head().build()
        httpClient.newCall(request).execute().use { response ->
            Mp3QuranAudioProbe.inspect(
                statusCode = response.code,
                contentType = response.header("Content-Type"),
                contentLength = response.header("Content-Length")?.toLongOrNull(),
                acceptRanges = response.header("Accept-Ranges"),
            )
        }
    }

    private companion object {
        const val BASE_URL = "https://mp3quran.net/api/v3/reciters"
        val LANGUAGE_CODE = Regex("[a-z]{2,3}")
    }
}
