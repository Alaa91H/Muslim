package org.muslim.app.feature.hadith.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HadithCollectionTest {
    @Test
    fun requested_sunni_collections_are_available() {
        val ids = HadithCollection.entries.map { it.id }
        assertThat(ids).containsAtLeast(
            "bukhari",
            "muslim",
            "abudawud",
            "tirmidhi",
            "nasai",
            "ibnmajah",
            "muwatta",
            "riyad",
            "nawawi40",
        )
    }

    @Test
    fun unknown_collection_is_safe() {
        assertThat(HadithCollection.fromId("missing")).isEqualTo(HadithCollection.Other)
    }
}
