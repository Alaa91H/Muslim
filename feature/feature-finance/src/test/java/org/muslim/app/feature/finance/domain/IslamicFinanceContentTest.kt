package org.muslim.app.feature.finance.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.finance.ui.IslamicFinanceUiState

class IslamicFinanceContentTest {
    @Test
    fun `transactions guide covers trade loans and ecommerce`() {
        assertThat(IslamicFinanceContent.transactionGuides.map(TransactionsGuide::id))
            .containsExactly("trade", "loans", "ecommerce").inOrder()
        assertThat(IslamicFinanceContent.transactionGuides).allMatch { it.points.isNotEmpty() }
    }

    @Test
    fun `screening providers use secure provider pages`() {
        assertThat(ScreeningProvider.entries).hasSize(2)
        assertThat(ScreeningProvider.entries).allMatch { it.url.startsWith("https://") }
    }

    @Test
    fun `ledger totals remain separated by direction and currency`() {
        val state = IslamicFinanceUiState(
            debts = listOf(
                debt("a", DebtDirection.Receivable, 100.0, "USD"),
                debt("b", DebtDirection.Receivable, 75.0, "USD"),
                debt("c", DebtDirection.Receivable, 200.0, "SAR"),
                debt("d", DebtDirection.Payable, 50.0, "USD"),
            ),
        )

        assertThat(state.receivableByCurrency).containsExactly("USD", 175.0, "SAR", 200.0)
        assertThat(state.payableByCurrency).containsExactly("USD", 50.0)
    }

    private fun debt(id: String, direction: DebtDirection, amount: Double, currency: String) = DebtEntry(
        id = id,
        partyName = "Party $id",
        direction = direction,
        amount = amount,
        currency = currency,
        createdAt = "2026-08-22T00:00:00Z",
    )
}
