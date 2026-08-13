package org.example.islamicapp.feature.quran.domain

/** A Quran surah (PROJECT_PROMPT.md §6 Phase 2). */
data class Surah(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val translation: String,
    /** "Meccan" or "Medinan". */
    val revelationType: String,
    val ayahCount: Int,
)
