package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SurahRevelationDataTest {

    @Test
    fun revelationOrder_coversEverySurahExactlyOnce() {
        assertThat(SurahRevelationData.revelationOrder).hasSize(114)
        // Every surah number 1..114 has an order.
        for (surah in 1..114) {
            assertThat(SurahRevelationData.orderOf(surah)).isNotNull()
        }
        // Orders are a permutation of 1..114.
        val orders = SurahRevelationData.revelationOrder.values.sorted()
        assertThat(orders).containsExactlyElementsIn((1..114).toList()).inOrder()
    }

    @Test
    fun revelationOrder_spotChecks() {
        // Al-Alaq was revealed first; An-Nasr last.
        assertThat(SurahRevelationData.orderOf(96)).isEqualTo(1)
        assertThat(SurahRevelationData.orderOf(110)).isEqualTo(114)
        assertThat(SurahRevelationData.orderOf(1)).isEqualTo(5)
        assertThat(SurahRevelationData.orderOf(2)).isEqualTo(87)
        assertThat(SurahRevelationData.orderOf(113)).isEqualTo(20)
    }

    @Test
    fun reasons_areCuratedAndBilingual() {
        SurahRevelationData.revelationReason.forEach { (surah, reason) ->
            assertThat(surah).isIn(1..114)
            assertThat(reason.first).isNotEmpty()
            assertThat(reason.second).isNotEmpty()
        }
        // Famous reasons exist.
        assertThat(SurahRevelationData.reasonOf(96)).isNotNull()
        assertThat(SurahRevelationData.reasonOf(93)).isNotNull()
        assertThat(SurahRevelationData.reasonOf(108)).isNotNull()
        // Not every surah has a curated reason (fine by design).
        assertThat(SurahRevelationData.reasonOf(11)).isNull()
    }
}
