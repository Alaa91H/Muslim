package org.muslim.app.feature.qibla.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HalalClassificationTest {
    @Test
    fun knownHaramAdditiveWinsOverOtherIngredients() {
        val result = HalalClassifier.summarize(
            barcode = "123",
            productName = "Sample",
            ingredients = listOf(
                HalalClassifier.classify("E330"),
                HalalClassifier.classify("E120"),
            ),
        )
        assertThat(result.status).isEqualTo(HalalStatus.HARAM)
    }

    @Test
    fun animalSourceAdditivesAreQuestionableWhenCodeAloneIsInsufficient() {
        assertThat(HalalClassifier.classify("E471").status).isEqualTo(HalalStatus.QUESTIONABLE)
        assertThat(HalalClassifier.classify("E999").status).isEqualTo(HalalStatus.UNKNOWN)
    }

    @Test
    fun allKnownHalalIngredientsProduceHalalResult() {
        val result = HalalClassifier.summarize(
            "123",
            "Sample",
            listOf(HalalClassifier.classify("E330"), HalalClassifier.classify("E322")),
        )
        assertThat(result.status).isEqualTo(HalalStatus.HALAL)
    }
}
