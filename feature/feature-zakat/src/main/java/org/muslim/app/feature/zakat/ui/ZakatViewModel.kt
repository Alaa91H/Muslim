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
import org.muslim.app.feature.zakat.data.MetalsPriceRepository
import org.muslim.app.feature.zakat.data.ZakatHistoryEntry
import org.muslim.app.feature.zakat.data.ZakatRepository
import org.muslim.app.feature.zakat.domain.CountryCurrencies
import org.muslim.app.feature.zakat.domain.CountryCurrency
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
    /** Catalog of countries/currencies for the global calculator. */
    val countries: List<CountryCurrency> = CountryCurrencies.ALL,
    /** The user's chosen country, resolved from the persisted country code. */
    val selectedCountry: CountryCurrency? = null,
    /** True when live prices are fetched automatically. */
    val autoPrices: Boolean = false,
    /** True while a live-price fetch is in flight. */
    val isFetching: Boolean = false,
    /** True when the most recent live-price fetch failed. */
    val fetchFailed: Boolean = false,
    /** ISO timestamp of the last successful live-price fetch, if any. */
    val lastUpdatedAt: String? = null,
) {
    val currencySymbol: String get() = selectedCountry?.symbol ?: ""
    val currencyCode: String get() = selectedCountry?.currency ?: ""
}

@HiltViewModel
class ZakatViewModel @Inject constructor(
    private val repository: ZakatRepository,
    private val metalsRepository: MetalsPriceRepository,
) : ViewModel() {

    private val fitrPersons = MutableStateFlow(1)
    private val isFetching = MutableStateFlow(false)
    private val fetchFailed = MutableStateFlow(false)

    val state: StateFlow<ZakatUiState> = combine(
        repository.preferences,
        fitrPersons,
        isFetching,
        fetchFailed,
    ) { prefs, persons, fetching, failed ->
        val input = prefs.lastInput
        val result = ZakatCalculator.calculate(input)
        ZakatUiState(
            input = input,
            result = result,
            fitrSaaValue = prefs.fitrSaaValue,
            fitrPersons = persons,
            fitrTotal = ZakatCalculator.fitrTotal(prefs.fitrSaaValue, persons),
            history = prefs.history,
            selectedCountry = CountryCurrencies.byCountry(prefs.countryCode),
            autoPrices = prefs.autoPrices,
            isFetching = fetching,
            fetchFailed = failed,
            lastUpdatedAt = prefs.lastUpdatedAt,
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

    /** Remembers the chosen country and refreshes prices when auto mode is on. */
    fun selectCountry(code: String) {
        viewModelScope.launch {
            repository.saveCountry(code)
            if (repository.preferences.first().autoPrices) fetchPrices()
        }
    }

    /** Turns auto-fetching on/off; enabling it fetches once immediately. */
    fun setAutoPrices(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoPrices(enabled)
            if (enabled) fetchPrices()
        }
    }

    /** Fetches live gold/silver prices for the selected country's currency. */
    fun fetchPrices() {
        val currency = state.value.currencyCode
        if (currency.isEmpty() || isFetching.value) return
        viewModelScope.launch {
            isFetching.value = true
            fetchFailed.value = false
            when (val result = metalsRepository.fetchPrices(currency)) {
                is MetalsPriceRepository.Result.Success -> {
                    repository.saveLivePrices(
                        goldPerGram = result.metals.goldPerGram,
                        silverPerGram = result.metals.silverPerGram,
                        updatedAt = result.metals.updatedAtUtc,
                    )
                }
                MetalsPriceRepository.Result.Failure -> fetchFailed.value = true
            }
            isFetching.value = false
        }
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
