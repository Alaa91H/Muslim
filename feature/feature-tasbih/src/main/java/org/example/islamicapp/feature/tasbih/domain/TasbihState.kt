package org.example.islamicapp.feature.tasbih.domain

/**
 * Pure tasbih counter logic (PROJECT_PROMPT.md §6 Phase 4):
 * a customizable target, automatic cycle completion with reset,
 * and a daily total that survives app restarts via the repository.
 */
data class TasbihState(
    /** Repetitions in the current cycle. */
    val count: Int = 0,
    /** Completed cycles in this session. */
    val cycles: Int = 0,
    /** Prescribed repetitions per cycle (user-configurable). */
    val target: Int = DEFAULT_TARGET,
    /** Total tasbih counted today (all cycles, persisted). */
    val todayTotal: Int = 0,
) {
    val progress: Float
        get() = if (target <= 0) 0f else (count.toFloat() / target).coerceIn(0f, 1f)

    /** One tap: advances the cycle, rolling over at the target. */
    fun tap(): TasbihState =
        if (count + 1 >= target) {
            copy(count = 0, cycles = cycles + 1, todayTotal = todayTotal + 1)
        } else {
            copy(count = count + 1, todayTotal = todayTotal + 1)
        }

    fun resetCycle(): TasbihState = copy(count = 0)

    fun withTarget(newTarget: Int): TasbihState =
        copy(target = newTarget.coerceIn(MIN_TARGET, MAX_TARGET), count = 0)

    companion object {
        const val DEFAULT_TARGET = 33
        const val MIN_TARGET = 1
        const val MAX_TARGET = 10_000
        val TARGET_CHOICES = listOf(33, 100, 500, 1000)
    }
}
