package org.muslim.app.feature.adhkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.adhkar.data.AdhkarPrefsRepository
import org.muslim.app.feature.adhkar.data.AdhkarReminderScheduler
import org.muslim.app.feature.adhkar.data.AdhkarRepository
import org.muslim.app.feature.adhkar.data.AdhkarSpeechController
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val repository: AdhkarRepository,
    private val prefsRepository: AdhkarPrefsRepository,
    private val reminderScheduler: AdhkarReminderScheduler,
    private val speechController: AdhkarSpeechController,
) : ViewModel() {

    private val all: StateFlow<List<Dhikr>> = repository.observeAdhkar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedCategory = MutableStateFlow<DhikrCategory?>(null)
    val searchQuery = MutableStateFlow("")
    val favoritesOnly = MutableStateFlow(false)

    private val libraryFilter = combine(
        selectedCategory,
        searchQuery,
        favoritesOnly,
    ) { category, query, favoritesOnly ->
        LibraryFilter(
            category = category,
            query = query,
            favoritesOnly = favoritesOnly,
        )
    }

    /**
     * The complete library after category, search, favorite-only and visibility
     * filters. Reader mode consumes this exact queue so the session always
     * mirrors what the user selected in the library screen.
     */
    val visibleAdhkar: StateFlow<List<Dhikr>> = combine(
        all,
        libraryFilter,
        prefsRepository.prefs,
    ) { list, filter, prefs ->
        list.filter { dhikr ->
            prefs.isDhikrEnabled(dhikr.id) &&
                (filter.category == null || dhikr.category == filter.category) &&
                (!filter.favoritesOnly || prefs.isDhikrFavorite(dhikr.id)) &&
                dhikr.matchesAdhkarQuery(filter.query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Ids the user pinned as favorites (persisted in DataStore). */
    val favoriteIds: StateFlow<Set<Long>> = prefsRepository.prefs
        .map { it.favoriteDhikrIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Favorite dhikr in the active search/category filter, in stable library order. */
    val favorites: StateFlow<List<Dhikr>> = combine(
        visibleAdhkar,
        favoriteIds,
    ) { list, favoriteIds ->
        list.filter { it.id in favoriteIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Non-favorite cards. Favorites are shown once in their dedicated section,
     * so a pinned dhikr never appears twice in the same filtered result set.
     */
    val adhkar: StateFlow<List<Dhikr>> = combine(
        visibleAdhkar,
        favoriteIds,
    ) { list, favoriteIds ->
        list.filter { it.id !in favoriteIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val resultCount: StateFlow<Int> = visibleAdhkar
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Cache of per-dhikr counters so each card collects a stable flow. */
    private val counts = mutableMapOf<Long, StateFlow<Int>>()

    fun toggleFavorite(dhikrId: Long) {
        viewModelScope.launch {
            val prefs = prefsRepository.prefs.first()
            prefsRepository.setDhikrFavorite(dhikrId, !prefs.isDhikrFavorite(dhikrId))
        }
    }

    val categories: List<DhikrCategory> = DhikrCategory.entries

    val speechEnabled: StateFlow<Boolean> = prefsRepository.prefs
        .map { it.speechEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val speechReady: StateFlow<Boolean> = speechController.ready

    val speakingDhikrId: StateFlow<Long?> = speechController.activeUtteranceId
        .map { utteranceId ->
            utteranceId
                ?.takeIf { it.startsWith(SPEECH_UTTERANCE_PREFIX) }
                ?.removePrefix(SPEECH_UTTERANCE_PREFIX)
                ?.toLongOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Master switch for the daily morning/evening adhkar reminders. */
    val morningEveningReminderEnabled: StateFlow<Boolean> = prefsRepository.prefs
        .map { it.morningEveningReminderEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Turns both the morning and evening daily reminders on or off together,
     * preserving their individually configured times, then re-arms the alarms.
     */
    fun setMorningEveningReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = prefsRepository.prefs.first()
            prefsRepository.setMorningReminder(enabled, prefs.morningHour, prefs.morningMinute)
            prefsRepository.setEveningReminder(enabled, prefs.eveningHour, prefs.eveningMinute)
            reminderScheduler.schedule(prefsRepository.prefs.first())
        }
    }

    fun selectCategory(category: DhikrCategory?) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFavoritesOnly(enabled: Boolean) {
        favoritesOnly.value = enabled
    }

    fun count(dhikrId: Long): StateFlow<Int> = counts.getOrPut(dhikrId) {
        repository.observeCount(dhikrId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    }

    fun increment(dhikrId: Long) {
        viewModelScope.launch { repository.increment(dhikrId) }
    }

    fun reset(dhikrId: Long) {
        viewModelScope.launch { repository.reset(dhikrId) }
    }

    fun toggleSpeech(dhikr: Dhikr) {
        viewModelScope.launch {
            val prefs = prefsRepository.prefs.first()
            if (!prefs.speechEnabled || !speechController.ready.value) return@launch

            if (speakingDhikrId.value == dhikr.id) {
                speechController.stop()
            } else {
                speechController.speak(
                    text = dhikr.arabic,
                    voiceName = prefs.speechVoiceName,
                    rate = prefs.speechRate,
                    allowNetworkVoices = prefs.speechAllowNetworkVoices,
                    utteranceId = "$SPEECH_UTTERANCE_PREFIX${dhikr.id}",
                )
            }
        }
    }

    override fun onCleared() {
        if (speechController.activeUtteranceId.value?.startsWith(SPEECH_UTTERANCE_PREFIX) == true) {
            speechController.stop()
        }
        super.onCleared()
    }

    private data class LibraryFilter(
        val category: DhikrCategory?,
        val query: String,
        val favoritesOnly: Boolean,
    )

    private companion object {
        const val SPEECH_UTTERANCE_PREFIX = "adhkar-dhikr-"
    }
}
