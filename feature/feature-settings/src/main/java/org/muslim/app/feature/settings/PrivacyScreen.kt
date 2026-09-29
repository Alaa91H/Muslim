package org.muslim.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimGroup
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimStatusChip
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.core.ui.theme.MuslimTopBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.muslim.app.feature.settings.R

/**
 * In-app privacy policy (PROJECT_PROMPT.md §8): short, honest, human-readable.
 * The full policy also ships as a file in the repository root.
 */
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MuslimScreen(
        modifier = modifier,
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.privacy_title),
                onNavigateBack = onBack,
                navigationContentDescription = stringResource(R.string.back),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(IslamicSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
        ) {
            MuslimGroup {
                MuslimStatusChip(
                    label = stringResource(R.string.privacy_no_account),
                    tone = MuslimStateTone.Positive,
                )
                MuslimStatusChip(
                    label = stringResource(R.string.privacy_no_ads),
                    tone = MuslimStateTone.Positive,
                )
                MuslimStatusChip(
                    label = stringResource(R.string.privacy_no_analytics),
                    tone = MuslimStateTone.Positive,
                )
                MuslimStatusChip(
                    label = stringResource(R.string.privacy_local_first),
                    tone = MuslimStateTone.Positive,
                )
            }
            Text(
                text = stringResource(R.string.privacy_body),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
