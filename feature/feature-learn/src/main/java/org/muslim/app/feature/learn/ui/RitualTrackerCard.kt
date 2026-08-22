package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.RitualCounter

@Composable
fun RitualTrackerCard(
    tawaf: RitualCounter,
    sai: RitualCounter,
    onTawafChanged: (Int) -> Unit,
    onSaiChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.hajj_ritual_counter_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.hajj_ritual_counter_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {
                    onTawafChanged(0)
                    onSaiChanged(0)
                }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.hajj_counter_reset),
                    )
                }
            }
            CounterRow(
                label = stringResource(R.string.hajj_tawaf),
                counter = tawaf,
                onChanged = onTawafChanged,
            )
            CounterRow(
                label = stringResource(R.string.hajj_sai),
                counter = sai,
                onChanged = onSaiChanged,
            )
        }
    }
}

@Composable
private fun CounterRow(
    label: String,
    counter: RitualCounter,
    onChanged: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            IconButton(
                onClick = { onChanged(counter.decrement().completed) },
                enabled = counter.completed > 0,
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = stringResource(R.string.hajj_counter_minus),
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                stringResource(R.string.hajj_counter_progress, counter.completed, counter.target),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = { onChanged(counter.increment().completed) },
                enabled = !counter.isComplete,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.hajj_counter_plus),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        LinearProgressIndicator(
            progress = { counter.completed.toFloat() / counter.target },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
