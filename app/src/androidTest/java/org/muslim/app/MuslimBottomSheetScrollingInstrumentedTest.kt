package org.muslim.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.muslim.app.core.ui.theme.AppTheme
import org.muslim.app.core.ui.theme.MuslimBottomSheet
import org.muslim.app.core.ui.theme.MuslimSettingsItem

class MuslimBottomSheetScrollingInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun longSettingsSheet_reachesAndActivatesLastControlAt200PercentFont() {
        var activated = false
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                AppTheme(dynamicColor = false) {
                    MuslimBottomSheet(onDismiss = {}, title = "Settings", scrollable = true) {
                        repeat(30) { index ->
                            MuslimSettingsItem(title = "Option $index", onClick = {})
                        }
                        MuslimSettingsItem(title = "Final control", onClick = { activated = true })
                    }
                }
            }
        }
        composeRule.onNodeWithText("Final control").performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertTrue(activated) }
    }
}
