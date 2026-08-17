package org.muslim.app.feature.tasbih.domain

import java.time.LocalDate

/** The dhikr phrase currently being counted. */
enum class TasbihPhrase(val text: String) {
    SubhanAllah("سُبْحَانَ اللَّهِ"),
    Alhamdulillah("الْحَمْدُ لِلَّهِ"),
    AllahuAkbar("اللَّهُ أَكْبَرُ"),
    LaIlahaIllaAllah("لَا إِلَهَ إِلَّا اللَّهُ"),
    Astaghfirullah("أَسْتَغْفِرُ اللَّهَ"),
    SubhanAllahiWaBihamdihi("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"),
    LaHawlaWalaQuwwata("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ"),
}

/** One day's total count, for the history chart. */
data class DailyCount(
    val date: LocalDate,
    val count: Int,
)

/** Persisted misbaha state. */
data class TasbihState(
    val count: Int,
    val target: Int,
    val phrase: TasbihPhrase,
    val history: List<DailyCount>,
) {
    /** Whether the current target has been reached. */
    val targetReached: Boolean get() = target > 0 && count >= target
}

/**
 * Pure counter logic (PROJECT_PROMPT.md §6 Phase 4: "مسبحة إلكترونية ...
 * هدف قابل للتخصيص، سجل إحصائي يومي/أسبوعي").
 *
 * Kept as pure functions so the daily roll-over behaviour is unit-testable
 * without Android dependencies.
 */
object TasbihCounter {

    /** Count to display for [today]: the stored count, or 0 after a day change. */
    fun effectiveCount(storedCount: Int, storedDate: LocalDate, today: LocalDate): Int =
        if (storedDate == today) storedCount else 0

    /**
     * Applies one tap. Rolls the previous day's total into [history]
     * (newest first, trimmed to [historyLimit]) when the date changed.
     */
    fun increment(
        storedCount: Int,
        storedDate: LocalDate,
        today: LocalDate,
        history: List<DailyCount>,
        historyLimit: Int = 30,
    ): IncrementResult {
        return if (storedDate == today) {
            IncrementResult(
                count = storedCount + 1,
                date = today,
                history = history,
            )
        } else {
            val rolled = if (storedCount > 0) {
                (listOf(DailyCount(storedDate, storedCount)) + history).take(historyLimit)
            } else {
                history
            }
            IncrementResult(count = 1, date = today, history = rolled)
        }
    }

    data class IncrementResult(
        val count: Int,
        val date: LocalDate,
        val history: List<DailyCount>,
    )
}
