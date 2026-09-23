package org.muslim.app.feature.reference.ui

import org.muslim.app.feature.reference.domain.HistorySearchResult
import org.muslim.app.feature.reference.domain.HistorySearchType

internal enum class HistoryTargetType {
    Era,
    State,
    Event,
    Person,
    Place,
    CivilizationTopic,
}

internal data class HistoryNavigationTarget(
    val type: HistoryTargetType,
    val id: String,
)

internal fun HistorySearchResult.toNavigationTarget(): HistoryNavigationTarget =
    HistoryNavigationTarget(
        type = when (type) {
            HistorySearchType.Era -> HistoryTargetType.Era
            HistorySearchType.State -> HistoryTargetType.State
            HistorySearchType.Event -> HistoryTargetType.Event
            HistorySearchType.Person -> HistoryTargetType.Person
            HistorySearchType.Place -> HistoryTargetType.Place
            HistorySearchType.CivilizationTopic -> HistoryTargetType.CivilizationTopic
        },
        id = id,
    )

internal fun HistoryTargetType.tabIndex(): Int = when (this) {
    HistoryTargetType.Era -> 0
    HistoryTargetType.State -> 1
    HistoryTargetType.CivilizationTopic -> 2
    HistoryTargetType.Event -> 3
    HistoryTargetType.Place -> 4
    HistoryTargetType.Person -> 5
}
