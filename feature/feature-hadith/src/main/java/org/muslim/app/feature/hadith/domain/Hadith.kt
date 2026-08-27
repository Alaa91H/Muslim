package org.muslim.app.feature.hadith.domain

/** A hadith row provided by the currently opened, Room-backed collection. */
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

/** A chapter entry built from the currently opened collection only. */
data class HadithChapter(
    val title: String,
    val firstHadithNumber: Int?,
    val lastHadithNumber: Int?,
    val hadithCount: Int,
)

/**
 * The offline collections that the user can open. Catalogue metadata is small
 * and is available before any full book asset is read. The count and chapter
 * values are generated from the versioned per-book assets bundled with the app.
 */
enum class HadithCollection(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val coverRes: Int,
    val hadithCount: Int,
    val chapterCount: Int,
) {
    Bukhari(
        "bukhari",
        org.muslim.app.feature.hadith.R.string.hadith_collection_bukhari,
        org.muslim.app.feature.hadith.R.string.hadith_collection_bukhari_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_bukhari,
        7_580,
        97,
    ),
    Muslim(
        "muslim",
        org.muslim.app.feature.hadith.R.string.hadith_collection_muslim,
        org.muslim.app.feature.hadith.R.string.hadith_collection_muslim_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_muslim,
        7_360,
        57,
    ),
    AbuDawud(
        "abudawud",
        org.muslim.app.feature.hadith.R.string.hadith_collection_abudawud,
        org.muslim.app.feature.hadith.R.string.hadith_collection_abudawud_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_abudawud,
        5_272,
        43,
    ),
    Tirmidhi(
        "tirmidhi",
        org.muslim.app.feature.hadith.R.string.hadith_collection_tirmidhi,
        org.muslim.app.feature.hadith.R.string.hadith_collection_tirmidhi_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_tirmidhi,
        3_924,
        49,
    ),
    Nasai(
        "nasai",
        org.muslim.app.feature.hadith.R.string.hadith_collection_nasai,
        org.muslim.app.feature.hadith.R.string.hadith_collection_nasai_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_nasai,
        5_679,
        51,
    ),
    IbnMajah(
        "ibnmajah",
        org.muslim.app.feature.hadith.R.string.hadith_collection_ibnmajah,
        org.muslim.app.feature.hadith.R.string.hadith_collection_ibnmajah_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_ibnmajah,
        4_338,
        38,
    ),
    Muwatta(
        "muwatta",
        org.muslim.app.feature.hadith.R.string.hadith_collection_muwatta,
        org.muslim.app.feature.hadith.R.string.hadith_collection_muwatta_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_muwatta,
        1_829,
        61,
    ),
    Riyad(
        "riyad",
        org.muslim.app.feature.hadith.R.string.hadith_collection_riyad,
        org.muslim.app.feature.hadith.R.string.hadith_collection_riyad_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_riyad,
        1_895,
        20,
    ),
    Nawawi40(
        "nawawi40",
        org.muslim.app.feature.hadith.R.string.hadith_collection_nawawi,
        org.muslim.app.feature.hadith.R.string.hadith_collection_nawawi_description,
        org.muslim.app.feature.hadith.R.drawable.hadith_cover_nawawi40,
        42,
        1,
    ),
    /** Safety value for migrated or unknown data; it is never shown as a book card. */
    Other(
        "other",
        org.muslim.app.feature.hadith.R.string.hadith_collection_other,
        org.muslim.app.feature.hadith.R.string.hadith_collection_other_description,
        0,
        0,
        0,
    );

    val isBundled: Boolean get() = this != Other

    companion object {
        val browsableCollections: List<HadithCollection> get() = entries.filter { it.isBundled }

        fun fromId(id: String): HadithCollection = entries.firstOrNull { it.id == id } ?: Other
    }
}
