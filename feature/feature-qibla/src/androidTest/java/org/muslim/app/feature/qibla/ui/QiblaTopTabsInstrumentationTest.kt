package org.muslim.app.feature.qibla.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.muslim.app.feature.qibla.R

/** Device regression for the Qibla/Mosques top-tab contract. */
class QiblaTopTabsInstrumentationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun qibla_is_selected_by_default_and_tabs_switch_in_both_directions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val qibla = context.getString(R.string.qibla_tab_qibla)
        val mosques = context.getString(R.string.qibla_tab_mosques)

        composeRule.setContent {
            var selectedTab by remember { mutableIntStateOf(0) }
            MaterialTheme {
                QiblaTopTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            }
        }

        composeRule.onNodeWithText(qibla).assertIsSelected()
        composeRule.onNodeWithText(mosques).assertIsNotSelected().performClick()
        composeRule.onNodeWithText(mosques).assertIsSelected()
        composeRule.onNodeWithText(qibla).performClick()
        composeRule.onNodeWithText(qibla).assertIsSelected()
    }
}
