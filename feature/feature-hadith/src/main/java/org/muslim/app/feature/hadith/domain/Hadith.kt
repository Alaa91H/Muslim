package org.muslim.app.feature.hadith.domain

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
    Nawawi40("nawawi40", org.muslim.app.feature.hadith.R.string.hadith_collection_nawawi),
    Riyad("riyad", org.muslim.app.feature.hadith.R.string.hadith_collection_riyad),
    Bukhari("bukhari", org.muslim.app.feature.hadith.R.string.hadith_collection_bukhari),
    Muslim("muslim", org.muslim.app.feature.hadith.R.string.hadith_collection_muslim),
    Tirmidhi("tirmidhi", org.muslim.app.feature.hadith.R.string.hadith_collection_tirmidhi),
    AbuDawud("abudawud", org.muslim.app.feature.hadith.R.string.hadith_collection_abudawud),
    Nasai("nasai", org.muslim.app.feature.hadith.R.string.hadith_collection_nasai),
    IbnMajah("ibnmajah", org.muslim.app.feature.hadith.R.string.hadith_collection_ibnmajah),
    Muwatta("muwatta", org.muslim.app.feature.hadith.R.string.hadith_collection_muwatta),
    Other("other", org.muslim.app.feature.hadith.R.string.hadith_collection_other);

    companion object {
        fun fromId(id: String): HadithCollection = entries.firstOrNull { it.id == id } ?: Other
    }
}
