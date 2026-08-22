package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FuneralContentTest {
    @Test
    fun `funeral guide covers the requested practical areas`() {
        val sectionIds = FuneralContent.guideSections.map(FuneralGuideSection::id)

        assertThat(sectionIds).containsExactly(
            "first_steps",
            "washing",
            "shrouding",
            "prayer",
            "condolences",
        ).inOrder()
        assertThat(FuneralContent.guideSections).allMatch { it.steps.isNotEmpty() }
    }

    @Test
    fun `will content records the primary educational references`() {
        assertThat(FuneralContent.willReferences.arabic).contains("البقرة 2:180")
        assertThat(FuneralContent.willReferences.arabic).contains("صحيح البخاري 2738")
        assertThat(FuneralContent.willChecklist).hasSize(5)
    }
}
