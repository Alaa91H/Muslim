package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
class RecitationMediaIdTest {
    private val knownReciters = setOf("abdul_basit_murattal_192kbps", "reader")

    @Test
    fun `parses legacy surah id using selected reciter`() {
        assertThat(RecitationMediaId.parse("muslim_surah_3", knownReciters))
            .isEqualTo(3 to null)
    }

    @Test
    fun `parses surah id with exact reciter id containing underscores`() {
        val reciterId = "abdul_basit_murattal_192kbps"
        assertThat(RecitationMediaId.parse("muslim_surah_3_$reciterId", knownReciters))
            .isEqualTo(3 to reciterId)
    }

    @Test
    fun `rejects invalid surah numbers and unknown reciters`() {
        assertThat(RecitationMediaId.parse("muslim_surah_115", knownReciters)).isNull()
        assertThat(RecitationMediaId.parse("muslim_surah_3_unknown_reader", knownReciters)).isNull()
        assertThat(RecitationMediaId.parse("other_3", knownReciters)).isNull()
    }

    @Test
    fun `parses only valid bookmarked ayah identifiers`() {
        assertThat(RecitationMediaId.parseBookmarkedAyah("muslim_ayah_6236")).isEqualTo(6236)
        assertThat(RecitationMediaId.parseBookmarkedAyah("muslim_ayah_0")).isNull()
        assertThat(RecitationMediaId.parseBookmarkedAyah("muslim_ayah_6237")).isNull()
        assertThat(RecitationMediaId.parseBookmarkedAyah("muslim_ayah_2_extra")).isNull()
        assertThat(RecitationMediaId.parseBookmarkedAyah("other_2")).isNull()
    }
}
