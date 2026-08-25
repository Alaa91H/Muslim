package org.muslim.app.feature.quran.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.AyahOfTheDayScheduler
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.QuranSupplementRepository
import org.muslim.app.feature.quran.domain.LastRead
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Surah
import javax.inject.Inject

@HiltViewModel
class SurahListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    repository: QuranRepository,
    prefsRepository: QuranPrefsRepository,
    supplementRepository: QuranSupplementRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val surahs: List<Surah> = emptyList(),
        val lastRead: LastRead? = null,
        val readThroughGlobal: Int = 0,
        val totalAyahs: Int = 6236,
    ) {
        val progressFraction: Float
            get() = if (totalAyahs == 0) 0f else (readThroughGlobal.toFloat() / totalAyahs).coerceIn(0f, 1f)
    }

    val uiState: StateFlow<UiState> = combine(
        repository.observeSurahs().onStart { emit(emptyList()) },
        prefsRepository.lastRead,
        prefsRepository.readThroughGlobal,
    ) { surahs, lastRead, readThrough ->
        UiState(
            loading = surahs.isEmpty(),
            surahs = surahs,
            lastRead = lastRead,
            readThroughGlobal = readThrough,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        // Schedule the optional daily ayah notification (idempotent) and
        // remove the old placeholder tafsir on upgrades.
        viewModelScope.launch {
            AyahOfTheDayScheduler.schedule(context)
            supplementRepository.removeLegacySampleTafsir()
        }
    }
}
