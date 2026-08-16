package org.example.islamicapp.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.feature.prayertimes.domain.Prayer
import org.junit.Test

class AdhanScheduleSelectionTest {

    private val now = 1_000L

    @Test
    fun `selectNextOccurrences keeps today's future time when tomorrow also contains the prayer`() {
        val today = mapOf(
            Prayer.Fajr to 500L,
            Prayer.Dhuhr to 1_200L,
            Prayer.Asr to 1_600L,
            Prayer.Maghrib to 1_900L,
        )
        val tomorrow = mapOf(
            Prayer.Fajr to 10_500L,
            Prayer.Dhuhr to 11_200L,
            Prayer.Asr to 11_600L,
            Prayer.Maghrib to 11_900L,
        )

        val result = selectNextOccurrences(listOf(today, tomorrow), now)

        assertThat(result[Prayer.Dhuhr]).isEqualTo(1_200L)
        assertThat(result[Prayer.Asr]).isEqualTo(1_600L)
        assertThat(result[Prayer.Maghrib]).isEqualTo(1_900L)
    }

    @Test
    fun `selectNextOccurrences uses tomorrow only for a prayer already passed today`() {
        val today = mapOf(
            Prayer.Fajr to 500L,
            Prayer.Dhuhr to 1_200L,
        )
        val tomorrow = mapOf(
            Prayer.Fajr to 10_500L,
            Prayer.Dhuhr to 11_200L,
        )

        val result = selectNextOccurrences(listOf(today, tomorrow), now)

        assertThat(result[Prayer.Fajr]).isEqualTo(10_500L)
        assertThat(result[Prayer.Dhuhr]).isEqualTo(1_200L)
    }

    @Test
    fun `selectNextOccurrences excludes sunrise and past occurrences`() {
        val result = selectNextOccurrences(
            dailyOccurrences = listOf(
                mapOf(
                    Prayer.Sunrise to 1_100L,
                    Prayer.Fajr to 900L,
                    Prayer.Isha to 2_000L,
                ),
            ),
            nowMillis = now,
        )

        assertThat(result).containsExactly(Prayer.Isha, 2_000L)
    }
}
