package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrayerLearningContentTest {

    @Test
    fun `prayer course covers the complete planned lesson set`() {
        assertThat(PrayerLearningContent.lessonIds).containsExactly(
            "prayer_intro",
            "shurut",
            "salah_times",
            "adhan",
            "qibla_niyyah",
            "salah",
            "salah_arkan",
            "rakats",
            "nullifiers",
            "sujud_sahw",
            "congregation_imamah",
            "rawatib",
            "traveler_prayer",
            "sick_prayer",
            "jumuah",
            "special",
        )
    }

    @Test
    fun `expanded prayer lessons are wired into academy catalog`() {
        val academyById = LearningAcademyCatalog.lessons.associateBy { it.id }

        PrayerLearningContent.lessons.forEach { expanded ->
            assertThat(academyById[expanded.id]).isEqualTo(expanded)
            assertThat(expanded.sections).isNotEmpty()
            assertThat(expanded.estimatedMinutes).isNotNull()
            assertThat(expanded.reviewStatus)
                .isEqualTo(LearningReviewStatus.NEEDS_SCHOLAR_REVIEW)
        }
    }

    @Test
    fun `every prayer evidence reference resolves inside its lesson`() {
        PrayerLearningContent.lessons.forEach { lesson ->
            val referenceIds = lesson.references.map { it.id }.toSet()
            assertThat(referenceIds).hasSize(lesson.references.size)

            lesson.sections
                .flatMap { it.blocks }
                .flatMap(::referenceIdsFor)
                .forEach { referenceId ->
                    assertThat(referenceIds).contains(referenceId)
                }
        }
    }

    @Test
    fun `core prayer lessons are genuinely long form`() {
        val byId = PrayerLearningContent.lessons.associateBy { it.id }

        assertThat(byId.getValue("salah").sections.size).isAtLeast(7)
        assertThat(byId.getValue("salah_arkan").sections.size).isAtLeast(4)
        assertThat(byId.getValue("salah_times").sections.size).isAtLeast(5)
        assertThat(byId.getValue("sujud_sahw").sections.size).isAtLeast(5)
        assertThat(byId.getValue("jumuah").sections.size).isAtLeast(4)
    }

    private fun referenceIdsFor(block: LearningContentBlock): List<String> = when (block) {
        is LearningContentBlock.Evidence -> block.referenceIds
        is LearningContentBlock.Comparison -> block.items.flatMap { it.referenceIds }
        is LearningContentBlock.QuestionAnswer -> block.referenceIds
        is LearningContentBlock.Quiz -> block.referenceIds
        is LearningContentBlock.Paragraph,
        is LearningContentBlock.Steps,
        is LearningContentBlock.Callout -> emptyList()
    }
}
