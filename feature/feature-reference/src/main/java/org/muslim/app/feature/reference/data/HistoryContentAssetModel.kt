package org.muslim.app.feature.reference.data

import kotlinx.serialization.Serializable
import org.muslim.app.feature.reference.domain.CivilizationCategory
import org.muslim.app.feature.reference.domain.CivilizationTopic
import org.muslim.app.feature.reference.domain.HistoricalDate
import org.muslim.app.feature.reference.domain.HistoricalEvent
import org.muslim.app.feature.reference.domain.HistoricalEventCategory
import org.muslim.app.feature.reference.domain.HistoricalPlaceProfile
import org.muslim.app.feature.reference.domain.HistoricalState
import org.muslim.app.feature.reference.domain.HistoryArticle
import org.muslim.app.feature.reference.domain.HistoryArticleSection
import org.muslim.app.feature.reference.domain.HistoryDatePrecision
import org.muslim.app.feature.reference.domain.HistoryPersonProfile
import org.muslim.app.feature.reference.domain.HistoryRegion
import org.muslim.app.feature.reference.domain.HistoryText

@Serializable
internal data class BilingualContentDto(
    val arabic: String,
    val english: String,
) {
    fun toDomain(): HistoryText = HistoryText(arabic, english)
}

@Serializable
internal data class HistorySectionDto(
    val id: String,
    val title: BilingualContentDto,
    val paragraphs: List<BilingualContentDto>,
) {
    fun toDomain(): HistoryArticleSection =
        HistoryArticleSection(
            id = id,
            title = title.toDomain(),
            paragraphs = paragraphs.map(BilingualContentDto::toDomain),
        )
}

@Serializable
internal data class HistoricalDateDto(
    val startCe: Int? = null,
    val endCe: Int? = null,
    val precision: String = HistoryDatePrecision.Exact.name,
    val note: BilingualContentDto? = null,
) {
    fun toDomain(): HistoricalDate =
        HistoricalDate(
            startCe = startCe,
            endCe = endCe,
            precision = enumValueOrDefault(precision, HistoryDatePrecision.Exact),
            note = note?.toDomain(),
        )
}

@Serializable
internal data class HistoryArticleDto(
    val id: String,
    val eraId: String,
    val title: BilingualContentDto,
    val lead: BilingualContentDto,
    val sections: List<HistorySectionDto>,
    val sourceIds: List<String>,
    val relatedEraIds: List<String> = emptyList(),
    val tags: Set<String> = emptySet(),
) {
    fun toDomain(): HistoryArticle =
        HistoryArticle(
            id = id,
            eraId = eraId,
            title = title.toDomain(),
            lead = lead.toDomain(),
            sections = sections.map(HistorySectionDto::toDomain),
            sourceIds = sourceIds,
            relatedEraIds = relatedEraIds,
            tags = tags,
        )
}

@Serializable
internal data class HistoricalStateDto(
    val id: String,
    val title: BilingualContentDto,
    val period: HistoricalDateDto,
    val summary: BilingualContentDto,
    val region: String,
    val capitalPlaceIds: List<String> = emptyList(),
    val eraIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
) {
    fun toDomain(): HistoricalState =
        HistoricalState(
            id = id,
            title = title.toDomain(),
            period = period.toDomain(),
            summary = summary.toDomain(),
            region = enumValueOrDefault(region, HistoryRegion.MultiRegional),
            capitalPlaceIds = capitalPlaceIds,
            eraIds = eraIds,
            sourceIds = sourceIds,
        )
}

@Serializable
internal data class HistoricalEventDto(
    val id: String,
    val date: HistoricalDateDto,
    val category: String,
    val title: BilingualContentDto,
    val summary: BilingualContentDto,
    val context: BilingualContentDto,
    val significance: BilingualContentDto,
    val eraIds: List<String>,
    val stateIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val personIds: List<String> = emptyList(),
    val relatedTopicIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
) {
    fun toDomain(): HistoricalEvent =
        HistoricalEvent(
            id = id,
            date = date.toDomain(),
            category = enumValueOrDefault(
                category,
                HistoricalEventCategory.ReligiousAndCommunity,
            ),
            title = title.toDomain(),
            summary = summary.toDomain(),
            context = context.toDomain(),
            significance = significance.toDomain(),
            eraIds = eraIds,
            stateIds = stateIds,
            placeIds = placeIds,
            personIds = personIds,
            relatedTopicIds = relatedTopicIds,
            sourceIds = sourceIds,
        )
}

@Serializable
internal data class CivilizationTopicDto(
    val id: String,
    val category: String,
    val title: BilingualContentDto,
    val summary: BilingualContentDto,
    val sections: List<HistorySectionDto>,
    val personIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
    val relatedTopicIds: List<String> = emptyList(),
) {
    fun toDomain(): CivilizationTopic =
        CivilizationTopic(
            id = id,
            category = enumValueOrDefault(
                category,
                CivilizationCategory.KnowledgeAndSciences,
            ),
            title = title.toDomain(),
            summary = summary.toDomain(),
            sections = sections.map(HistorySectionDto::toDomain),
            personIds = personIds,
            placeIds = placeIds,
            sourceIds = sourceIds,
            relatedTopicIds = relatedTopicIds,
        )
}

@Serializable
internal data class HistoryPersonProfileDto(
    val personId: String,
    val overview: BilingualContentDto,
    val sections: List<HistorySectionDto>,
    val eraIds: List<String> = emptyList(),
    val stateIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val relatedTopicIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
) {
    fun toDomain(): HistoryPersonProfile =
        HistoryPersonProfile(
            personId = personId,
            overview = overview.toDomain(),
            sections = sections.map(HistorySectionDto::toDomain),
            eraIds = eraIds,
            stateIds = stateIds,
            placeIds = placeIds,
            eventIds = eventIds,
            relatedTopicIds = relatedTopicIds,
            sourceIds = sourceIds,
        )
}

@Serializable
internal data class HistoricalPlaceProfileDto(
    val placeId: String,
    val overview: BilingualContentDto,
    val sections: List<HistorySectionDto>,
    val eraIds: List<String> = emptyList(),
    val stateIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val relatedTopicIds: List<String> = emptyList(),
    val relatedPersonIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
) {
    fun toDomain(): HistoricalPlaceProfile =
        HistoricalPlaceProfile(
            placeId = placeId,
            overview = overview.toDomain(),
            sections = sections.map(HistorySectionDto::toDomain),
            eraIds = eraIds,
            stateIds = stateIds,
            eventIds = eventIds,
            relatedTopicIds = relatedTopicIds,
            relatedPersonIds = relatedPersonIds,
            sourceIds = sourceIds,
        )
}

@Serializable
internal data class HistoryContentAsset(
    val schemaVersion: Int,
    val contentVersion: Int,
    val articles: List<HistoryArticleDto>,
    val states: List<HistoricalStateDto>,
    val events: List<HistoricalEventDto>,
    val civilizationTopics: List<CivilizationTopicDto>,
    val peopleProfiles: List<HistoryPersonProfileDto>,
    val placeProfiles: List<HistoricalPlaceProfileDto>,
) {
    val recordCount: Int
        get() =
            articles.size +
                states.size +
                events.size +
                civilizationTopics.size +
                peopleProfiles.size +
                placeProfiles.size

    fun validated(): HistoryContentAsset {
        require(schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported history content schema: $schemaVersion"
        }
        require(contentVersion > 0) { "History content version must be positive" }
        require(recordCount > 0) { "History content asset must not be empty" }

        val keys = buildList {
            addAll(articles.map { "Article:${it.id}" })
            addAll(states.map { "State:${it.id}" })
            addAll(events.map { "Event:${it.id}" })
            addAll(civilizationTopics.map { "CivilizationTopic:${it.id}" })
            addAll(peopleProfiles.map { "PersonProfile:${it.personId}" })
            addAll(placeProfiles.map { "PlaceProfile:${it.placeId}" })
        }
        require(keys.size == keys.toSet().size) {
            "History content asset contains duplicate entity keys"
        }
        require(articles.all { it.sections.isNotEmpty() && it.sourceIds.isNotEmpty() })
        require(events.all { it.eraIds.isNotEmpty() && it.sourceIds.isNotEmpty() })
        require(civilizationTopics.all { it.sections.isNotEmpty() && it.sourceIds.isNotEmpty() })
        require(peopleProfiles.all { it.sections.isNotEmpty() && it.sourceIds.isNotEmpty() })
        require(placeProfiles.all { it.sections.isNotEmpty() && it.sourceIds.isNotEmpty() })
        return this
    }

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    value: String,
    fallback: T,
): T = enumValues<T>().firstOrNull { it.name == value } ?: fallback
