package org.muslim.app.feature.family.domain

data class QuranFamilyReference(
    val surahNumber: Int,
    val firstAyah: Int?,
)

object FamilyArticleTextFormatter {
    fun format(
        article: FamilyGuideArticle,
        isArabic: Boolean,
    ): String = buildString {
        appendLine(article.title.pick(isArabic))
        appendLine()
        appendLine(article.summary.pick(isArabic))
        article.sections.forEach { section ->
            appendLine()
            appendLine(section.title.pick(isArabic))
            section.paragraphs.forEach { paragraph ->
                appendLine(paragraph.pick(isArabic))
            }
        }
        if (article.references.isNotEmpty()) {
            appendLine()
            appendLine(if (isArabic) "المراجع" else "References")
            article.references.forEach { reference ->
                append("• ")
                append(reference.title.pick(isArabic))
                append(": ")
                appendLine(reference.citation)
            }
        }
    }.trim()

    private fun LocalizedFamilyText.pick(isArabic: Boolean): String =
        if (isArabic) arabic else english
}

object FamilyReferenceParser {
    private val quranPattern = Regex(
        pattern = """^Quran\s+(\d{1,3})(?::(\d{1,3}))?""",
        option = RegexOption.IGNORE_CASE,
    )

    fun quranReference(reference: FamilyEvidenceReference): QuranFamilyReference? {
        if (reference.type != FamilyEvidenceType.Quran) return null
        val match = quranPattern.find(reference.citation.trim()) ?: return null
        val surah = match.groupValues[1].toIntOrNull() ?: return null
        val ayah = match.groupValues[2].toIntOrNull()
        if (surah !in 1..114 || (ayah != null && ayah <= 0)) return null
        return QuranFamilyReference(surahNumber = surah, firstAyah = ayah)
    }

    fun canOpenInApp(reference: FamilyEvidenceReference): Boolean =
        quranReference(reference) != null || reference.type == FamilyEvidenceType.Hadith
}
