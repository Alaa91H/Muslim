package org.muslim.app.feature.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.data.OfficialQuranTextKind
import org.muslim.app.feature.quran.data.OfficialQuranTextSource
import org.muslim.app.feature.quran.data.QuranPrefsRepository

@Composable
internal fun SupplementTafsirSources(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    InstalledTafsirSources(state, actions)
    OfficialTextCatalogue(state, actions)
    state.tafsirDownloadState.error?.let { error ->
        Text(
            stringResource(R.string.quran_tafsir_download_failed, error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    Text(
        stringResource(R.string.quran_tafsir_attribution),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun InstalledTafsirSources(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    Text(
        stringResource(R.string.quran_tafsir_sources_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(6.dp))
    if (state.installedTafsirSources.isEmpty()) {
        Text(
            stringResource(R.string.quran_tafsir_sources_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        state.installedTafsirSources.forEach { source ->
            TafsirSourceChoice(
                source = source,
                selected = state.selectedTafsirSource == source,
                onSelected = { actions.onTafsirSourceSelected(source) },
            )
        }
    }
}

@Composable
private fun OfficialTextCatalogue(
    state: SupplementControlsState,
    actions: SupplementControlsActions,
) {
    Text(
        stringResource(R.string.quran_text_catalog_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { actions.onRefreshOfficialTextSources(state.language) }) {
            Text(stringResource(R.string.quran_text_catalog_refresh))
        }
        Text(stringResource(R.string.quran_text_catalog_source))
    }
    val currentLanguage = LocalConfiguration.current.locales[0].language
    val selectedLanguage = if (state.language.equals(QuranPrefsRepository.AUTO_LANGUAGE, ignoreCase = true)) {
        currentLanguage
    } else {
        state.language.substringBefore('-')
    }
    val sources = state.officialTextSources.filter { it.languageTag.equals(selectedLanguage, ignoreCase = true) }
    if (sources.isEmpty()) {
        Text(
            stringResource(R.string.quran_text_catalog_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    listOf(
        R.string.quran_meaning_catalog_title to sources.filter { it.kind == OfficialQuranTextKind.Meaning },
        R.string.quran_tafsir_translation_catalog_title to
            sources.filter { it.kind == OfficialQuranTextKind.TranslatedTafsir },
        R.string.quran_original_tafsir_catalog_title to
            sources.filter { it.kind == OfficialQuranTextKind.OriginalTafsir },
    ).forEach { (titleId, groupedSources) ->
        if (groupedSources.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(titleId), style = MaterialTheme.typography.labelMedium)
            groupedSources.forEach { source ->
                OfficialQuranTextDownloadRow(source, state, actions.onDownloadOfficialText)
            }
        }
    }
}

@Composable
internal fun TafsirSourceChoice(source: String, selected: Boolean, onSelected: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
            .clickable(onClick = onSelected).padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(source, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun OfficialQuranTextDownloadRow(
    source: OfficialQuranTextSource,
    state: SupplementControlsState,
    onDownload: (OfficialQuranTextSource) -> Unit,
) {
    val installed = state.installedPacks.any { it.id == source.storageKey }
    val downloading = state.tafsirDownloadState.downloading == source
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(source.title, style = MaterialTheme.typography.bodySmall)
            Text(
                "${source.languageTag} · ${source.translator}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${source.sourceAttribution}\n${source.sourceUrl}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(
            enabled = source.canDownload && !installed && !downloading &&
                state.tafsirDownloadState.downloading == null,
            onClick = { onDownload(source) },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    when {
                        downloading -> stringResource(R.string.quran_tafsir_downloading)
                        installed -> stringResource(R.string.quran_tafsir_installed)
                        !source.canDownload -> stringResource(R.string.quran_text_catalog_review_missing)
                        else -> stringResource(R.string.quran_tafsir_download)
                    },
                )
                if (downloading) {
                    Text(
                        stringResource(
                            R.string.quran_tafsir_download_progress,
                            state.tafsirDownloadState.completedSurahs,
                            114,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
