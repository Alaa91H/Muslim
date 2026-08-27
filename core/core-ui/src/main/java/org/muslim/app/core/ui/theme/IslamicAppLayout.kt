package org.muslim.app.core.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicMotion

/**
 * Shared motion preference. Screen code may make state transitions immediate
 * when the user has asked for reduced animation, while static reading content
 * remains unaffected.
 */
data class MuslimMotionPreferences(
    val reduceMotion: Boolean = false,
) {
    val fastMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.FastMillis
    val standardMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.StandardMillis
    val emphasisMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.EmphasisMillis
    val enterExitMillis: Int get() = if (reduceMotion) 0 else IslamicMotion.EnterExitMillis
}

val LocalMuslimMotionPreferences = staticCompositionLocalOf { MuslimMotionPreferences() }

/**
 * Shared Material scaffold with a deliberate application surface baseline.
 * Features still own their navigation, state and system-bar contracts.
 */
@Composable
fun MuslimAppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

/**
 * Centers ordinary task surfaces on tablets and foldables without stretching
 * phone-first content. Immersive readers may intentionally opt out.
 */
@Composable
fun MuslimContentFrame(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = DefaultMuslimContentWidth,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxContentWidth),
            content = content,
        )
    }
}

val DefaultMuslimContentWidth = 760.dp
