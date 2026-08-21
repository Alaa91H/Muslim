package org.muslim.app.feature.adhkar.ui

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import kotlinx.coroutines.launch

/**
 * Adhkar library (PROJECT_PROMPT.md §6 Phase 4): category filters, a
 * per-dhikr counter that stops at the prescribed repetition, and full
 * source attribution.
 */
private enum class AdhkarRoute { Library, Settings, Customize }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarViewModel = hiltViewModel(),
) {
    var route by remember { mutableStateOf(AdhkarRoute.Library) }

    when (route) {
        AdhkarRoute.Settings -> AdhkarSettingsScreen(
            onBack = { route = AdhkarRoute.Library },
            onOpenCustomize = { route = AdhkarRoute.Customize },
        )
        AdhkarRoute.Customize -> AdhkarCustomizeScreen(
            onBack = { route = AdhkarRoute.Settings },
        )
        AdhkarRoute.Library -> AdhkarLibraryContent(
            onBack = onBack,
            onOpenSettings = { route = AdhkarRoute.Settings },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhkarLibraryContent(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarViewModel = hiltViewModel(),
) {
    val adhkar by viewModel.adhkar.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val morningEveningReminderEnabled by viewModel.morningEveningReminderEnabled.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.adhkar_copied)
    val onCopied: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(copiedMessage) } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhkar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.adhkar_back))
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.adhkar_settings_title),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text(stringResource(R.string.adhkar_all)) },
                    modifier = Modifier.padding(end = 8.dp),
                )
                viewModel.categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(stringResource(category.titleRes)) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.adhkar_morning_evening_notification),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.adhkar_morning_evening_notification_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = morningEveningReminderEnabled,
                    onCheckedChange = viewModel::setMorningEveningReminderEnabled,
                )
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (favorites.isNotEmpty()) {
                    item(key = "favorites-header") {
                        Text(
                            text = stringResource(R.string.adhkar_favorites),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(favorites, key = { "fav-${it.id}" }) { dhikr ->
                        DhikrCard(
                            dhikr = dhikr,
                            count = viewModel.count(dhikr.id).collectAsStateWithLifecycle(),
                            isFavorite = dhikr.id in favoriteIds,
                            onToggleFavorite = { viewModel.toggleFavorite(dhikr.id) },
                            onIncrement = { viewModel.increment(dhikr.id) },
                            onReset = { viewModel.reset(dhikr.id) },
                            onCopied = onCopied,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
                items(adhkar, key = { it.id }) { dhikr ->
                    DhikrCard(
                        dhikr = dhikr,
                        count = viewModel.count(dhikr.id).collectAsStateWithLifecycle(),
                        isFavorite = dhikr.id in favoriteIds,
                        onToggleFavorite = { viewModel.toggleFavorite(dhikr.id) },
                        onIncrement = { viewModel.increment(dhikr.id) },
                        onReset = { viewModel.reset(dhikr.id) },
                        onCopied = onCopied,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DhikrCard(
    dhikr: Dhikr,
    count: androidx.compose.runtime.State<Int>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onCopied: () -> Unit,
) {
    val currentCount by count
    val haptics = LocalHapticFeedback.current
    val complete = currentCount >= dhikr.repetition
    val context = LocalContext.current
    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
    // English fallback is hidden when the UI language is Arabic (each language
    // shows its own texts — an Arabic reader reads the Arabic original only).
    val showEnglishFallback = AppLanguage.showEnglishFallback()
    val shareText = buildString {
        append(dhikr.arabic)
        if (showEnglishFallback) append("\n\n").append(dhikr.translation)
        append("\n\n").append(dhikr.source)
    }
    fun copyDhikr() {
        clipboard?.setPrimaryClip(ClipData.newPlainText("dhikr", shareText))
        onCopied()
    }
    fun shareDhikr() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, null)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = dhikr.arabic,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.adhkar_remove_favorite else R.string.adhkar_add_favorite,
                        ),
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(onClick = ::copyDhikr) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.adhkar_copy),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = ::shareDhikr) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.adhkar_share),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            dhikr.virtue?.let { virtue ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = virtue,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (showEnglishFallback) {
                Spacer(Modifier.height(6.dp))
                TranslationToggle(dhikr.translation)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = dhikr.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(
                        R.string.adhkar_repetition_label,
                        dhikr.repetition.toString(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIncrement()
                    },
                    enabled = !complete,
                    modifier = Modifier.size(72.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (complete) {
                                stringResource(R.string.adhkar_complete)
                            } else {
                                currentCount.toString()
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (complete) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                if (currentCount > 0) {
                    IconButton(onClick = onReset) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.adhkar_reset),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationToggle(translation: String) {
    var showTranslation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = if (showTranslation) translation else stringResource(R.string.adhkar_show_translation),
            style = if (showTranslation) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.labelMedium
            },
            color = if (showTranslation) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.clickable { showTranslation = !showTranslation },
        )
    }
}
