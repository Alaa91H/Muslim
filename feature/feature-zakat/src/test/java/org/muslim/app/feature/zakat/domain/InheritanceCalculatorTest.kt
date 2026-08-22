package org.muslim.app.feature.zakat.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InheritanceCalculatorTest {
    @Test
    fun spouse_and_children_receive_expected_common_shares() {
        val result = InheritanceCalculator.calculate(
            InheritanceInput(
                estate = 100_000.0,
                heirs = mapOf(Heir.Wife to 1, Heir.Son to 1, Heir.Daughter to 1),
            ),
        )
        assertThat(result.shares.first { it.heir == Heir.Wife }.amountEach).isWithin(0.001).of(12_500.0)
        assertThat(result.shares.first { it.heir == Heir.Son }.amountEach).isWithin(0.001).of(58_333.333)
        assertThat(result.shares.first { it.heir == Heir.Daughter }.amountEach).isWithin(0.001).of(29_166.666)
    }

    @Test
    fun debts_and_bequest_reduce_distributable_estate() {
        val result = InheritanceCalculator.calculate(
            InheritanceInput(
                estate = 1000.0,
                funeralAndDebt = 100.0,
                bequest = 100.0,
                heirs = mapOf(Heir.Son to 1),
            ),
        )
        assertThat(result.distributableEstate).isEqualTo(800.0)
        assertThat(result.shares.single().amountEach).isEqualTo(800.0)
    }
}
