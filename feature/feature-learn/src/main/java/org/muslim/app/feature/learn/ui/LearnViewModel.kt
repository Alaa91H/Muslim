package org.muslim.app.feature.learn.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationPrefsRepository
import org.muslim.app.feature.learn.data.HajjCompanionScheduler
import org.muslim.app.feature.learn.data.HajjPrefsRepository
import org.muslim.app.feature.learn.data.LearnPrefsRepository
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val prefsRepository: LearnPrefsRepository,
    private val hajjPrefsRepository: HajjPrefsRepository,
    private val notificationPrefsRepository: NotificationPrefsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    /** Ids of the topics the user saved as favourites (persisted in DataStore). */
    val favoriteIds: StateFlow<Set<String>> = prefsRepository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            prefsRepository.setFavorite(id, id !in favoriteIds.value)
        }
    }

    /**
     * Keys ("topicId:stepIndex") of the Hajj/Umrah steps the pilgrim marked as
     * done — the interactive checklist progress (persisted in DataStore).
     */
    val hajjCheckedSteps: StateFlow<Set<String>> = hajjPrefsRepository.checkedStepKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleHajjStep(topicId: String, stepIndex: Int) {
        viewModelScope.launch {
            val key = "$topicId:$stepIndex"
            hajjPrefsRepository.setStepChecked(key, key !in hajjCheckedSteps.value)
        }
    }

    /**
     * Whether the Pilgrim Companion (daily Hajj rite reminders) is enabled.
     * Backed by the unified notification manager's Hajj category, so the
     * master switch in the notification settings stays in sync with the
     * Hajj screen.
     */
    val hajjCompanionEnabled: StateFlow<Boolean> = notificationPrefsRepository.prefs
        .map { it[NotificationCategory.Hajj]?.enabled ?: NotificationCategory.Hajj.defaultEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationCategory.Hajj.defaultEnabled)

    /** Toggles the Pilgrim Companion and (un)schedules its daily reminder. */
    fun setHajjCompanionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPrefsRepository.setEnabled(NotificationCategory.Hajj, enabled)
            if (enabled) {
                HajjCompanionScheduler.schedule(context)
            } else {
                HajjCompanionScheduler.cancel(context)
            }
        }
    }
}
