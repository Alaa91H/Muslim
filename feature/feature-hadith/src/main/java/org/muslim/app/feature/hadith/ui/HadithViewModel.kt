package org.muslim.app.feature.hadith.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.feature.hadith.data.HadithCorpusState
import org.muslim.app.feature.hadith.data.HadithOfTheDayScheduler
import org.muslim.app.feature.hadith.data.HadithPrefsRepository
import org.muslim.app.feature.hadith.data.HadithRepository
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithCollection

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HadithViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HadithRepository,
    private val prefsRepository: HadithPrefsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    val use24h: StateFlow<Boolean> = appPreferencesRepository.preferences
        .map { it.timeFormat24h }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val queryInput = MutableStateFlow("")
    val query: StateFlow<String> = queryInput

    private val selectedCollection = MutableStateFlow<HadithCollection?>(null)
    val collection: StateFlow<HadithCollection?> = selectedCollection

    val bookmarkedIds: StateFlow<Set<Long>> = prefsRepository.bookmarkedIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val dailyNotificationEnabled: StateFlow<Boolean> = prefsRepository.dailyNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dailyNotificationTimeMinutes: StateFlow<Int> = prefsRepository.dailyNotificationTimeMinutes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HadithPrefsRepository.DEFAULT_NOTIFICATION_TIME_MINUTES,
        )

    val corpusState: StateFlow<HadithCorpusState> = repository.corpusState
    private val mutableDaily = MutableStateFlow<Hadith?>(null)
    val daily: StateFlow<Hadith?> = mutableDaily

    /**
     * A Room Paging source emits a bounded window of rows to LazyColumn. The
     * debounce prevents a new SQLite/FTS source for every input keystroke.
     */
    val pagedHadiths = combine(
        queryInput.debounce(SEARCH_DEBOUNCE_MILLIS),
        selectedCollection,
    ) { query, collection -> query.trim() to collection }
        .flatMapLatest { (query, collection) -> repository.pagedHadiths(query, collection) }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            runCatching {
                repository.ensureSeeded()
                if (prefsRepository.dailyNotificationEnabled.first()) {
                    HadithOfTheDayScheduler.schedule(
                        context,
                        prefsRepository.dailyNotificationTimeMinutes.first(),
                    )
                }
                mutableDaily.value = repository.hadithOfTheDay()
            }
        }
    }

    fun setQuery(value: String) {
        queryInput.value = value
    }

    fun setCollection(collection: HadithCollection?) {
        selectedCollection.value = collection
    }

    fun retryCorpusPreparation() {
        viewModelScope.launch {
            runCatching {
                repository.ensureSeeded()
                mutableDaily.value = repository.hadithOfTheDay()
            }
        }
    }

    fun setDailyNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setDailyNotificationEnabled(enabled)
            if (enabled) {
                HadithOfTheDayScheduler.schedule(
                    context,
                    prefsRepository.dailyNotificationTimeMinutes.first(),
                )
            } else {
                HadithOfTheDayScheduler.cancel(context)
            }
        }
    }

    fun setDailyNotificationTimeMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepository.setDailyNotificationTimeMinutes(minutes)
            if (prefsRepository.dailyNotificationEnabled.first()) {
                HadithOfTheDayScheduler.schedule(context, minutes)
            }
        }
    }

    fun toggleBookmark(id: Long) {
        viewModelScope.launch {
            val ids = prefsRepository.bookmarkedIds.first()
            if (id in ids) prefsRepository.removeBookmark(id) else prefsRepository.addBookmark(id)
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 250L
    }
}
