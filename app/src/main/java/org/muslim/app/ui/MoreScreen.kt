package org.muslim.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.muslim.app.R
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimSectionHeader

private data class MoreEntry(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
    /** Optional dynamic subtitle (e.g. a permission status) replacing [subtitleRes]. */
    val subtitleText: String? = null,
)

private data class MoreSection(
    val id: String,
    val titleRes: Int,
    val entries: List<MoreEntry>,
)

/**
 * The "More" hub (المزيد): every secondary feature lives here so the primary
 * navigation stays at the recommended 3–5 destinations. Settings also lives
 * here as a sub-screen (the `muslim://settings` shortcut still works).
 *
 * Grouped into themed sections and rendered as compact rows so the hub reads
 * as an organized list rather than a scattered, oversized grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenRamadan: () -> Unit,
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
    /** User-customized section order (ids from [org.muslim.app.core.datastore.AppPreferences]). */
    sectionOrder: List<String> = org.muslim.app.core.datastore.AppPreferences.DEFAULT_MORE_SECTION_ORDER,
    /** Sections the user chose to hide (ids from [org.muslim.app.core.datastore.AppPreferences]). */
    hiddenSections: Set<String> = emptySet(),
) {
    val sectionsById = mapOf(
        org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_WORSHIP to MoreSection(
            org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_WORSHIP,
            R.string.more_section_worship,
            listOf(
                MoreEntry(R.string.more_adhkar, R.string.more_adhkar_desc, Icons.Filled.Favorite, onOpenAdhkar),
                MoreEntry(R.string.more_tasbih, R.string.more_tasbih_desc, Icons.Filled.AutoStories, onOpenTasbih),
                MoreEntry(R.string.more_ramadan, R.string.more_ramadan_desc, Icons.Filled.NightsStay, onOpenRamadan),
                MoreEntry(R.string.more_habits, R.string.more_habits_desc, Icons.Filled.SelfImprovement, onOpenHabits),
            ),
        ),
        org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_KNOWLEDGE to MoreSection(
            org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_KNOWLEDGE,
            R.string.more_section_knowledge,
            listOf(
                MoreEntry(R.string.more_hadith, R.string.more_hadith_desc, Icons.AutoMirrored.Filled.MenuBook, onOpenHadith),
                MoreEntry(R.string.more_learn, R.string.more_learn_desc, Icons.Filled.School, onOpenLearn),
                MoreEntry(R.string.more_noorani, R.string.more_noorani_desc, Icons.Filled.School, onOpenNoorani),
                MoreEntry(R.string.more_traveler, R.string.more_traveler_desc, Icons.Filled.Place, onOpenTraveler),
                MoreEntry(R.string.more_family, R.string.more_family_desc, Icons.Filled.FamilyRestroom, onOpenFamily),
                MoreEntry(R.string.more_funeral_will, R.string.more_funeral_will_desc, Icons.Filled.HealthAndSafety, onOpenFuneralWill),
                MoreEntry(R.string.more_reference, R.string.more_reference_desc, Icons.Filled.AutoStories, onOpenReference),
                MoreEntry(R.string.more_islamic_history, R.string.more_islamic_history_desc, Icons.Filled.AutoStories, onOpenIslamicHistory),
                MoreEntry(R.string.more_scholar_library, R.string.more_scholar_library_desc, Icons.AutoMirrored.Filled.LibraryBooks, onOpenScholarLibrary),
            ),
        ),
        org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_TOOLS to MoreSection(
            org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_TOOLS,
            R.string.more_section_tools,
            listOf(
                MoreEntry(R.string.more_zakat, R.string.more_zakat_desc, Icons.Filled.Calculate, onOpenZakat),
                MoreEntry(R.string.more_islamic_finance, R.string.more_islamic_finance_desc, Icons.Filled.AccountBalance, onOpenIslamicFinance),
                MoreEntry(R.string.more_downloads, R.string.more_downloads_desc, Icons.Filled.Download, onOpenDownloads),
            ),
        ),
        org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_APP to MoreSection(
            org.muslim.app.core.datastore.AppPreferences.MORE_SECTION_APP,
            R.string.more_section_app,
            listOf(
                MoreEntry(R.string.more_accessibility, R.string.more_accessibility_desc, Icons.Filled.Visibility, onOpenAccessibility),
                MoreEntry(R.string.more_settings, R.string.more_settings_desc, Icons.Filled.Settings, onOpenSettings),
            ),
        ),
    )
    val sections = sectionOrder.filter { it !in hiddenSections }.mapNotNull { sectionsById[it] }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_more)) }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = IslamicSpacing.PageHorizontal,
                vertical = IslamicSpacing.Compact,
            ),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            sections.forEach { section ->
                item(key = "header_${section.titleRes}") {
                    MuslimSectionHeader(
                        title = stringResource(section.titleRes),
                        modifier = Modifier.padding(top = IslamicSpacing.Small),
                    )
                }
                items(section.entries, key = { it.titleRes }) { entry ->
                    MoreCard(entry)
                }
            }
        }
    }
}

@Composable
private fun MoreCard(entry: MoreEntry) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = entry.onClick),
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = IslamicSpacing.Medium,
                vertical = IslamicSpacing.Compact,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(IslamicSpacing.Small)
                        .size(IslamicIconSize.Standard),
                )
            }
            Spacer(Modifier.width(IslamicSpacing.Compact))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(entry.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.subtitleText ?: stringResource(entry.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
