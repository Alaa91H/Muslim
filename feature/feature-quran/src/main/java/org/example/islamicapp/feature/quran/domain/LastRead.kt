package org.example.islamicapp.feature.quran.domain

/** The reader's last position, used to resume reading (Phase 2). */
data class LastRead(
    val surahNumber: Int,
    /** Global mushaf numbering (1..6236) — used for navigation/scroll. */
    val globalNumber: Int,
    /** Per-surah numbering — used for display. */
    val numberInSurah: Int,
)
