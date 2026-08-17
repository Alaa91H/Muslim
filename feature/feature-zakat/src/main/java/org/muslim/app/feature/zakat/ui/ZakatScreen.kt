package org.muslim.app.feature.zakat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.zakat.R
import java.text.NumberFormat
import java.time.LocalDate

/**
 * Zakat calculator (PROJECT_PROMPT.md §6 Phase 7): money zakat with nisab
 * check, fitr zakat and a yearly history. Works fully offline — gold/silver
 * prices are entered manually (network updates are an optional future step).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ZakatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formatter = NumberFormat.getNumberInstance()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.zakat_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.zakat_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.zakat_money_section),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            MoneyForm(state, viewModel, formatter)
            Spacer(Modifier.height(12.dp))
            ResultCard(state, formatter, viewModel)
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            FitrSection(state, viewModel, formatter)
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            HistorySection(state, viewModel, formatter)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.zakat_rulings_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoneyForm(
    state: ZakatUiState,
    viewModel: ZakatViewModel,
    formatter: NumberFormat,
) {
    val input = state.input
    NumberField(stringResource(R.string.zakat_cash), input.cash, formatter, viewModel::setCash)
    NumberField(stringResource(R.string.zakat_gold_grams), input.goldGrams, formatter, viewModel::setGoldGrams)
    NumberField(stringResource(R.string.zakat_gold_price), input.goldPricePerGram, formatter, viewModel::setGoldPrice)
    NumberField(stringResource(R.string.zakat_silver_grams), input.silverGrams, formatter, viewModel::setSilverGrams)
    NumberField(stringResource(R.string.zakat_silver_price), input.silverPricePerGram, formatter, viewModel::setSilverPrice)
    NumberField(stringResource(R.string.zakat_trade), input.tradeGoods, formatter, viewModel::setTradeGoods)
    NumberField(stringResource(R.string.zakat_investments), input.investments, formatter, viewModel::setInvestments)
    NumberField(stringResource(R.string.zakat_debts), input.debtsOwed, formatter, viewModel::setDebtsOwed)
}

@Composable
private fun NumberField(
    label: String,
    value: Double,
    formatter: NumberFormat,
    onValueChange: (Double) -> Unit,
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else formatter.format(value),
        onValueChange = { raw ->
            val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
            onValueChange(cleaned.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun ResultCard(
    state: ZakatUiState,
    formatter: NumberFormat,
    viewModel: ZakatViewModel,
) {
    val result = state.result
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.nisabExceeded) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultRow(stringResource(R.string.zakat_total_assets), formatter.format(result.totalAssets))
            ResultRow(stringResource(R.string.zakat_zakatable), formatter.format(result.zakatableAmount))
            ResultRow(stringResource(R.string.zakat_nisab_gold), formatter.format(result.goldNisab))
            ResultRow(stringResource(R.string.zakat_nisab_silver), formatter.format(result.silverNisab))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (result.nisabExceeded) R.string.zakat_nisab_met else R.string.zakat_nisab_not_met
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.zakat_due, formatter.format(result.zakatDue)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (result.zakatableAmount > 0) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::saveCalculation) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.zakat_save_result))
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun FitrSection(state: ZakatUiState, viewModel: ZakatViewModel, formatter: NumberFormat) {
    Text(
        text = stringResource(R.string.zakat_fitr_section),
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(8.dp))
    NumberField(stringResource(R.string.zakat_fitr_saa_value), state.fitrSaaValue, formatter, viewModel::setFitrSaaValue)
    OutlinedTextField(
        value = state.fitrPersons.toString(),
        onValueChange = { raw -> raw.toIntOrNull()?.let(viewModel::setFitrPersons) },
        label = { Text(stringResource(R.string.zakat_fitr_persons)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
    Text(
        text = stringResource(R.string.zakat_fitr_total, formatter.format(state.fitrTotal)),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 6.dp),
    )
}

@Composable
private fun HistorySection(state: ZakatUiState, viewModel: ZakatViewModel, formatter: NumberFormat) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.zakat_history),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (state.history.isNotEmpty()) {
            IconButton(onClick = viewModel::clearHistory) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.zakat_clear_history))
            }
        }
    }
    if (state.history.isEmpty()) {
        Text(
            text = stringResource(R.string.zakat_history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    state.history.forEach { entry ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = runCatching { LocalDate.parse(entry.date) }.getOrNull()?.toString() ?: entry.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.zakat_history_amount, formatter.format(entry.zakatDue)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
