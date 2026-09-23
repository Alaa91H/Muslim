package org.muslim.app.feature.tasbih.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.tasbih.data.TasbihActionCoordinator
import org.muslim.app.feature.tasbih.data.TasbihRepository
import org.muslim.app.feature.tasbih.data.TasbihSessionRepository
import org.muslim.app.feature.tasbih.domain.TargetSoundSettings
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionHistoryItem
import org.muslim.app.feature.tasbih.domain.TasbihSessionMode
import org.muslim.app.feature.tasbih.domain.TasbihState
import javax.inject.Inject

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val repository: TasbihRepository,
    private val actionCoordinator: TasbihActionCoordinator,
    sessionRepository: TasbihSessionRepository,
) : ViewModel() {

    /** One full round of the active phrase reached a multiple of the target. */
    data class RoundCompleted(val count: Int, val round: Int)

    private val _roundCompleted = MutableSharedFlow<RoundCompleted>(extraBufferCapacity = 1)

    val state: StateFlow<TasbihState> = repository.state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TasbihState(emptyMap(), 33, TasbihPhrase.SubhanAllah, emptyList()),
        )

    /** Emitted whenever a full round completes (count reaches a multiple of the target). */
    val roundCompleted: SharedFlow<RoundCompleted> = _roundCompleted.asSharedFlow()

    /** Durable recent sessions, ready for statistics/history UI. */
    val sessionHistory: StateFlow<List<TasbihSessionHistoryItem>> = sessionRepository.observeRecent()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    /** The current continuous session, independent from today's aggregate count. */
    val activeSession: StateFlow<TasbihSessionHistoryItem?> = sessionRepository.observeActive()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )

    fun increment() = viewModelScope.launch {
        val current = state.value
        val transition = actionCoordinator.increment(
            phrase = current.phrase,
            target = current.target,
            mode = current.sessionMode,
            roundsGoal = current.roundsGoal,
        )
        if (transition?.roundCompleted == true) {
            _roundCompleted.tryEmit(
                RoundCompleted(
                    count = transition.state.count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    round = transition.state.completedRounds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                ),
            )
        }
    }

    fun decrement() = viewModelScope.launch {
        val current = state.value
        actionCoordinator.decrement(
            phrase = current.phrase,
            target = current.target,
            mode = current.sessionMode,
            roundsGoal = current.roundsGoal,
        )
    }

    fun reset() = viewModelScope.launch { actionCoordinator.reset(state.value.phrase) }

    fun resetAll() = viewModelScope.launch { actionCoordinator.resetAll() }

    fun setTarget(target: Int) {
        val current = state.value
        if (target == current.target) return
        viewModelScope.launch {
            actionCoordinator.configureSession(
                mode = current.sessionMode,
                target = target,
                roundsGoal = current.roundsGoal,
            )
        }
    }

    fun setSessionMode(mode: TasbihSessionMode) {
        val current = state.value
        if (mode == current.sessionMode) return
        viewModelScope.launch {
            actionCoordinator.configureSession(
                mode = mode,
                target = current.target,
                roundsGoal = current.roundsGoal,
            )
        }
    }

    fun setRoundsGoal(roundsGoal: Int) {
        val current = state.value
        val normalized = roundsGoal.coerceIn(1, TasbihRepository.MAX_ROUNDS_GOAL)
        if (normalized == current.roundsGoal) return
        viewModelScope.launch {
            actionCoordinator.configureSession(
                mode = current.sessionMode,
                target = current.target,
                roundsGoal = normalized,
            )
        }
    }

    fun applySessionPreset(
        mode: TasbihSessionMode,
        target: Int,
        roundsGoal: Int,
    ) {
        val current = state.value
        val normalizedTarget = target.coerceIn(1, 100_000)
        val normalizedRounds = roundsGoal.coerceIn(1, TasbihRepository.MAX_ROUNDS_GOAL)
        if (
            current.sessionMode == mode &&
            current.target == normalizedTarget &&
            current.roundsGoal == normalizedRounds
        ) {
            return
        }
        viewModelScope.launch {
            actionCoordinator.configureSession(mode, normalizedTarget, normalizedRounds)
        }
    }

    fun setPhrase(phrase: TasbihPhrase) {
        if (phrase == state.value.phrase) return
        viewModelScope.launch { actionCoordinator.setPhrase(phrase) }
    }

    /** Sound-on-target preferences for the whole misbaha session. */
    val targetSoundSettings: StateFlow<TargetSoundSettings> = repository.targetSoundSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TargetSoundSettings())

    fun setTargetSoundEnabled(enabled: Boolean) =
        viewModelScope.launch { repository.setTargetSoundEnabled(enabled) }
}
