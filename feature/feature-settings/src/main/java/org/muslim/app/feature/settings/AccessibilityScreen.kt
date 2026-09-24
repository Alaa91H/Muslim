package org.muslim.app.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val BSL_WUDU_URL = "https://www.youtube.com/watch?v=VwqROPP-dq0"
private const val BSL_SALAH_URL = "https://www.youtube.com/watch?v=gaO3dbYxWcE"

/**
 * A local control centre for visual readability, TalkBack guidance and the
 * optional one-shot voice-navigation button. External BSL links are opened by
 * the user in the installed browser/video application; nothing is embedded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences = viewModel.preferences.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    MuslimAppScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accessibility_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(IslamicSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
            modifier = Modifier.padding(innerPadding),
        ) {
            item { IntroCard() }
            item { TalkBackCard() }
            item {
                AccessibilityToggleCard(
                    title = stringResource(R.string.accessibility_reading_title),
                    description = stringResource(R.string.accessibility_reading_desc),
                    checked = preferences.accessibilityReadingMode,
                    onCheckedChange = viewModel::setAccessibilityReadingMode,
                )
            }
            item {
                AccessibilityToggleCard(
                    title = stringResource(R.string.accessibility_high_contrast_title),
                    description = stringResource(R.string.accessibility_high_contrast_desc),
                    checked = preferences.accessibilityHighContrast,
                    onCheckedChange = viewModel::setAccessibilityHighContrast,
                )
            }
            item {
                VoiceNavigationCard(
                    enabled = preferences.voiceNavigationEnabled,
                    onEnabledChange = viewModel::setVoiceNavigationEnabled,
                )
            }
            item { SignLanguageCard(onOpenUrl = { url -> context.openExternalUrl(url) }) }
            item { SupportCard() }
        }
    }
}

@Composable
private fun IntroCard() {
    IslamicCard {
        Text(
            text = stringResource(R.string.accessibility_intro),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun TalkBackCard() {
    InfoCard(
        title = stringResource(R.string.accessibility_talkback_title),
        body = stringResource(R.string.accessibility_talkback_body),
    )
}

@Composable
private fun VoiceNavigationCard(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    IslamicCard {
        Column {
            ToggleHeader(
                title = stringResource(R.string.accessibility_voice_title),
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
            Text(stringResource(R.string.accessibility_voice_desc), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.size(IslamicSpacing.Small))
            Text(
                text = stringResource(R.string.accessibility_voice_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(IslamicSpacing.Compact))
            HeadingText(stringResource(R.string.accessibility_voice_examples_title))
            Text(stringResource(R.string.accessibility_voice_examples), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SignLanguageCard(onOpenUrl: (String) -> Unit) {
    IslamicCard {
        Column {
            HeadingText(stringResource(R.string.accessibility_sign_title))
            Text(stringResource(R.string.accessibility_sign_intro), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.size(IslamicSpacing.Compact))
            AssistChip(
                onClick = { onOpenUrl(BSL_WUDU_URL) },
                label = { Text(stringResource(R.string.accessibility_sign_wudu)) },
                leadingIcon = {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(IslamicIconSize.Supporting))
                },
            )
            Spacer(Modifier.size(IslamicSpacing.Small))
            AssistChip(
                onClick = { onOpenUrl(BSL_SALAH_URL) },
                label = { Text(stringResource(R.string.accessibility_sign_salah)) },
                leadingIcon = {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(IslamicIconSize.Supporting))
                },
            )
            Spacer(Modifier.size(IslamicSpacing.Small))
            Text(
                text = stringResource(R.string.accessibility_sign_external),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = IslamicSpacing.Compact))
            Text(
                text = stringResource(R.string.accessibility_sign_review),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SupportCard() {
    InfoCard(
        title = stringResource(R.string.accessibility_support_title),
        body = stringResource(R.string.accessibility_support_body),
    )
}

@Composable
private fun InfoCard(title: String, body: String) {
    IslamicCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Column {
            Icon(Icons.Filled.Info, contentDescription = null)
            Spacer(Modifier.size(IslamicSpacing.Small))
            HeadingText(title)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AccessibilityToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    IslamicCard {
        Column {
            ToggleHeader(title, checked, onCheckedChange)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ToggleHeader(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        Spacer(Modifier.width(IslamicSpacing.Small))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HeadingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() },
    )
}

private fun android.content.Context.openExternalUrl(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
