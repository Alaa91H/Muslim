package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.MuslimExpandableSection
import org.muslim.app.core.ui.theme.MuslimSegmentedControl
import org.muslim.app.core.ui.theme.MuslimSettingsItem
import org.muslim.app.core.ui.theme.MuslimSection
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.DownloadScope

private val nightTimeOptions: List<Int> = (0 until 24 * 60 step 30).toList()
private val downloadScopes = listOf(
    DownloadScope.Ayah,
    DownloadScope.Surah,
    DownloadScope.FullQuran,
)

internal data class DownloadConfigurationState(
    val scope: DownloadScope,
    val surahInput: String,
    val ayahInput: String,
    val verifiedBytes: Long?,
    val estimateBytes: Long?,
    val nightOnly: Boolean,
    val nightWindowStart: Int,
    val nightWindowEnd: Int,
)

internal data class DownloadConfigurationActions(
    val onScopeChanged: (DownloadScope) -> Unit,
    val onSurahInputChanged: (String) -> Unit,
    val onAyahInputChanged: (String) -> Unit,
    val onNightOnlyChanged: (Boolean) -> Unit,
    val onNightWindowStartChanged: (Int) -> Unit,
    val onNightWindowEndChanged: (Int) -> Unit,
    val onStartDownload: () -> Unit,
)

@Composable
internal fun DownloadConfigurationPanel(
    state: DownloadConfigurationState,
    actions: DownloadConfigurationActions,
) {
    MuslimSection(
        title = stringResource(R.string.quran_download_new_title),
    ) {
        MuslimSegmentedControl(
            options = listOf(
                stringResource(R.string.quran_download_scope_ayah),
                stringResource(R.string.quran_download_scope_surah),
                stringResource(R.string.quran_download_scope_full),
            ),
            selectedIndex = downloadScopes.indexOf(state.scope).coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                downloadScopes.getOrNull(index)?.let(actions.onScopeChanged)
            },
        )

        DownloadTargetFields(
            state = state,
            actions = actions,
        )

        DownloadSizeSummary(
            verifiedBytes = state.verifiedBytes,
            estimateBytes = state.estimateBytes,
        )

        NightDownloadSettings(
            state = state,
            actions = actions,
        )

        IslamicPrimaryButton(
            onClick = actions.onStartDownload,
            enabled = state.estimateBytes != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.quran_download_start))
        }
    }
}

@Composable
private fun DownloadTargetFields(
    state: DownloadConfigurationState,
    actions: DownloadConfigurationActions,
) {
    when (state.scope) {
        DownloadScope.Ayah -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
            ) {
                DownloadNumberField(
                    value = state.surahInput,
                    onValueChange = actions.onSurahInputChanged,
                    label = stringResource(R.string.quran_download_surah_number),
                    modifier = Modifier.weight(1f),
                )
                DownloadNumberField(
                    value = state.ayahInput,
                    onValueChange = actions.onAyahInputChanged,
                    label = stringResource(R.string.quran_download_ayah_number),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        DownloadScope.Surah -> DownloadNumberField(
            value = state.surahInput,
            onValueChange = actions.onSurahInputChanged,
            label = stringResource(R.string.quran_download_surah_number),
            modifier = Modifier.fillMaxWidth(),
        )
        DownloadScope.FullQuran -> Text(
            text = stringResource(R.string.quran_download_full_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DownloadNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun DownloadSizeSummary(
    verifiedBytes: Long?,
    estimateBytes: Long?,
) {
    when {
        verifiedBytes != null -> Text(
            text = stringResource(
                R.string.quran_download_size_verified,
                formatBytes(verifiedBytes),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        estimateBytes != null -> Text(
            text = stringResource(
                R.string.quran_download_size_estimate,
                formatBytes(estimateBytes),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        else -> Text(
            text = stringResource(R.string.quran_download_size_unknown),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NightDownloadSettings(
    state: DownloadConfigurationState,
    actions: DownloadConfigurationActions,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    MuslimExpandableSection(
        title = stringResource(R.string.quran_download_night_only),
        supportingText = stringResource(
            R.string.quran_download_night_hint,
            formatWindow(state.nightWindowStart, state.nightWindowEnd),
        ),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        MuslimSettingsItem(
            title = stringResource(R.string.quran_download_night_only),
            onClick = { actions.onNightOnlyChanged(!state.nightOnly) },
            trailing = {
                Switch(
                    checked = state.nightOnly,
                    onCheckedChange = actions.onNightOnlyChanged,
                )
            },
        )

        if (state.nightOnly) {
            Row {
                TimeDropdown(
                    label = stringResource(R.string.quran_download_night_start),
                    selectedMinutes = state.nightWindowStart,
                    options = nightTimeOptions,
                    onSelected = actions.onNightWindowStartChanged,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(IslamicSpacing.Compact))
                TimeDropdown(
                    label = stringResource(R.string.quran_download_night_end),
                    selectedMinutes = state.nightWindowEnd,
                    options = nightTimeOptions,
                    onSelected = actions.onNightWindowEndChanged,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDropdown(
    label: String,
    selectedMinutes: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = formatMinutes(selectedMinutes),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(formatMinutes(minutes)) },
                    onClick = {
                        expanded = false
                        onSelected(minutes)
                    },
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String =
    String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

private fun formatWindow(startMinutes: Int, endMinutes: Int): String =
    "${formatMinutes(startMinutes)} – ${formatMinutes(endMinutes)}"
