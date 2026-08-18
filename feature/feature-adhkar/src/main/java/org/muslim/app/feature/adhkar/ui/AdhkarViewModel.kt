package org.muslim.app.feature.adhkar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import javax.inject.Inject

@HiltViewModel
class AdhkarViewModel @Inject constructor(
    private val repository: AdhkarRepository,
    private val prefsRepository: AdhkarPrefsRepository,
    private val reminderScheduler: AdhkarReminderScheduler,
) : ViewModel() {

    private val all: StateFlow<List<Dhikr>> = repository.observeAdhkar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedCategory = MutableStateFlow<DhikrCategory?>(null)

    /** Cache of per-dhikr counters so each card collects a stable flow. */
    private val counts = mutableMapOf<Long, StateFlow<Int>>()

    /**
     * Adhkar filtered by the selected category (null = all) and by the user's
     * customization choices — dhikr disabled in the customize screen stay hidden
     * from the library too, not just from reminders/overlay.
     */
    val adhkar: StateFlow<List<Dhikr>> = combine(
        all,
        selectedCategory,
        prefsRepository.prefs,
    ) { list, category, prefs ->
        list.filter { dhikr ->
            prefs.isDhikrEnabled(dhikr.id) &&
                (category == null || dhikr.category == category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Ids the user pinned as favorites (persisted in DataStore). */
    val favoriteIds: StateFlow<Set<Long>> = prefsRepository.prefs
        .map { it.favoriteDhikrIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Favorite dhikr (enabled ones, matching the selected category), in seed order. */
    val favorites: StateFlow<List<Dhikr>> = combine(
        all,
        favoriteIds,
        selectedCategory,
        prefsRepository.prefs,
    ) { list, favoriteIds, category, prefs ->
        list.filter { dhikr ->
            dhikr.id in favoriteIds &&
                prefs.isDhikrEnabled(dhikr.id) &&
                (category == null || dhikr.category == category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavorite(dhikrId: Long) {
        viewModelScope.launch {
            val prefs = prefsRepository.prefs.first()
            prefsRepository.setDhikrFavorite(dhikrId, !prefs.isDhikrFavorite(dhikrId))
        }
    }

    val categories: List<DhikrCategory> = DhikrCategory.entries

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
}
