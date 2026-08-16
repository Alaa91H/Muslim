package org.example.islamicapp.feature.zakat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.feature.zakat.data.ZakatRecord
import org.example.islamicapp.feature.zakat.data.ZakatRecordsRepository
import org.example.islamicapp.feature.zakat.domain.ZakatAlFitrCalculator
import org.example.islamicapp.feature.zakat.domain.ZakatCalculator
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class ZakatUiState(
    // ---- Zakat al-mal inputs ----
    val cash: String = "",
    val goldGrams: String = "",
    val silverGrams: String = "",
    val tradeGoods: String = "",
    val investments: String = "",
    val debts: String = "",
    val goldPrice: String = "",
    val silverPrice: String = "",
    val hawlCompleted: Boolean = true,
    val malResult: ZakatCalculator.Result? = null,

    // ---- Zakat al-fitr inputs ----
    val familyMembers: String = "1",
    val staplePrice: String = "",
    val fitrResult: ZakatAlFitrCalculator.Result? = null,

    val records: List<ZakatRecord> = emptyList(),
    /** Which tab is open: 0 = mal, 1 = fitr. */
    val tab: Int = 0,
)

@HiltViewModel
class ZakatViewModel @Inject constructor(
    private val recordsRepository: ZakatRecordsRepository,
) : ViewModel() {

    private val local = MutableStateFlow(ZakatUiState())

    val uiState: StateFlow<ZakatUiState> =
        combine(local, recordsRepository.records) { state, records ->
            state.copy(
                malResult = state.malResult,
                fitrResult = state.fitrResult,
                records = records,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ZakatUiState())

    fun update(transform: (ZakatUiState) -> ZakatUiState) {
        local.value = transform(local.value)
    }

    fun setTab(tab: Int) = update { it.copy(tab = tab) }

    /** Runs the zakat al-mal calculation over the current inputs. */
    fun calculateMal() {
        val s = local.value
        val result = ZakatCalculator.calculate(
            ZakatCalculator.Inputs(
                cash = s.cash.toAmountOrZero(),
                goldGrams = s.goldGrams.toAmountOrZero(),
                silverGrams = s.silverGrams.toAmountOrZero(),
                tradeGoods = s.tradeGoods.toAmountOrZero(),
                investments = s.investments.toAmountOrZero(),
                debts = s.debts.toAmountOrZero(),
                goldPricePerGram = s.goldPrice.toAmountOrZero(),
                silverPricePerGram = s.silverPrice.toAmountOrZero(),
                hawlCompleted = s.hawlCompleted,
            ),
        )
        update { it.copy(malResult = result) }
    }

    fun calculateFitr() {
        val s = local.value
        val members = s.familyMembers.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val result = ZakatAlFitrCalculator.calculate(
            ZakatAlFitrCalculator.Inputs(
                familyMembers = members,
                pricePerKg = s.staplePrice.toAmountOrZero(),
            ),
        )
        update { it.copy(fitrResult = result) }
    }

    fun saveCurrent() {
        val s = local.value
        val date = LocalDate.now().toString()
        val record = if (s.tab == 0) {
            val r = s.malResult ?: return
            ZakatRecord(date, "mal", "زكاة المال", r.zakatDue.toPlainString())
        } else {
            val r = s.fitrResult ?: return
            ZakatRecord(date, "fitr", "زكاة الفطر", r.totalCash.toPlainString())
        }
        viewModelScope.launch { recordsRepository.add(record) }
    }

    fun clearRecords() {
        viewModelScope.launch { recordsRepository.clear() }
    }

    private fun String.toAmountOrZero(): BigDecimal =
        toBigDecimalOrNull() ?: BigDecimal.ZERO
}
