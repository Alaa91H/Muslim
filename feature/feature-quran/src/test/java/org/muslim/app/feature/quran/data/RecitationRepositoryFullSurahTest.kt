package org.muslim.app.feature.quran.data

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.muslim.app.core.network.FileDownloader
import java.io.File
import java.nio.file.Files

class RecitationRepositoryFullSurahTest {
    @Test
    fun `downloads trusted recording to app private resumable destination`() = runBlocking {
        val appFiles = Files.createTempDirectory("quran-offline-test").toFile()
        try {
            val context = mockk<Context>()
            every { context.filesDir } returns appFiles
            val downloader = mockk<FileDownloader>()
            val url = "https://server9.mp3quran.net/abkr/002.mp3"
            val recordingId = "mp3quran:1:10"
            val storageKey = requireNotNull(fullSurahStorageKey(recordingId))
            val expected = File(appFiles, "quran_recitations/full_surah/$storageKey/2.mp3")
            coEvery { downloader.download(url, expected, any()) } coAnswers {
                expected.parentFile?.mkdirs()
                expected.writeBytes(byteArrayOf(1, 2, 3))
                FileDownloader.Result.Success(expected)
            }
            val repository = RecitationRepository(context, downloader, mockk())

            val result = repository.downloadFullSurah(recordingId, 2, url)

            assertThat(result).isEqualTo(FileDownloader.Result.Success(expected))
            assertThat(repository.localFullSurahFile(recordingId, 2)).isEqualTo(expected)
            coVerify(exactly = 1) { downloader.download(url, expected, any()) }
        } finally {
            appFiles.deleteRecursively()
        }
    }

    @Test
    fun `rejects untrusted source and path traversal before download`() = runBlocking {
        val context = mockk<Context>()
        val downloader = mockk<FileDownloader>()
        val repository = RecitationRepository(context, downloader, mockk())

        assertThat(repository.downloadFullSurah("../escape", 2, "https://attacker.example/2.mp3"))
            .isInstanceOf(FileDownloader.Result.Failure::class.java)
        coVerify(exactly = 0) { downloader.download(any(), any(), any()) }
    }
}
