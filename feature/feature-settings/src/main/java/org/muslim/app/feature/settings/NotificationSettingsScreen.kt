package org.muslim.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.feature.settings.R
import org.muslim.app.feature.settings.R.string

/**
 * Master notification manager (PROJECT_PROMPT.md §3.3): a single switch per
 * [NotificationCategory] controls every notifier in the app — prayers &
 * adhan, adhkar, Quran ayah, Ramadan, and the daily hadith. Toggling off is
 * immediate: the DataStore flag flips (all notifiers check it) and any
 * already-posted alert from that channel is cancelled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(string.settings_back))
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
            item {
                Text(
                    text = stringResource(string.notifications_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(NotificationCategory.entries) { category ->
                NotificationCategoryRow(
                    category = category,
                    enabled = preferences[category] ?: category.defaultEnabled,
                    onToggle = { viewModel.setEnabled(category, it) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun NotificationCategoryRow(
    category: NotificationCategory,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(categoryLabelRes(category))) },
        supportingContent = { Text(stringResource(categoryDescriptionRes(category))) },
        leadingContent = { Icon(categoryIcon(category), contentDescription = null) },
        trailingContent = {
            Switch(checked = enabled, onCheckedChange = onToggle)
        },
    )
}

@Composable
private fun categoryIcon(category: NotificationCategory): ImageVector = when (category) {
    NotificationCategory.Adhan -> Icons.Filled.NotificationsActive
    NotificationCategory.PrayerReminder -> Icons.Filled.Alarm
    NotificationCategory.QuranDaily -> Icons.Filled.MenuBook
    NotificationCategory.Ramadan -> Icons.Filled.Nightlight
    NotificationCategory.Adhkar -> Icons.Filled.AutoAwesome
    NotificationCategory.HadithDaily -> Icons.Filled.Book
}

@Composable
private fun categoryLabelRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> string.notif_category_adhan
    NotificationCategory.PrayerReminder -> string.notif_category_reminder
    NotificationCategory.QuranDaily -> string.notif_category_quran
    NotificationCategory.Ramadan -> string.notif_category_ramadan
    NotificationCategory.Adhkar -> string.notif_category_adhkar
    NotificationCategory.HadithDaily -> string.notif_category_hadith
}

@Composable
private fun categoryDescriptionRes(category: NotificationCategory): Int = when (category) {
    NotificationCategory.Adhan -> string.notif_category_adhan_desc
    NotificationCategory.PrayerReminder -> string.notif_category_reminder_desc
    NotificationCategory.QuranDaily -> string.notif_category_quran_desc
    NotificationCategory.Ramadan -> string.notif_category_ramadan_desc
    NotificationCategory.Adhkar -> string.notif_category_adhkar_desc
    NotificationCategory.HadithDaily -> string.notif_category_hadith_desc
}
