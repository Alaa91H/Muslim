package org.muslim.app.feature.zakat.domain

/**
 * Pure zakat-of-money calculation (PROJECT_PROMPT.md §6 Phase 7):
 * cash, gold, silver, trade goods and investments, minus debts owed,
 * compared against the gold/silver nisab, with 2.5% due.
 *
 * Religious note (§10 of the project prompt): these are the mainstream
 * (جمهور) rulings — 85g gold / 595g silver nisab, 2.5% on the zakatable
 * amount after deducting due debts. Always subject to independent scholarly
 * review before release.
 */
data class ZakatInput(
    val cash: Double = 0.0,
    val goldGrams: Double = 0.0,
    val goldPricePerGram: Double = 0.0,
    val silverGrams: Double = 0.0,
    val silverPricePerGram: Double = 0.0,
    val tradeGoods: Double = 0.0,
    val investments: Double = 0.0,
    /** Debts the user owes to others — deducted before checking the nisab. */
    val debtsOwed: Double = 0.0,
)

data class ZakatResult(
    val totalAssets: Double,
    val zakatableAmount: Double,
    val goldNisab: Double,
    val silverNisab: Double,
    val nisabExceeded: Boolean,
    val zakatDue: Double,
)

object ZakatCalculator {

    /** Nisab threshold in grams (جمهور الفقهاء). */
    const val GOLD_NISAB_GRAMS = 85.0
    const val SILVER_NISAB_GRAMS = 595.0

    /** The due rate: 2.5% (ربع العشر). */
    const val ZAKAT_RATE = 0.025

    fun calculate(input: ZakatInput): ZakatResult {
        val goldValue = input.goldGrams.coerceAtLeast(0.0) * input.goldPricePerGram.coerceAtLeast(0.0)
        val silverValue = input.silverGrams.coerceAtLeast(0.0) * input.silverPricePerGram.coerceAtLeast(0.0)
        val totalAssets = (input.cash.coerceAtLeast(0.0) + goldValue + silverValue +
            input.tradeGoods.coerceAtLeast(0.0) + input.investments.coerceAtLeast(0.0))
        val zakatableAmount = (totalAssets - input.debtsOwed.coerceAtLeast(0.0)).coerceAtLeast(0.0)

        val goldNisab = GOLD_NISAB_GRAMS * input.goldPricePerGram.coerceAtLeast(0.0)
        val silverNisab = SILVER_NISAB_GRAMS * input.silverPricePerGram.coerceAtLeast(0.0)
        val nisabThreshold = if (goldNisab > 0 && silverNisab > 0) {
            minOf(goldNisab, silverNisab)
        } else {
            maxOf(goldNisab, silverNisab)
        }
        val nisabExceeded = zakatableAmount >= nisabThreshold && nisabThreshold > 0

        return ZakatResult(
            totalAssets = totalAssets,
            zakatableAmount = zakatableAmount,
            goldNisab = goldNisab,
            silverNisab = silverNisab,
            nisabExceeded = nisabExceeded,
            zakatDue = if (nisabExceeded) zakatableAmount * ZAKAT_RATE else 0.0,
        )
    }

    /** Fitr per person: the value of one sa' (≈2.5–3 kg) of the staple food. */
    fun fitrTotal(saaValue: Double, persons: Int): Double =
        saaValue.coerceAtLeast(0.0) * persons.coerceAtLeast(0)
}
