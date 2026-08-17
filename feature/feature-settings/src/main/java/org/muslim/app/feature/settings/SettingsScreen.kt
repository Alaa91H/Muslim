package org.muslim.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppThemeMode
import org.muslim.app.feature.settings.R

/** A user-selectable UI language (PROJECT_PROMPT.md §5). */
private data class LanguageOption(
    val code: String,
    val labelRes: Int,
)

private val languageOptions = listOf(
    LanguageOption(AppPreferences.SYSTEM_LANGUAGE, R.string.settings_language_system),
    LanguageOption("ar", R.string.settings_language_arabic),
    LanguageOption("en", R.string.settings_language_english),
)

/** A selectable start (default) tab shown when the app opens. */
private data class StartTabOption(val route: String, val labelRes: Int)

private val startTabOptions = listOf(
    StartTabOption("home", R.string.settings_start_home),
    StartTabOption("quran", R.string.settings_start_quran),
    StartTabOption("times", R.string.settings_start_times),
    StartTabOption("qibla", R.string.settings_start_qibla),
    StartTabOption("more", R.string.settings_start_more),
)

/**
 * Central settings hub (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة").
 * Cross-feature sections (prayer & adhan) are reached via app-level
 * navigation so this module never depends on another feature module.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenPrayerSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLanguageChanged: () -> Unit,
    /** Back affordance when opened as a sub-screen (from the More hub). */
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item { SectionHeader(stringResource(R.string.settings_section_appearance)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_theme_mode)) },
                    leadingContent = {
                        Icon(
                            imageVector = if (preferences.themeMode == AppThemeMode.Dark) {
                                Icons.Filled.DarkMode
                            } else {
                                Icons.Filled.LightMode
                            },
                            contentDescription = null,
                        )
                    },
                )
                ThemeModeSelector(
                    selected = preferences.themeMode,
                    onSelect = viewModel::setThemeMode,
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
                    supportingContent = { Text(stringResource(R.string.settings_dynamic_color_desc)) },
                    leadingContent = {
                        Icon(Icons.Filled.Palette, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = preferences.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor,
                        )
                    },
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_reduce_animations)) },
                    supportingContent = { Text(stringResource(R.string.settings_reduce_animations_desc)) },
                    leadingContent = {
                        Icon(Icons.Filled.Nightlight, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = preferences.reduceAnimations,
                            onCheckedChange = viewModel::setReduceAnimations,
                        )
                    },
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader(stringResource(R.string.settings_section_start)) }

            item {
                Column(Modifier.selectableGroup()) {
                    startTabOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = preferences.startTab == option.route,
                                    onClick = { viewModel.setStartTab(option.route) },
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = stringResource(option.labelRes),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            RadioButton(
                                selected = preferences.startTab == option.route,
                                onClick = { viewModel.setStartTab(option.route) },
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider() }
            item { SectionHeader(stringResource(R.string.settings_section_language)) }

            item {
                Column(Modifier.selectableGroup()) {
                    languageOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = preferences.languageCode == option.code,
                                    onClick = {
                                        scope.launch {
                                            viewModel.setLanguage(option.code)
                                            onLanguageChanged()
                                        }
                                    },
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(option.labelRes),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            RadioButton(
                                selected = preferences.languageCode == option.code,
                                onClick = {
                                    scope.launch {
                                        viewModel.setLanguage(option.code)
                                        onLanguageChanged()
                                    }
                                },
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider() }
            item { SectionHeader(stringResource(R.string.settings_section_prayer)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_prayer_section)) },
                    supportingContent = { Text(stringResource(R.string.settings_prayer_section_desc)) },
                    leadingContent = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                    trailingContent = { Chevron() },
                    modifier = Modifier.clickable(onClick = onOpenPrayerSettings),
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                    supportingContent = { Text(stringResource(R.string.settings_notifications_desc)) },
                    leadingContent = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
                    trailingContent = { Chevron() },
                    modifier = Modifier.clickable(onClick = onOpenNotifications),
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader(stringResource(R.string.settings_section_data)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_privacy)) },
                    supportingContent = { Text(stringResource(R.string.settings_privacy_desc)) },
                    leadingContent = { Icon(Icons.Filled.PrivacyTip, contentDescription = null) },
                    trailingContent = { Chevron() },
                    modifier = Modifier.clickable(onClick = onOpenPrivacy),
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader(stringResource(R.string.settings_section_about)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about)) },
                    supportingContent = { Text(stringResource(R.string.settings_about_desc)) },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    trailingContent = { Chevron() },
                    modifier = Modifier.clickable(onClick = onOpenAbout),
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_open_source)) },
                    supportingContent = { Text(stringResource(R.string.settings_open_source_desc)) },
                    leadingContent = { Icon(Icons.Filled.Code, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenAbout),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ThemeModeSelector(
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .selectableGroup(),
    ) {
        ThemeMode.entries.forEach { mode ->
            val label = stringResource(mode.labelRes)
            val icon = when (mode) {
                ThemeMode.System -> Icons.Filled.Palette
                ThemeMode.Light -> Icons.Filled.LightMode
                ThemeMode.Dark -> Icons.Filled.DarkMode
            }
            androidx.compose.material3.FilterChip(
                selected = selected == mode.mode,
                onClick = { onSelect(mode.mode) },
                label = { Text(label) },
                leadingIcon = { Icon(icon, contentDescription = null) },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

private enum class ThemeMode(val mode: AppThemeMode, val labelRes: Int) {
    System(AppThemeMode.System, R.string.settings_theme_system),
    Light(AppThemeMode.Light, R.string.settings_theme_light),
    Dark(AppThemeMode.Dark, R.string.settings_theme_dark),
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
