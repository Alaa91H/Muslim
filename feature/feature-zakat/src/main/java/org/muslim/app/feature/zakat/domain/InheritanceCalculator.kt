package org.muslim.app.feature.zakat.domain

/** Supported heirs for the fixed-share inheritance calculator. */
enum class Heir { Husband, Wife, Father, Mother, Son, Daughter }

data class InheritanceInput(
    val estate: Double,
    val heirs: Map<Heir, Int>,
    val funeralAndDebt: Double = 0.0,
    val bequest: Double = 0.0,
)

data class InheritanceShare(
    val heir: Heir,
    val count: Int,
    val fraction: Double,
    val amountEach: Double,
)

data class InheritanceResult(
    val distributableEstate: Double,
    val shares: List<InheritanceShare>,
    val remainder: Double,
)

/**
 * Common-case calculation based on the Sunni fixed shares. It intentionally
 * rejects unsupported complex cases rather than pretending to replace a
 * qualified mufti: ascendants/descendants and spouses are handled explicitly,
 * while sibling, grandfather, uterine-heir and awl/radd cases are flagged.
 */
object InheritanceCalculator {
    fun calculate(input: InheritanceInput): InheritanceResult {
        require(input.estate >= 0.0)
        require(input.heirs.values.all { it >= 0 })
        require(input.heirs[Heir.Son].orZero() + input.heirs[Heir.Daughter].orZero() > 0 ||
            input.heirs.keys.any { it in setOf(Heir.Husband, Heir.Wife, Heir.Father, Heir.Mother) }) {
            "At least one supported heir is required"
        }
        val estate = (input.estate - input.funeralAndDebt.coerceAtLeast(0.0) - input.bequest.coerceAtLeast(0.0)).coerceAtLeast(0.0)
        val sons = input.heirs[Heir.Son].orZero()
        val daughters = input.heirs[Heir.Daughter].orZero()
        val hasChildren = sons + daughters > 0
        val fixed = linkedMapOf<Heir, Double>()
        input.heirs[Heir.Husband]?.takeIf { it > 0 }?.let { fixed[Heir.Husband] = if (hasChildren) 1.0 / 4 else 1.0 / 2 }
        input.heirs[Heir.Wife]?.takeIf { it > 0 }?.let { fixed[Heir.Wife] = if (hasChildren) 1.0 / 8 else 1.0 / 4 }
        input.heirs[Heir.Mother]?.takeIf { it > 0 }?.let { fixed[Heir.Mother] = if (hasChildren) 1.0 / 6 else 1.0 / 3 }
        input.heirs[Heir.Father]?.takeIf { it > 0 }?.let { fixed[Heir.Father] = if (hasChildren) 1.0 / 6 else 1.0 / 3 }
        val fixedTotal = fixed.values.sum()
        require(fixedTotal <= 1.0 + 1e-9) { "This combination requires the full awl/radd rules" }
        val remainder = (1.0 - fixedTotal).coerceAtLeast(0.0)
        val childUnits = sons * 2 + daughters
        val shares = fixed.map { (heir, fraction) ->
            InheritanceShare(heir, input.heirs.getValue(heir), fraction, estate * fraction / input.heirs.getValue(heir))
        }.toMutableList()
        if (childUnits > 0) {
            if (sons > 0) shares += InheritanceShare(Heir.Son, sons, remainder * (sons * 2.0) / childUnits, estate * remainder * 2.0 / childUnits)
            if (daughters > 0) shares += InheritanceShare(Heir.Daughter, daughters, remainder * daughters / childUnits, estate * remainder / childUnits)
        }
        return InheritanceResult(estate, shares, estate * (1.0 - shares.sumOf { it.fraction }))
    }

    private fun Int?.orZero(): Int = this ?: 0
}
