package org.example.islamicapp.feature.adhkar.domain

/**
 * A single dhikr/dua entry with its prescribed repetition count and source
 * (PROJECT_PROMPT.md §6 Phase 4 — adhkar must be documented and sourced).
 */
data class DhikrItem(
    val id: String,
    /** Arabic text of the dhikr or dua. */
    val text: String,
    /** English rendering of the meaning (shown when the UI language is English). */
    val translationEn: String,
    /** How many times the dhikr is prescribed to be repeated. */
    val count: Int,
    /** Human-readable source reference, e.g. "رواه مسلم". */
    val reference: String,
    /** Optional virtue/reward mentioned in the source. */
    val virtue: String? = null,
)

/** A themed group of adhkar (morning, evening, after prayer, ...). */
data class DhikrCategory(
    val id: String,
    /** Arabic category title. */
    val titleAr: String,
    /** English category title. */
    val titleEn: String,
    val items: List<DhikrItem>,
)

/**
 * Tracks per-item repetition progress for one reading session.
 * Pure logic so it is unit-testable.
 */
data class DhikrProgress(
    val done: Map<String, Int> = emptyMap(),
) {
    /** Registers one repetition of [itemId] against its target [count]. */
    fun increment(itemId: String, count: Int): DhikrProgress {
        val current = done[itemId] ?: 0
        if (current >= count) return this
        return copy(done = done + (itemId to current + 1))
    }

    fun countOf(itemId: String): Int = done[itemId] ?: 0

    fun isComplete(item: DhikrItem): Boolean = countOf(item.id) >= item.count

    /** True when every item in the category reached its prescribed count. */
    fun categoryComplete(category: DhikrCategory): Boolean =
        category.items.all { isComplete(it) }

    companion object {
        fun reset(items: Collection<String>): DhikrProgress =
            DhikrProgress(items.associateWith { 0 })
    }
}
