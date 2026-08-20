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
import org.muslim.app.feature.tasbih.data.TasbihRepository
import org.muslim.app.feature.tasbih.domain.TargetSoundSettings
import org.muslim.app.feature.tasbih.domain.TasbihCounter
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihState
import javax.inject.Inject

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val repository: TasbihRepository,
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

    fun increment() = viewModelScope.launch {
        val current = state.value
        val newCount = current.count + 1
        repository.increment(current.phrase)
        if (TasbihCounter.completesRound(newCount, current.target)) {
            _roundCompleted.tryEmit(
                RoundCompleted(
                    count = newCount,
                    round = TasbihCounter.roundNumberAt(newCount, current.target),
                ),
            )
        }
    }

    fun decrement() = viewModelScope.launch { repository.decrement(state.value.phrase) }

    fun reset() = viewModelScope.launch { repository.reset(state.value.phrase) }

    fun resetAll() = viewModelScope.launch { repository.resetAll() }

    fun setTarget(target: Int) = viewModelScope.launch { repository.setTarget(target) }

    fun setPhrase(phrase: TasbihPhrase) = viewModelScope.launch { repository.setPhrase(phrase) }

    /** Sound-on-target preferences for the whole misbaha session. */
    val targetSoundSettings: StateFlow<TargetSoundSettings> = repository.targetSoundSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TargetSoundSettings())

    fun setTargetSoundEnabled(enabled: Boolean) =
        viewModelScope.launch { repository.setTargetSoundEnabled(enabled) }
}
