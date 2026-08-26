package org.muslim.app.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.prayer.Prayer

class AdhanSchedulerTest {

    @Test
    fun `selectEarliestUpcoming retains todays future prayer instead of replacing it with tomorrow`() {
        val now = 1_000L
        val result = AdhanScheduler.selectEarliestUpcoming(
            candidates = listOf(
                Prayer.Fajr to 1_200L,
                Prayer.Dhuhr to 1_800L,
                Prayer.Sunrise to 1_300L,
                Prayer.Fajr to 86_401_200L,
                Prayer.Dhuhr to 86_401_800L,
                Prayer.Asr to 86_402_100L,
            ),
            now = now,
        )

        assertThat(result).containsExactly(
            Prayer.Fajr, 1_200L,
            Prayer.Dhuhr, 1_800L,
            Prayer.Asr, 86_402_100L,
        ).inOrder()
    }

    @Test
    fun `selectEarliestUpcoming skips past and sunrise entries while retaining tomorrows missing prayer`() {
        val result = AdhanScheduler.selectEarliestUpcoming(
            candidates = listOf(
                Prayer.Fajr to 900L,
                Prayer.Sunrise to 1_100L,
                Prayer.Maghrib to 1_400L,
                Prayer.Fajr to 86_401_000L,
            ),
            now = 1_000L,
        )

        assertThat(result).containsExactly(
            Prayer.Maghrib, 1_400L,
            Prayer.Fajr, 86_401_000L,
        ).inOrder()
    }
}
