package org.example.islamicapp.feature.quran.domain

/** A user bookmark with enough context to display in a list. */
data class Bookmark(
    val ayah: Ayah,
    val surahName: String,
    val addedAt: Long,
)
