package org.example.islamicapp.feature.hadith.domain

/** A hadith (PROJECT_PROMPT.md §6 Phase 3). */
data class Hadith(
    val id: Long,
    val collection: HadithCollection,
    val chapter: String?,
    val numberInBook: Int?,
    val arabicText: String,
    val translation: String,
    val grade: String,
    val source: String,
)

/** The hadith collections bundled or importable into the app. */
enum class HadithCollection(
    val id: String,
    val titleRes: Int,
) {
    Nawawi40("nawawi40", org.example.islamicapp.feature.hadith.R.string.hadith_collection_nawawi),
    Riyad("riyad", org.example.islamicapp.feature.hadith.R.string.hadith_collection_riyad),
    Bukhari("bukhari", org.example.islamicapp.feature.hadith.R.string.hadith_collection_bukhari),
    Muslim("muslim", org.example.islamicapp.feature.hadith.R.string.hadith_collection_muslim),
    Tirmidhi("tirmidhi", org.example.islamicapp.feature.hadith.R.string.hadith_collection_tirmidhi),
    AbuDawud("abudawud", org.example.islamicapp.feature.hadith.R.string.hadith_collection_abudawud),
    Nasai("nasai", org.example.islamicapp.feature.hadith.R.string.hadith_collection_nasai),
    IbnMajah("ibnmajah", org.example.islamicapp.feature.hadith.R.string.hadith_collection_ibnmajah),
    Other("other", org.example.islamicapp.feature.hadith.R.string.hadith_collection_other);

    companion object {
        fun fromId(id: String): HadithCollection = entries.firstOrNull { it.id == id } ?: Other
    }
}
