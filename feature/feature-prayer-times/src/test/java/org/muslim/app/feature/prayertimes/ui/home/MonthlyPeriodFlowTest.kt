package org.muslim.app.feature.prayertimes.ui.home

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MonthlyPeriodFlowTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `rebuilds month data only when settings or visible month changes`() = runTest {
        val settings = MutableStateFlow(1)
        val date = MutableStateFlow(LocalDate.of(2026, 1, 31))
        val computed = mutableListOf<Pair<Int, YearMonth>>()
        val job = backgroundScope.launch {
            monthlyPeriodFlow(settings, date) { setting, month ->
                computed += setting to month
                month
            }.collect { }
        }
        runCurrent()

        date.value = LocalDate.of(2026, 1, 15)
        runCurrent()
        assertThat(computed).containsExactly(1 to YearMonth.of(2026, 1))

        date.value = LocalDate.of(2026, 2, 1)
        runCurrent()
        settings.value = 2
        runCurrent()

        assertThat(computed).containsExactly(
            1 to YearMonth.of(2026, 1),
            1 to YearMonth.of(2026, 2),
            2 to YearMonth.of(2026, 2),
        ).inOrder()
        job.cancel()
    }
}
