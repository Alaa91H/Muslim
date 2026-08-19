package org.muslim.app.feature.hadith.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.feature.hadith.data.HadithOfTheDayScheduler
import org.muslim.app.feature.hadith.data.HadithPrefsRepository
import org.muslim.app.feature.hadith.data.HadithRepository
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithCollection
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HadithViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HadithRepository,
    private val prefsRepository: HadithPrefsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _collection = MutableStateFlow<HadithCollection?>(null)
    val collection: StateFlow<HadithCollection?> = _collection

    val bookmarkedIds: StateFlow<Set<Long>> = prefsRepository.bookmarkedIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val dailyNotificationEnabled: StateFlow<Boolean> = prefsRepository.dailyNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** Daily notification time, in minutes from midnight. */
    val dailyNotificationTimeMinutes: StateFlow<Int> = prefsRepository.dailyNotificationTimeMinutes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HadithPrefsRepository.DEFAULT_NOTIFICATION_TIME_MINUTES,
        )

    private val _daily = MutableStateFlow<Hadith?>(null)
    val daily: StateFlow<Hadith?> = _daily

    val hadiths: StateFlow<List<Hadith>> = combine(_query, _collection) { q, collection -> q to collection }
        .flatMapLatest { (q, collection) ->
            if (q.isBlank()) {
                if (collection == null) {
                    repository.observeAll()
                } else {
                    repository.observeCollection(collection)
                }
            } else {
                flowOf(repository.search(q.trim()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Schedule the daily hadith notification only when the user has it
        // enabled (default on), at the stored time. The worker re-checks the
        // flag as a guard.
        viewModelScope.launch {
            if (prefsRepository.dailyNotificationEnabled.first()) {
                HadithOfTheDayScheduler.schedule(
                    context,
                    prefsRepository.dailyNotificationTimeMinutes.first(),
                )
            }
            _daily.value = repository.hadithOfTheDay()
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setCollection(collection: HadithCollection?) {
        _collection.value = collection
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

    /** Persists the new time and re-schedules the daily notification. */
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
}
