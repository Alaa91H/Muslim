package org.muslim.app.feature.family.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.family.data.AqiqahPrefsRepository
import org.muslim.app.feature.family.data.AqiqahReminderScheduler
import org.muslim.app.feature.family.data.FamilyLibraryPrefsRepository
import org.muslim.app.feature.family.domain.AqiqahReminderDay
import java.time.LocalDate
import javax.inject.Inject

data class FamilyLifeUiState(
    val birthDate: LocalDate? = null,
    val aqiqahReminderEnabled: Boolean = false,
    val aqiqahReminderDay: AqiqahReminderDay = AqiqahReminderDay.Seventh,
    val favoriteArticleIds: Set<String> = emptySet(),
    val recentArticleIds: List<String> = emptyList(),
    val completedChecklistItemIds: Set<String> = emptySet(),
)

private data class AqiqahUiPrefs(
    val birthDate: LocalDate?,
    val reminderEnabled: Boolean,
    val reminderDay: AqiqahReminderDay,
)

private data class FamilyLibraryUiPrefs(
    val favoriteArticleIds: Set<String>,
    val recentArticleIds: List<String>,
    val completedChecklistItemIds: Set<String>,
)

@HiltViewModel
class FamilyLifeViewModel @Inject constructor(
    private val aqiqahPrefsRepository: AqiqahPrefsRepository,
    private val familyLibraryPrefsRepository: FamilyLibraryPrefsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val aqiqahUiPrefs = combine(
        aqiqahPrefsRepository.birthDate,
        aqiqahPrefsRepository.reminderEnabled,
        aqiqahPrefsRepository.reminderDay,
    ) { birthDate, reminderEnabled, reminderDay ->
        AqiqahUiPrefs(birthDate, reminderEnabled, reminderDay)
    }

    private val familyLibraryUiPrefs = combine(
        familyLibraryPrefsRepository.favoriteArticleIds,
        familyLibraryPrefsRepository.recentArticleIds,
        familyLibraryPrefsRepository.completedChecklistItemIds,
    ) { favoriteIds, recentIds, completedIds ->
        FamilyLibraryUiPrefs(favoriteIds, recentIds, completedIds)
    }

    val state: StateFlow<FamilyLifeUiState> = combine(
        aqiqahUiPrefs,
        familyLibraryUiPrefs,
    ) { aqiqah, library ->
        FamilyLifeUiState(
            birthDate = aqiqah.birthDate,
            aqiqahReminderEnabled = aqiqah.reminderEnabled,
            aqiqahReminderDay = aqiqah.reminderDay,
            favoriteArticleIds = library.favoriteArticleIds,
            recentArticleIds = library.recentArticleIds,
            completedChecklistItemIds = library.completedChecklistItemIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyLifeUiState())

    init {
        viewModelScope.launch {
            val birthDate = aqiqahPrefsRepository.birthDate.first()
            val reminderEnabled = aqiqahPrefsRepository.reminderEnabled.first()
            val reminderDay = aqiqahPrefsRepository.reminderDay.first()
            if (reminderEnabled && birthDate != null) {
                AqiqahReminderScheduler.schedule(context, birthDate, reminderDay)
            }
        }
    }

    fun setBirthDate(date: LocalDate?) {
        viewModelScope.launch {
            aqiqahPrefsRepository.setBirthDate(date)
            if (state.value.aqiqahReminderEnabled) {
                if (date == null || !AqiqahReminderScheduler.schedule(
                        context,
                        date,
                        state.value.aqiqahReminderDay,
                    )
                ) {
                    aqiqahPrefsRepository.setReminderEnabled(false)
                    AqiqahReminderScheduler.cancel(context)
                }
            }
        }
    }


    fun setAqiqahReminderDay(day: AqiqahReminderDay) {
        viewModelScope.launch {
            aqiqahPrefsRepository.setReminderDay(day)
            val current = state.value
            if (current.aqiqahReminderEnabled && current.birthDate != null) {
                if (!AqiqahReminderScheduler.schedule(context, current.birthDate, day)) {
                    aqiqahPrefsRepository.setReminderEnabled(false)
                    AqiqahReminderScheduler.cancel(context)
                }
            }
        }
    }


    fun toggleArticleFavorite(articleId: String) {
        viewModelScope.launch {
            val shouldFavorite = articleId !in state.value.favoriteArticleIds
            familyLibraryPrefsRepository.setArticleFavorite(articleId, shouldFavorite)
        }
    }

    fun recordArticleOpened(articleId: String) {
        viewModelScope.launch {
            familyLibraryPrefsRepository.recordArticleOpened(articleId)
        }
    }

    fun clearReadingHistory() {
        viewModelScope.launch {
            familyLibraryPrefsRepository.clearRecentArticles()
        }
    }

    fun setChecklistItemCompleted(
        checklistId: String,
        itemId: String,
        completed: Boolean,
    ) {
        viewModelScope.launch {
            familyLibraryPrefsRepository.setChecklistItemCompleted(
                checklistId = checklistId,
                itemId = itemId,
                completed = completed,
            )
        }
    }

    /** Returns false when no future selected-day reminder can be scheduled. */
    fun setAqiqahReminderEnabled(enabled: Boolean): Boolean {
        val date = state.value.birthDate
        if (enabled && date == null) return false
        viewModelScope.launch {
            if (enabled && date != null) {
                if (AqiqahReminderScheduler.schedule(
                        context,
                        date,
                        state.value.aqiqahReminderDay,
                    )
                ) {
                    aqiqahPrefsRepository.setReminderEnabled(true)
                }
            } else {
                aqiqahPrefsRepository.setReminderEnabled(false)
                AqiqahReminderScheduler.cancel(context)
            }
        }
        return true
    }
}
