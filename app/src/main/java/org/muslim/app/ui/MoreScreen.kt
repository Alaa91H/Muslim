package org.muslim.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.muslim.app.R
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimEmptyState
import org.muslim.app.core.ui.theme.MuslimExpandableSection
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimSearchBar
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.core.ui.theme.MuslimTopBar

private data class MoreEntry(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val subtitleText: String? = null,
)

private data class MoreSection(
    val id: String,
    val titleRes: Int,
    val entries: List<MoreEntry>,
)

@Suppress("LongParameterList")
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenRamadan: () -> Unit,
    showRamadanShortcut: Boolean = true,
    onOpenHabits: () -> Unit = {},
    onOpenZakat: () -> Unit,
    onOpenIslamicFinance: () -> Unit = {},
    onOpenLearn: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenIslamicHistory: () -> Unit = {},
    onOpenScholarLibrary: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onOpenDownloads: () -> Unit,
    onOpenFamily: () -> Unit = {},
    onOpenFuneralWill: () -> Unit = {},
    onOpenNoorani: () -> Unit = {},
    onOpenTraveler: () -> Unit = {},
    onOpenMoreOrder: () -> Unit = {},
    sectionOrder: List<String> = AppPreferences.DEFAULT_MORE_SECTION_ORDER,
    hiddenSections: Set<String> = emptySet(),
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var expandedSections by rememberSaveable {
        mutableStateOf(setOf(AppPreferences.MORE_SECTION_WORSHIP))
    }

    val sections = moreSections(
        onOpenSettings = onOpenSettings,
        onOpenHadith = onOpenHadith,
        onOpenAdhkar = onOpenAdhkar,
        onOpenTasbih = onOpenTasbih,
        onOpenRamadan = onOpenRamadan,
        showRamadanShortcut = showRamadanShortcut,
        onOpenHabits = onOpenHabits,
        onOpenZakat = onOpenZakat,
        onOpenIslamicFinance = onOpenIslamicFinance,
        onOpenLearn = onOpenLearn,
        onOpenReference = onOpenReference,
        onOpenIslamicHistory = onOpenIslamicHistory,
        onOpenScholarLibrary = onOpenScholarLibrary,
        onOpenAccessibility = onOpenAccessibility,
        onOpenDownloads = onOpenDownloads,
        onOpenFamily = onOpenFamily,
        onOpenFuneralWill = onOpenFuneralWill,
        onOpenNoorani = onOpenNoorani,
        onOpenTraveler = onOpenTraveler,
    )
    val orderedSections = sectionOrder
        .filter { it !in hiddenSections }
        .mapNotNull(sections::get)
    val filteredSections = orderedSections.mapNotNull { section ->
        val visibleEntries = if (searchQuery.isBlank()) {
            section.entries
        } else {
            section.entries.filter { entryMatches(context, it, searchQuery) }
        }
        section.takeIf { visibleEntries.isNotEmpty() }?.copy(entries = visibleEntries)
    }

    MuslimScreen(
        modifier = modifier,
        topBar = {
            MuslimTopBar(
                title = stringResource(R.string.tab_more),
                actions = {
                    IconButton(onClick = onOpenMoreOrder) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(R.string.more_customize),
                        )
                    }
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Compact,
            ),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            item(key = "search") {
                MuslimSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(R.string.more_search_hint),
                    clearContentDescription = stringResource(R.string.more_search_clear),
                )
            }

            if (filteredSections.isEmpty()) {
                item(key = "empty") {
                    MuslimEmptyState(
                        title = stringResource(R.string.more_search_empty),
                        icon = Icons.Filled.Search,
                        modifier = Modifier.padding(vertical = IslamicSpacing.Large),
                    )
                }
            } else {
                filteredSections.forEach { section ->
                    item(key = "section_${section.id}") {
                        val searchActive = searchQuery.isNotBlank()
                        val expanded = searchActive || section.id in expandedSections
                        MuslimExpandableSection(
                            title = stringResource(section.titleRes),
                            expanded = expanded,
                            onExpandedChange = { next ->
                                if (!searchActive) {
                                    expandedSections = if (next) {
                                        expandedSections + section.id
                                    } else {
                                        expandedSections - section.id
                                    }
                                }
                            },
                        ) {
                            section.entries.forEach { entry ->
                                MoreItem(entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("LongParameterList")
private fun moreSections(
    onOpenSettings: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenRamadan: () -> Unit,
    showRamadanShortcut: Boolean,
    onOpenHabits: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenIslamicFinance: () -> Unit,
    onOpenLearn: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenIslamicHistory: () -> Unit,
    onOpenScholarLibrary: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenFuneralWill: () -> Unit,
    onOpenNoorani: () -> Unit,
    onOpenTraveler: () -> Unit,
): Map<String, MoreSection> = mapOf(
    AppPreferences.MORE_SECTION_WORSHIP to MoreSection(
        id = AppPreferences.MORE_SECTION_WORSHIP,
        titleRes = R.string.more_section_worship,
        entries = buildList {
            add(MoreEntry(R.string.more_adhkar, R.string.more_adhkar_desc, Icons.Filled.Favorite, onOpenAdhkar))
            add(MoreEntry(R.string.more_tasbih, R.string.more_tasbih_desc, Icons.Filled.AutoStories, onOpenTasbih))
            if (showRamadanShortcut) {
                add(MoreEntry(R.string.more_ramadan, R.string.more_ramadan_desc, Icons.Filled.NightsStay, onOpenRamadan))
            }
            add(MoreEntry(R.string.more_habits, R.string.more_habits_desc, Icons.Filled.SelfImprovement, onOpenHabits))
        },
    ),
    AppPreferences.MORE_SECTION_KNOWLEDGE to MoreSection(
        id = AppPreferences.MORE_SECTION_KNOWLEDGE,
        titleRes = R.string.more_section_knowledge,
        entries = listOf(
            MoreEntry(R.string.more_hadith, R.string.more_hadith_desc, Icons.AutoMirrored.Filled.MenuBook, onOpenHadith),
            MoreEntry(R.string.more_learn, R.string.more_learn_desc, Icons.Filled.School, onOpenLearn),
            MoreEntry(R.string.more_noorani, R.string.more_noorani_desc, Icons.Filled.School, onOpenNoorani),
            MoreEntry(R.string.more_traveler, R.string.more_traveler_desc, Icons.Filled.Place, onOpenTraveler),
            MoreEntry(R.string.more_family, R.string.more_family_desc, Icons.Filled.FamilyRestroom, onOpenFamily),
            MoreEntry(R.string.more_funeral_will, R.string.more_funeral_will_desc, Icons.Filled.HealthAndSafety, onOpenFuneralWill),
            MoreEntry(R.string.more_reference, R.string.more_reference_desc, Icons.Filled.AutoStories, onOpenReference),
            MoreEntry(R.string.more_islamic_history, R.string.more_islamic_history_desc, Icons.Filled.AutoStories, onOpenIslamicHistory),
            MoreEntry(
                R.string.more_scholar_library,
                R.string.more_scholar_library_desc,
                Icons.AutoMirrored.Filled.LibraryBooks,
                onOpenScholarLibrary,
            ),
        ),
    ),
    AppPreferences.MORE_SECTION_TOOLS to MoreSection(
        id = AppPreferences.MORE_SECTION_TOOLS,
        titleRes = R.string.more_section_tools,
        entries = listOf(
            MoreEntry(R.string.more_zakat, R.string.more_zakat_desc, Icons.Filled.Calculate, onOpenZakat),
            MoreEntry(
                R.string.more_islamic_finance,
                R.string.more_islamic_finance_desc,
                Icons.Filled.AccountBalance,
                onOpenIslamicFinance,
            ),
            MoreEntry(R.string.more_downloads, R.string.more_downloads_desc, Icons.Filled.Download, onOpenDownloads),
        ),
    ),
    AppPreferences.MORE_SECTION_APP to MoreSection(
        id = AppPreferences.MORE_SECTION_APP,
        titleRes = R.string.more_section_app,
        entries = listOf(
            MoreEntry(
                R.string.more_accessibility,
                R.string.more_accessibility_desc,
                Icons.Filled.Visibility,
                onOpenAccessibility,
            ),
            MoreEntry(R.string.more_settings, R.string.more_settings_desc, Icons.Filled.Settings, onOpenSettings),
        ),
    ),
)

private fun entryMatches(
    context: Context,
    entry: MoreEntry,
    query: String,
): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    val title = context.getString(entry.titleRes)
    val subtitle = entry.subtitleText ?: context.getString(entry.subtitleRes)
    return title.contains(needle, ignoreCase = true) ||
        subtitle.contains(needle, ignoreCase = true)
}

@Composable
private fun MoreItem(entry: MoreEntry) {
    MuslimSettingsItem(
        title = stringResource(entry.titleRes),
        supportingText = entry.subtitleText ?: stringResource(entry.subtitleRes),
        icon = entry.icon,
        iconContentDescription = stringResource(entry.titleRes),
        onClick = entry.onClick,
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.more_open_item),
            )
        },
    )
}
