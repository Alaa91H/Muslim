package org.muslim.app.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.muslim.app.core.designsystem.IslamicLayout
import org.muslim.app.core.designsystem.IslamicMotion

data class MuslimMotionPreferences(
    val reduceMotion: Boolean = false,
) {
    val fastMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.FastMillis
    val standardMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.StandardMillis
    val emphasisMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.EmphasisMillis
    val enterExitMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.EnterExitMillis
}

val LocalMuslimMotionPreferences = staticCompositionLocalOf { MuslimMotionPreferences() }

/** Shared app shell; set liquidGlassEnabled false for the Quran reader. */
@Composable
fun MuslimAppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    liquidGlassEnabled: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    val alreadyInGlassScope = LocalLiquidGlassMode.current
    val scaffoldContent: @Composable () -> Unit = {
        Box(modifier = modifier.fillMaxSize()) {
            if (LocalLiquidGlassMode.current) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = topBar,
                    bottomBar = bottomBar,
                    snackbarHost = snackbarHost,
                    floatingActionButton = floatingActionButton,
                    contentWindowInsets = contentWindowInsets,
                    content = content,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                ) {
                    IslamicDecorationLayer(modifier = Modifier.fillMaxSize())
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        topBar = topBar,
                        bottomBar = bottomBar,
                        snackbarHost = snackbarHost,
                        floatingActionButton = floatingActionButton,
                        contentWindowInsets = contentWindowInsets,
                        content = content,
                    )
                }
            }
        }
    }

    if (alreadyInGlassScope) scaffoldContent()
    else LiquidGlassScreen(modifier = modifier, enabled = liquidGlassEnabled, content = scaffoldContent)
}

@Composable
fun MuslimContentFrame(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = DefaultMuslimContentWidth,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier.fillMaxWidth().widthIn(max = maxContentWidth),
            content = content,
        )
    }
}

val DefaultMuslimContentWidth = IslamicLayout.ReadableContentMaxWidth
