package org.muslim.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranPagerDirectionComposeInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun arabicPager_advancesWithRightwardBookTurn() {
        val pager = AtomicReference<PagerState?>(null)
        setReaderPager(LayoutDirection.Rtl, pager)

        composeRule.onNodeWithTag(PAGER_TAG).performTouchInput { swipeRight() }
        composeRule.waitUntil(PAGER_TIMEOUT_MS) { pager.get()?.currentPage == TARGET_PAGE }
        composeRule.runOnIdle { assertEquals(TARGET_PAGE, pager.get()?.currentPage) }
    }

    @Test
    fun englishPager_advancesWithLeftwardBookTurn() {
        val pager = AtomicReference<PagerState?>(null)
        setReaderPager(LayoutDirection.Ltr, pager)

        composeRule.onNodeWithTag(PAGER_TAG).performTouchInput { swipeLeft() }
        composeRule.waitUntil(PAGER_TIMEOUT_MS) { pager.get()?.currentPage == TARGET_PAGE }
        composeRule.runOnIdle { assertEquals(TARGET_PAGE, pager.get()?.currentPage) }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun setReaderPager(direction: LayoutDirection, pager: AtomicReference<PagerState?>) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                val state = rememberPagerState(initialPage = INITIAL_PAGE, pageCount = { PAGE_COUNT })
                pager.set(state)
                HorizontalPager(
                    state = state,
                    reverseLayout = direction == LayoutDirection.Rtl,
                    modifier = Modifier.fillMaxSize().testTag(PAGER_TAG),
                ) { page ->
                    Box(Modifier.fillMaxSize().testTag("mushaf-test-page-$page"))
                }
            }
        }
    }

    private companion object {
        const val PAGER_TAG = "mushaf-test-pager"
        const val PAGE_COUNT = 3
        const val INITIAL_PAGE = 1
        const val TARGET_PAGE = 2
        const val PAGER_TIMEOUT_MS = 5_000L
    }
}
