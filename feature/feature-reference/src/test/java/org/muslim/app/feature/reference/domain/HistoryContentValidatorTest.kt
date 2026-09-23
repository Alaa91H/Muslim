package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HistoryContentValidatorTest {
    @Test
    fun `current long form catalogue is structurally valid`() {
        assertThat(HistoryContentValidator.validate()).isEmpty()
    }

    @Test
    fun `every timeline era has a long form article`() {
        val articleEraIds = IslamicHistoryArticles.articles.map { it.eraId }

        assertThat(articleEraIds)
            .containsExactlyElementsIn(IslamicHistoryContent.timeline.map { it.id })
        assertThat(articleEraIds).containsNoDuplicates()
    }

    @Test
    fun `long form articles contain bilingual sections and sources`() {
        IslamicHistoryArticles.articles.forEach { article ->
            assertThat(article.sections.size).isAtLeast(3)
            assertThat(article.sourceIds).isNotEmpty()
            article.sections.forEach { section ->
                assertThat(section.paragraphs).isNotEmpty()
                section.paragraphs.forEach { paragraph ->
                    assertThat(paragraph.arabic).isNotEmpty()
                    assertThat(paragraph.english).isNotEmpty()
                }
            }
        }
    }

    @Test
    fun `validator rejects an unknown source reference`() {
        val brokenArticle = IslamicHistoryArticles.articles.first().copy(
            sourceIds = listOf("missing-source"),
        )

        val errors = HistoryContentValidator.validate(
            articles = listOf(brokenArticle),
        )

        assertThat(errors.joinToString("\n")).contains("unknown source")
    }

    @Test
    fun `states catalogue is chronological bilingual and source linked`() {
        val states = IslamicHistoryStates.states

        assertThat(states).hasSize(18)
        assertThat(states.map { it.id }).containsNoDuplicates()
        assertThat(states.mapNotNull { it.period.startCe }).isInOrder()
        states.forEach { state ->
            assertThat(state.title.arabic).isNotEmpty()
            assertThat(state.title.english).isNotEmpty()
            assertThat(state.summary.arabic).isNotEmpty()
            assertThat(state.summary.english).isNotEmpty()
            assertThat(state.sourceIds).isNotEmpty()
        }
    }

    @Test
    fun `states preserve overlapping regional histories`() {
        val year1250 = IslamicHistoryStates.states.filter { state ->
            val start = state.period.startCe ?: return@filter false
            val end = state.period.endCe ?: return@filter false
            1250 in start..end
        }

        assertThat(year1250.map { it.id }).containsAtLeast(
            "abbasid_caliphate",
            "seljuqs_rum",
            "almohads",
            "ayyubids",
            "nasrids",
            "mamluks",
        )
    }

    @Test
    fun `shared history source registry has unique ids`() {
        assertThat(IslamicHistorySources.all.map { it.id }).containsNoDuplicates()
        assertThat(IslamicHistorySources.byId("met_major_dynasties")).isNotNull()
    }


    @Test
    fun `civilization catalogue covers all four thematic categories`() {
        val topics = IslamicCivilizationContent.topics

        assertThat(topics).hasSize(12)
        assertThat(topics.map { it.id }).containsNoDuplicates()
        assertThat(topics.map { it.category }.toSet())
            .containsExactlyElementsIn(CivilizationCategory.entries)
        topics.forEach { topic ->
            assertThat(topic.sections).isNotEmpty()
            assertThat(topic.sourceIds).isNotEmpty()
            assertThat(topic.title.arabic).isNotEmpty()
            assertThat(topic.title.english).isNotEmpty()
        }
    }

    @Test
    fun `civilization category filtering keeps unrelated topics out`() {
        val science = IslamicCivilizationContent.byCategory(
            CivilizationCategory.KnowledgeAndSciences,
        )

        assertThat(science.map { it.id }).containsAtLeast(
            "translation_books",
            "mathematics",
            "astronomy",
            "medicine_bimaristans",
            "optics",
        )
        assertThat(science.map { it.id }).doesNotContain("architecture")
    }


    @Test
    fun `historical events are chronological bilingual and fully categorized`() {
        val events = IslamicHistoricalEvents.events

        assertThat(events).hasSize(22)
        assertThat(events.map { it.id }).containsNoDuplicates()
        assertThat(events.mapNotNull { it.date.startCe }).isInOrder()
        assertThat(events.map { it.category }.toSet())
            .containsExactlyElementsIn(HistoricalEventCategory.entries)
        events.forEach { event ->
            assertThat(event.title.arabic).isNotEmpty()
            assertThat(event.title.english).isNotEmpty()
            assertThat(event.summary.arabic).isNotEmpty()
            assertThat(event.summary.english).isNotEmpty()
            assertThat(event.context.arabic).isNotEmpty()
            assertThat(event.context.english).isNotEmpty()
            assertThat(event.significance.arabic).isNotEmpty()
            assertThat(event.significance.english).isNotEmpty()
            assertThat(event.sourceIds).isNotEmpty()
        }
    }

    @Test
    fun `events can be filtered by era and category without losing chronology`() {
        val abbasid = IslamicHistoricalEvents.byEra("abbasid")
        val foundations = IslamicHistoricalEvents.byCategory(
            HistoricalEventCategory.FoundationAndUrbanism,
        )

        assertThat(abbasid.map { it.id }).containsAtLeast(
            "abbasid_revolution_750",
            "baghdad_founded_762",
            "abbasid_translation_scholarship",
            "baghdad_1258",
        )
        assertThat(abbasid.mapNotNull { it.date.startCe }).isInOrder()
        assertThat(foundations.map { it.id }).containsAtLeast(
            "baghdad_founded_762",
            "cairo_founded_969",
        )
    }

    @Test
    fun `event catalogue links into states people places and civilization topics`() {
        val linked = IslamicHistoricalEvents.events.filter { event ->
            event.stateIds.isNotEmpty() ||
                event.personIds.isNotEmpty() ||
                event.placeIds.isNotEmpty() ||
                event.relatedTopicIds.isNotEmpty()
        }

        assertThat(linked.size).isAtLeast(18)
    }


    @Test
    fun `every exposed person and atlas place has a full profile`() {
        val personIds = IslamicHistoryContent.personalities.map { it.id }.toSet()
        val profiledPeople = IslamicHistoryProfiles.people.map { it.personId }.toSet()
        val placeIds = IslamicHistoryContent.atlasLayers
            .flatMap { it.places }
            .map { it.id }
            .toSet()
        val profiledPlaces = IslamicHistoryProfiles.places.map { it.placeId }.toSet()

        assertThat(profiledPeople).containsExactlyElementsIn(personIds)
        assertThat(profiledPlaces).containsExactlyElementsIn(placeIds)
        assertThat(IslamicHistoryProfiles.people).hasSize(8)
        assertThat(IslamicHistoryProfiles.places).hasSize(13)
    }

    @Test
    fun `people and place profiles contain bilingual long form sections and sources`() {
        IslamicHistoryProfiles.people.forEach { profile ->
            assertThat(profile.sections.size).isAtLeast(2)
            assertThat(profile.sourceIds).isNotEmpty()
            assertThat(profile.overview.arabic).isNotEmpty()
            assertThat(profile.overview.english).isNotEmpty()
        }
        IslamicHistoryProfiles.places.forEach { profile ->
            assertThat(profile.sections.size).isAtLeast(2)
            assertThat(profile.sourceIds).isNotEmpty()
            assertThat(profile.overview.arabic).isNotEmpty()
            assertThat(profile.overview.english).isNotEmpty()
        }
    }

    @Test
    fun `profiles cross link into events states topics and atlas entities`() {
        val linkedPeople = IslamicHistoryProfiles.people.count { profile ->
            profile.eventIds.isNotEmpty() ||
                profile.stateIds.isNotEmpty() ||
                profile.placeIds.isNotEmpty() ||
                profile.relatedTopicIds.isNotEmpty()
        }
        val linkedPlaces = IslamicHistoryProfiles.places.count { profile ->
            profile.eventIds.isNotEmpty() ||
                profile.stateIds.isNotEmpty() ||
                profile.relatedTopicIds.isNotEmpty() ||
                profile.relatedPersonIds.isNotEmpty()
        }

        assertThat(linkedPeople).isAtLeast(7)
        assertThat(linkedPlaces).isAtLeast(10)
    }


    @Test
    fun `atlas layers expose valid time ranges and unique places`() {
        val layers = IslamicHistoryContent.atlasLayers
        val places = layers.flatMap { it.places }

        assertThat(layers).isNotEmpty()
        layers.forEach { layer ->
            assertThat(layer.startCe).isAtLeast(610)
            layer.endCe?.let { end ->
                assertThat(end).isAtLeast(layer.startCe)
            }
        }
        assertThat(places.map { it.id }).containsNoDuplicates()
    }

}
