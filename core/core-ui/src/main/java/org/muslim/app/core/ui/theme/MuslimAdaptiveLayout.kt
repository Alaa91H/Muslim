package org.muslim.app.core.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.muslim.app.core.designsystem.IslamicLayout
import org.muslim.app.core.designsystem.MuslimAdaptiveLayoutSpec

/**
 * Shared adaptive content frame for ordinary task screens.
 *
 * It is the single place that translates current available width into the
 * Compact / Medium / Expanded policy used by UI/UX V2.
 */
@Composable
fun MuslimAdaptiveContentFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(MuslimAdaptiveLayoutSpec) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val spec = IslamicLayout.adaptiveSpec(maxWidth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = spec.maxContentWidth)
                .padding(horizontal = spec.horizontalPadding),
        ) {
            content(spec)
        }
    }
}

/**
 * Adaptive task-screen shell with shared scaffold/inset behavior.
 */
@Composable
fun MuslimAdaptiveScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(MuslimAdaptiveLayoutSpec) -> Unit,
) {
    MuslimAppScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
    ) { innerPadding: PaddingValues ->
        MuslimAdaptiveContentFrame(
            modifier = Modifier.padding(innerPadding),
            content = content,
        )
    }
}
