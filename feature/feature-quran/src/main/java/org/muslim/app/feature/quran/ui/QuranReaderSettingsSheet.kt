package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimBottomSheet
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.ReaderTheme

private const val MIN_FONT_SP = 18f
private const val MAX_FONT_SP = 40f
private const val FONT_STEP_SP = 2f

internal data class ReaderSettingsState(
    val theme: ReaderTheme,
    val fontSize: Float,
    val keepScreenOn: Boolean,
    val tajweedEnabled: Boolean,
    val supplementEnabled: Boolean,
    val canOpenSupplement: Boolean,
    val canOpenDetails: Boolean,
)

internal data class ReaderSettingsActions(
    val onDismiss: () -> Unit,
    val onThemeChange: (ReaderTheme) -> Unit,
    val onFontSizeChanged: (Float) -> Unit,
    val onKeepScreenOnChanged: (Boolean) -> Unit,
    val onTajweedChanged: (Boolean) -> Unit,
    val onOpenSupplement: () -> Unit,
    val onOpenDetails: () -> Unit,
    val onOpenDownloads: () -> Unit,
)

@Composable
internal fun ReaderSettingsSheet(
    state: ReaderSettingsState,
    actions: ReaderSettingsActions,
) {
    MuslimBottomSheet(
        onDismiss = actions.onDismiss,
        title = stringResource(R.string.quran_more_actions),
        scrollable = true,
    ) {
        MuslimSettingsItem(
            title = stringResource(R.string.quran_reader_theme),
            icon = when (state.theme) {
                ReaderTheme.Light -> Icons.Filled.LightMode
                ReaderTheme.Sepia -> Icons.Filled.Nightlight
                ReaderTheme.Dark -> Icons.Filled.DarkMode
            },
            onClick = { actions.onThemeChange(state.theme.nextReaderTheme()) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = IslamicSpacing.Medium,
                    vertical = IslamicSpacing.Small,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.quran_font_size),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            FontSizeControls(
                fontSize = state.fontSize,
                onChanged = actions.onFontSizeChanged,
            )
        }
        MuslimSettingsItem(
            title = stringResource(R.string.quran_keep_screen_on),
            onClick = { actions.onKeepScreenOnChanged(!state.keepScreenOn) },
            trailing = {
                Switch(
                    checked = state.keepScreenOn,
                    onCheckedChange = actions.onKeepScreenOnChanged,
                )
            },
        )
        MuslimSettingsItem(
            title = stringResource(R.string.quran_tajweed_show),
            icon = Icons.Filled.Nightlight,
            onClick = { actions.onTajweedChanged(!state.tajweedEnabled) },
            trailing = {
                Switch(
                    checked = state.tajweedEnabled,
                    onCheckedChange = actions.onTajweedChanged,
                )
            },
        )
        MuslimSettingsItem(
            title = stringResource(R.string.quran_supplement_controls),
            supportingText = if (state.supplementEnabled) {
                stringResource(R.string.quran_supplement_show)
            } else {
                null
            },
            icon = Icons.Filled.Translate,
            enabled = state.canOpenSupplement,
            onClick = actions.onOpenSupplement,
        )
        MuslimSettingsItem(
            title = stringResource(R.string.quran_details),
            icon = Icons.Filled.Info,
            enabled = state.canOpenDetails,
            onClick = actions.onOpenDetails,
        )
        MuslimSettingsItem(
            title = stringResource(R.string.quran_downloads_title),
            icon = Icons.Filled.Download,
            onClick = actions.onOpenDownloads,
        )
    }
}

@Composable
private fun FontSizeControls(
    fontSize: Float,
    onChanged: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { onChanged((fontSize - FONT_STEP_SP).coerceAtLeast(MIN_FONT_SP)) },
            enabled = fontSize > MIN_FONT_SP,
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.quran_font_smaller),
            )
        }
        Text(
            text = fontSize.toInt().toString(),
            modifier = Modifier.padding(horizontal = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
        IconButton(
            onClick = { onChanged((fontSize + FONT_STEP_SP).coerceAtMost(MAX_FONT_SP)) },
            enabled = fontSize < MAX_FONT_SP,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.quran_font_larger),
            )
        }
    }
}

private fun ReaderTheme.nextReaderTheme(): ReaderTheme = when (this) {
    ReaderTheme.Light -> ReaderTheme.Sepia
    ReaderTheme.Sepia -> ReaderTheme.Dark
    ReaderTheme.Dark -> ReaderTheme.Light
}
