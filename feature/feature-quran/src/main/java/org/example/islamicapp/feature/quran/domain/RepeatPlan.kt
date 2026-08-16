package org.example.islamicapp.feature.quran.domain

/**
 * Pure logic for the reader's repeat (memorization) mode
 * (PROJECT_PROMPT.md §6 Phase 2: "وضع التكرار لحفظ آية أو مقطع بعدد مرات
 * محدد"): the ordered list of ayah global-numbers read by looping
 * `start..end` exactly [count] times. JVM-testable (see `RepeatPlanTest`).
 */
object RepeatPlan {

    /**
     * Returns the flat sequence of ayah global-numbers for [count] passes over
     * the range [start]..[end] (inclusive, mushaf order). Returns an empty
     * list for any invalid input (non-positive bounds, inverted range, or a
     * non-positive count).
     */
    fun sequence(start: Int, end: Int, count: Int): List<Int> {
        if (start <= 0 || end < start || count <= 0) return emptyList()
        return buildList {
            repeat(count) {
                for (global in start..end) add(global)
            }
        }
    }
}
