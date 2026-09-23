package org.muslim.app.feature.family.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyGlobalSearchTest {
    @Test
    fun `unified search finds each supported content kind`() {
        assertThat(FamilyGlobalSearch.search("budget").map { it.kind })
            .contains(FamilySearchKind.Article)
        assertThat(FamilyGlobalSearch.search("Maryam").map { it.kind })
            .contains(FamilySearchKind.BabyName)
        assertThat(FamilyGlobalSearch.search("الشفاء").map { it.kind })
            .contains(FamilySearchKind.Ruqyah)
        assertThat(FamilyGlobalSearch.search("الطوارئ").map { it.kind })
            .contains(FamilySearchKind.Checklist)
    }

    @Test
    fun `unified search respects result kind and limit`() {
        val names = FamilyGlobalSearch.search(
            query = "ا",
            kind = FamilySearchKind.BabyName,
            limit = 5,
        )

        assertThat(names).hasSize(5)
        assertThat(names.all { it.kind == FamilySearchKind.BabyName }).isTrue()
    }

    @Test
    fun `blank unified search returns no noisy default results`() {
        assertThat(FamilyGlobalSearch.search("   ")).isEmpty()
    }
}
