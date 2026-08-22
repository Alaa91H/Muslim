package org.muslim.app.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val label: String,
)

/**
 * All UI languages the APK ships resources for, in native names, with the
 * System option pinned first. Uses [android.content.res.AssetManager.locales]
 * (the merged set of every `values-*` folder across all modules) so the list
 * always matches exactly what the app can actually display and stays correct
 * whenever a new locale folder is added.
 */
@Composable
private fun rememberLanguageOptions(): List<LanguageOption> {
    val context = LocalContext.current
    val systemLabel = stringResource(R.string.settings_language_system)
    return remember(context, systemLabel) {
        val system = LanguageOption(AppPreferences.SYSTEM_LANGUAGE, systemLabel)
        val bundled = context.assets.locales
            .mapNotNull { tag -> runCatching { Locale.forLanguageTag(tag) }.getOrNull() }
            .filter { it.language.isNotBlank() && it.language != "und" && it.language != "zz" }
            .distinctBy { it.toLanguageTag() }
            .map { locale ->
                LanguageOption(locale.toLanguageTag(), locale.getDisplayName(locale))
            }
            .sortedBy { it.label.lowercase() }
        listOf(system) + bundled
    }
}

/** A selectable start (default) tab shown when the app opens. */
private data class StartTabOption(val route: String, val labelRes: Int)

private val startTabOptions = listOf(
    StartTabOption("home", R.string.settings_start_home),
    StartTabOption("quran", R.string.settings_start_quran),
    StartTabOption("qibla", R.string.settings_start_qibla),
    StartTabOption("more", R.string.settings_start_more),
)

/** The app-wide clock system: 12-hour (default) or 24-hour. */
private data class TimeFormatOption(val use24h: Boolean, val labelRes: Int)

private val timeFormatOptions = listOf(
    TimeFormatOption(false, R.string.settings_time_12),
    TimeFormatOption(true, R.string.settings_time_24),
)

/**
 * Central settings hub (PROJECT_PROMPT.md §6 "وحدة الإعدادات العامة").
 * Cross-feature sections (prayer & adhan) are reached via app-level
 * navigation so this module never depends on another feature module.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenPrayerSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLanguageChanged: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenMoreOrder: () -> Unit = {},
    /** Opens the in-app update screen (changelog + download). */
    onOpenUpdates: () -> Unit = {},
    /** Opens local reading, contrast, TalkBack and optional voice-navigation settings. */
    onOpenAccessibility: () -> Unit = {},
    /** Opens optional Android Auto, Wear OS and home-automation bridge settings. */
    onOpenSmartDevices: () -> Unit = {},
    /** Back affordance when opened as a sub-screen (from the More hub). */
    onBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val updateCheckResult by viewModel.updateCheckResult.collectAsStateWithLifecycle()
    val updateCheckError by viewModel.updateCheckError.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Accordion: sections start collapsed; tapping one expands it and collapses
    // the previously expanded section (single expanded card at a time).
    var expandedSection by rememberSaveable { mutableStateOf<String?>(null) }

    // One-time confirmation before fully-automatic updates can install APKs.
    var confirmAutoUpdate by remember { mutableStateOf(false) }

    // Every locale the APK ships resources for, shown in its own native name
    // (System first). Built once per composition from the merged assets.
    val languageOptions = rememberLanguageOptions()
    fun toggleSection(section: SettingsSection) {
        expandedSection = if (expandedSection == section.name) null else section.name
    }

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
        if (confirmAutoUpdate) {
            AlertDialog(
                onDismissRequest = { confirmAutoUpdate = false },
                title = { Text(stringResource(R.string.settings_auto_update_confirm_title)) },
                text = { Text(stringResource(R.string.settings_auto_update_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setAutoUpdateEnabled(true)
                        confirmAutoUpdate = false
                    }) {
                        Text(stringResource(R.string.settings_auto_update_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmAutoUpdate = false }) {
                        Text(stringResource(R.string.settings_cancel))
                    }
                },
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = SettingsSection.Appearance.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_appearance),
                    icon = Icons.Filled.Palette,
                    expanded = expandedSection == SettingsSection.Appearance.name,
                    onToggle = { toggleSection(SettingsSection.Appearance) },
                ) {
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
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
                        supportingContent = { Text(stringResource(R.string.settings_dynamic_color_desc)) },
                        leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = preferences.dynamicColor,
                                onCheckedChange = viewModel::setDynamicColor,
                            )
                        },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_reduce_animations)) },
                        supportingContent = { Text(stringResource(R.string.settings_reduce_animations_desc)) },
                        leadingContent = { Icon(Icons.Filled.Nightlight, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = preferences.reduceAnimations,
                                onCheckedChange = viewModel::setReduceAnimations,
                            )
                        },
                    )
                }
            }

            item(key = SettingsSection.Start.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_start),
                    icon = Icons.Filled.Home,
                    expanded = expandedSection == SettingsSection.Start.name,
                    onToggle = { toggleSection(SettingsSection.Start) },
                ) {
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
            }

            item(key = SettingsSection.TimeFormat.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_time_format),
                    icon = Icons.Filled.Schedule,
                    expanded = expandedSection == SettingsSection.TimeFormat.name,
                    onToggle = { toggleSection(SettingsSection.TimeFormat) },
                ) {
                    Column(Modifier.selectableGroup()) {
                        timeFormatOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = preferences.timeFormat24h == option.use24h,
                                        onClick = { viewModel.setTimeFormat24h(option.use24h) },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
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
                                    selected = preferences.timeFormat24h == option.use24h,
                                    onClick = { viewModel.setTimeFormat24h(option.use24h) },
                                )
                            }
                        }
                    }
                }
            }

            item(key = SettingsSection.Language.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_language),
                    icon = Icons.Filled.Language,
                    expanded = expandedSection == SettingsSection.Language.name,
                    onToggle = { toggleSection(SettingsSection.Language) },
                ) {
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
                                    text = option.label,
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
            }

            item(key = SettingsSection.More.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_more),
                    icon = Icons.Filled.ExpandLess,
                    expanded = expandedSection == SettingsSection.More.name,
                    onToggle = { toggleSection(SettingsSection.More) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_more_order)) },
                        supportingContent = { Text(stringResource(R.string.settings_more_order_desc)) },
                        leadingContent = { Icon(Icons.Filled.ExpandLess, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenMoreOrder),
                    )
                }
            }

            item(key = SettingsSection.Prayer.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_prayer),
                    icon = Icons.Filled.NotificationsActive,
                    expanded = expandedSection == SettingsSection.Prayer.name,
                    onToggle = { toggleSection(SettingsSection.Prayer) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_prayer_section)) },
                        supportingContent = { Text(stringResource(R.string.settings_prayer_section_desc)) },
                        leadingContent = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenPrayerSettings),
                    )
                }
            }

            item(key = SettingsSection.Managers.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_managers),
                    icon = Icons.Filled.Lock,
                    expanded = expandedSection == SettingsSection.Managers.name,
                    onToggle = { toggleSection(SettingsSection.Managers) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_notifications)) },
                        supportingContent = { Text(stringResource(R.string.settings_notifications_desc)) },
                        leadingContent = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenNotifications),
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_permissions)) },
                        supportingContent = { Text(stringResource(R.string.settings_permissions_desc)) },
                        leadingContent = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenPermissions),
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.accessibility_title)) },
                        supportingContent = { Text(stringResource(R.string.accessibility_intro)) },
                        leadingContent = { Icon(Icons.Filled.Visibility, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenAccessibility),
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_smart_devices)) },
                        supportingContent = { Text(stringResource(R.string.settings_smart_devices_desc)) },
                        leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenSmartDevices),
                    )
                }
            }

            item(key = SettingsSection.Updates.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_updates),
                    icon = Icons.Filled.SystemUpdate,
                    expanded = expandedSection == SettingsSection.Updates.name,
                    onToggle = { toggleSection(SettingsSection.Updates) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_updates_check)) },
                        supportingContent = { Text(stringResource(R.string.settings_updates_check_desc)) },
                        leadingContent = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = preferences.updateCheckEnabled,
                                onCheckedChange = viewModel::setUpdateCheckEnabled,
                            )
                        },
                    )
                    if (preferences.updateCheckEnabled) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_auto_update)) },
                            supportingContent = { Text(stringResource(R.string.settings_auto_update_desc)) },
                            leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = preferences.autoUpdateEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            confirmAutoUpdate = true
                                        } else {
                                            viewModel.setAutoUpdateEnabled(false)
                                        }
                                    },
                                )
                            },
                        )
                        Text(
                            text = stringResource(R.string.settings_updates_frequency),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            updateFrequencyOptions.forEach { option ->
                                androidx.compose.material3.FilterChip(
                                    selected = preferences.updateCheckFrequency == option.frequency,
                                    onClick = { viewModel.setUpdateCheckFrequency(option.frequency) },
                                    label = { Text(stringResource(option.labelRes)) },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = viewModel::checkForUpdatesNow,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_updates_check_now))
                        }
                        Button(
                            onClick = onOpenUpdates,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.settings_updates_open))
                        }
                    }
                    when (updateCheckResult) {
                        is org.muslim.app.feature.settings.update.UpdateChecker.Result.UpdateAvailable -> {
                            val release = (updateCheckResult as org.muslim.app.feature.settings.update.UpdateChecker.Result.UpdateAvailable).release
                            Text(
                                text = stringResource(R.string.settings_updates_found, release.version),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                            LaunchedEffect(updateCheckResult) {
                                viewModel.consumeUpdateCheckResult()
                                onOpenUpdates()
                            }
                        }
                        is org.muslim.app.feature.settings.update.UpdateChecker.Result.UpToDate -> {
                            Text(
                                text = stringResource(R.string.settings_updates_latest),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                            LaunchedEffect(updateCheckResult) {
                                viewModel.consumeUpdateCheckResult()
                            }
                        }
                        is org.muslim.app.feature.settings.update.UpdateChecker.Result.Unavailable -> {
                            Text(
                                text = stringResource(R.string.settings_updates_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                            updateCheckError?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                )
                            }
                            LaunchedEffect(updateCheckResult) {
                                viewModel.consumeUpdateCheckResult()
                                viewModel.consumeUpdateCheckError()
                            }
                        }
                        null -> Unit
                    }
                }
            }

            item(key = SettingsSection.Data.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_data),
                    icon = Icons.Filled.PrivacyTip,
                    expanded = expandedSection == SettingsSection.Data.name,
                    onToggle = { toggleSection(SettingsSection.Data) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_privacy)) },
                        supportingContent = { Text(stringResource(R.string.settings_privacy_desc)) },
                        leadingContent = { Icon(Icons.Filled.PrivacyTip, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenPrivacy),
                    )
                }
            }

            item(key = SettingsSection.About.name) {
                SectionCard(
                    title = stringResource(R.string.settings_section_about),
                    icon = Icons.Filled.Info,
                    expanded = expandedSection == SettingsSection.About.name,
                    onToggle = { toggleSection(SettingsSection.About) },
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_about)) },
                        supportingContent = { Text(stringResource(R.string.settings_about_desc)) },
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                        trailingContent = { Chevron() },
                        modifier = Modifier.clickable(onClick = onOpenAbout),
                    )
                }
            }
        }
    }
}

/** One collapsible settings card: header toggles, content below. */
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                content()
            }
        }
    }
}

/** The settings sections shown as accordion cards (one expanded at a time). */
private enum class SettingsSection(val titleRes: Int) {
    Appearance(R.string.settings_section_appearance),
    Start(R.string.settings_section_start),
    TimeFormat(R.string.settings_section_time_format),
    Language(R.string.settings_section_language),
    More(R.string.settings_section_more),
    Prayer(R.string.settings_section_prayer),
    Managers(R.string.settings_section_managers),
    Updates(R.string.settings_section_updates),
    Data(R.string.settings_section_data),
    About(R.string.settings_section_about),
}

/** Update-check cadence options (see [org.muslim.app.core.datastore.AppPreferences]). */
private data class UpdateFrequencyOption(val frequency: String, val labelRes: Int)

private val updateFrequencyOptions = listOf(
    UpdateFrequencyOption(AppPreferences.UPDATE_CHECK_DAILY, R.string.settings_updates_daily),
    UpdateFrequencyOption(AppPreferences.UPDATE_CHECK_WEEKLY, R.string.settings_updates_weekly),
    UpdateFrequencyOption(AppPreferences.UPDATE_CHECK_MONTHLY, R.string.settings_updates_monthly),
)

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
