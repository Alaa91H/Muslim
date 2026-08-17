package org.muslim.app.feature.quran.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.QuranSupplementRepository
import org.muslim.app.feature.quran.data.RecitationQueueItem
import org.muslim.app.feature.quran.data.RecitationRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.LastRead
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.ReaderTheme
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import javax.inject.Inject

/** Playback range selected in the reader's recitation controls. */
enum class RecitationRange { SingleAyah, FromAyahToEnd }

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsRepository: QuranPrefsRepository,
    private val supplementRepository: QuranSupplementRepository,
    private val recitationRepository: RecitationRepository,
    private val audioPlayer: QuranAudioPlayer,
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

    // --- Reader comfort (Phase C1/C2) ---

    val readerTheme: StateFlow<ReaderTheme> = prefsRepository.readerTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderTheme.Light)

    val readerFontSize: StateFlow<Float> = prefsRepository.readerFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 26f)

    /** Highest global ayah read so far (khatma progress). */
    val readThroughGlobal: StateFlow<Int> = prefsRepository.readThroughGlobal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setReaderTheme(theme: ReaderTheme) = viewModelScope.launch {
        prefsRepository.setReaderTheme(theme)
    }

    fun setReaderFontSize(sp: Float) = viewModelScope.launch {
        prefsRepository.setReaderFontSize(sp)
    }

    /** Called as the user scrolls; advances the khatma progress monotonically. */
    fun advanceReadThrough(globalNumber: Int) = viewModelScope.launch {
        prefsRepository.advanceReadThrough(globalNumber)
    }

    // --- Meaning + tafsir (Phase C3/C4) ---

    /** Translations + tafsir of the currently viewed ayah, when installed. */
    val supplements: StateFlow<SupplementUi> = currentAyah
        .flatMapLatest { ayah ->
            if (ayah == null) {
                kotlinx.coroutines.flow.flowOf(SupplementUi())
            } else {
                combine(
                    supplementRepository.observeTranslations(ayah.globalNumber),
                    supplementRepository.observeTafsir(ayah.globalNumber),
                ) { translations, tafsir ->
                    SupplementUi(translations = translations, tafsir = tafsir)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupplementUi())

    data class SupplementUi(
        val translations: List<Translation> = emptyList(),
        val tafsir: List<TafsirEntry> = emptyList(),
    )

    // --- Recitation (Phase C5/C7) ---

    private val _downloaded = MutableStateFlow(false)
    val downloaded: StateFlow<Boolean> = _downloaded

    val selectedReciter: StateFlow<Reciter> = prefsRepository.selectedReciterId
        .map { id -> Reciter.Bundled.firstOrNull { it.id == id } ?: Reciter.Bundled.first() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Reciter.Bundled.first())

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private val _downloading = MutableStateFlow(false)
    val downloading: StateFlow<Boolean> = _downloading

    val playbackState = audioPlayer.playbackState
    val currentAudioAyah = audioPlayer.currentAyah
    val hasNextAyah = audioPlayer.hasNext
    val hasPreviousAyah = audioPlayer.hasPrevious
    val positionMs = audioPlayer.positionMs
    val durationMs = audioPlayer.durationMs

    /** Increments on each failed playback attempt (shown as a hint in the UI). */
    val playbackErrorCount: StateFlow<Int> = audioPlayer.errorCount

    init {
        // Poll the media position while the reader is open so the mini
        // player's progress bar stays live.
        viewModelScope.launch {
            while (isActive) {
                audioPlayer.refreshPosition()
                kotlinx.coroutines.delay(250)
            }
        }

        // Track whether the whole surah is downloaded for the selected reciter.
        // isDownloaded() hops to Dispatchers.IO internally, so this collector
        // never blocks the main thread.
        viewModelScope.launch {
            combine(prefsRepository.selectedReciterId, repository.observeSurah(surahNumber)) { id, ayahs ->
                id to ayahs
            }.collect { (reciterId, ayahs) ->
                val reciter = Reciter.Bundled.firstOrNull { it.id == reciterId } ?: Reciter.Bundled.first()
                _downloaded.value = ayahs.isNotEmpty() && ayahs.all { ayah ->
                    recitationRepository.isDownloaded(reciter.id, surahNumber, ayah.globalNumber)
                }
            }
        }
    }

    fun selectReciter(reciter: Reciter) = viewModelScope.launch {
        prefsRepository.setSelectedReciterId(reciter.id)
    }

    /** Downloads the current surah for the selected reciter. */
    fun downloadCurrentSurah() {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty() || _downloading.value) return
        viewModelScope.launch {
            _downloading.value = true
            _downloadProgress.value = 0f
            val reciter = selectedReciter.value
            val mapping = ayahs.associate { it.numberInSurah to it.globalNumber }
            recitationRepository.downloadSurah(reciter, surahNumber, mapping) { progress ->
                _downloadProgress.value = progress
            }
            _downloading.value = false
            _downloadProgress.value = null
        }
    }

    /**
     * Builds and plays a queue from the given ayahs, downloading any missing
     * audio first (with progress). [repeatCount] is applied per ayah.
     */
    private fun playQueueOf(ayahs: List<Ayah>, repeatCount: Int) {
        if (ayahs.isEmpty() || _downloading.value) return
        viewModelScope.launch {
            val reciter = selectedReciter.value

            _downloading.value = true
            _downloadProgress.value = 0f
            val result = recitationRepository.downloadSurah(
                reciter,
                surahNumber,
                ayahs.associate { it.numberInSurah to it.globalNumber },
            ) { progress -> _downloadProgress.value = progress }
            _downloading.value = false
            _downloadProgress.value = null
            if (result !is org.muslim.app.core.network.FileDownloader.Result.Success) return@launch

            val items = ayahs.map {
                RecitationQueueItem(
                    file = recitationRepository.fileFor(reciter.id, it.surahNumber, it.globalNumber),
                    globalNumber = it.globalNumber,
                )
            }
            audioPlayer.playQueue(items, startIndex = 0, repeatCount = repeatCount)
        }
    }

    /**
     * Continuous playback: plays [ayah] then auto-advances through the rest
     * of the surah. Missing audio is downloaded (with progress) first, so
     * playback then works fully offline.
     */
    fun playFromAyah(ayah: Ayah, repeatCount: Int) {
        val ayahs = uiState.value.ayahs
        val start = ayahs.indexOfFirst { it.globalNumber == ayah.globalNumber }.coerceAtLeast(0)
        playQueueOf(ayahs.drop(start), repeatCount)
    }

    /** Plays only [ayah], repeating it [repeatCount] times (memorisation). */
    fun playSingleAyah(ayah: Ayah, repeatCount: Int) {
        playQueueOf(listOf(ayah), repeatCount)
    }

    /** Applies the reader's chosen [range] to playback starting at [ayah]. */
    fun playAyahWithRange(ayah: Ayah, repeatCount: Int, range: RecitationRange) {
        when (range) {
            RecitationRange.SingleAyah -> playSingleAyah(ayah, repeatCount)
            RecitationRange.FromAyahToEnd -> playFromAyah(ayah, repeatCount)
        }
    }

    /**
     * Plays the whole surah continuously from the first ayah to the last,
     * applying the same per-ayah [repeatCount] before each advance.
     */
    fun playWholeSurah(repeatCount: Int) {
        val first = uiState.value.ayahs.firstOrNull() ?: return
        playFromAyah(first, repeatCount)
    }

    fun pausePlayback() = audioPlayer.pause()
    fun resumePlayback() = audioPlayer.resume()
    fun stopPlayback() = audioPlayer.stop()
    fun nextAyah() = audioPlayer.next()
    fun previousAyah() = audioPlayer.previous()

    // --- Bookmarks / last-read (existing) ---

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
            prefsRepository.advanceReadThrough(ayah.globalNumber)
        }
    }
}
