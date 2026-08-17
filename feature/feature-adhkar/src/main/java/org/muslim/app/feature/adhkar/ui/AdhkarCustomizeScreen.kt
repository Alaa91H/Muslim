package org.muslim.app.feature.adhkar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.DhikrCategory

/**
 * Lets the user choose exactly which adhkar appear in the library and in the
 * reminders/overlay (disabled ones are hidden everywhere).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarCustomizeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdhkarCustomizeViewModel = hiltViewModel(),
) {
    val visibility by viewModel.visibility.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adhkar_customize_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.adhkar_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            DhikrCategory.entries.forEach { category ->
                val categoryItems = visibility.filter { it.dhikr.category == category }
                if (categoryItems.isNotEmpty()) {
                    item(key = "header_${category.id}") {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                    items(categoryItems, key = { it.dhikr.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.dhikr.arabic,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = item.dhikr.source,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = item.enabled,
                                    onCheckedChange = { viewModel.setEnabled(item.dhikr.id, it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
