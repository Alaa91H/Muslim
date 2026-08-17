package org.muslim.app.feature.settings

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationPrefsRepository
import javax.inject.Inject

/**
 * Master notification settings (PROJECT_PROMPT.md §3.3): one switch per
 * [NotificationCategory]. Turning a category off both flips the DataStore
 * flag (consulted by every notifier) and cancels that channel's posted
 * notifications so the change is immediate.
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: NotificationPrefsRepository,
) : ViewModel() {

    val preferences: StateFlow<Map<NotificationCategory, Boolean>> =
        prefsRepository.prefs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setEnabled(category: NotificationCategory, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setEnabled(category, enabled)
            if (!enabled) {
                // Remove already-posted alerts from this channel immediately.
                runCatching {
                    val manager = context.getSystemService(NotificationManager::class.java)
                    manager.activeNotifications
                        .filter { it.notification.channelId == category.channelId }
                        .forEach { manager.cancel(it.id) }
                }
            }
        }
    }
}
