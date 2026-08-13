package org.example.islamicapp.feature.prayertimes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.prayertimes.R
import org.example.islamicapp.feature.prayertimes.domain.Prayer
import org.example.islamicapp.feature.prayertimes.ui.formatCountdown
import org.example.islamicapp.feature.prayertimes.ui.localDateFormatter
import org.example.islamicapp.feature.prayertimes.ui.localTimeFormatter
import org.example.islamicapp.feature.prayertimes.ui.prayerLabelRes

/**
 * Main screen: Hijri/Gregorian date, live next-prayer countdown and today's
 * prayer times (PROJECT_PROMPT.md §6 Phase 1).
 */
@Composable
fun HomeScreen(
    onSelectLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // ---- Date header ----
        state.hijri?.let { hijri ->
            Text(
                text = hijri.formatArabicLong(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.hijri?.gregorian?.format(localDateFormatter) ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Surface(
                onClick = onSelectLocation,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(Modifier.padding(start = 4.dp))
                    Text(
                        text = if (state.hasLocation) state.locationName
                        else stringResource(R.string.home_select_location),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!state.hasLocation) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_location_unknown),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Column
        }

        // ---- Next prayer + countdown ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.home_next_prayer),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                state.nextPrayer?.let { prayer ->
                    Text(
                        text = stringResource(prayerLabelRes(prayer)),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    state.nextPrayerAt?.let { at ->
                        Text(
                            text = at.format(localTimeFormatter),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatCountdown(state.countdownSeconds),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Today's times ----
        Text(
            text = stringResource(R.string.home_today_times),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(8.dp))

        if (!state.isValid) {
            Text(
                text = stringResource(R.string.home_cannot_compute),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Prayer.entries.forEachIndexed { index, prayer ->
                    if (index > 0) HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(prayerLabelRes(prayer)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (prayer == state.nextPrayer) FontWeight.Bold else FontWeight.Normal,
                        )
                        Spacer(Modifier.weight(1f))
                        state.times[prayer]?.let { time ->
                            Text(
                                text = time.format(localTimeFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (prayer == state.nextPrayer) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
