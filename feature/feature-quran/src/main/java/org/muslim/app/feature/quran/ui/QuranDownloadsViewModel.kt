package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.DownloadRequest
import org.muslim.app.feature.quran.data.DownloadScope
import org.muslim.app.feature.quran.data.DownloadStatus
import org.muslim.app.feature.quran.data.DownloadTaskUi
import org.muslim.app.feature.quran.data.QuranDownloadManager
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import javax.inject.Inject

@HiltViewModel
class QuranDownloadsViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
    private val manager: QuranDownloadManager,
) : ViewModel() {

    val reciters: List<Reciter> = Reciter.Bundled

    val selectedReciterId: StateFlow<String> = prefsRepository.selectedReciterId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Reciter.Bundled.first().id)

    val selectedReciter: StateFlow<Reciter> = selectedReciterId
        .combine(MutableStateFlow(Unit)) { id, _ ->
            reciters.firstOrNull { it.id == id } ?: Reciter.Bundled.first()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Reciter.Bundled.first())

    val surahs: StateFlow<List<Surah>> = repository.observeSurahs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<DownloadTaskUi>> = manager.tasks

    private val _scope = MutableStateFlow(DownloadScope.Surah)
    val scope: StateFlow<DownloadScope> = _scope.asStateFlow()

    private val _surahInput = MutableStateFlow("")
    val surahInput: StateFlow<String> = _surahInput.asStateFlow()

    private val _ayahInput = MutableStateFlow("")
    val ayahInput: StateFlow<String> = _ayahInput.asStateFlow()

    val activeCount: StateFlow<Int> = manager.tasks
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.count { it.status == DownloadStatus.Queued || it.status == DownloadStatus.Downloading }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Approximate size of the currently selected scope, or null if invalid. */
    val estimateBytes: StateFlow<Long?> = combine(
        selectedReciter, _scope, _surahInput, _ayahInput, surahs,
    ) { reciter, scope, surahText, ayahText, surahs ->
        estimate(reciter, scope, surahText.toIntOrNull(), ayahText.toIntOrNull(), surahs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setScope(scope: DownloadScope) { _scope.value = scope }

    fun setSurahInput(value: String) { _surahInput.value = value.filter { it.isDigit() }.take(3) }

    fun setAyahInput(value: String) { _ayahInput.value = value.filter { it.isDigit() }.take(3) }

    fun selectReciter(id: String) = viewModelScope.launch { prefsRepository.setSelectedReciterId(id) }

    fun startDownload() {
        val reciter = selectedReciter.value
        val surahNumber = _surahInput.value.toIntOrNull()
        val ayahNumber = _ayahInput.value.toIntOrNull()
        val scopeValue = _scope.value

        viewModelScope.launch {
            when (scopeValue) {
                DownloadScope.Surah -> {
                    if (surahNumber == null || surahNumber !in 1..114) return@launch
                    val meta = surahs.value.firstOrNull { it.number == surahNumber } ?: return@launch
                    val total = reciter.estimatedBytesPerAyah() * meta.ayahCount
                    manager.enqueue(
                        DownloadRequest(
                            id = "surah-$surahNumber-${reciter.id}-${System.currentTimeMillis()}",
                            reciterId = reciter.id,
                            reciterName = reciter.name,
                            scope = scopeValue,
                            surahNumber = surahNumber,
                            globalNumber = null,
                            label = "سورة ${meta.arabicName}",
                            totalBytes = total,
                        )
                    )
                }
                DownloadScope.Ayah -> {
                    if (surahNumber == null || surahNumber !in 1..114 || ayahNumber == null) return@launch
                    val ayahs = repository.observeSurah(surahNumber).first()
                    val ayah = ayahs.firstOrNull { it.numberInSurah == ayahNumber } ?: return@launch
                    manager.enqueue(
                        DownloadRequest(
                            id = "ayah-${ayah.globalNumber}-${reciter.id}-${System.currentTimeMillis()}",
                            reciterId = reciter.id,
                            reciterName = reciter.name,
                            scope = scopeValue,
                            surahNumber = surahNumber,
                            globalNumber = ayah.globalNumber,
                            label = "الآية $surahNumber:$ayahNumber",
                            totalBytes = reciter.estimatedBytesPerAyah(),
                        )
                    )
                }
                DownloadScope.FullQuran -> {
                    manager.enqueue(
                        DownloadRequest(
                            id = "full-${reciter.id}-${System.currentTimeMillis()}",
                            reciterId = reciter.id,
                            reciterName = reciter.name,
                            scope = scopeValue,
                            surahNumber = null,
                            globalNumber = null,
                            label = "القرآن الكريم كاملًا",
                            totalBytes = reciter.estimatedBytesPerAyah() * TOTAL_AYAHS,
                        )
                    )
                }
            }
        }
    }

    fun cancel(id: String) = manager.cancel(id)

    private fun estimate(
        reciter: Reciter,
        scope: DownloadScope,
        surahNumber: Int?,
        ayahNumber: Int?,
        surahs: List<Surah>,
    ): Long? = when (scope) {
        DownloadScope.Ayah ->
            if (surahNumber != null && surahNumber in 1..114 && ayahNumber != null) {
                reciter.estimatedBytesPerAyah()
            } else null
        DownloadScope.Surah -> surahs.firstOrNull { it.number == surahNumber }
            ?.let { reciter.estimatedBytesPerAyah() * it.ayahCount }
        DownloadScope.FullQuran -> reciter.estimatedBytesPerAyah() * TOTAL_AYAHS
    }

    private companion object {
        const val TOTAL_AYAHS = 6236L
    }
}
