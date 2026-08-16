package org.example.islamicapp.feature.quran.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.quran.data.QuranPrefsRepository
import org.example.islamicapp.feature.quran.data.RecitationRepository
import org.example.islamicapp.feature.quran.domain.Ayah
import org.example.islamicapp.feature.quran.domain.LastRead
import org.example.islamicapp.feature.quran.domain.QuranRepository
import org.example.islamicapp.feature.quran.domain.Reciter
import org.example.islamicapp.feature.quran.domain.RepeatPlan
import org.example.islamicapp.feature.quran.domain.Surah
import javax.inject.Inject

@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
    private val recitationRepository: RecitationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val surahNumber: Int = savedStateHandle["surahNumber"] ?: 1

    /** Global ayah number to scroll to (search/bookmarks/last-read), -1 = none. */
    val initialAyahGlobal: Int = savedStateHandle["ayah"] ?: -1

    data class UiState(
        val loading: Boolean = true,
        val surah: Surah? = null,
        val ayahs: List<Ayah> = emptyList(),
    )

    /** The ayah currently in view; the UI updates this as the user scrolls. */
    val currentAyah = MutableStateFlow<Ayah?>(null)

    val uiState: StateFlow<UiState> = combine(
        repository.observeSurahMetadata(surahNumber),
        repository.observeSurah(surahNumber),
    ) { surah, ayahs ->
        UiState(loading = false, surah = surah, ayahs = ayahs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    val isBookmarked: StateFlow<Boolean> = combine(
        currentAyah,
        repository.observeBookmarks(),
    ) { ayah, bookmarks ->
        ayah != null && bookmarks.any { it.ayah.globalNumber == ayah.globalNumber }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleBookmark() {
        val ayah = currentAyah.value ?: return
        viewModelScope.launch {
            if (repository.isBookmarked(ayah.globalNumber)) {
                repository.removeBookmark(ayah.globalNumber)
            } else {
                repository.addBookmark(ayah)
            }
        }
    }

    /** Persists the currently viewed ayah as the resume position. */
    fun saveLastRead() {
        val ayah = currentAyah.value ?: return
        viewModelScope.launch {
            prefsRepository.saveLastRead(
                LastRead(
                    surahNumber = ayah.surahNumber,
                    globalNumber = ayah.globalNumber,
                    numberInSurah = ayah.numberInSurah,
                )
            )
        }
    }

    // ---- Repeat (memorization) mode ---------------------------------------

    /** User-configured repeat settings (range is ayah global-numbers). */
    data class RepeatConfig(
        val rangeStart: Int = -1,
        val rangeEnd: Int = -1,
        val count: Int = 3,
        val pacingSeconds: Int = 6,
    )

    /** Live playback state of the repeat session. */
    data class RepeatSession(
        val panelOpen: Boolean = false,
        val playing: Boolean = false,
        /** The ayah currently highlighted while repeating, if any. */
        val currentGlobal: Int? = null,
        val completedPasses: Int = 0,
        val finished: Boolean = false,
    )

    data class RepeatState(
        val config: RepeatConfig = RepeatConfig(),
        val session: RepeatSession = RepeatSession(),
    )

    private val repeatConfig = MutableStateFlow(RepeatConfig())
    private val repeatSession = MutableStateFlow(RepeatSession())

    val repeatState: StateFlow<RepeatState> =
        combine(repeatConfig, repeatSession) { config, session ->
            RepeatState(config, session)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RepeatState())

    /** Emits the next ayah (global number) the reader should scroll to. */
    val scrollRequests = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    private var repeatJob: Job? = null

    fun toggleRepeatPanel() {
        repeatSession.update { it.copy(panelOpen = !it.panelOpen) }
    }

    fun closeRepeatPanel() {
        repeatSession.update { it.copy(panelOpen = false) }
    }

    /** Sets the range start to the currently visible ayah. */
    fun setRangeStart() {
        val ayah = currentAyah.value ?: return
        repeatConfig.update { config ->
            config.copy(
                rangeStart = ayah.globalNumber,
                rangeEnd = if (config.rangeEnd < ayah.globalNumber) ayah.globalNumber else config.rangeEnd,
            )
        }
    }

    /** Sets the range end to the currently visible ayah. */
    fun setRangeEnd() {
        val ayah = currentAyah.value ?: return
        repeatConfig.update { config ->
            config.copy(
                rangeStart = if (config.rangeStart <= 0 || config.rangeStart > ayah.globalNumber) {
                    ayah.globalNumber
                } else {
                    config.rangeStart
                },
                rangeEnd = ayah.globalNumber,
            )
        }
    }

    /** Steps the repeat count by [delta] (bounded 1..30). */
    fun stepCount(delta: Int) {
        repeatConfig.update { it.copy(count = (it.count + delta).coerceIn(1, 30)) }
    }

    /** Steps the per-ayah pacing by [delta] seconds (bounded 3..15). */
    fun stepPacing(delta: Int) {
        repeatConfig.update { it.copy(pacingSeconds = (it.pacingSeconds + delta).coerceIn(3, 15)) }
    }

    /** Starts (or resumes after completion) the repeat playback. */
    fun play() {
        val config = repeatConfig.value
        if (config.rangeStart <= 0 || config.rangeEnd < config.rangeStart) return
        if (repeatJob?.isActive == true) return
        // A finished session restarts from pass one.
        if (repeatSession.value.finished) {
            repeatSession.update { it.copy(finished = false, completedPasses = 0) }
        }
        repeatJob = viewModelScope.launch {
            repeatSession.update { it.copy(playing = true, finished = false, completedPasses = 0) }
            for (pass in 1..config.count) {
                for (global in config.rangeStart..config.rangeEnd) {
                    repeatSession.update {
                        it.copy(currentGlobal = global, completedPasses = pass - 1)
                    }
                    scrollRequests.emit(global)
                    delay(config.pacingSeconds * 1_000L)
                }
            }
            repeatSession.update {
                it.copy(playing = false, finished = true, currentGlobal = null, completedPasses = config.count)
            }
        }
    }

    fun pause() {
        repeatJob?.cancel()
        repeatJob = null
        repeatSession.update { it.copy(playing = false) }
    }

    /** Cancels playback and clears the session progress (range/count kept). */
    fun resetRepeat() {
        repeatJob?.cancel()
        repeatJob = null
        repeatSession.update {
            it.copy(playing = false, currentGlobal = null, completedPasses = 0, finished = false)
        }
    }

    // ---- Recitation (audio) mode ------------------------------------------

    data class RecitationUiState(
        val reciter: Reciter = Reciter.Alafasy,
        val panelOpen: Boolean = false,
        val status: RecitationRepository.RecitationStatus = RecitationRepository.RecitationStatus(),
        /** Index into the surah's ayahs currently being recited; -1 when idle. */
        val playingIndex: Int = -1,
        val isPlaying: Boolean = false,
        val error: String? = null,
    )

    private val selectedReciter = MutableStateFlow(Reciter.Alafasy)
    private val recitationPanelOpen = MutableStateFlow(false)

    private val recitationPlayer = RecitationPlayer(context)

    private data class ReciterSelection(
        val reciter: Reciter,
        val panelOpen: Boolean,
        val statuses: Map<String, RecitationRepository.RecitationStatus>,
    )

    private data class PlaybackSnapshot(
        val isPlaying: Boolean,
        val index: Int,
        val error: String?,
    )

    val recitationState: StateFlow<RecitationUiState> = combine(
        combine(selectedReciter, recitationPanelOpen, recitationRepository.statuses) { reciter, open, statuses ->
            ReciterSelection(reciter, open, statuses)
        },
        combine(recitationPlayer.isPlaying, recitationPlayer.currentIndex, recitationPlayer.error) { playing, index, error ->
            PlaybackSnapshot(playing, index, error)
        },
    ) { selection, playback ->
        RecitationUiState(
            reciter = selection.reciter,
            panelOpen = selection.panelOpen,
            status = selection.statuses[recitationRepository.statusKey(selection.reciter, surahNumber)]
                ?: RecitationRepository.RecitationStatus(),
            playingIndex = if (playback.isPlaying) playback.index else -1,
            isPlaying = playback.isPlaying,
            error = playback.error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecitationUiState())

    init {
        // Keep the reader scrolled to the ayah being recited.
        viewModelScope.launch {
            recitationPlayer.currentIndex.collect { index ->
                val ayah = uiState.value.ayahs.getOrNull(index) ?: return@collect
                scrollRequests.emit(ayah.globalNumber)
            }
        }
    }

    fun toggleRecitationPanel() {
        recitationPanelOpen.update { !it }
    }

    fun closeRecitationPanel() {
        recitationPanelOpen.update { false }
    }

    /** Switches the reciter; playback stops (the new reciter may not be downloaded). */
    fun selectReciter(reciter: Reciter) {
        if (reciter == selectedReciter.value) return
        recitationPlayer.stop()
        selectedReciter.value = reciter
    }

    /** True when the current surah is fully downloaded for the selected reciter. */
    fun isCurrentSurahDownloaded(): Boolean {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty()) return false
        return recitationRepository.isSurahDownloaded(selectedReciter.value, ayahs.map { it.globalNumber })
    }

    /** Starts downloading the current surah for the selected reciter. */
    fun downloadRecitation() {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty()) return
        if (recitationState.value.status.state == RecitationRepository.DownloadState.Downloading) return
        recitationRepository.download(
            reciter = selectedReciter.value,
            surahNumber = surahNumber,
            ayahGlobals = ayahs.map { it.globalNumber },
        )
    }

    /** Deletes the downloaded surah for the selected reciter and stops playback. */
    fun deleteRecitation() {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty()) return
        recitationPlayer.stop()
        recitationRepository.deleteSurah(selectedReciter.value, surahNumber, ayahs.map { it.globalNumber })
    }

    /** Plays the downloaded surah from the currently visible ayah, or toggles. */
    fun togglePlayback() {
        val state = recitationState.value
        if (state.isPlaying) {
            recitationPlayer.pause()
        } else if (recitationPlayer.hasContent) {
            recitationPlayer.resume()
        } else {
            val ayahs = uiState.value.ayahs
            if (ayahs.isEmpty()) return
            val files = ayahs.map { recitationRepository.audioFile(state.reciter, it.globalNumber) }
            if (files.any { !it.isFile || it.length() == 0L }) return // not downloaded
            val startIndex = currentAyah.value?.let { visible ->
                ayahs.indexOfFirst { it.globalNumber == visible.globalNumber }.takeIf { it >= 0 }
            } ?: 0
            recitationPlayer.play(files, startIndex)
        }
    }

    /** Stops playback and closes the panel. */
    fun stopPlayback() {
        recitationPlayer.stop()
    }

    override fun onCleared() {
        recitationPlayer.release()
        super.onCleared()
    }
}
