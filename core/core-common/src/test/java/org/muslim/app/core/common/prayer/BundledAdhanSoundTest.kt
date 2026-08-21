package org.muslim.app.core.common.prayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BundledAdhanSoundTest {

    @Test
    fun `every bundled sound has a unique id and resolves back`() {
        val ids = BundledAdhanSound.entries.map { it.id }
        assertThat(ids).containsNoDuplicates()
        ids.forEach { id ->
            assertThat(BundledAdhanSound.fromId(id).id).isEqualTo(id)
        }
    }

    @Test
    fun `default is makkah`() {
        assertThat(BundledAdhanSound.DEFAULT_ID).isEqualTo("makkah")
        assertThat(BundledAdhanSound.fromId(BundledAdhanSound.DEFAULT_ID))
            .isEqualTo(BundledAdhanSound.Makkah)
    }

    @Test
    fun `unknown id falls back to makkah`() {
        assertThat(BundledAdhanSound.fromId("does_not_exist")).isEqualTo(BundledAdhanSound.Makkah)
        assertThat(BundledAdhanSound.fromId(null)).isEqualTo(BundledAdhanSound.Makkah)
    }

    @Test
    fun `the global Sunni adhan library covers the famous mosques and reciters`() {
        val ids = BundledAdhanSound.entries.map { it.id }.toSet()
        // The most requested sounds: the Two Holy Mosques, Al-Aqsa,
        // and the famous Egyptian/Levantine reciters.
        assertThat(ids).containsAtLeast(
            "makkah",
            "madinah",
            "alaqsa",
            "egypt",
            "halab",
            "abdul_basit",
            "minshawi",
        )
    }
}
