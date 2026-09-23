package org.muslim.app.feature.family.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.muslim.app.feature.family.R
import org.muslim.app.feature.family.domain.FamilyEvidenceReference
import org.muslim.app.feature.family.domain.FamilyLifeContent

class FamilyReaderInstrumentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun guideSearch_filters_results_and_exposes_favorite_action() {
        var toggledArticleId: String? = null
        composeRule.setContent {
            MaterialTheme {
                FamilyGuideCatalogContent(
                    isArabic = false,
                    initialCategory = null,
                    favoriteIds = emptySet(),
                    onOpenArticle = {},
                    onToggleFavorite = { toggledArticleId = it },
                )
            }
        }

        composeRule.onNodeWithTag(FamilyUiTags.GUIDE_SEARCH_FIELD)
            .performTextInput("Household spending without excess or deprivation")
        composeRule.onNodeWithText("Household spending without excess or deprivation")
            .assertIsDisplayed()

        val addFavorite = context.getString(R.string.family_add_favorite)
        composeRule.onAllNodesWithContentDescription(addFavorite)
            .onFirst()
            .performClick()

        assertThat(toggledArticleId).isEqualTo("family_budget_moderation")
    }

    @Test
    fun articleReader_exposes_copy_share_and_quran_reference_actions() {
        val article = FamilyLifeContent.articleById("family_budget_moderation")!!
        var copied = false
        var shared = false
        var openedReference: FamilyEvidenceReference? = null

        composeRule.setContent {
            MaterialTheme {
                FamilyArticleDetailContent(
                    article = article,
                    isArabic = false,
                    isFavorite = false,
                    relatedArticles = emptyList(),
                    actions = FamilyArticleReaderActions(
                        onToggleFavorite = {},
                        onCopyArticle = { copied = true },
                        onShareArticle = { shared = true },
                        onOpenReference = { openedReference = it },
                        onOpenArticle = {},
                    ),
                )
            }
        }

        composeRule.onNodeWithTag(FamilyUiTags.ARTICLE_READER).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.family_copy_article)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.family_share_article)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.family_open_quran_reference))
            .performScrollTo()
            .performClick()

        assertThat(copied).isTrue()
        assertThat(shared).isTrue()
        assertThat(openedReference?.citation).isEqualTo("Quran 25:67")
    }
}
