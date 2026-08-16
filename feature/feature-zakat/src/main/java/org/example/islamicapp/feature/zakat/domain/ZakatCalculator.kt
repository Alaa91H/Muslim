package org.example.islamicapp.feature.zakat.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Zakat al-mal calculator (PROJECT_PROMPT.md §6 Phase 7).
 *
 * Monetary zakat is 2.5% of net zakatable wealth (cash + gold + silver +
 * trade goods + investments − immediate debts) once it reaches the nisab and
 * a lunar year (hawl) has passed. All arithmetic uses BigDecimal so amounts
 * are exact to the fraction of the smallest currency unit.
 */
object ZakatCalculator {

    val RATE = BigDecimal("0.025")

    /** Nisab expressed in grams of pure gold (85g per the majority opinion). */
    const val NISAB_GOLD_GRAMS = 85.0

    /** Nisab expressed in grams of pure silver (595g per the majority opinion). */
    const val NISAB_SILVER_GRAMS = 595.0

    data class Inputs(
        val cash: BigDecimal = BigDecimal.ZERO,
        /** Grams of gold owned. */
        val goldGrams: BigDecimal = BigDecimal.ZERO,
        /** Grams of silver owned. */
        val silverGrams: BigDecimal = BigDecimal.ZERO,
        /** Trade-goods market value. */
        val tradeGoods: BigDecimal = BigDecimal.ZERO,
        /** Investments' current value. */
        val investments: BigDecimal = BigDecimal.ZERO,
        /** Immediate due debts deducted before zakat. */
        val debts: BigDecimal = BigDecimal.ZERO,
        /** Gold price per gram in the user's currency (manual entry). */
        val goldPricePerGram: BigDecimal = BigDecimal.ZERO,
        /** Silver price per gram in the user's currency (manual entry). */
        val silverPricePerGram: BigDecimal = BigDecimal.ZERO,
        /** Whether a full lunar year has passed over the wealth. */
        val hawlCompleted: Boolean = true,
    )

    data class Result(
        /** Total zakatable assets before deducting debts. */
        val grossWealth: BigDecimal,
        val netWealth: BigDecimal,
        /** Nisab threshold in the user's currency. */
        val nisab: BigDecimal,
        val reachesNisab: Boolean,
        /** 2.5% of net wealth — payable only when [reachesNisab] && hawl passed. */
        val zakatDue: BigDecimal,
        val isDue: Boolean,
    )

    fun calculate(inputs: Inputs): Result {
        val goldValue = inputs.goldGrams.multiply(inputs.goldPricePerGram)
        val silverValue = inputs.silverGrams.multiply(inputs.silverPricePerGram)
        val gross = inputs.cash
            .add(goldValue)
            .add(silverValue)
            .add(inputs.tradeGoods)
            .add(inputs.investments)
        val net = gross.subtract(inputs.debts).max(BigDecimal.ZERO)

        // Nisab: gold standard when a gold price is supplied, else silver.
        val nisab = when {
            inputs.goldPricePerGram > BigDecimal.ZERO ->
                inputs.goldPricePerGram.multiply(NISAB_GOLD_GRAMS.toBigDecimal())
            inputs.silverPricePerGram > BigDecimal.ZERO ->
                inputs.silverPricePerGram.multiply(NISAB_SILVER_GRAMS.toBigDecimal())
            else -> BigDecimal.ZERO
        }

        val reaches = nisab > BigDecimal.ZERO && net >= nisab
        val due = reaches && inputs.hawlCompleted
        val zakat = if (due) {
            net.multiply(RATE).setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }
        return Result(
            grossWealth = gross,
            netWealth = net,
            nisab = nisab.setScale(2, RoundingMode.HALF_UP),
            reachesNisab = reaches,
            zakatDue = zakat,
            isDue = due,
        )
    }
}

/**
 * Zakat al-fitr: one sa' (~2.5–3 kg) of the local staple food (or its cash
 * value, per the position the user follows) for every household member.
 */
object ZakatAlFitrCalculator {

    /** Sa' of wheat in kilograms (commonly used measure ≈ 2.75 kg). */
    const val SAA_KG = 2.75

    data class Inputs(
        val familyMembers: Int,
        /** Local price of one kilogram of the chosen staple. */
        val pricePerKg: BigDecimal,
        /** Pay in food or cash equivalent (both computed). */
        val kilogramsPerPerson: BigDecimal = SAA_KG.toBigDecimal(),
    )

    data class Result(
        val totalKilograms: BigDecimal,
        val totalCash: BigDecimal,
    )

    fun calculate(inputs: Inputs): Result {
        require(inputs.familyMembers >= 0) { "familyMembers must be >= 0" }
        val kg = inputs.kilogramsPerPerson.multiply(inputs.familyMembers.toBigDecimal())
        return Result(
            totalKilograms = kg,
            totalCash = kg.multiply(inputs.pricePerKg).setScale(2, RoundingMode.HALF_UP),
        )
    }
}
