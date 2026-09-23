package org.muslim.app.feature.family.domain

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
        assertThat(FamilyLifeContent.babyNames.size).isAtLeast(120)
        assertThat(FamilyLifeContent.searchNames("Maryam")).containsExactly(
            FamilyLifeContent.babyNames.first { it.id == "maryam" },
        )
        assertThat(FamilyLifeContent.searchNames("prophet", BabyNameGender.Boy))
            .containsAtLeastElementsIn(FamilyLifeContent.babyNames.filter { it.gender == BabyNameGender.Boy }.take(1))
        assertThat(FamilyLifeContent.searchNames("Maryam", BabyNameGender.Boy)).isEmpty()
        assertThat(FamilyLifeContent.searchNames("كوثر").map { it.id }).contains("kawthar")
        assertThat(FamilyLifeContent.searchNames("صحابي", BabyNameGender.Boy).map { it.id })
            .containsAtLeast("talha", "musab")
    }

    @Test
    fun `family guide includes marriage rights parenting and conflict safety`() {
        val searchable = FamilyLifeContent.familyArticles
            .flatMap { article -> article.sections.flatMap { it.paragraphs } }
            .joinToString(" ") { it.arabic }
        assertThat(FamilyLifeContent.familyArticles.size).isAtLeast(49)
        assertThat(FamilyLifeContent.familyArticles.map { it.id })
            .containsAtLeast(
                "engagement",
                "choosing_spouse",
                "premarital_conversations",
                "nikah",
                "mahr_financial_agreements",
                "marriage_documentation",
                "marital_rights",
                "marital_communication",
                "household_finances",
                "parenting",
                "conflict_resolution",
                "mediation_reconciliation",
                "abuse_safety",
                "separation_divorce",
                "divorce_general_principles",
                "khul_annulment",
                "newborn",
                "kinship",
                "daily_family_life",
                "pregnancy_preparation",
                "postpartum_family_support",
                "newborn_sunnahs_evidence",
                "breastfeeding_child_care",
                "aqiqah_complete_guide",
                "choosing_child_name",
                "parenting_early_years",
                "parenting_school_age",
                "parenting_teens",
                "children_prayer_quran",
                "discipline_without_harm",
                "child_digital_safety",
                "body_privacy_safeguarding",
                "sibling_fairness",
                "children_faith_questions",
                "parents_kindness_boundaries",
                "elder_parent_care",
                "supporting_parents_financially",
                "maintaining_kinship",
                "harmful_relatives_boundaries",
                "inlaws_household_boundaries",
                "family_reconciliation_after_distance",
                "household_worship_routine",
                "family_shura_decisions",
                "family_budget_moderation",
                "household_privacy_devices",
                "family_work_study_balance",
                "guests_neighbours_home",
                "family_healthcare_planning",
                "family_weekly_meeting",
            )
        assertThat(searchable).contains("العنف")
        assertThat(searchable).contains("الرضا")
        assertThat(searchable).contains("الأبناء")
    }
    @Test
    fun `family guide search handles Arabic normalization and English keywords`() {
        assertThat(FamilyLifeContent.searchArticles("طلاق").map { it.id })
            .contains("separation_divorce")
        assertThat(FamilyLifeContent.searchArticles("الاسره").map { it.id })
            .isNotEmpty()
        assertThat(FamilyLifeContent.searchArticles("privacy").map { it.id })
            .contains("daily_family_life")
        assertThat(FamilyLifeContent.familyArticleMetadata.map { it.articleId }.toSet())
            .containsExactlyElementsIn(FamilyLifeContent.familyArticles.map { it.id }.toSet())
        assertThat(FamilyLifeContent.searchArticles("مهر", FamilyTopicCategory.Marriage).map { it.id })
            .contains("mahr_financial_agreements")
        assertThat(FamilyLifeContent.articlesFor(FamilyTopicCategory.BeforeMarriage).map { it.id })
            .containsAtLeast("engagement", "choosing_spouse", "premarital_conversations")
        assertThat(FamilyLifeContent.articleById("divorce_general_principles")?.references)
            .isNotEmpty()
    }

    @Test
    fun `ruqyah supplications are curated and referenced`() {
        assertThat(FamilyLifeContent.ruqyahSupplications).hasSize(5)
        assertThat(FamilyLifeContent.ruqyahSupplications.all { it.arabic.isNotBlank() }).isTrue()
        assertThat(FamilyLifeContent.ruqyahSupplications.all { it.reference.arabic.isNotBlank() }).isTrue()
    }

    @Test
    fun `newborn and parenting categories contain staged guidance`() {
        assertThat(FamilyLifeContent.articlesFor(FamilyTopicCategory.Newborn).map { it.id })
            .containsAtLeast(
                "newborn",
                "pregnancy_preparation",
                "postpartum_family_support",
                "breastfeeding_child_care",
                "aqiqah_complete_guide",
                "choosing_child_name",
            )
        assertThat(FamilyLifeContent.articlesFor(FamilyTopicCategory.Parenting).map { it.id })
            .containsAtLeast(
                "parenting",
                "parenting_early_years",
                "parenting_school_age",
                "parenting_teens",
                "child_digital_safety",
                "body_privacy_safeguarding",
            )
    }

    @Test
    fun `kinship and daily-life expansion is categorized and searchable`() {
        assertThat(FamilyLifeContent.articlesFor(FamilyTopicCategory.Kinship).map { it.id })
            .containsAtLeast(
                "kinship",
                "parents_kindness_boundaries",
                "maintaining_kinship",
                "inlaws_household_boundaries",
            )
        assertThat(FamilyLifeContent.articlesFor(FamilyTopicCategory.DailyLife).map { it.id })
            .containsAtLeast(
                "daily_family_life",
                "household_worship_routine",
                "family_budget_moderation",
                "family_weekly_meeting",
            )
        assertThat(FamilyLifeContent.searchArticles("الجيران").map { it.id })
            .contains("guests_neighbours_home")
        assertThat(FamilyLifeContent.searchArticles("budget").map { it.id })
            .contains("family_budget_moderation")
    }

    @Test
    fun `related articles stay in the same topic category`() {
        val related = FamilyLifeContent.relatedArticles("family_weekly_meeting", limit = 4)
        assertThat(related).isNotEmpty()
        assertThat(related).hasSize(4)
        assertThat(related.all {
            FamilyLifeContent.categoryFor(it.id) == FamilyTopicCategory.DailyLife
        }).isTrue()
        assertThat(related.map { it.id }).doesNotContain("family_weekly_meeting")
    }

    @Test
    fun `typed references distinguish quran hadith and specialist guidance`() {
        val allReferences = FamilyLifeContent.familyArticles.flatMap { it.references }
        assertThat(allReferences.map { it.type }).contains(FamilyEvidenceType.Quran)
        assertThat(allReferences.map { it.type }).contains(FamilyEvidenceType.Hadith)
        assertThat(allReferences.map { it.type }).contains(FamilyEvidenceType.Health)
        assertThat(allReferences.map { it.type }).contains(FamilyEvidenceType.Guidance)
    }

}
