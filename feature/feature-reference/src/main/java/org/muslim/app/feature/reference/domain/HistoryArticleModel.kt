package org.muslim.app.feature.reference.domain

/**
 * Rich content contracts for the history & civilization library.
 *
 * These models intentionally separate historical knowledge from the Compose UI so the
 * content can later be loaded from assets/Room without changing the reader components.
 */
enum class HistoryDatePrecision {
    Exact,
    Approximate,
    Range,
    Disputed,
}

data class HistoricalDate(
    val startCe: Int?,
    val endCe: Int? = null,
    val precision: HistoryDatePrecision = HistoryDatePrecision.Exact,
    val note: HistoryText? = null,
)

enum class HistorySourceKind {
    Museum,
    Unesco,
    Academic,
    Reference,
    Internal,
}

data class HistorySource(
    val id: String,
    val title: HistoryText,
    val kind: HistorySourceKind,
    val url: String? = null,
    val note: HistoryText? = null,
)

data class HistoryArticleSection(
    val id: String,
    val title: HistoryText,
    val paragraphs: List<HistoryText>,
)

data class HistoryArticle(
    val id: String,
    val eraId: String,
    val title: HistoryText,
    val lead: HistoryText,
    val sections: List<HistoryArticleSection>,
    val sourceIds: List<String>,
    val relatedEraIds: List<String> = emptyList(),
    val tags: Set<String> = emptySet(),
)

data class HistoricalEvent(
    val id: String,
    val date: HistoricalDate,
    val title: HistoryText,
    val summary: HistoryText,
    val eraIds: List<String>,
    val placeIds: List<String> = emptyList(),
    val personIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
)

data class HistoricalState(
    val id: String,
    val title: HistoryText,
    val period: HistoricalDate,
    val summary: HistoryText,
    val capitalPlaceIds: List<String> = emptyList(),
    val eraIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
)

data class CivilizationTopic(
    val id: String,
    val title: HistoryText,
    val summary: HistoryText,
    val articleIds: List<String> = emptyList(),
    val personIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
)
