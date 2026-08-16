package org.example.islamicapp.feature.zakat.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZakatCalculatorTest {

    @Test
    fun `no assets means no zakat`() {
        val result = ZakatCalculator.calculate(ZakatInput())
        assertThat(result.totalAssets).isEqualTo(0.0)
        assertThat(result.zakatDue).isEqualTo(0.0)
        assertThat(result.nisabExceeded).isFalse()
    }

    @Test
    fun `cash above nisab yields 2_5 percent`() {
        // Gold at 300/gram → nisab = 25_500. Cash of 100_000 exceeds it.
        val result = ZakatCalculator.calculate(
            ZakatInput(cash = 100_000.0, goldPricePerGram = 300.0)
        )
        assertThat(result.nisabExceeded).isTrue()
        assertThat(result.zakatDue).isEqualTo(2_500.0)
    }

    @Test
    fun `debts owed are deducted before nisab check`() {
        val result = ZakatCalculator.calculate(
            ZakatInput(
                cash = 20_000.0,
                debtsOwed = 19_500.0,
                goldPricePerGram = 300.0,
            )
        )
        assertThat(result.zakatableAmount).isEqualTo(500.0)
        assertThat(result.nisabExceeded).isFalse()
        assertThat(result.zakatDue).isEqualTo(0.0)
    }

    @Test
    fun `gold value counts towards zakat`() {
        // 100g gold at 300/gram = 30_000 value, above the 25_500 nisab.
        val result = ZakatCalculator.calculate(
            ZakatInput(goldGrams = 100.0, goldPricePerGram = 300.0)
        )
        assertThat(result.totalAssets).isEqualTo(30_000.0)
        assertThat(result.zakatDue).isEqualTo(750.0)
    }

    @Test
    fun `nisab uses the lower of gold and silver thresholds`() {
        // Silver at 1.0/gram → nisab 595. Cash 1_000 exceeds it.
        val result = ZakatCalculator.calculate(
            ZakatInput(cash = 1_000.0, goldPricePerGram = 300.0, silverPricePerGram = 1.0)
        )
        assertThat(result.silverNisab).isEqualTo(595.0)
        assertThat(result.nisabExceeded).isTrue()
        assertThat(result.zakatDue).isEqualTo(25.0)
    }

    @Test
    fun `negative inputs are clamped to zero`() {
        val result = ZakatCalculator.calculate(
            ZakatInput(cash = -5.0, goldGrams = -10.0, goldPricePerGram = 300.0)
        )
        assertThat(result.totalAssets).isEqualTo(0.0)
        assertThat(result.zakatDue).isEqualTo(0.0)
    }

    @Test
    fun `fitr total scales with persons`() {
        assertThat(ZakatCalculator.fitrTotal(25.0, 4)).isEqualTo(100.0)
        assertThat(ZakatCalculator.fitrTotal(25.0, 0)).isEqualTo(0.0)
    }
}
