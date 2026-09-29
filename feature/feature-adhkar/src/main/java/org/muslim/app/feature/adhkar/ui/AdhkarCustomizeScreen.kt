package org.muslim.app.feature.adhkar.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.core.ui.theme.MuslimTopBar
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.DhikrCategory

/**
 * Lets the user choose exactly which adhkar appear in the library and in the
 * reminders/overlay (disabled ones are hidden everywhere).
 */
@Composable
fun AdhkarCustomizeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarCustomizeViewModel = hiltViewModel(),
) {
    val visibility by viewModel.visibility.collectAsStateWithLifecycle()

    MuslimScreen(
        modifier = modifier,
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.adhkar_customize_title),
                onNavigateBack = onBack,
                navigationContentDescription = stringResource(R.string.adhkar_back),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = IslamicSpacing.PageHorizontal,
                bottom = IslamicSpacing.Large,
            ),
        ) {
            item(key = "adhkar-customize-decoration") {
                IslamicDecorationBand(
                    compact = true,
                    modifier = Modifier.padding(vertical = IslamicSpacing.Small),
                )
            }

            DhikrCategory.entries.forEach { category ->
                val categoryItems = visibility.filter { it.dhikr.category == category }
                if (categoryItems.isNotEmpty()) {
                    item(key = "header_${category.id}") {
                        MuslimSectionHeader(
                            title = stringResource(category.titleRes),
                            modifier = Modifier.padding(top = IslamicSpacing.Compact),
                        )
                    }
                    items(categoryItems, key = { it.dhikr.id }) { item ->
                        MuslimSettingsItem(
                            title = item.dhikr.arabic,
                            supportingText = item.dhikr.source,
                            onClick = {
                                viewModel.setEnabled(item.dhikr.id, !item.enabled)
                            },
                            trailing = {
                                Switch(
                                    checked = item.enabled,
                                    onCheckedChange = {
                                        viewModel.setEnabled(item.dhikr.id, it)
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
