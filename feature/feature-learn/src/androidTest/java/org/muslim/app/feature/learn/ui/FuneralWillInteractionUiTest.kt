package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FuneralWillInteractionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchFieldAcceptsAndClearsQuery() {
        var query by mutableStateOf("")
        composeRule.setContent {
            MaterialTheme {
                FuneralWillSearchField(
                    query = query,
                    onQueryChange = { query = it },
                )
            }
        }

        composeRule
            .onNodeWithTag(FuneralWillSearchTestTags.FIELD)
            .performTextInput("burial")
        composeRule.runOnIdle {
            assertEquals("burial", query)
        }

        composeRule
            .onNodeWithTag(FuneralWillSearchTestTags.CLEAR)
            .assertIsDisplayed()
            .performClick()
        composeRule.runOnIdle {
            assertEquals("", query)
        }
    }

    @Test
    fun formSectionExpandsAndCollapsesWithoutLosingStructure() {
        composeRule.setContent {
            MaterialTheme {
                WillFormSection(
                    id = "interaction_test",
                    title = "Section",
                ) {
                    Text(
                        text = "Private field",
                        modifier = Modifier.testTag("will_form_test_body"),
                    )
                }
            }
        }

        composeRule
            .onNodeWithTag("will_form_test_body")
            .assertDoesNotExist()

        composeRule
            .onNodeWithTag("will_form_header_interaction_test")
            .performClick()
        composeRule
            .onNodeWithTag("will_form_test_body")
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag("will_form_header_interaction_test")
            .performClick()
        composeRule
            .onNodeWithTag("will_form_test_body")
            .assertDoesNotExist()
    }

    @Test
    fun searchControlRendersInRtlLayout() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize()) {
                        FuneralWillSearchField(
                            query = "الدفن",
                            onQueryChange = {},
                        )
                    }
                }
            }
        }

        composeRule
            .onNodeWithTag(FuneralWillSearchTestTags.FIELD)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(FuneralWillSearchTestTags.CLEAR)
            .assertIsDisplayed()
    }
}
