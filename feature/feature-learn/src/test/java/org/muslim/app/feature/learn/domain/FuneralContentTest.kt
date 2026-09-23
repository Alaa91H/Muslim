package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FuneralContentTest {
    @Test
    fun `funeral guide covers the full practical journey`() {
        val sectionIds = FuneralContent.guideSections.map(FuneralGuideSection::id)

        assertThat(sectionIds).containsExactly(
            "first_steps",
            "documents_and_coordination",
            "washing",
            "shrouding",
            "procession",
            "prayer",
            "burial",
            "after_burial",
            "condolences",
            "cemetery_visits",
        ).inOrder()
        assertThat(FuneralContent.guideSections.all { it.steps.size >= 3 }).isTrue()
        assertThat(FuneralContent.quickActionSteps).hasSize(6)
    }

    @Test
    fun `will guidance covers planning legal and review topics`() {
        val sectionIds = FuneralContent.willEducationSections.map(WillEducationSection::id)

        assertThat(sectionIds).containsExactly(
            "purpose_and_scope",
            "debts_and_trusts",
            "bequests_and_inheritance",
            "executor_and_documents",
            "guardianship",
            "funeral_wishes",
            "witnesses_and_legal_form",
            "review_and_updates",
        ).inOrder()
        assertThat(FuneralContent.willEducationSections.all { it.steps.size >= 3 }).isTrue()
        assertThat(FuneralContent.willChecklist).hasSize(9)
    }

    @Test
    fun `will content records primary educational references and cautions`() {
        assertThat(FuneralContent.willReferences.arabic).contains("البقرة 2:180")
        assertThat(FuneralContent.willReferences.arabic).contains("النساء 4:11")
        assertThat(FuneralContent.willReferences.english).contains("qualified scholar")
        assertThat(
            FuneralContent.willChecklist.any {
                it.arabic.contains("كلمات المرور") && it.english.contains("passwords")
            },
        ).isTrue()
    }
    @Test
    fun `funeral search matches titles and practical steps in both languages`() {
        val english = FuneralContent.searchGuideSections(
            query = "burial",
            isArabic = false,
        )
        val arabic = FuneralContent.searchGuideSections(
            query = "التكفين",
            isArabic = true,
        )

        assertThat(english.map(FuneralGuideSection::id)).contains("burial")
        assertThat(arabic.map(FuneralGuideSection::id)).contains("shrouding")
    }

    @Test
    fun `will education search matches content and empty query returns all`() {
        val matches = FuneralContent.searchWillEducationSections(
            query = "digital secrets",
            isArabic = false,
        )
        val all = FuneralContent.searchWillEducationSections(
            query = "   ",
            isArabic = false,
        )

        assertThat(matches.map(WillEducationSection::id)).contains("executor_and_documents")
        assertThat(all).hasSize(FuneralContent.willEducationSections.size)
    }

    @Test
    fun `arabic search tolerates common alif variants and diacritics`() {
        val matches = FuneralContent.searchGuideSections(
            query = "الوفاه",
            isArabic = true,
        )

        assertThat(matches.map(FuneralGuideSection::id)).contains("first_steps")
    }
}
