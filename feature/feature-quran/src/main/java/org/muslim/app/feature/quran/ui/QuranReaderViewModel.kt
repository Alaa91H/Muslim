package org.muslim.app.feature.quran.ui

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.PlaybackState
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.QuranSupplementRepository
import org.muslim.app.feature.quran.data.OfficialTafsirSource
import org.muslim.app.feature.quran.data.QuranTajweedRepository
import org.muslim.app.feature.quran.data.DownloadRequest
import org.muslim.app.feature.quran.data.DownloadScope
import org.muslim.app.feature.quran.data.QuranDownloadManager
import org.muslim.app.feature.quran.data.RecitationDownloadNotifier
import org.muslim.app.feature.quran.data.RecitationFailureEvent
import org.muslim.app.feature.quran.data.RecitationFailureReason
import org.muslim.app.feature.quran.data.PersistedRecitationSession
import org.muslim.app.feature.quran.data.RecitationQueueItem
import org.muslim.app.feature.quran.data.RecitationSessionIntent
import org.muslim.app.feature.quran.data.RecitationSessionRuntime
import org.muslim.app.feature.quran.data.RecitationSessionStore
import org.muslim.app.feature.quran.data.remainingGlobalNumbers
import org.muslim.app.feature.quran.data.RecitationRepository
import org.muslim.app.feature.quran.data.ReciterDownloadState
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.LastRead
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.ReaderTheme
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import org.muslim.app.feature.quran.domain.TajweedAnnotation
import javax.inject.Inject

/** Playback range selected in the reader's recitation controls. */
enum class RecitationRange { SingleAyah, FromAyahToEnd, WholeSurah }

/** The reader always starts a new recitation through the end of the mushaf. */
val DEFAULT_RECITATION_RANGE = RecitationRange.FromAyahToEnd

/** Content and reader-preference dependencies for the Quran reader. */
class QuranReaderContentDependencies @Inject constructor(
    val repository: QuranRepository,
    val prefsRepository: QuranPrefsRepository,
    val supplementRepository: QuranSupplementRepository,
    val tajweedRepository: QuranTajweedRepository,
)

/** Audio, download and durable-session dependencies for Quran recitation. */
class QuranReaderRecitationDependencies @Inject constructor(
    val recitationRepository: RecitationRepository,
    val downloadManager: QuranDownloadManager,
    val audioPlayer: QuranAudioPlayer,
    val sessionStore: RecitationSessionStore,
    val sessionRuntime: RecitationSessionRuntime,
)

/** Cohesive dependencies required by the Quran reader runtime. */
class QuranReaderDependencies @Inject constructor(
    val content: QuranReaderContentDependencies,
    val recitation: QuranReaderRecitationDependencies,
)

data class QuranReaderUiState(
    val loading: Boolean = true,
    val surah: Surah? = null,
    val ayahs: List<Ayah> = emptyList(),
)

private data class QuranReaderQuranReaderSupplementRequest(
    val ayah: Ayah?,
    val enabled: Boolean,
    val language: String,
    val tafsirSource: String?,
)

data class QuranReaderQuranReaderTafsirDownloadState(
    val downloading: OfficialTafsirSource? = null,
    val completedSource: OfficialTafsirSource? = null,
    val completedSurahs: Int = 0,
    val error: String? = null,
)

data class QuranReaderQuranReaderSupplementUi(
    val translations: List<Translation> = emptyList(),
    val tafsir: List<TafsirEntry> = emptyList(),
)

private const val TAFSIR_SURAH_TOTAL = 114

private fun publishDownloadProgress(
    notifier: RecitationDownloadNotifier,
    surahName: String,
    progress: Float,
    startElapsed: Long,
    ayahCount: Int,
    reciter: Reciter,
) {
    val elapsedSec =
        ((SystemClock.elapsedRealtime() - startElapsed) / 1000f).coerceAtLeast(1f)
    val done = (progress * ayahCount).toInt().coerceAtMost(ayahCount)
    val ayahsPerSec = done / elapsedSec
    val remainingSec = if (ayahsPerSec > 0f) {
        ((ayahCount - done) / ayahsPerSec).toLong()
    } else {
        0L
    }
    notifier.show(
        surahName = surahName,
        percent = (progress * 100).toInt(),
        remainingSeconds = remainingSec,
        bytesPerSecond = (ayahsPerSec * reciter.estimatedBytesPerAyah()).toLong(),
    )
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    dependencies: QuranReaderDependencies,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository = dependencies.content.repository
    private val prefsRepository = dependencies.content.prefsRepository
    private val supplementRepository = dependencies.content.supplementRepository
    private val tajweedRepository = dependencies.content.tajweedRepository
    private val recitationRepository = dependencies.recitation.recitationRepository
    private val downloadManager = dependencies.recitation.downloadManager
    private val audioPlayer = dependencies.recitation.audioPlayer
    private val sessionStore = dependencies.recitation.sessionStore
    private val sessionRuntime = dependencies.recitation.sessionRuntime
    private val downloadNotifier = RecitationDownloadNotifier(context)

    // Last recitation range/repeat the user played with, so switching the
    // reciter from the player bar resumes playback with the same settings.
    private var lastRepeatCount = 1
    private var lastRange = DEFAULT_RECITATION_RANGE

    override fun onCleared() {
        downloadNotifier.dismiss()

}


    private val initialSurahNumber: Int = savedStateHandle["surahNumber"] ?: 1

    /** Current surah (mutable so continuous playback can auto-advance). */
    private val _surahNumber = MutableStateFlow(initialSurahNumber)
    val surahNumber: StateFlow<Int> = _surahNumber.asStateFlow()

    /** Global ayah number to scroll to (search/bookmarks/last-read), -1 = none. */
    val initialAyahGlobal: Int = savedStateHandle["ayah"] ?: -1

    /** True only when the user tapped the play action on a surah in the list. */
    private var autoplayWholeSurahPending: Boolean = savedStateHandle["autoplay"] ?: false

    /** Consumes the one-shot request to start the selected surah from ayah one. */
    fun consumeAutoplayWholeSurah(): Boolean = autoplayWholeSurahPending.also {
        autoplayWholeSurahPending = false
    }

    /** The ayah currently in view; the UI updates this as the user scrolls. */
    val currentAyah = MutableStateFlow<Ayah?>(null)

    /** Opens an adjacent surah for manual Mushaf-style paging. */
    fun openSurah(number: Int) {
        if (number in 1..114 && number != _surahNumber.value) {
            _surahNumber.value = number
        }
    }

    val uiState: StateFlow<QuranReaderUiState> = combine(
        _surahNumber.flatMapLatest { repository.observeSurahMetadata(it)
}
,
        _surahNumber.flatMapLatest { repository.observeSurah(it)
}
,
    ) { surah, ayahs ->
        QuranReaderUiState(loading = false, surah = surah, ayahs = ayahs)

}
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranReaderUiState())

    val isBookmarked: StateFlow<Boolean> = combine(
        currentAyah,
        repository.observeBookmarks(),
    ) { ayah, bookmarks ->
        ayah != null && bookmarks.any { it.ayah.globalNumber == ayah.globalNumber
}


}
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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


    /** Keep the screen awake while the reader is open (and during recitation). */
    val keepScreenOn: StateFlow<Boolean> = prefsRepository.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setKeepScreenOn(enabled)

}


    /** Called as the user scrolls; advances the khatma progress monotonically. */
    fun advanceReadThrough(globalNumber: Int) = viewModelScope.launch {
        prefsRepository.advanceReadThrough(globalNumber)

}


    // --- Meaning + tafsir (Phase C3/C4) ---

    val installedTafsirSources: StateFlow<List<String>> = supplementRepository.observeInstalledTafsirSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val effectiveSelectedTafsirSource: StateFlow<String?> = combine(
        prefsRepository.selectedTafsirSource,
        installedTafsirSources,
    ) { requested, installed ->
        requested?.takeIf { it in installed } ?: installed.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Never exposes a stale preference: the selected source is always installed. */
    val selectedTafsirSource: StateFlow<String?> = effectiveSelectedTafsirSource

    /**
     * Translations + tafsir of the currently viewed ayah, when installed.
     * Honors the reader's meanings/tafsir controls: the panel hides when
     * [supplementEnabled] is off, and translations are filtered to the chosen
     * language ("auto" resolves to the current app language).
     */
    val supplements: StateFlow<QuranReaderSupplementUi> = combine(
        currentAyah,
        prefsRepository.supplementEnabled,
        prefsRepository.supplementLanguage,
        effectiveSelectedTafsirSource,
    ) { ayah, enabled, language, tafsirSource ->
        QuranReaderSupplementRequest(ayah, enabled, language, tafsirSource)
    }
        .flatMapLatest { (ayah, enabled, language, tafsirSource) ->
            if (ayah == null || !enabled) {
                flowOf(QuranReaderQuranReaderSupplementUi())

}
 else {
                combine(
                    supplementRepository.observeTranslations(ayah.globalNumber),
                    supplementRepository.observeTafsir(ayah.globalNumber),
                ) { translations, tafsir ->
                    val resolved = if (language == QuranPrefsRepository.AUTO_LANGUAGE) {
                        java.util.Locale.getDefault().language

}
 else {
                        language

}

                    // Prefer the chosen language, but never show an empty panel
                    // when only other languages are installed — fall back to
                    // whatever is available so المعاني/التفسير always works.
                    val forLanguage = translations.filter { it.language == resolved
}

                    QuranReaderSupplementUi(
                        translations = if (forLanguage.isNotEmpty()) forLanguage else translations,
                        tafsir = tafsirSource?.let { selected -> tafsir.filter { it.source == selected } } ?: tafsir,
                    )

}


}


}

        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranReaderSupplementUi())

    /** Installed translation languages, for the meanings panel language picker. */
    val availableSupplementLanguages: StateFlow<List<String>> =
        supplementRepository.observeLanguages()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val supplementEnabled: StateFlow<Boolean> = prefsRepository.supplementEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val supplementLanguage: StateFlow<String> = prefsRepository.supplementLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranPrefsRepository.AUTO_LANGUAGE)

    private val _tafsirDownloadState = MutableStateFlow(QuranReaderQuranReaderTafsirDownloadState())
    val tafsirDownloadState: StateFlow<QuranReaderTafsirDownloadState> = _tafsirDownloadState.asStateFlow()

    fun setSelectedTafsirSource(source: String?) = viewModelScope.launch {
        prefsRepository.setSelectedTafsirSource(source)
    }

    fun downloadOfficialTafsir(source: OfficialTafsirSource) = viewModelScope.launch {
        _tafsirDownloadState.value = QuranReaderTafsirDownloadState(downloading = source, completedSurahs = 0)
        runCatching {
            supplementRepository.downloadOfficialTafsir(source) { completedSurahs ->
                _tafsirDownloadState.value = QuranReaderTafsirDownloadState(
                    downloading = source,
                    completedSurahs = completedSurahs,
                )
            }
        }.onSuccess {
            prefsRepository.setSelectedTafsirSource(source.storageKey)
            _tafsirDownloadState.value = QuranReaderTafsirDownloadState(
                completedSource = source,
                completedSurahs = TAFSIR_SURAH_TOTAL,
            )
        }.onFailure { error ->
            _tafsirDownloadState.value = QuranReaderTafsirDownloadState(
                error = error.message ?: "Download failed",
            )
        }
    }

    fun setSupplementEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setSupplementEnabled(enabled)

}


    fun setSupplementLanguage(language: String) = viewModelScope.launch {
        prefsRepository.setSupplementLanguage(language)

}

    /** Hafs tajweed colourization remains an explicit, off-by-default option. */
    val tajweedEnabled: StateFlow<Boolean> = prefsRepository.tajweedEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val tajweedAnnotations: StateFlow<Map<Int, List<TajweedAnnotation>>> = combine(
        _surahNumber,
        prefsRepository.tajweedEnabled,
    ) { surahNumber, enabled -> surahNumber to enabled }
        .flatMapLatest { (surahNumber, enabled) ->
            if (enabled) flow { emit(tajweedRepository.annotationsForSurah(surahNumber)) }
            else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setTajweedEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setTajweedEnabled(enabled)
    }

    // --- Recitation (Phase C5/C7) ---

    private val _downloaded = MutableStateFlow(false)
    val downloaded: StateFlow<Boolean> = _downloaded

    val selectedReciter: StateFlow<Reciter> = prefsRepository.selectedReciterId
        .map { id -> Reciter.Bundled.firstOrNull { it.id == id
}
 ?: Reciter.Bundled.first()
}

        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Reciter.Bundled.first())

    /**
     * What is downloaded for the selected reciter (per-reciter state shown in
     * the reader's download dialog instead of only the current surah's flag).
     */
    val reciterDownloadState: StateFlow<ReciterDownloadState?> = prefsRepository.selectedReciterId
        .flatMapLatest { id ->
            flow { emit(recitationRepository.downloadState(id))
}


}

        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Deletes the downloaded audio of [surahNumber] for the selected reciter. */
    fun deleteDownloadedSurah(surahNumber: Int) = viewModelScope.launch {
        recitationRepository.deleteSurah(selectedReciter.value.id, surahNumber)
        val reciterId = selectedReciter.value.id
        val ayahs = uiState.value.ayahs
        _downloaded.value = ayahs.isNotEmpty() && ayahs.all { ayah ->
            recitationRepository.isDownloaded(reciterId, _surahNumber.value, ayah.globalNumber)
        }
    }


    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private val _downloading = MutableStateFlow(false)
    val downloading: StateFlow<Boolean> = _downloading

    /** Continuous playback stops after surah 114 (instead of wrapping). */
    private val _continuousStopAtEnd = MutableStateFlow(false)
    val continuousStopAtEnd: StateFlow<Boolean> = _continuousStopAtEnd.asStateFlow()

    fun setContinuousStopAtEnd(stopAtEnd: Boolean) {
        _continuousStopAtEnd.value = stopAtEnd
        viewModelScope.launch { prefsRepository.setContinuousStopAtEnd(stopAtEnd)
}


}


    val playbackState = audioPlayer.playbackState
    val currentAudioAyah = audioPlayer.currentAyah
    val hasNextAyah = audioPlayer.hasNext
    val hasPreviousAyah = audioPlayer.hasPrevious
    val positionMs = audioPlayer.positionMs
    val durationMs = audioPlayer.durationMs

    private var recitationFailureSequence = 0L
    private val _recitationFailure = MutableStateFlow<RecitationFailureEvent?>(null)
    val recitationFailure: StateFlow<RecitationFailureEvent?> = _recitationFailure.asStateFlow()

    private val _restorableSession = MutableStateFlow<PersistedRecitationSession?>(null)
    val restorableSession: StateFlow<PersistedRecitationSession?> =
        _restorableSession.asStateFlow()

    private var lastPlaybackRequest: RecitationSessionIntent? = null

    init {
        // Hydrate the stop-at-end mirror and remove the legacy placeholder
        // tafsir from older installs. Production sources are explicit,
        // attributable downloads rather than pre-seeded sample content.
        viewModelScope.launch {
            _continuousStopAtEnd.value = prefsRepository.continuousStopAtEnd.first()
            supplementRepository.removeLegacySampleTafsir()

}


        viewModelScope.launch {
            _restorableSession.value = sessionStore.session.first()
        }

        viewModelScope.launch {
            audioPlayer.lastFailure.collect { failure ->
                if (failure != null) {
                    reportRecitationFailure(failure.reason, failure.globalNumber)
                }
            }
        }

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
            combine(
                prefsRepository.selectedReciterId,
                _surahNumber.flatMapLatest { repository.observeSurah(it)
}
,
            ) { id, ayahs -> id to ayahs
}
.collect { (reciterId, ayahs) ->
                val reciter = Reciter.Bundled.firstOrNull { it.id == reciterId
}
 ?: Reciter.Bundled.first()
                _downloaded.value = ayahs.isNotEmpty() && ayahs.all { ayah ->
                    recitationRepository.isDownloaded(reciter.id, _surahNumber.value, ayah.globalNumber)

}


}


}


}


    /**
     * Switches the reciter from the player bar. If recitation is active, the
     * current reciter is stopped first, then the new reciter is applied and
     * playback resumes from the start of the same ayah where it stopped — the
     * queue is rebuilt against the new reciter and any missing audio for it is
     * downloaded automatically (with progress).
     */
    fun selectReciter(reciter: Reciter) {
        if (reciter.id == selectedReciter.value.id) return
        val wasActive = shouldResumeAfterReciterChange(
            audioPlayer.playbackState.value,
            audioPlayer.currentAyah.value,
        )
        val ayahGlobal = audioPlayer.currentAyah.value
        val repeat = lastRepeatCount
        val range = lastRange
        audioPlayer.stop()
        viewModelScope.launch {
            prefsRepository.setSelectedReciterId(reciter.id)
            if (wasActive && ayahGlobal != null) {
                // The playing ayah may belong to a surah the reader is no
                // longer displaying (continuous playback advanced surahs), so
                // resolve it from its own surah — never from a stale list.
                val surahOfAyah = QuranAyahIndex.surahOf(ayahGlobal)
                val ayahs = if (surahOfAyah in 1..114 && surahOfAyah != _surahNumber.value) {
                    _surahNumber.value = surahOfAyah
                    repository.observeSurah(surahOfAyah).first()

}
 else {
                    uiState.value.ayahs

}

                val ayah = ayahs.firstOrNull { it.globalNumber == ayahGlobal
}

                if (ayah != null) playAyahWithRange(ayah, repeat, range)

}


}


}


    /** Downloads the current surah for the selected reciter. */
    fun downloadCurrentSurah() {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty() || _downloading.value) return
        viewModelScope.launch {
            _downloading.value = true
            _downloadProgress.value = 0f
            val reciter = selectedReciter.value
            val mapping = ayahs.associate { it.numberInSurah to it.globalNumber
}

            recitationRepository.downloadSurah(reciter, surahNumber.value, mapping) { progress ->
                _downloadProgress.value = progress

}

            _downloading.value = false
            _downloadProgress.value = null

}


}


    /**
     * Downloads the ENTIRE mushaf (all 114 surahs) for the selected reciter
     * in the background (foreground service + progress notification).
     */
    fun downloadWholeQuran() {
        val reciter = selectedReciter.value
        downloadManager.enqueue(
            DownloadRequest(
                id = "full-${reciter.id}-${System.currentTimeMillis()}",
                reciterId = reciter.id,
                reciterName = reciter.name,
                scope = DownloadScope.FullQuran,
                surahNumber = null,
                globalNumber = null,
                label = "القرآن الكريم كاملًا",
                totalBytes = reciter.estimatedBytesPerAyah() * 6236L,
            )
        )

}


    /** Downloads just the currently highlighted ayah (single-ayah granularity). */
    fun downloadCurrentAyah() {
        val ayah = currentAyah.value ?: uiState.value.ayahs.firstOrNull() ?: return
        val reciter = selectedReciter.value
        downloadManager.enqueue(
            DownloadRequest(
                id = "ayah-${ayah.globalNumber}-${reciter.id}-${System.currentTimeMillis()}",
                reciterId = reciter.id,
                reciterName = reciter.name,
                scope = DownloadScope.Ayah,
                surahNumber = ayah.surahNumber,
                globalNumber = ayah.globalNumber,
                label = "الآية ${ayah.surahNumber}:${ayah.numberInSurah}",
                totalBytes = reciter.estimatedBytesPerAyah(),
            )
        )

}


    /**
     * Builds and plays a queue from the given ayahs, downloading any missing
     * audio first (with progress). [repeatCount] is applied per ayah.
     */
    private fun playQueueOf(
        ayahs: List<Ayah>,
        repeatCount: Int,
        continuous: Boolean = false,
        advanceToNext: Boolean = false,
        toEndOfQuran: Boolean = false,
    ) {
        if (ayahs.isEmpty() || _downloading.value) return

        val queueSurahNumber = ayahs.first().surahNumber
        val globalNumbers = ayahs.map { it.globalNumber }
        val reciter = selectedReciter.value
        val intent = RecitationSessionIntent(
            reciterId = reciter.id,
            surahNumber = queueSurahNumber,
            globalNumbers = globalNumbers,
            repeatCount = repeatCount,
            continuous = continuous,
            advanceToNext = advanceToNext,
            toEndOfQuran = toEndOfQuran,
        )
        lastPlaybackRequest = intent
        _restorableSession.value = null
        _recitationFailure.value = null

        viewModelScope.launch {
            val items = prepareQueueForPlayback(
                ayahs = ayahs,
                queueSurahNumber = queueSurahNumber,
                globalNumbers = globalNumbers,
                reciter = reciter,
            ) ?: return@launch

            startPreparedQueue(
                items = items,
                intent = intent,
            )
        }
    }

    private suspend fun prepareQueueForPlayback(
        ayahs: List<Ayah>,
        queueSurahNumber: Int,
        globalNumbers: List<Int>,
        reciter: Reciter,
    ): List<RecitationQueueItem>? {
        recitationRepository.localQueue(
            reciterId = reciter.id,
            surahNumber = queueSurahNumber,
            globalNumbers = globalNumbers,
        )?.let { return it }

        _downloading.value = true
        _downloadProgress.value = 0f
        val startElapsed = SystemClock.elapsedRealtime()
        var lastNotifiedPercent = -1

        return try {
            val result = recitationRepository.downloadSurah(
                reciter,
                queueSurahNumber,
                ayahs.associate { it.numberInSurah to it.globalNumber },
            ) { progress ->
                _downloadProgress.value = progress
                val percent = (progress * 100).toInt()
                if (progress < 1f && percent != lastNotifiedPercent) {
                    lastNotifiedPercent = percent
                    publishDownloadProgress(
                        notifier = downloadNotifier,
                        surahName = uiState.value.surah?.arabicName.orEmpty(),
                        progress = progress,
                        startElapsed = startElapsed,
                        ayahCount = ayahs.size,
                        reciter = reciter,
                    )
                }
            }

            if (result !is org.muslim.app.core.network.FileDownloader.Result.Success) {
                reportRecitationFailure(
                    RecitationFailureReason.DownloadFailed,
                    globalNumbers.firstOrNull(),
                )
                null
            } else {
                recitationRepository.localQueue(
                    reciterId = reciter.id,
                    surahNumber = queueSurahNumber,
                    globalNumbers = globalNumbers,
                ) ?: run {
                    reportRecitationFailure(
                        RecitationFailureReason.AudioFileUnavailable,
                        globalNumbers.firstOrNull(),
                    )
                    null
                }
            }
        } finally {
            _downloading.value = false
            _downloadProgress.value = null
            downloadNotifier.dismiss()
        }
    }

    private fun startPreparedQueue(
        items: List<RecitationQueueItem>,
        intent: RecitationSessionIntent,
        startPositionMs: Long = 0L,
        remainingRepeatsForCurrent: Int? = null,
    ) {
        val continuousMode = intent.continuous || intent.repeatCount <= 0
        val effectiveRepeat =
            if (continuousMode) 1 else intent.repeatCount.coerceAtLeast(1)

        sessionRuntime.begin(
            intent = intent,
            positionMs = startPositionMs,
            initialRemainingRepeats = remainingRepeatsForCurrent,
        )
        audioPlayer.onQueueCompleted =
            if (intent.advanceToNext) {
                { advanceToNextSurah(effectiveRepeat, intent.toEndOfQuran) }
            } else {
                null
            }
        audioPlayer.playQueue(
            items = items,
            startIndex = 0,
            repeatCount = effectiveRepeat,
            continuous = intent.advanceToNext,
            startPositionMs = startPositionMs,
            remainingRepeatsForCurrent = remainingRepeatsForCurrent,
        )
    }

    private fun reportRecitationFailure(
        reason: RecitationFailureReason,
        globalNumber: Int?,
    ) {
        recitationFailureSequence += 1
        _recitationFailure.value = RecitationFailureEvent(
            sequence = recitationFailureSequence,
            reason = reason,
            globalNumber = globalNumber,
        )
    }

    fun resumeRestorableSession() {
        val session = _restorableSession.value ?: return
        val reciter = Reciter.Bundled.firstOrNull {
            it.id == session.intent.reciterId
        } ?: run {
            discardRestorableSession()
            return
        }

        _restorableSession.value = null
        _recitationFailure.value = null

        viewModelScope.launch {
            prefsRepository.setSelectedReciterId(reciter.id)
            _surahNumber.value = session.intent.surahNumber

            val surahAyahs = repository.observeSurah(session.intent.surahNumber).first()
            val byGlobal = surahAyahs.associateBy { it.globalNumber }
            val remainingGlobals = session.remainingGlobalNumbers()
            val restoreAyahs = remainingGlobals.mapNotNull(byGlobal::get)
            if (restoreAyahs.size != remainingGlobals.size) {
                sessionRuntime.clear()
                reportRecitationFailure(
                    RecitationFailureReason.AudioFileUnavailable,
                    remainingGlobals.firstOrNull(),
                )
                return@launch
            }

            val resumeIntent = session.intent.copy(globalNumbers = remainingGlobals)
            lastPlaybackRequest = resumeIntent
            val items = prepareQueueForPlayback(
                ayahs = restoreAyahs,
                queueSurahNumber = resumeIntent.surahNumber,
                globalNumbers = remainingGlobals,
                reciter = reciter,
            ) ?: return@launch

            startPreparedQueue(
                items = items,
                intent = resumeIntent,
                startPositionMs = session.positionMs,
                remainingRepeatsForCurrent = session.remainingRepeats,
            )
        }
    }

    fun discardRestorableSession() {
        _restorableSession.value = null
        sessionRuntime.clear()
    }

    fun retryPlaybackAfterFailure() {
        val request = lastPlaybackRequest ?: return
        val failedGlobal = _recitationFailure.value?.globalNumber
        val retryGlobals = retryGlobalNumbers(request.globalNumbers, failedGlobal)
        _recitationFailure.value = null
        viewModelScope.launch {
            val surahAyahs = repository.observeSurah(request.surahNumber).first()
            val byGlobal = surahAyahs.associateBy { it.globalNumber }
            val retryAyahs = retryGlobals.mapNotNull(byGlobal::get)
            if (retryAyahs.size != retryGlobals.size) {
                reportRecitationFailure(
                    RecitationFailureReason.AudioFileUnavailable,
                    retryGlobals.firstOrNull(),
                )
                return@launch
            }
            playQueueOf(
                ayahs = retryAyahs,
                repeatCount = request.repeatCount,
                continuous = request.continuous,
                advanceToNext = request.advanceToNext,
                toEndOfQuran = request.toEndOfQuran,
            )
        }
    }


    /**
     * A queue finished (its last item completed): move to the next surah and
     * keep playing. "إلى نهاية القرآن" runs stop at surah 114; other
     * continuous runs wrap 114 -> 1 unless the user chose to stop at the end
     * of the mushaf. Called from the audio player's completion path.
     */
    private fun advanceToNextSurah(repeat: Int, toEndOfQuran: Boolean) {
        val next = nextSurahForAdvance(_surahNumber.value, toEndOfQuran, _continuousStopAtEnd.value)
            ?: run {
                audioPlayer.stop()
                return

}

        _surahNumber.value = next
        viewModelScope.launch {
            val ayahs = repository.observeSurah(next).first()
            // Carry the same per-ayah repeat and end-of-Quran rule into the
            // next surah so the whole run keeps the user's settings.
            playQueueOf(ayahs, repeat, advanceToNext = true, toEndOfQuran = toEndOfQuran)

}


}


    /**
     * "من الآية إلى نهاية القرآن": plays [ayah] then auto-advances through
     * the rest of the surah AND every following surah until the end of the
     * mushaf (surah 114), where playback stops. Missing audio is downloaded
     * (with progress) first, so playback then works fully offline.
     */
    fun playFromAyah(ayah: Ayah, repeatCount: Int) {
        val ayahs = uiState.value.ayahs
        val start = ayahs.indexOfFirst { it.globalNumber == ayah.globalNumber
}
.coerceAtLeast(0)
        playQueueOf(ayahs.drop(start), repeatCount, advanceToNext = true, toEndOfQuran = true)

}


    /** Plays only [ayah], repeating it [repeatCount] times (memorisation). */
    fun playSingleAyah(ayah: Ayah, repeatCount: Int) {
        // "بدون توقف" continuous playback keeps moving through the mushaf;
        // finite repeats stay on the single ayah.
        playQueueOf(listOf(ayah), repeatCount, advanceToNext = repeatCount <= 0)

}


    /**
     * Starts at an ayah explicitly selected in the reader and ends at the end
     * of the current surah. This is intentionally separate from the advanced
     * range menu: the ordinary "select ayah, then play" action must never
     * silently restart from ayah one or continue through the whole mushaf.
     */
    fun playFromSelectedAyahToSurahEnd(ayah: Ayah, repeatCount: Int) {
        val ayahs = uiState.value.ayahs
        val start = ayahs.indexOfFirst { it.globalNumber == ayah.globalNumber }
        if (start < 0) return
        playQueueOf(ayahs.drop(start), repeatCount)
    }

    /** Applies the reader's chosen [range] to playback starting at [ayah]. */
    fun playAyahWithRange(ayah: Ayah, repeatCount: Int, range: RecitationRange) {
        // Remember what the user was playing so a reciter switch from the
        // player bar can resume the same run with the new reciter.
        lastRepeatCount = repeatCount
        lastRange = range
        when (range) {
            RecitationRange.SingleAyah -> playSingleAyah(ayah, repeatCount)
            RecitationRange.FromAyahToEnd -> playFromAyah(ayah, repeatCount)
            RecitationRange.WholeSurah -> playWholeSurah(repeatCount)

}


}


    /**
     * Plays the whole current surah from the first ayah to the last, applying
     * the same per-ayah [repeatCount]; stops at the end of the surah.
     */
    fun playWholeSurah(repeatCount: Int) {
        val ayahs = uiState.value.ayahs
        if (ayahs.isEmpty()) return
        playQueueOf(ayahs, repeatCount)

}


    fun pausePlayback() = audioPlayer.pause()
    fun resumePlayback() = audioPlayer.resume()
    fun stopPlayback() {
        _restorableSession.value = null
        audioPlayer.stop()
    }
    fun nextAyah() = audioPlayer.next()
    fun previousAyah() = audioPlayer.previous()

    // --- Bookmarks / last-read (existing) ---

    fun toggleBookmark() {
        val ayah = currentAyah.value ?: return
        viewModelScope.launch {
            if (repository.isBookmarked(ayah.globalNumber)) {
                repository.removeBookmark(ayah.globalNumber)

}
 else {
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

/**
 * Decides where playback advances once the current surah's queue finishes.
 * Returns the next surah number, or null when playback must stop: either a
 * "to the end of the Quran" run reached surah 114, or continuous playback is
 * set to stop at the end of the mushaf instead of wrapping.
 */
internal fun shouldResumeAfterReciterChange(state: PlaybackState, currentAyah: Int?): Boolean =
    currentAyah != null && state != PlaybackState.Idle

internal fun retryGlobalNumbers(requested: List<Int>, failedGlobal: Int?): List<Int> {
    if (failedGlobal == null) return requested
    val failedIndex = requested.indexOf(failedGlobal)
    return if (failedIndex >= 0) requested.drop(failedIndex) else requested
}

internal fun nextSurahForAdvance(currentSurah: Int, toEndOfQuran: Boolean, stopAtEnd: Boolean): Int? {
    if (currentSurah >= 114) return if (toEndOfQuran || stopAtEnd) null else 1
    return currentSurah + 1
}

