package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HajjUmrahContentTest {

    @Test
    fun `Hajj and Umrah reference covers all core and practical categories`() {
        val categoryIds = HajjUmrahContent.CATEGORIES.map { it.id }

        assertThat(categoryIds).containsAtLeast(
            "hajj_intro",
            "hajj_types",
            "hajj_miqat",
            "hajj_rites",
            "umrah_rites",
            "ihram_prohibitions",
            "hajj_practical",
            "duas",
        )
    }

    @Test
    fun `Hajj topics remain unique and substantial`() {
        val topics = HajjUmrahContent.CATEGORIES.flatMap { it.topics }
        val ids = topics.map { it.id }

        assertThat(ids.toSet()).hasSize(ids.size)
        assertThat(topics.size).isAtLeast(29)
        topics.forEach { topic ->
            assertThat(topic.title).isNotEmpty()
            assertThat(topic.titleEn).isNotEmpty()
            assertThat(topic.summary).isNotEmpty()
            assertThat(topic.summaryEn).isNotEmpty()
            assertThat(topic.steps).isNotEmpty()
        }
    }

    @Test
    fun `every Hajj step is bilingual and paired evidence stays bilingual`() {
        HajjUmrahContent.CATEGORIES
            .flatMap { it.topics }
            .flatMap { it.steps }
            .forEach { step ->
                assertThat(step.title).isNotEmpty()
                assertThat(step.titleEn).isNotEmpty()
                assertThat(step.what).isNotEmpty()
                assertThat(step.whatEn).isNotEmpty()

                step.evidenceEn?.let { assertThat(it).isNotEmpty() }
                step.whyEn?.let { assertThat(it).isNotEmpty() }
                step.sayEn?.let { assertThat(it).isNotEmpty() }
            }
    }

    @Test
    fun `practical Hajj guidance covers preparation special cases and mistakes`() {
        val practical = HajjUmrahContent.categoryById("hajj_practical")
        val topicIds = practical?.topics?.map { it.id }.orEmpty()

        assertThat(topicIds).containsExactly(
            "hajj_preparation",
            "hajj_special_cases",
            "hajj_common_mistakes",
        ).inOrder()
    }
}
