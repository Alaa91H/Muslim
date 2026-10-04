package org.muslim.app.feature.quran.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.OfficialQuranTextKind
import org.muslim.app.feature.quran.data.OfficialQuranTextSource
import org.muslim.app.feature.quran.data.QuranCastSupplementSelector
import org.muslim.app.feature.quran.data.QuranCastSupplements
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.data.QuranSupplementRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation

private data class QuranSupplementRequest(
    val ayah: Ayah?,
    val enabled: Boolean,
    val language: String,
    val tafsirSource: String?,
)

private data class QuranCastSupplementRequest(
    val globalAyah: Int?,
    val enabled: Boolean,
    val language: String,
)

/** Owns Quran meaning/tafsir catalog state, source selection and pack installation actions. */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class QuranSupplementController(
    private val repository: QuranSupplementRepository,
    private val preferences: QuranPrefsRepository,
    private val scope: CoroutineScope,
    supplementAyah: StateFlow<Ayah?>,
    playingGlobalAyah: StateFlow<Int?>,
) {
    val installedTafsirSources: StateFlow<List<String>> = repository.observeInstalledTafsirSources()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val effectiveSelectedTafsirSource = combine(
        preferences.selectedTafsirSource,
        installedTafsirSources,
    ) { requested, installed -> requested?.takeIf { it in installed } ?: installed.firstOrNull() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    val selectedTafsirSource: StateFlow<String?> = effectiveSelectedTafsirSource

    val supplements: StateFlow<QuranReaderSupplementUi> = combine(
        supplementAyah,
        preferences.supplementEnabled,
        preferences.supplementLanguage,
        effectiveSelectedTafsirSource,
    ) { ayah, enabled, language, tafsirSource -> QuranSupplementRequest(ayah, enabled, language, tafsirSource) }
        .flatMapLatest(::observeAyahSupplements)
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), QuranReaderSupplementUi())

    val castSupplements: StateFlow<QuranCastSupplements> = combine(
        playingGlobalAyah,
        preferences.supplementEnabled,
        preferences.supplementLanguage,
    ) { globalAyah, enabled, language -> QuranCastSupplementRequest(globalAyah, enabled, language) }
        .flatMapLatest { request ->
            val globalAyah = request.globalAyah
            if (!request.enabled || globalAyah == null) {
                flowOf(QuranCastSupplements())
            } else {
                combine(
                    repository.observeTranslations(globalAyah),
                    repository.observeTafsir(globalAyah),
                ) { translations, tafsir ->
                    val selectedLanguage = if (request.language == QuranPrefsRepository.AUTO_LANGUAGE) {
                        java.util.Locale.getDefault().language
                    } else {
                        request.language
                    }
                    QuranCastSupplementSelector.select(
                        enabled = true,
                        selectedLanguage = selectedLanguage,
                        translations = translations,
                        tafsir = tafsir,
                    )
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), QuranCastSupplements())

    val installedSupplementPacks = repository.observeInstalledTextPacks()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val supplementEnabled: StateFlow<Boolean> = preferences.supplementEnabled
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), false)

    val supplementLanguage: StateFlow<String> = preferences.supplementLanguage
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), QuranPrefsRepository.AUTO_LANGUAGE)

    private val _tafsirDownloadState = MutableStateFlow(QuranReaderTafsirDownloadState())
    val tafsirDownloadState: StateFlow<QuranReaderTafsirDownloadState> = _tafsirDownloadState.asStateFlow()

    private val _textPackImportState = MutableStateFlow(QuranTextPackImportState())
    val textPackImportState: StateFlow<QuranTextPackImportState> = _textPackImportState.asStateFlow()

    private val _officialTextSources = MutableStateFlow<List<OfficialQuranTextSource>>(emptyList())
    val officialTextSources: StateFlow<List<OfficialQuranTextSource>> = _officialTextSources.asStateFlow()

    val availableSupplementLanguages: StateFlow<List<String>> = combine(
        repository.observeLanguages(),
        _officialTextSources,
    ) { installed, catalogue ->
        (installed + catalogue.map(OfficialQuranTextSource::languageTag)).distinct().sorted()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun importTextPack(contents: String) = scope.launch {
        _textPackImportState.value = QuranTextPackImportState(importing = true)
        runCatching { repository.installContentPack(contents) }
            .onSuccess { count -> _textPackImportState.value = QuranTextPackImportState(installedCount = count) }
            .onFailure { error ->
                _textPackImportState.value = QuranTextPackImportState(error = error.message ?: "Pack validation failed")
            }
    }

    fun importTextPackFailure(message: String) {
        _textPackImportState.value = QuranTextPackImportState(error = message)
    }

    fun refreshOfficialTextSources(languageTag: String) = scope.launch {
        runCatching { repository.listOfficialSources(languageTag) }
            .onSuccess { _officialTextSources.value = it }
            .onFailure { _tafsirDownloadState.value = QuranReaderTafsirDownloadState(error = it.message) }
    }

    fun setSelectedTafsirSource(source: String?) = scope.launch {
        preferences.setSelectedTafsirSource(source)
    }

    fun downloadOfficialText(source: OfficialQuranTextSource) = scope.launch {
        _tafsirDownloadState.value = QuranReaderTafsirDownloadState(downloading = source, completedSurahs = 0)
        runCatching {
            repository.downloadOfficialText(source) { completedSurahs ->
                _tafsirDownloadState.value = QuranReaderTafsirDownloadState(
                    downloading = source,
                    completedSurahs = completedSurahs,
                )
            }
        }.onSuccess {
            if (source.kind != OfficialQuranTextKind.Meaning) {
                preferences.setSelectedTafsirSource(source.storageKey)
            }
            _tafsirDownloadState.value = QuranReaderTafsirDownloadState(
                completedSource = source,
                completedSurahs = 114,
            )
        }.onFailure { error ->
            _tafsirDownloadState.value = QuranReaderTafsirDownloadState(error = error.message ?: "Download failed")
        }
    }

    fun setSupplementEnabled(enabled: Boolean) = scope.launch { preferences.setSupplementEnabled(enabled) }

    fun setSupplementLanguage(language: String) = scope.launch { preferences.setSupplementLanguage(language) }

    private fun observeAyahSupplements(request: QuranSupplementRequest) = with(request) {
        if (ayah == null || !enabled) {
            flowOf(QuranReaderSupplementUi())
        } else {
            combine(
                repository.observeTranslations(ayah.globalNumber),
                repository.observeTafsir(ayah.globalNumber),
            ) { translations, tafsir ->
                val resolvedLanguage = if (language == QuranPrefsRepository.AUTO_LANGUAGE) {
                    java.util.Locale.getDefault().language
                } else {
                    language
                }
                val matchingTranslations = translations.translationsForLanguage(resolvedLanguage)
                val matchingTafsir = tafsir.tafsirForLanguage(resolvedLanguage)
                QuranReaderSupplementUi(
                    translations = matchingTranslations,
                    tafsir = matchingTafsir.filterSelectedSource(tafsirSource),
                )
            }
        }
    }

    private fun List<Translation>.translationsForLanguage(language: String) = filter {
        it.language.equals(language, ignoreCase = true)
    }

    private fun List<TafsirEntry>.tafsirForLanguage(language: String) = filter {
        it.language.equals(language, ignoreCase = true)
    }

    private fun List<TafsirEntry>.filterSelectedSource(source: String?) =
        source?.let { selected -> filter { it.source == selected } } ?: this
}
