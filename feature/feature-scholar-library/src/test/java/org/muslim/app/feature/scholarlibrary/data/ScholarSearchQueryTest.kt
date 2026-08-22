package org.muslim.app.feature.scholarlibrary.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScholarSearchQueryTest {
    @Test
    fun `normalizes Arabic diacritics before building a prefix query`() {
        assertThat(ScholarSearchQuery.build("مَرَاجِعُ العِلْم"))
            .isEqualTo("مراجع* AND العلم*")
    }

    @Test
    fun `removes FTS operators supplied by the user`() {
        assertThat(ScholarSearchQuery.build("+بحث (المراجع)*"))
            .isEqualTo("بحث* AND المراجع*")
    }

    @Test
    fun `returns an empty query for blank input`() {
        assertThat(ScholarSearchQuery.build("   ")).isEmpty()
    }
}
