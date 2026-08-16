package org.example.islamicapp.feature.zakat.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

/**
 * Zakat arithmetic must be exact (PROJECT_PROMPT.md §6 Phase 7 — religious
 * correctness): 2.5% rate, nisab detection, debt deduction, rounding.
 */
class ZakatCalculatorTest {

    private val goldPrice = BigDecimal("100") // => nisab = 85 × 100 = 8500

    @Test
    fun `exact 2_5 percent on wealth above nisab`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(cash = BigDecimal("10000"), goldPricePerGram = goldPrice),
        )
        assertThat(result.isDue).isTrue()
        assertThat(result.zakatDue).isEqualTo(BigDecimal("250.00"))
    }

    @Test
    fun `wealth below nisab owes nothing`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(cash = BigDecimal("8499"), goldPricePerGram = goldPrice),
        )
        assertThat(result.reachesNisab).isFalse()
        assertThat(result.zakatDue.toDouble()).isEqualTo(0.0)
    }

    @Test
    fun `exactly at nisab reaches the threshold`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(cash = BigDecimal("8500"), goldPricePerGram = goldPrice),
        )
        assertThat(result.reachesNisab).isTrue()
        assertThat(result.zakatDue).isEqualTo(BigDecimal("212.50"))
    }

    @Test
    fun `debts are deducted before the test`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(
                cash = BigDecimal("10000"),
                debts = BigDecimal("2000"),
                goldPricePerGram = goldPrice,
            ),
        )
        assertThat(result.netWealth).isEqualTo(BigDecimal("8000"))
        assertThat(result.reachesNisab).isFalse()
    }

    @Test
    fun `gold and silver valued by weight times price`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(
                goldGrams = BigDecimal("85"),
                silverGrams = BigDecimal("595"),
                goldPricePerGram = goldPrice,
                silverPricePerGram = BigDecimal("1"),
            ),
        )
        // 85×100 + 595×1 = 9 095 ≥ nisab 8 500
        assertThat(result.netWealth.toDouble()).isEqualTo(9095.0)
        assertThat(result.isDue).isTrue()
    }

    @Test
    fun `hawl not passed shows zero but keeps nisab status`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(
                cash = BigDecimal("10000"),
                goldPricePerGram = goldPrice,
                hawlCompleted = false,
            ),
        )
        assertThat(result.reachesNisab).isTrue()
        assertThat(result.isDue).isFalse()
        assertThat(result.zakatDue.toDouble()).isEqualTo(0.0)
    }

    @Test
    fun `falls back to silver nisab when only silver price given`() {
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(
                cash = BigDecimal("1000"),
                silverPricePerGram = BigDecimal("2"), // nisab = 595×2 = 1190
            ),
        )
        assertThat(result.nisab.toDouble()).isEqualTo(1190.0)
        assertThat(result.reachesNisab).isFalse()
    }
}

class ZakatAlFitrCalculatorTest {

    @Test
    fun `kg and cash scale with family size`() {
        val result = ZakatAlFitrCalculator.calculate(
            ZakatAlFitrCalculator.Inputs(
                familyMembers = 4,
                pricePerKg = BigDecimal("10"),
            ),
        )
        assertThat(result.totalKilograms.toDouble()).isEqualTo(11.0) // 4 × 2.75
        assertThat(result.totalCash.toDouble()).isEqualTo(110.0)
    }

    @Test
    fun `zero family yields zero`() {
        val result = ZakatAlFitrCalculator.calculate(
            ZakatAlFitrCalculator.Inputs(familyMembers = 0, pricePerKg = BigDecimal("10")),
        )
        assertThat(result.totalCash.toDouble()).isEqualTo(0.0)
    }

    @Test
    fun `negative family is rejected`() {
        var threw = false
        try {
            ZakatAlFitrCalculator.calculate(
                ZakatAlFitrCalculator.Inputs(familyMembers = -1, pricePerKg = BigDecimal.ONE),
            )
        } catch (expected: IllegalArgumentException) {
            threw = true
        }
        assertThat(threw).isTrue()
    }
}
