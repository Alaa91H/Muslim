package org.muslim.app.feature.finance.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.muslim.app.feature.finance.data.DebtReminderScheduler
import org.muslim.app.feature.finance.data.DebtRepository
import org.muslim.app.feature.finance.domain.DebtDirection
import org.muslim.app.feature.finance.domain.DebtEntry
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class IslamicFinanceUiState(
    val debts: List<DebtEntry> = emptyList(),
    val reminderUnavailable: Boolean = false,
) {
    val receivableByCurrency: Map<String, Double>
        get() = debts.filter { it.direction == DebtDirection.Receivable }
            .groupBy(DebtEntry::currency)
            .mapValues { (_, entries) -> entries.sumOf(DebtEntry::amount) }

    val payableByCurrency: Map<String, Double>
        get() = debts.filter { it.direction == DebtDirection.Payable }
            .groupBy(DebtEntry::currency)
            .mapValues { (_, entries) -> entries.sumOf(DebtEntry::amount) }
}

@HiltViewModel
class IslamicFinanceViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val reminderUnavailable = MutableStateFlow(false)

    val state: StateFlow<IslamicFinanceUiState> = combine(
        debtRepository.debts,
        reminderUnavailable,
    ) { debts, unavailable ->
        IslamicFinanceUiState(debts = debts, reminderUnavailable = unavailable)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IslamicFinanceUiState())

    fun saveDebt(
        partyName: String,
        direction: DebtDirection,
        amount: Double,
        currency: String,
        dueDate: String?,
        reminderEnabled: Boolean,
        notes: String,
    ) {
        val normalizedDate = dueDate?.trim()?.takeIf(String::isNotBlank)
        val entry = DebtEntry(
            id = UUID.randomUUID().toString(),
            partyName = partyName.trim(),
            direction = direction,
            amount = amount,
            currency = currency.trim().uppercase().ifBlank { "USD" },
            dueDate = normalizedDate,
            reminderEnabled = reminderEnabled,
            notes = notes.trim(),
            createdAt = Instant.now().toString(),
        )
        viewModelScope.launch {
            debtRepository.save(entry)
            val scheduled = if (entry.reminderEnabled) DebtReminderScheduler.schedule(context, entry) else true
            reminderUnavailable.value = entry.reminderEnabled && !scheduled
        }
    }

    fun deleteDebt(entry: DebtEntry) {
        viewModelScope.launch {
            DebtReminderScheduler.cancel(context, entry.id)
            debtRepository.delete(entry.id)
        }
    }

    fun clearReminderNotice() {
        reminderUnavailable.update { false }
    }

    fun isValidDate(value: String): Boolean = value.isBlank() || runCatching { LocalDate.parse(value) }.isSuccess
}
