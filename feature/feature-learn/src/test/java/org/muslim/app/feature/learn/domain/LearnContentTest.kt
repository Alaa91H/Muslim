package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Content integrity for the learning guides (PROJECT_PROMPT.md §6 Phase 5).
 * Pins the ninety-nine names of Allah topic at exactly 99 unique names, and
 * guards every topic against empty steps.
 */
class LearnContentTest {

    @Test
    fun `the ninety-nine names are complete and unique`() {
        val names = LearnContent.byId("names_of_allah")
        assertThat(names).isNotNull()
        val steps = names!!.steps
        assertThat(steps).hasSize(99)
        assertThat(steps.map { it.title }.distinct()).hasSize(99)
        steps.forEach { step ->
            assertThat(step.title).isNotEmpty()
            assertThat(step.description).isNotEmpty()
        }
    }

    @Test
    fun `every topic has a title and at least one step`() {
        LearnContent.topics.forEach { topic ->
            assertThat(topic.titleRes).isGreaterThan(0)
            assertThat(topic.subtitleRes).isGreaterThan(0)
            assertThat(topic.steps).isNotEmpty()
        }
    }
}
