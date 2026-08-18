package org.muslim.app.feature.tasbih.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class TasbihCounterTest {

    private val today = LocalDate.of(2026, 8, 16)
    private val yesterday = today.minusDays(1)

    @Test
    fun `effectiveCounts are the stored counts on the same day`() {
        val counts = mapOf(TasbihPhrase.SubhanAllah to 12)
        assertThat(TasbihCounter.effectiveCounts(counts, today, today)).isEqualTo(counts)
    }

    @Test
    fun `effectiveCounts are empty after a day change`() {
        val counts = mapOf(TasbihPhrase.SubhanAllah to 12)
        assertThat(TasbihCounter.effectiveCounts(counts, yesterday, today)).isEmpty()
    }

    @Test
    fun `increment same day only increments the tapped phrase`() {
        val result = TasbihCounter.increment(
            storedCounts = mapOf(TasbihPhrase.SubhanAllah to 5),
            storedDate = today,
            today = today,
            history = emptyList(),
            phrase = TasbihPhrase.SubhanAllah,
        )
        assertThat(result.counts[TasbihPhrase.SubhanAllah]).isEqualTo(6)
        assertThat(result.date).isEqualTo(today)
        assertThat(result.history).isEmpty()
    }

    @Test
    fun `different phrases keep independent counters`() {
        val first = TasbihCounter.increment(
            mapOf(), today, today, emptyList(), TasbihPhrase.SubhanAllah,
        )
        val second = TasbihCounter.increment(
            first.counts, today, today, emptyList(), TasbihPhrase.Alhamdulillah,
        )
        assertThat(second.counts[TasbihPhrase.SubhanAllah]).isEqualTo(1)
        assertThat(second.counts[TasbihPhrase.Alhamdulillah]).isEqualTo(1)
    }

    @Test
    fun `increment after day change rolls the previous total into history`() {
        val stored = mapOf(TasbihPhrase.SubhanAllah to 20, TasbihPhrase.Alhamdulillah to 10)
        val result = TasbihCounter.increment(stored, yesterday, today, emptyList(), TasbihPhrase.AllahuAkbar)
        assertThat(result.counts[TasbihPhrase.AllahuAkbar]).isEqualTo(1)
        assertThat(result.date).isEqualTo(today)
        assertThat(result.history).containsExactly(DailyCount(yesterday, 30))
    }

    @Test
    fun `increment after day change with zero total keeps history untouched`() {
        val history = listOf(DailyCount(yesterday.minusDays(1), 50))
        val result = TasbihCounter.increment(emptyMap(), yesterday, today, history, TasbihPhrase.SubhanAllah)
        assertThat(result.counts[TasbihPhrase.SubhanAllah]).isEqualTo(1)
        assertThat(result.history).containsExactlyElementsIn(history)
    }

    @Test
    fun `decrement removes one tap and floors at zero`() {
        assertThat(TasbihCounter.decrement(mapOf(TasbihPhrase.SubhanAllah to 3), TasbihPhrase.SubhanAllah))
            .containsExactly(TasbihPhrase.SubhanAllah, 2)
        assertThat(TasbihCounter.decrement(emptyMap(), TasbihPhrase.SubhanAllah))
            .containsExactly(TasbihPhrase.SubhanAllah, 0)
    }

    @Test
    fun `resetPhrase clears only the active phrase`() {
        val counts = mapOf(TasbihPhrase.SubhanAllah to 5, TasbihPhrase.Alhamdulillah to 7)
        val result = TasbihCounter.resetPhrase(counts, TasbihPhrase.SubhanAllah)
        assertThat(result).doesNotContainKey(TasbihPhrase.SubhanAllah)
        assertThat(result[TasbihPhrase.Alhamdulillah]).isEqualTo(7)
    }

    @Test
    fun `history is trimmed to the configured limit`() {
        val history = (1..29).map { DailyCount(today.minusDays(it.toLong() + 1), it * 10) }
        val result = TasbihCounter.increment(
            mapOf(TasbihPhrase.SubhanAllah to 99), yesterday, today, history, TasbihPhrase.SubhanAllah,
            historyLimit = 30,
        )
        assertThat(result.history).hasSize(30)
        assertThat(result.history.first()).isEqualTo(DailyCount(yesterday, 99))
    }

    @Test
    fun `targetReached and rounds reflect the goal`() {
        assertThat(TasbihState(mapOf(TasbihPhrase.SubhanAllah to 33), 33, TasbihPhrase.SubhanAllah, emptyList()).targetReached).isTrue()
        assertThat(TasbihState(mapOf(TasbihPhrase.SubhanAllah to 32), 33, TasbihPhrase.SubhanAllah, emptyList()).targetReached).isFalse()
        assertThat(TasbihState(mapOf(TasbihPhrase.SubhanAllah to 66), 33, TasbihPhrase.SubhanAllah, emptyList()).rounds).isEqualTo(2)
    }

    @Test
    fun `totalToday sums every phrase counter`() {
        val state = TasbihState(
            mapOf(TasbihPhrase.SubhanAllah to 33, TasbihPhrase.Alhamdulillah to 20),
            33,
            TasbihPhrase.SubhanAllah,
            emptyList(),
        )
        assertThat(state.totalToday).isEqualTo(53)
        assertThat(state.count).isEqualTo(33)
    }

    @Test
    fun `all twenty-four dhikr phrases are complete and unique`() {
        val phrases = TasbihPhrase.entries
        assertThat(phrases).hasSize(24)
        assertThat(phrases.map { it.text }.distinct()).hasSize(24)
        assertThat(phrases.map { it.transliteration }.distinct()).hasSize(24)
        phrases.forEach {
            assertThat(it.text).isNotEmpty()
            assertThat(it.transliteration).isNotEmpty()
            assertThat(it.virtue).isNotEmpty()
            assertThat(it.category).isNotNull()
        }
        TasbihCategory.entries.forEach { category ->
            assertThat(phrases.any { it.category == category }).isTrue()
        }
    }

    @Test
    fun `completesRound fires exactly on target multiples`() {
        // target 33: milestones at 33, 66, 99
        assertThat(TasbihCounter.completesRound(33, 33)).isTrue()
        assertThat(TasbihCounter.completesRound(66, 33)).isTrue()
        assertThat(TasbihCounter.completesRound(99, 33)).isTrue()
        assertThat(TasbihCounter.completesRound(32, 33)).isFalse()
        assertThat(TasbihCounter.completesRound(34, 33)).isFalse()
        assertThat(TasbihCounter.completesRound(100, 33)).isFalse()
    }

    @Test
    fun `completesRound supports 99 and 100 targets`() {
        assertThat(TasbihCounter.completesRound(99, 99)).isTrue()
        assertThat(TasbihCounter.completesRound(198, 99)).isTrue()
        assertThat(TasbihCounter.completesRound(100, 100)).isTrue()
        assertThat(TasbihCounter.completesRound(200, 100)).isTrue()
        assertThat(TasbihCounter.completesRound(99, 100)).isFalse()
        assertThat(TasbihCounter.completesRound(101, 100)).isFalse()
    }

    @Test
    fun `completesRound is false for zero, negative or absent target`() {
        assertThat(TasbihCounter.completesRound(33, 0)).isFalse()
        assertThat(TasbihCounter.completesRound(0, 33)).isFalse()
        assertThat(TasbihCounter.completesRound(33, -33)).isFalse()
    }

    @Test
    fun `roundNumberAt counts completed full rounds`() {
        assertThat(TasbihCounter.roundNumberAt(33, 33)).isEqualTo(1)
        assertThat(TasbihCounter.roundNumberAt(66, 33)).isEqualTo(2)
        assertThat(TasbihCounter.roundNumberAt(99, 99)).isEqualTo(1)
        assertThat(TasbihCounter.roundNumberAt(100, 100)).isEqualTo(1)
        assertThat(TasbihCounter.roundNumberAt(34, 33)).isEqualTo(1)
        assertThat(TasbihCounter.roundNumberAt(100, 0)).isEqualTo(0)
    }

    @Test
    fun `target sound is off with the notification tone by default`() {
        val settings = TargetSoundSettings()
        assertThat(settings.enabled).isFalse()
        assertThat(settings.tone).isEqualTo(TargetSoundSettings.TONE_NOTIFICATION)
    }

    @Test
    fun `target sound tone ids are distinct`() {
        assertThat(
            setOf(
                TargetSoundSettings.TONE_NOTIFICATION,
                TargetSoundSettings.TONE_RINGTONE,
                TargetSoundSettings.TONE_ALARM,
            ),
        ).hasSize(3)
    }

    @Test
    fun `dhikr phrases use only western digits`() {
        val arabicIndic = "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669"
        TasbihPhrase.entries.forEach {
            assertThat(it.text).doesNotContain(arabicIndic)
            assertThat(it.virtue).doesNotContain(arabicIndic)
            assertThat(it.transliteration).doesNotContain(arabicIndic)
        }
    }
}
