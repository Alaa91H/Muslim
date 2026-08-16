package org.example.islamicapp.feature.learn.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Content-integrity tests for the learning guides (Phase 5). */
class LearnRepositoryTest {

    private val repository = LearnRepository(
        islamIntroRepository = IslamIntroRepository(),
        sirahRepository = SirahRepository(),
        prophetsStoriesRepository = ProphetsStoriesRepository(),
        namesOfAllahRepository = NamesOfAllahRepository(),
    )

    @Test
    fun `covers the core worship guides`() {
        val ids = repository.topics.map { it.id }
        assertThat(ids).containsAtLeast(
            "islam_intro", "sirah", "prophets_stories", "names_of_allah",
            "wudu", "ghusl", "tayammum", "salah", "special_prayers",
        )
    }

    @Test
    fun `the ninety-nine names are complete and unique`() {
        val names = repository.topics.first { it.id == "names_of_allah" }
        assertThat(names.steps).hasSize(99)
        val arTitles = names.steps.map { it.titleAr }
        assertThat(arTitles.distinct()).hasSize(99)
    }

    @Test
    fun `reference content is substantial in both languages`() {
        val intro = repository.topics.first { it.id == "islam_intro" }
        val sirah = repository.topics.first { it.id == "sirah" }
        val prophets = repository.topics.first { it.id == "prophets_stories" }
        assertThat(intro.steps.size).isAtLeast(12)
        assertThat(sirah.steps.size).isAtLeast(20)
        assertThat(prophets.steps.size).isEqualTo(25)
        (intro.steps + sirah.steps + prophets.steps).forEach { step ->
            assertThat(step.detailAr.length).isAtLeast(80)
            assertThat(step.detailEn.length).isAtLeast(40)
        }
    }

    @Test
    fun `every topic has ordered steps in both languages`() {
        repository.topics.forEach { topic ->
            assertThat(topic.steps).isNotEmpty()
            topic.steps.forEach { step ->
                assertThat(step.titleAr).isNotEmpty()
                assertThat(step.titleEn).isNotEmpty()
                assertThat(step.detailAr).isNotEmpty()
                assertThat(step.detailEn).isNotEmpty()
            }
        }
    }

    @Test
    fun `madhhab notes only cite the four sunni schools`() {
        val allowed = listOf(
            "الحنفية", "الشافعية", "المالكية", "الحنابلة", "الجمهور", "المذاهب الأربعة",
        )
        val allowedEn = listOf(
            "Hanafi", "Shafi'i", "Maliki", "Hanbali", "majority", "four schools",
        )
        repository.topics.flatMap { it.differences }.forEach { note ->
            val citesSchool = allowed.any { note.pointAr.contains(it) } ||
                allowedEn.any { note.pointEn.contains(it, ignoreCase = true) }
            assertThat(citesSchool).isTrue()
        }
    }
}
