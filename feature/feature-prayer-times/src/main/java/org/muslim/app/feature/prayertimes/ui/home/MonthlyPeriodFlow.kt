package org.muslim.app.feature.prayertimes.ui.home

import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Rebuilds month-scoped data only when its settings or visible calendar month change. */
internal fun <Settings, Value> monthlyPeriodFlow(
    settings: Flow<Settings>,
    selectedDate: Flow<LocalDate>,
    build: (Settings, YearMonth) -> Value,
): Flow<Value> = combine(
    settings,
    selectedDate.map(YearMonth::from).distinctUntilChanged(),
) { currentSettings, month ->
    build(currentSettings, month)
}
