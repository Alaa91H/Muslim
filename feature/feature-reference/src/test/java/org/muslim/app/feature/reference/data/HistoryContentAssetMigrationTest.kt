package org.muslim.app.feature.reference.data

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test
import org.muslim.app.feature.reference.domain.IslamicCivilizationContent
import org.muslim.app.feature.reference.domain.IslamicHistoricalEvents
import org.muslim.app.feature.reference.domain.IslamicHistoryArticles
import org.muslim.app.feature.reference.domain.IslamicHistoryPeopleProfiles
import org.muslim.app.feature.reference.domain.IslamicHistoryPlaceProfiles
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

class HistoryContentAssetMigrationTest {
    @Test
    fun `packaged long form content matches current canonical catalogue`() {
        val asset = loadAsset()

        assertThat(asset.recordCount).isEqualTo(79)
        assertThat(asset.articles.map(HistoryArticleDto::toDomain))
            .containsExactlyElementsIn(IslamicHistoryArticles.articles)
            .inOrder()
        assertThat(asset.states.map(HistoricalStateDto::toDomain))
            .containsExactlyElementsIn(IslamicHistoryStates.states)
            .inOrder()
        assertThat(asset.events.map(HistoricalEventDto::toDomain))
            .containsExactlyElementsIn(IslamicHistoricalEvents.events)
            .inOrder()
        assertThat(asset.civilizationTopics.map(CivilizationTopicDto::toDomain))
            .containsExactlyElementsIn(IslamicCivilizationContent.topics)
            .inOrder()
        assertThat(asset.peopleProfiles.map(HistoryPersonProfileDto::toDomain))
            .containsExactlyElementsIn(IslamicHistoryPeopleProfiles.people)
            .inOrder()
        assertThat(asset.placeProfiles.map(HistoricalPlaceProfileDto::toDomain))
            .containsExactlyElementsIn(IslamicHistoryPlaceProfiles.places)
            .inOrder()
    }

    @Test
    fun `packaged content has unique storage keys`() {
        val asset = loadAsset()
        val keys = buildList {
            addAll(asset.articles.map { "Article:${it.eraId}" })
            addAll(asset.states.map { "State:${it.id}" })
            addAll(asset.events.map { "Event:${it.id}" })
            addAll(asset.civilizationTopics.map { "CivilizationTopic:${it.id}" })
            addAll(asset.peopleProfiles.map { "PersonProfile:${it.personId}" })
            addAll(asset.placeProfiles.map { "PlaceProfile:${it.placeId}" })
        }

        assertThat(keys).containsNoDuplicates()
        assertThat(keys).hasSize(asset.recordCount)
    }

    private fun loadAsset(): HistoryContentAsset {
        val file = listOf(
            File("src/main/assets/history/content_v1.json"),
            File("feature/feature-reference/src/main/assets/history/content_v1.json"),
        ).firstOrNull { it.exists() }
        requireNotNull(file) { "Packaged history content asset was not found" }

        return HistoryContentJson.decodeFromString(
            HistoryContentAsset.serializer(),
            file.readText(),
        ).validated()
    }
}
