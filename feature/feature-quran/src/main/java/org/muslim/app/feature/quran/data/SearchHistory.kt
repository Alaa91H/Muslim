package org.muslim.app.feature.quran.data

/**
 * Pure, testable helpers for the persisted search history (PROJECT_PROMPT.md §6:
 * البحث النصي). The history is stored newest-first, deduplicated and capped so
 * the screen can offer quick re-runs without growing the DataStore unbounded.
 */
object SearchHistory {

    /** Maximum number of recent queries kept on the device. */
    const val MAX_ENTRIES = 10

    /**
     * Separator used when the history list is serialised into a single
     * DataStore string. A control character is used so it can never collide
     * with a real search query (the field is single-line and users search for
     * words, not control codes).
     */
    const val SEPARATOR = ""

    /** Records [query] newest-first, removing any earlier duplicate and capping the size. */
    fun record(history: List<String>, query: String, max: Int = MAX_ENTRIES): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return history
        return (listOf(trimmed) + history.filter { it != trimmed }).take(max)
    }

    fun encode(history: List<String>): String = history.joinToString(SEPARATOR)

    fun decode(stored: String?): List<String> =
        stored.orEmpty().split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
}
