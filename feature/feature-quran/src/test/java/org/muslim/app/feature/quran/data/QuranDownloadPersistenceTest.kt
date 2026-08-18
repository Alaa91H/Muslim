package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class QuranDownloadPersistenceTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `download request round-trips through json`() {
        val request = DownloadRequest(
            id = "surah-2-reciter-1",
            reciterId = "reciter",
            reciterName = "القارئ",
            scope = DownloadScope.Surah,
            surahNumber = 2,
            globalNumber = null,
            label = "سورة البقرة",
            totalBytes = 12_345L,
            nightOnly = true,
        )
        val persisted = PersistedDownload(
            request = request,
            status = DownloadStatus.Paused.name,
            downloadedBytes = 100L,
            progress = 0.5f,
        )

        val encoded = json.encodeToString(PersistedDownload.serializer(), persisted)
        val decoded = json.decodeFromString(PersistedDownload.serializer(), encoded)

        assertThat(decoded.request).isEqualTo(request)
        assertThat(decoded.request.scope).isEqualTo(DownloadScope.Surah)
        assertThat(decoded.status).isEqualTo(DownloadStatus.Paused.name)
        assertThat(decoded.downloadedBytes).isEqualTo(100L)
        assertThat(decoded.progress).isEqualTo(0.5f)
    }

    @Test
    fun `empty queue decodes to an empty list`() {
        val decoded = json.decodeFromString<List<PersistedDownload>>("[]")
        assertThat(decoded).isEmpty()
    }

    @Test
    fun `full quran scope serializes without a surah number`() {
        val request = DownloadRequest(
            id = "full-reciter-1",
            reciterId = "reciter",
            reciterName = "القارئ",
            scope = DownloadScope.FullQuran,
            surahNumber = null,
            globalNumber = null,
            label = "القرآن الكريم كاملًا",
            totalBytes = 1_000_000L,
        )
        val encoded = json.encodeToString(DownloadRequest.serializer(), request)
        val decoded = json.decodeFromString(DownloadRequest.serializer(), encoded)
        assertThat(decoded.surahNumber).isNull()
        assertThat(decoded.scope).isEqualTo(DownloadScope.FullQuran)
    }
}
