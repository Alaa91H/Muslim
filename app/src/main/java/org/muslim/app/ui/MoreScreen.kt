package org.muslim.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.muslim.app.R

private data class MoreEntry(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * The "More" hub (المزيد): every secondary feature lives here so the bottom
 * navigation stays at the recommended 3–5 destinations. Settings also lives
 * here as a sub-screen (the `muslim://settings` shortcut still works).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onOpenSettings: () -> Unit,
    onOpenHadith: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenRamadan: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenLearn: () -> Unit,
    onOpenReference: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = listOf(
        MoreEntry(R.string.more_reference, R.string.more_reference_desc, Icons.Filled.Star, onOpenReference),
        MoreEntry(R.string.more_settings, R.string.more_settings_desc, Icons.Filled.Settings, onOpenSettings),
        MoreEntry(R.string.more_hadith, R.string.more_hadith_desc, Icons.AutoMirrored.Filled.MenuBook, onOpenHadith),
        MoreEntry(R.string.more_adhkar, R.string.more_adhkar_desc, Icons.Filled.Favorite, onOpenAdhkar),
        MoreEntry(R.string.more_tasbih, R.string.more_tasbih_desc, Icons.Filled.AutoStories, onOpenTasbih),
        MoreEntry(R.string.more_ramadan, R.string.more_ramadan_desc, Icons.Filled.NightsStay, onOpenRamadan),
        MoreEntry(R.string.more_zakat, R.string.more_zakat_desc, Icons.Filled.Calculate, onOpenZakat),
        MoreEntry(R.string.more_learn, R.string.more_learn_desc, Icons.AutoMirrored.Filled.MenuBook, onOpenLearn),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_more)) }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(entries.size) { index ->
                val entry = entries[index]
                ListItem(
                    headlineContent = { Text(stringResource(entry.titleRes)) },
                    supportingContent = { Text(stringResource(entry.subtitleRes)) },
                    leadingContent = {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(8.dp).size(20.dp),
                            )
                        }
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = entry.onClick),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
