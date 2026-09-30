@file:Suppress("LongParameterList")

package org.muslim.app.core.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.muslim.app.core.designsystem.IslamicLayout

/**
 * Standard task-screen shell for UI/UX V2.
 *
 * It centralizes the app surface, scaffold slots and readable content width.
 * Immersive readers should use [MuslimReaderScaffold] instead.
 */
@Composable
fun MuslimScreen(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = IslamicLayout.ReadableContentMaxWidth,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    MuslimAppScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        MuslimContentFrame(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            maxContentWidth = maxContentWidth,
            content = content,
        )
    }
}

/**
 * Shared top bar with an optional back action and caller-owned action slots.
 *
 * When [onNavigateBack] is supplied, callers must also supply a localized
 * [navigationContentDescription] for accessibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuslimTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    navigationContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = navigationContentDescription,
                    )
                }
            }
        },
        actions = actions,
    )
}

/**
 * Full-width reader shell for Quran, Hadith and other immersive reading surfaces.
 *
 * It intentionally skips [MuslimContentFrame] so each reader can own its text
 * measure and page geometry while still sharing the application scaffold.
 */
@Composable
fun MuslimReaderScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    MuslimAppScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            content = content,
        )
    }
}
