package org.muslim.app.feature.learn.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WillDraftProtectionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun disabledProtectionOffersDeviceLockAndInvokesEnable() {
        var enabled = false
        composeRule.setContent {
            MaterialTheme {
                WillDraftProtectionCard(
                    session = session(
                        enabled = false,
                        unlocked = true,
                        onEnable = { enabled = true },
                    ),
                )
            }
        }

        composeRule
            .onNodeWithTag(WillDraftProtectionTestTags.ENABLE)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertTrue(enabled)
    }

    @Test
    fun enabledProtectionOffersImmediateLockAndDisableActions() {
        var locked = false
        var disabled = false
        composeRule.setContent {
            MaterialTheme {
                WillDraftProtectionCard(
                    session = session(
                        enabled = true,
                        unlocked = true,
                        onLockNow = { locked = true },
                        onDisable = { disabled = true },
                    ),
                )
            }
        }

        composeRule
            .onNodeWithTag(WillDraftProtectionTestTags.LOCK_NOW)
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithTag(WillDraftProtectionTestTags.DISABLE)
            .assertIsDisplayed()
            .performClick()

        assertTrue(locked)
        assertTrue(disabled)
    }

    @Test
    fun lockedDraftHidesPrivateEditorBehindUnlockAction() {
        var unlocked = false
        composeRule.setContent {
            MaterialTheme {
                WillDraftLockedContent(
                    session = session(
                        enabled = true,
                        unlocked = false,
                        onUnlock = { unlocked = true },
                    ),
                )
            }
        }

        composeRule
            .onNodeWithTag(WillDraftProtectionTestTags.LOCKED_CONTENT)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(WillDraftProtectionTestTags.UNLOCK)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertTrue(unlocked)
    }

    private fun session(
        enabled: Boolean,
        unlocked: Boolean,
        onUnlock: () -> Unit = {},
        onEnable: () -> Unit = {},
        onDisable: () -> Unit = {},
        onLockNow: () -> Unit = {},
    ): WillDraftProtectionSession = WillDraftProtectionSession(
        loaded = true,
        enabled = enabled,
        unlocked = unlocked,
        availability = WillDraftAuthenticationAvailability.Available,
        errorMessage = null,
        unlock = onUnlock,
        enable = onEnable,
        disable = onDisable,
        lockNow = onLockNow,
    )
}
