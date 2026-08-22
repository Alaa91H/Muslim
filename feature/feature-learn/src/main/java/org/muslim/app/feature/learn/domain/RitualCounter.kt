package org.muslim.app.feature.learn.domain

/** A manually trackable ritual counter; sensor/GPS input can feed increment(). */
data class RitualCounter(
    val completed: Int = 0,
    val target: Int = 7,
) {
    init {
        require(target > 0)
        require(completed in 0..target)
    }

    val isComplete: Boolean get() = completed == target

    fun increment(): RitualCounter = copy(completed = (completed + 1).coerceAtMost(target))

    fun decrement(): RitualCounter = copy(completed = (completed - 1).coerceAtLeast(0))

    fun reset(): RitualCounter = copy(completed = 0)
}

enum class RitualKind { TAWAF, SAI }

data class RitualProgress(
    val tawaf: RitualCounter = RitualCounter(),
    val sai: RitualCounter = RitualCounter(),
) {
    fun counter(kind: RitualKind): RitualCounter = when (kind) {
        RitualKind.TAWAF -> tawaf
        RitualKind.SAI -> sai
    }

    fun increment(kind: RitualKind): RitualProgress = when (kind) {
        RitualKind.TAWAF -> copy(tawaf = tawaf.increment())
        RitualKind.SAI -> copy(sai = sai.increment())
    }

    fun decrement(kind: RitualKind): RitualProgress = when (kind) {
        RitualKind.TAWAF -> copy(tawaf = tawaf.decrement())
        RitualKind.SAI -> copy(sai = sai.decrement())
    }
}
