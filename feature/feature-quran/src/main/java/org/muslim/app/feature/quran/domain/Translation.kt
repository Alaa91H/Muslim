package org.muslim.app.feature.quran.domain

/** A meaning translation of one ayah (installed translation packs). */
data class Translation(
    val globalNumber: Int,
    val language: String,
    val text: String,
)

/** A tafsir (exegesis) entry for one ayah from a named source. */
data class TafsirEntry(
    val globalNumber: Int,
    val source: String,
    val text: String,
)
