package org.muslim.app.feature.quran.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranRepository

/** Read-only complete Mushaf snapshot and localized chapter headings for book pagination. */
internal class QuranReaderMushaf(repository: QuranRepository, scope: CoroutineScope) {
    val ayahs: StateFlow<List<Ayah>> = flow { emit(repository.allAyahs()) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val surahNames: StateFlow<Map<Int, String>> = repository.observeSurahs()
        .map { surahs -> surahs.associate { it.number to it.arabicName } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}
