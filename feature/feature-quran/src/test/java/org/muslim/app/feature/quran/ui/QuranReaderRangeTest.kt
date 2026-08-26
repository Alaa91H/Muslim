package org.muslim.app.feature.quran.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.data.PlaybackState

/**
 * Tests the pure playback-advance decision used by the reader's continuous
 * recitation ("بدون توقف") and "إلى نهاية القرآن" range.
 */
class QuranReaderRangeTest {

    @Test
    fun `reader default range continues to the end of the mushaf`() {
        assertThat(DEFAULT_RECITATION_RANGE).isEqualTo(RecitationRange.FromAyahToEnd)
    }

    @Test
    fun `reciter change resumes only when an ayah was active`() {
        assertThat(shouldResumeAfterReciterChange(PlaybackState.Playing, 42)).isTrue()
        assertThat(shouldResumeAfterReciterChange(PlaybackState.Paused, 42)).isTrue()
        assertThat(shouldResumeAfterReciterChange(PlaybackState.Idle, 42)).isFalse()
        assertThat(shouldResumeAfterReciterChange(PlaybackState.Playing, null)).isFalse()
    }

    @Test
    fun `to end of Quran stops at surah 114`() {
        assertThat(nextSurahForAdvance(114, toEndOfQuran = true, stopAtEnd = false)).isNull()
    }

    @Test
    fun `to end of Quran advances below 114`() {
        assertThat(nextSurahForAdvance(113, toEndOfQuran = true, stopAtEnd = false)).isEqualTo(114)
    }

    @Test
    fun `continuous mode wraps 114 back to 1 by default`() {
        assertThat(nextSurahForAdvance(114, toEndOfQuran = false, stopAtEnd = false)).isEqualTo(1)
    }

    @Test
    fun `continuous mode stops at 114 when the stop-at-end option is enabled`() {
        assertThat(nextSurahForAdvance(114, toEndOfQuran = false, stopAtEnd = true)).isNull()
    }

    @Test
    fun `advances one surah at a time below 114`() {
        assertThat(nextSurahForAdvance(1, toEndOfQuran = false, stopAtEnd = false)).isEqualTo(2)
        assertThat(nextSurahForAdvance(99, toEndOfQuran = true, stopAtEnd = false)).isEqualTo(100)
    }
}
