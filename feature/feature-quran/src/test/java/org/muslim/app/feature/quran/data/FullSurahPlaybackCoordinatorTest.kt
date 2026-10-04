package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import org.muslim.app.feature.quran.domain.FullSurahRecitation

class FullSurahPlaybackCoordinatorTest {
    @Test
    fun `hashes MP3Quran recording ids for offline storage paths`() {
        val first = fullSurahStorageKey("mp3quran:12:123")
        val second = fullSurahStorageKey("mp3quran:12:123")

        assertThat(first).isNotNull()
        assertThat(first).isEqualTo(second)
        assertThat(first.orEmpty()).matches("[a-f0-9]{64}")
        assertThat(fullSurahStorageKey("../outside")).isNull()
        assertThat(fullSurahStorageKey("reader/other")).isNull()
        assertThat(fullSurahStorageKey("")).isNull()
    }

    @Test
    fun `starts full surah on shared player preserving requested queue position`() {
        val player = mockk<QuranAudioPlayer>(relaxed = true)
        val sessions = mockk<RecitationSessionRuntime>(relaxed = true)
        val queue = slot<List<RecitationQueueItem>>()
        val repository = mockk<RecitationRepository>(relaxed = true)
        every { repository.localFullSurahFile(any(), any()) } returns null
        val coordinator = FullSurahPlaybackCoordinator(player, sessions, repository)
        val recording = recording()

        val intent = coordinator.start(recording, surahNumber = 2)

        assertThat(intent?.fullSurahAudioUrl).isEqualTo("https://server9.mp3quran.net/abkr/002.mp3")
        verify {
            player.playQueue(
                items = capture(queue),
                startIndex = 0,
                repeatCount = 1,
                continuous = false,
                startPositionMs = 0L,
                remainingRepeatsForCurrent = null,
            )
        }
        assertThat(queue.captured.single().playbackScope).isEqualTo(RecitationPlaybackScope.FullSurah)
        assertThat(queue.captured.single().streamUrl).isEqualTo(intent?.fullSurahAudioUrl)
        assertThat(queue.captured.single().globalNumber).isEqualTo(8)
    }

    @Test
    fun `rejects recording outside available surahs without starting player`() {
        val player = mockk<QuranAudioPlayer>(relaxed = true)
        val coordinator = FullSurahPlaybackCoordinator(player, mockk(relaxed = true), mockk(relaxed = true))

        assertThat(coordinator.start(recording(), surahNumber = 3)).isNull()
        verify(exactly = 0) { player.playQueue(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `does not start local audio while player is controlled remotely`() {
        val player = mockk<QuranAudioPlayer>(relaxed = true)
        every { player.isRemotelyControlled } returns true
        val coordinator = FullSurahPlaybackCoordinator(player, mockk(relaxed = true), mockk(relaxed = true))
        val intent = RecitationSessionIntent(
            reciterId = "reader-1",
            surahNumber = 2,
            globalNumbers = listOf(8),
            repeatCount = 1,
            continuous = false,
            advanceToNext = false,
            toEndOfQuran = false,
            fullSurahAudioUrl = "https://server9.mp3quran.net/abkr/002.mp3",
        )

        assertThat(coordinator.play(intent)).isFalse()
        verify(exactly = 0) { player.playQueue(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `preserves position and repeat state while restoring full surah session`() {
        val player = mockk<QuranAudioPlayer>(relaxed = true)
        val sessions = mockk<RecitationSessionRuntime>(relaxed = true)
        val queue = slot<List<RecitationQueueItem>>()
        val repository = mockk<RecitationRepository>(relaxed = true)
        every { repository.localFullSurahFile(any(), any()) } returns null
        val coordinator = FullSurahPlaybackCoordinator(player, sessions, repository)
        val intent = RecitationSessionIntent(
            reciterId = "reader-1",
            surahNumber = 2,
            globalNumbers = listOf(8),
            repeatCount = 2,
            continuous = false,
            advanceToNext = false,
            toEndOfQuran = false,
            fullSurahAudioUrl = "https://server9.mp3quran.net/abkr/002.mp3",
        )

        assertThat(coordinator.play(intent, startPositionMs = 45_000, remainingRepeats = 2)).isTrue()
        verify { sessions.begin(intent, 45_000, 2) }
        verify {
            player.playQueue(
                items = capture(queue),
                startIndex = 0,
                repeatCount = 2,
                continuous = false,
                startPositionMs = 45_000,
                remainingRepeatsForCurrent = 2,
            )
        }
        assertThat(queue.captured.single().playbackScope).isEqualTo(RecitationPlaybackScope.FullSurah)
    }

    @Test
    fun `uses downloaded full surah file instead of network url`() {
        val player = mockk<QuranAudioPlayer>(relaxed = true)
        val sessions = mockk<RecitationSessionRuntime>(relaxed = true)
        val queue = slot<List<RecitationQueueItem>>()
        val localFile = java.io.File("/private/reader/2.mp3")
        val repository = mockk<RecitationRepository>()
        every { repository.localFullSurahFile("reader-1", 2) } returns localFile
        val coordinator = FullSurahPlaybackCoordinator(player, sessions, repository)

        assertThat(coordinator.start(recording(), 2)).isNotNull()

        verify {
            player.playQueue(
                items = capture(queue),
                startIndex = 0,
                repeatCount = 1,
                continuous = false,
                startPositionMs = 0L,
                remainingRepeatsForCurrent = null,
            )
        }
        assertThat(queue.captured.single().file).isEqualTo(localFile)
        assertThat(queue.captured.single().streamUrl).isNull()
    }

    private fun recording() = FullSurahRecitation(
        id = "reader-1",
        reciterId = 1,
        reciterName = "Reader",
        recordingId = 10,
        rewayaName = "Hafs",
        sourceBaseUrl = "https://server9.mp3quran.net/abkr/",
        availableSurahs = listOf(2),
        declaredSurahCount = 1,
    )
}
