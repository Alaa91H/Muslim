package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyLifeContentTest {
    @Test
    fun `ruqyah catalog contains text, references and safe audio for every passage`() {
        assertThat(FamilyLifeContent.ruqyahPassages).hasSize(5)
        assertThat(FamilyLifeContent.ruqyahPassages.all { it.text.arabic.isNotBlank() }).isTrue()
        assertThat(FamilyLifeContent.ruqyahPassages.all { it.reference.arabic.isNotBlank() }).isTrue()
        assertThat(FamilyLifeContent.ruqyahPassages.all { FamilyLifeContent.isSafeAudioUrl(it.audioUrl) }).isTrue()
    }

    @Test
    fun `audio validator rejects non EveryAyah, traversal and non mp3 URLs`() {
        assertThat(FamilyLifeContent.isSafeAudioUrl("https://everyayah.com/data/reader/001001.mp3")).isTrue()
        assertThat(FamilyLifeContent.isSafeAudioUrl("http://everyayah.com/data/reader/001001.mp3")).isFalse()
        assertThat(FamilyLifeContent.isSafeAudioUrl("https://example.com/001001.mp3")).isFalse()
        assertThat(FamilyLifeContent.isSafeAudioUrl("https://everyayah.com/data/reader/../secret.mp3")).isFalse()
        assertThat(FamilyLifeContent.isSafeAudioUrl("https://everyayah.com/data/reader/001001.wav")).isFalse()
    }

    @Test
    fun `baby-name catalog is substantial and filters by gender and meaning`() {
        assertThat(FamilyLifeContent.babyNames.size).isAtLeast(50)
        assertThat(FamilyLifeContent.searchNames("Maryam")).containsExactly(
            FamilyLifeContent.babyNames.first { it.id == "maryam" },
        )
        assertThat(FamilyLifeContent.searchNames("prophet", BabyNameGender.Boy))
            .containsAtLeastElementsIn(FamilyLifeContent.babyNames.filter { it.gender == BabyNameGender.Boy }.take(1))
        assertThat(FamilyLifeContent.searchNames("Maryam", BabyNameGender.Boy)).isEmpty()
    }

    @Test
    fun `family guide includes marriage rights parenting and conflict safety`() {
        val searchable = FamilyLifeContent.familyArticles
            .flatMap { article -> article.sections.flatMap { it.paragraphs } }
            .joinToString(" ") { it.arabic }
        assertThat(FamilyLifeContent.familyArticles.map { it.id })
            .containsExactly("engagement", "nikah", "marital_rights", "parenting").inOrder()
        assertThat(searchable).contains("العنف")
        assertThat(searchable).contains("الرضا")
        assertThat(searchable).contains("الأبناء")
    }
}
