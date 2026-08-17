package org.muslim.app.feature.quran.domain

/** A Quran ayah (Uthmani text). */
data class Ayah(
    val globalNumber: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
    val text: String,
)
