package org.example.islamicapp.feature.zakat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.islamicapp.feature.zakat.R

/**
 * Zakat calculators (PROJECT_PROMPT.md §6 Phase 7): zakat al-mal with
 * nisab detection and per-field guidance, and zakat al-fitr — both fully
 * offline with manual price entry (optional online prices are future work).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    modifier: Modifier = Modifier,
    viewModel: ZakatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.zakat_title)) }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = state.tab) {
                Tab(
                    selected = state.tab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text(stringResource(R.string.zakat_mal_tab)) },
                )
                Tab(
                    selected = state.tab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text(stringResource(R.string.zakat_fitr_tab)) },
                )
            }
            if (state.tab == 0) MalTab(state, viewModel) else FitrTab(state, viewModel)
        }
    }
}

@Composable
private fun MalTab(state: ZakatUiState, viewModel: ZakatViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.zakat_mal_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_cash),
                supporting = stringResource(R.string.zakat_cash_hint),
                value = state.cash,
                onValueChange = { v -> viewModel.update { it.copy(cash = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_gold_grams),
                supporting = stringResource(R.string.zakat_gold_hint),
                value = state.goldGrams,
                onValueChange = { v -> viewModel.update { it.copy(goldGrams = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_silver_grams),
                supporting = stringResource(R.string.zakat_silver_hint),
                value = state.silverGrams,
                onValueChange = { v -> viewModel.update { it.copy(silverGrams = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_trade_goods),
                supporting = stringResource(R.string.zakat_trade_hint),
                value = state.tradeGoods,
                onValueChange = { v -> viewModel.update { it.copy(tradeGoods = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_investments),
                supporting = stringResource(R.string.zakat_invest_hint),
                value = state.investments,
                onValueChange = { v -> viewModel.update { it.copy(investments = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_debts),
                supporting = stringResource(R.string.zakat_debts_hint),
                value = state.debts,
                onValueChange = { v -> viewModel.update { it.copy(debts = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_gold_price),
                supporting = stringResource(R.string.zakat_price_hint),
                value = state.goldPrice,
                onValueChange = { v -> viewModel.update { it.copy(goldPrice = v) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_silver_price),
                supporting = stringResource(R.string.zakat_price_hint),
                value = state.silverPrice,
                onValueChange = { v -> viewModel.update { it.copy(silverPrice = v) } },
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.zakat_hawl),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = state.hawlCompleted,
                    onCheckedChange = { v -> viewModel.update { it.copy(hawlCompleted = v) } },
                )
            }
        }
        item {
            Button(onClick = viewModel::calculateMal, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.zakat_calculate))
            }
        }
        state.malResult?.let { result ->
            item { ResultCard(
                title = stringResource(R.string.zakat_mal_result),
                isDue = result.isDue,
                reaches = result.reachesNisab,
                nisab = result.nisab.toPlainString(),
                net = result.netWealth.toPlainString(),
                amount = result.zakatDue.toPlainString(),
                onSave = viewModel::saveCurrent,
            ) }
        }
        if (state.records.isNotEmpty()) {
            item { Text(stringResource(R.string.zakat_records), fontWeight = FontWeight.SemiBold) }
            items(state.records, key = { it.date + it.type + it.amount }) { record ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.summary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                record.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(record.amount, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FitrTab(state: ZakatUiState, viewModel: ZakatViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.zakat_fitr_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_family_members),
                supporting = stringResource(R.string.zakat_family_hint),
                value = state.familyMembers,
                onValueChange = { v -> viewModel.update { it.copy(familyMembers = v.filter(Char::isDigit)) } },
            )
        }
        item {
            AmountField(
                label = stringResource(R.string.zakat_staple_price),
                supporting = stringResource(R.string.zakat_staple_hint),
                value = state.staplePrice,
                onValueChange = { v -> viewModel.update { it.copy(staplePrice = v) } },
            )
        }
        item {
            Button(onClick = viewModel::calculateFitr, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.zakat_calculate))
            }
        }
        state.fitrResult?.let { result ->
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.zakat_fitr_result),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.zakat_fitr_kg, result.totalKilograms.toPlainString()))
                        Text(stringResource(R.string.zakat_fitr_cash, result.totalCash.toPlainString()))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::saveCurrent) {
                            Text(stringResource(R.string.zakat_save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountField(
    label: String,
    supporting: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = { Text(supporting) },
        singleLine = true,
    )
}

@Composable
private fun ResultCard(
    title: String,
    isDue: Boolean,
    reaches: Boolean,
    nisab: String,
    net: String,
    amount: String,
    onSave: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDue) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.zakat_net_wealth, net))
            Text(stringResource(R.string.zakat_nisab, nisab))
            Spacer(Modifier.height(8.dp))
            Text(
                if (isDue) stringResource(R.string.zakat_due_amount, amount)
                else if (!reaches) stringResource(R.string.zakat_below_nisab)
                else stringResource(R.string.zakat_hawl_not_completed, amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSave) { Text(stringResource(R.string.zakat_save)) }
        }
    }
}
