package org.muslim.app.feature.scholarlibrary.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScholarLibraryV2ModelsTest {
    @Test
    fun unknownDifficultyFallsBackToUnspecified() {
        assertThat(ScholarDifficulty.fromId("legacy-or-unknown")).isEqualTo(ScholarDifficulty.Unspecified)
    }

    @Test
    fun readingProgressFallbackIsPlanned() {
        assertThat(ScholarReadingStatus.fromId("unknown")).isEqualTo(ScholarReadingStatus.Planned)
    }

    @Test
    fun highlightStyleFallbackIsImportant() {
        assertThat(ScholarHighlightStyle.fromId("unknown")).isEqualTo(ScholarHighlightStyle.Important)
    }

    @Test
    fun citationKeepsOptionalEditionMetadataOutOfLegacyCalls() {
        val legacy = Citation(
            bookTitle = "كتاب",
            author = "مؤلف",
            chapter = "باب",
            volume = "1",
            page = "10",
        )

        assertThat(legacy.compactLabel()).contains("كتاب")
        assertThat(legacy.edition).isNull()
        assertThat(legacy.publisher).isNull()
    }
}
