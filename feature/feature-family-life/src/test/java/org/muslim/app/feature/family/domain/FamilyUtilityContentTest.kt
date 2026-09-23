package org.muslim.app.feature.family.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyUtilityContentTest {
    @Test
    fun `family checklists have stable unique ids and useful item counts`() {
        assertThat(FamilyUtilityContent.checklists).hasSize(5)
        assertThat(FamilyUtilityContent.checklists.map { it.id }.toSet())
            .hasSize(FamilyUtilityContent.checklists.size)
        assertThat(FamilyUtilityContent.checklists.all { it.items.size >= 5 }).isTrue()
        assertThat(
            FamilyUtilityContent.checklists.all { checklist ->
                checklist.items.map { it.id }.toSet().size == checklist.items.size
            },
        ).isTrue()
    }

    @Test
    fun `completion keys are scoped to checklist and item`() {
        assertThat(FamilyUtilityContent.completionKey("weekly", "calendar"))
            .isEqualTo("weekly:calendar")
        assertThat(
            FamilyUtilityContent.completionKey("weekly", "calendar"),
        ).isNotEqualTo(
            FamilyUtilityContent.completionKey("newborn", "calendar"),
        )
    }
}
