package org.muslim.app.feature.zakat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.zakat.data.ZakatHistoryEntry
import org.muslim.app.feature.zakat.data.ZakatRepository
import org.muslim.app.feature.zakat.domain.ZakatCalculator
import org.muslim.app.feature.zakat.domain.ZakatInput
import org.muslim.app.feature.zakat.domain.ZakatResult
import javax.inject.Inject

/** UI state: the raw input fields plus the computed result. */
data class ZakatUiState(
    val input: ZakatInput = ZakatInput(),
    val result: ZakatResult = ZakatCalculator.calculate(ZakatInput()),
    val fitrSaaValue: Double = 25.0,
    val fitrPersons: Int = 1,
    val fitrTotal: Double = 0.0,
    val history: List<ZakatHistoryEntry> = emptyList(),
)

@HiltViewModel
class ZakatViewModel @Inject constructor(
    private val repository: ZakatRepository,
) : ViewModel() {

    private val fitrPersons = MutableStateFlow(1)

    val state: StateFlow<ZakatUiState> = combine(
        repository.preferences,
        fitrPersons,
    ) { prefs, persons ->
        val input = prefs.lastInput
        val result = ZakatCalculator.calculate(input)
        ZakatUiState(
            input = input,
            result = result,
            fitrSaaValue = prefs.fitrSaaValue,
            fitrPersons = persons,
            fitrTotal = ZakatCalculator.fitrTotal(prefs.fitrSaaValue, persons),
            history = prefs.history,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZakatUiState())

    fun updateInput(transform: (ZakatInput) -> ZakatInput) {
        viewModelScope.launch {
            val current = repository.preferences.first().lastInput
            repository.saveInput(transform(current))
        }
    }

    fun setCash(value: Double) = updateInput { it.copy(cash = value) }
    fun setGoldGrams(value: Double) = updateInput { it.copy(goldGrams = value) }
    fun setGoldPrice(value: Double) = updateInput { it.copy(goldPricePerGram = value) }
    fun setSilverGrams(value: Double) = updateInput { it.copy(silverGrams = value) }
    fun setSilverPrice(value: Double) = updateInput { it.copy(silverPricePerGram = value) }
    fun setTradeGoods(value: Double) = updateInput { it.copy(tradeGoods = value) }
    fun setInvestments(value: Double) = updateInput { it.copy(investments = value) }
    fun setDebtsOwed(value: Double) = updateInput { it.copy(debtsOwed = value) }

    fun setFitrSaaValue(value: Double) {
        viewModelScope.launch { repository.saveFitrSaaValue(value) }
    }

    fun setFitrPersons(value: Int) {
        fitrPersons.value = value.coerceIn(1, 100)
    }

    fun saveCalculation() {
        viewModelScope.launch {
            val current = state.value
            if (current.result.zakatableAmount > 0) {
                repository.addHistoryEntry(current.result.zakatableAmount, current.result.zakatDue)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }
}
