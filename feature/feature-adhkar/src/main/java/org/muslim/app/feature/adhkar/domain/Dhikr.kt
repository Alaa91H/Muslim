package org.muslim.app.feature.adhkar.domain

import org.muslim.app.feature.adhkar.R

/**
 * A single remembrance (ذكر) with its source and prescribed repetition
 * (PROJECT_PROMPT.md §6 Phase 4).
 *
 * @param virtue the reward/virtue note when known, else null.
 */
data class Dhikr(
    val id: Long,
    val category: DhikrCategory,
    val arabic: String,
    val translation: String,
    val source: String,
    val repetition: Int,
    val virtue: String?,
) {
    /** Suitable for a compact one- or two-line floating reminder. */
    val isShort: Boolean
        get() = arabic.count { !it.isWhitespace() } <= 150 && arabic.count { it == '\n' } <= 1
}

/** Categories of adhkar, matching the sections of "Hisn al-Muslim". */
enum class DhikrCategory(
    val id: String,
    val titleRes: Int,
) {
    Morning("morning", R.string.adhkar_category_morning),
    Evening("evening", R.string.adhkar_category_evening),
    PostPrayer("postprayer", R.string.adhkar_category_postprayer),
    Sleep("sleep", R.string.adhkar_category_sleep),
    Waking("waking", R.string.adhkar_category_waking),
    Food("food", R.string.adhkar_category_food),
    Travel("travel", R.string.adhkar_category_travel),
    General("general", R.string.adhkar_category_general),
    DuaQuranic("dua_quranic", R.string.adhkar_category_dua_quranic),
    DuaDaily("dua_daily", R.string.adhkar_category_dua_daily),
    DuaOccasion("dua_occasion", R.string.adhkar_category_dua_occasion);

    companion object {
        fun fromId(id: String): DhikrCategory = entries.firstOrNull { it.id == id } ?: General
    }
}
