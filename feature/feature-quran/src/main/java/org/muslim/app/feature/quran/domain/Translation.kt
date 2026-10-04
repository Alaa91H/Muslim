package org.muslim.app.feature.quran.domain

/** A meaning translation of one ayah (installed translation packs). */
data class Translation(
    val globalNumber: Int,
    val language: String,
    val text: String,
    val packId: String = language,
    val title: String = "",
    val translator: String = "",
    val publisher: String = "",
    val sourceAttribution: String = "",
    val version: String = "",
    val sourceUrl: String = "",
    val footnotes: List<String> = emptyList(),
)

/** A tafsir (exegesis) entry for one ayah from a named source. */
data class TafsirEntry(
    val globalNumber: Int,
    val source: String,
    val text: String,
    val language: String = "und",
    val title: String = source,
    val translator: String = "",
    val publisher: String = "",
    val sourceAttribution: String = "",
    val version: String = "",
    val sourceUrl: String = "",
    val footnotes: List<String> = emptyList(),
)
