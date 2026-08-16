package org.example.islamicapp.feature.hadith.domain

/**
 * A hadith entry (PROJECT_PROMPT.md §6 Phase 3): Arabic text, concise
 * English meaning, the famous title, its source and grading — every text
 * must display its source (§10 religious-content standards).
 */
data class Hadith(
    /** Stable id, e.g. "nawawi_01". */
    val id: String,
    /** The hadith's famous name, e.g. "حديث النيات". */
    val titleAr: String,
    val titleEn: String,
    /** Full Arabic text. */
    val text: String,
    /** Concise English rendering of the meaning. */
    val meaningEn: String,
    /** Narrator of the famous narration, e.g. "عن أمير المؤمنين أبي حفص عمر بن الخطاب". */
    val narrator: String,
    /** Source reference, e.g. "رواه البخاري ومسلم". */
    val reference: String,
    /** Authenticity grading from recognized verification works. */
    val gradeAr: String,
    val gradeEn: String,
)

/** Grading labels reused across the collection. */
object Grades {
    const val AGREE_UPON = "متفق عليه"
    const val AGREE_UPON_EN = "Agreed upon (Bukhari & Muslim)"
    const val MUSLIM = "رواه مسلم"
    const val MUSLIM_EN = "Sahih Muslim"
    const val BUKHARI = "رواه البخاري"
    const val BUKHARI_EN = "Sahih al-Bukhari"
}
