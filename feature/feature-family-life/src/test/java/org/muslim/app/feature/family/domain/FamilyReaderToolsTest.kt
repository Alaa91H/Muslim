package org.muslim.app.feature.family.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyReaderToolsTest {
    @Test
    fun `article formatter includes title sections and references in Arabic and English`() {
        val article = FamilyLifeContent.articleById("family_budget_moderation")!!

        val arabic = FamilyArticleTextFormatter.format(article, isArabic = true)
        val english = FamilyArticleTextFormatter.format(article, isArabic = false)

        assertThat(arabic).contains(article.title.arabic)
        assertThat(arabic).contains(article.sections.first().title.arabic)
        assertThat(arabic).contains("المراجع")
        assertThat(english).contains(article.title.english)
        assertThat(english).contains(article.sections.first().title.english)
        assertThat(english).contains("References")
        assertThat(english).contains("Quran 25:67")
    }

    @Test
    fun `quran reference parser opens valid surah citation and rejects invalid values`() {
        val valid = FamilyEvidenceReference(
            title = LocalizedFamilyText("مرجع", "Reference"),
            citation = "Quran 30:21",
            type = FamilyEvidenceType.Quran,
        )
        val ranged = valid.copy(citation = "Quran 2:229-231")
        val invalid = valid.copy(citation = "Quran 115:1")
        val hadith = valid.copy(
            citation = "Sahih Muslim",
            type = FamilyEvidenceType.Hadith,
        )

        assertThat(FamilyReferenceParser.quranReference(valid))
            .isEqualTo(QuranFamilyReference(30, 21))
        assertThat(FamilyReferenceParser.quranReference(ranged))
            .isEqualTo(QuranFamilyReference(2, 229))
        assertThat(FamilyReferenceParser.quranReference(invalid)).isNull()
        assertThat(FamilyReferenceParser.quranReference(hadith)).isNull()
        assertThat(FamilyReferenceParser.canOpenInApp(hadith)).isTrue()
    }

    @Test
    fun `article search can filter by evidence type`() {
        val quran = FamilyLifeContent.searchArticles(
            query = "",
            evidenceType = FamilyEvidenceType.Quran,
        )
        val hadith = FamilyLifeContent.searchArticles(
            query = "",
            evidenceType = FamilyEvidenceType.Hadith,
        )

        assertThat(quran).isNotEmpty()
        assertThat(quran.all { article ->
            article.references.any { it.type == FamilyEvidenceType.Quran }
        }).isTrue()
        assertThat(hadith.map { it.id }).contains("guests_neighbours_home")
    }
}
