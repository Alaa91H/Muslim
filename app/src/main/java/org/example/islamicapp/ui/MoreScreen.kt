package org.example.islamicapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.islamicapp.R

private data class MoreEntry(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val entries = listOf(
    MoreEntry("adhkar", R.string.more_adhkar, Icons.Default.Spa),
    MoreEntry("tasbih", R.string.more_tasbih, Icons.Default.TouchApp),
    MoreEntry("hadith", R.string.more_hadith, Icons.AutoMirrored.Filled.VolumeUp),
    MoreEntry("learn", R.string.more_learn, Icons.Default.School),
    MoreEntry("ramadan", R.string.more_ramadan, Icons.AutoMirrored.Filled.MenuBook),
    MoreEntry("zakat", R.string.more_zakat, Icons.Default.Calculate),
    MoreEntry("settings", R.string.more_settings, Icons.Default.Settings),
    MoreEntry("about", R.string.more_about, Icons.Default.Info),
)

/** "More" hub listing every feature beyond the bottom-bar core tabs. */
@Composable
fun MoreScreen(
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            // Ornamental Islamic header (PROJECT_PROMPT.md §4.5).
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface,
                            ),
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
            ) {
                org.example.islamicapp.core.ui.decorations.IslamicStarPattern(
                    lineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    alpha = 0.10f,
                    modifier = Modifier.matchParentSize(),
                )
                Column(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 20.dp),
                ) {
                    Text(
                        stringResource(R.string.more_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        items(entries.size) { index ->
            val entry = entries[index]
            Card(onClick = { onOpen(entry.route) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.size(16.dp))
                    Text(stringResource(entry.labelRes), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
