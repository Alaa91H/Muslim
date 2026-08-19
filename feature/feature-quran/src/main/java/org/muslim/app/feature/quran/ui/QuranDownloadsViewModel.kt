package org.muslim.app.feature.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.DownloadRequest
import org.muslim.app.feature.quran.data.DownloadScope
import org.muslim.app.feature.quran.data.DownloadStatus
import org.muslim.app.feature.quran.data.DownloadTaskUi
import org.muslim.app.feature.quran.data.QuranDownloadManager
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.RecitationRepository
import org.muslim.app.feature.quran.data.ReciterDownloadState
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuranDownloadsViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
    private val manager: QuranDownloadManager,
    private val recitationRepository: RecitationRepository,
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

    /**
     * Actual size verified against the server via a ranged HEAD-style probe.
     * For an ayah this is the exact file size; for a surah/full Quran it is a
     * real per-ayah byte count multiplied by the number of ayahs (replacing
     * the bitrate-based estimate). Null when unresolved or offline.
     */
    val verifiedBytes: StateFlow<Long?> = combine(
        selectedReciter, _scope, _surahInput, _ayahInput, surahs,
    ) { reciter, scope, surahText, ayahText, surahs ->
        sizeProbe(reciter, scope, surahText.toIntOrNull(), ayahText.toIntOrNull(), surahs)
    }.flatMapLatest { probe ->
        flow {
            if (probe == null) {
                emit(null)
            } else {
                val oneAyah = recitationRepository.verifiedAyahSize(probe.reciter, probe.surah, probe.ayah)
                emit(oneAyah?.let { it * probe.multiplier })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setScope(scope: DownloadScope) { _scope.value = scope }

    fun setSurahInput(value: String) { _surahInput.value = value.filter { it.isDigit() }.take(3) }

    fun setAyahInput(value: String) { _ayahInput.value = value.filter { it.isDigit() }.take(3) }

    fun selectReciter(id: String) = viewModelScope.launch {
        prefsRepository.setSelectedReciterId(id)
        refreshReciterState()
    }

    fun startDownload() {
        val reciter = selectedReciter.value
        val surahNumber = _surahInput.value.toIntOrNull()
        val ayahNumber = _ayahInput.value.toIntOrNull()
        val scopeValue = _scope.value
        // Prefer the server-verified size; fall back to the estimate.
        val resolvedBytes = verifiedBytes.value ?: estimateBytes.value

        viewModelScope.launch {
            when (scopeValue) {
                DownloadScope.Surah -> {
                    if (surahNumber == null || surahNumber !in 1..114) return@launch
                    val meta = surahs.value.firstOrNull { it.number == surahNumber } ?: return@launch
                    val total = resolvedBytes ?: reciter.estimatedBytesPerAyah() * meta.ayahCount
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
                            nightOnly = nightOnly.value,
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
                            totalBytes = resolvedBytes ?: reciter.estimatedBytesPerAyah(),
                            nightOnly = nightOnly.value,
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
                            totalBytes = resolvedBytes ?: reciter.estimatedBytesPerAyah() * TOTAL_AYAHS,
                            nightOnly = nightOnly.value,
                        )
                    )
                }
            }
        }
    }

    fun cancel(id: String) = manager.cancel(id)

    fun pause(id: String) = manager.pause(id)

    fun resume(id: String) = manager.resume(id)

    init {
        // Refresh the per-reciter scan whenever a task finishes (or starts),
        // so the "downloaded" section reflects completed transfers.
        viewModelScope.launch {
            manager.tasks.collect { list ->
                if (list.any { it.status == DownloadStatus.Completed || it.status == DownloadStatus.Failed }) {
                    refreshReciterState()
                }
            }
        }
    }

    // --- Night-only downloads (التحميل الليلي) ---

    val nightOnly: StateFlow<Boolean> = prefsRepository.nightDownloadsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val nightWindowStart: StateFlow<Int> = prefsRepository.nightDownloadStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranPrefsRepository.DEFAULT_NIGHT_START)

    val nightWindowEnd: StateFlow<Int> = prefsRepository.nightDownloadEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranPrefsRepository.DEFAULT_NIGHT_END)

    fun setNightOnly(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setNightDownloadsEnabled(enabled)
    }

    fun setNightWindowStart(minutes: Int) = viewModelScope.launch {
        prefsRepository.setNightDownloadStart(minutes.coerceIn(0, 23 * 60 + 59))
    }

    fun setNightWindowEnd(minutes: Int) = viewModelScope.launch {
        prefsRepository.setNightDownloadEnd(minutes.coerceIn(0, 23 * 60 + 59))
    }

    // --- Per-reciter downloaded state (what is already on disk) ---

    private val _refreshTrigger = MutableStateFlow(0)

    /** Scans disk whenever the selected reciter changes (reactive). */
    val reciterState: StateFlow<ReciterDownloadState?> = combine(
        selectedReciterId, _refreshTrigger,
    ) { id, _ -> id }
        .flatMapLatest { id ->
            flow { emit(recitationRepository.downloadState(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Deletes one downloaded surah for the selected reciter. */
    fun deleteSurah(surahNumber: Int) = viewModelScope.launch {
        val reciter = selectedReciter.value
        recitationRepository.deleteSurah(reciter.id, surahNumber)
        refreshReciterState()
    }

    /** Deletes every downloaded surah for the selected reciter. */
    fun deleteReciter() = viewModelScope.launch {
        val reciter = selectedReciter.value
        recitationRepository.deleteReciter(reciter.id)
        refreshReciterState()
    }

    /** Force a rescan (after a delete or a finished download). */
    fun refreshReciterState() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    /**
     * Totals across every reciter: how much audio is downloaded on disk.
     * Re-scanned whenever a download finishes/fails or a delete happens
     * (the same trigger that refreshes the per-reciter state).
     */
    val totalSummary: StateFlow<TotalDownloadSummary> = _refreshTrigger
        .flatMapLatest { _ ->
            flow { emit(summarizeAllReciters()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TotalDownloadSummary(0, 0, 0L))

    private suspend fun summarizeAllReciters(): TotalDownloadSummary {
        var ayahs = 0L
        val surahs = mutableSetOf<Int>()
        var bytes = 0L
        for (reciter in Reciter.Bundled) {
            val state = recitationRepository.downloadState(reciter.id)
            ayahs += state.downloadedAyahs
            surahs += state.surahCounts.keys
            bytes += state.totalBytes
        }
        return TotalDownloadSummary(ayahs, surahs.size, bytes)
    }

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

    private data class SizeProbe(val reciter: Reciter, val surah: Int, val ayah: Int, val multiplier: Long)

    private fun sizeProbe(
        reciter: Reciter,
        scope: DownloadScope,
        surahNumber: Int?,
        ayahNumber: Int?,
        surahs: List<Surah>,
    ): SizeProbe? = when (scope) {
        DownloadScope.Ayah ->
            if (surahNumber != null && surahNumber in 1..114 && ayahNumber != null) {
                SizeProbe(reciter, surahNumber, ayahNumber, 1L)
            } else null
        DownloadScope.Surah -> surahs.firstOrNull { it.number == surahNumber }
            ?.let { SizeProbe(reciter, it.number, 1, it.ayahCount.toLong()) }
        DownloadScope.FullQuran -> SizeProbe(reciter, 1, 1, TOTAL_AYAHS)
    }

    private companion object {
        const val TOTAL_AYAHS = 6236L
    }
}

/** How much recitation audio is downloaded across all reciters, at a glance. */
data class TotalDownloadSummary(
    val downloadedAyahs: Long,
    /** Distinct surahs covered by at least one reciter (union). */
    val downloadedSurahs: Int,
    val totalBytes: Long,
)
