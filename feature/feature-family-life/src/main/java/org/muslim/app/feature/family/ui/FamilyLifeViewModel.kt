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
import org.muslim.app.feature.family.domain.AqiqahReminderDay
import java.time.LocalDate
import javax.inject.Inject

data class FamilyLifeUiState(
    val birthDate: LocalDate? = null,
    val aqiqahReminderEnabled: Boolean = false,
    val aqiqahReminderDay: AqiqahReminderDay = AqiqahReminderDay.Seventh,
)

@HiltViewModel
class FamilyLifeViewModel @Inject constructor(
    private val aqiqahPrefsRepository: AqiqahPrefsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val state: StateFlow<FamilyLifeUiState> = combine(
        aqiqahPrefsRepository.birthDate,
        aqiqahPrefsRepository.reminderEnabled,
        aqiqahPrefsRepository.reminderDay,
    ) { birthDate, reminderEnabled, reminderDay ->
        FamilyLifeUiState(birthDate, reminderEnabled, reminderDay)
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
