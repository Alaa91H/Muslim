package org.muslim.app.feature.finance.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.common.text.Digits
import org.muslim.app.feature.finance.R
import org.muslim.app.feature.finance.domain.DebtDirection
import org.muslim.app.feature.finance.domain.DebtEntry
import org.muslim.app.feature.finance.domain.IslamicFinanceContent
import org.muslim.app.feature.finance.domain.LocalizedFinanceText
import org.muslim.app.feature.finance.domain.ScreeningProvider
import org.muslim.app.feature.finance.domain.TransactionsGuide
import java.text.NumberFormat
import java.util.Locale

private enum class FinanceTab(val icon: ImageVector) {
    Transactions(Icons.AutoMirrored.Filled.MenuBook),
    Stocks(Icons.Filled.TrendingUp),
    Debts(Icons.Filled.AccountBalanceWallet),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicFinanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IslamicFinanceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isArabic = AppLanguage.isArabicUi()
    var selectedTab by rememberSaveable { mutableIntStateOf(FinanceTab.Transactions.ordinal) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.finance_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.finance_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                FinanceTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = {
                            Text(
                                when (tab) {
                                    FinanceTab.Transactions -> stringResource(R.string.finance_tab_transactions)
                                    FinanceTab.Stocks -> stringResource(R.string.finance_tab_stocks)
                                    FinanceTab.Debts -> stringResource(R.string.finance_tab_debts)
                                },
                            )
                        },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
            when (FinanceTab.entries[selectedTab]) {
                FinanceTab.Transactions -> TransactionsContent(isArabic = isArabic)
                FinanceTab.Stocks -> StockCheckerContent(isArabic = isArabic)
                FinanceTab.Debts -> DebtLedgerContent(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun TransactionsContent(isArabic: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FinanceIntroCard(
                icon = Icons.Filled.AccountBalance,
                title = stringResource(R.string.finance_transactions_title),
                text = stringResource(R.string.finance_transactions_intro),
            )
        }
        item {
            FinanceNoticeCard(stringResource(R.string.finance_transactions_notice))
        }
        items(IslamicFinanceContent.transactionGuides, key = TransactionsGuide::id) { guide ->
            TransactionsGuideCard(guide, isArabic)
        }
    }
}

@Composable
private fun TransactionsGuideCard(guide: TransactionsGuide, isArabic: Boolean) {
    var expanded by rememberSaveable(guide.id) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(guide.title.pick(isArabic), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(guide.summary.pick(isArabic), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                guide.points.forEachIndexed { index, point ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(point.pick(isArabic), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(guide.reference.pick(isArabic), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun StockCheckerContent(isArabic: Boolean) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var selectedProvider by rememberSaveable { mutableStateOf(ScreeningProvider.Zoya) }
    var showQueryError by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FinanceIntroCard(
                icon = Icons.Filled.TrendingUp,
                title = stringResource(R.string.finance_stocks_title),
                text = stringResource(R.string.finance_stocks_intro),
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    showQueryError = false
                },
                label = { Text(stringResource(R.string.finance_stock_ticker)) },
                placeholder = { Text(stringResource(R.string.finance_stock_ticker_hint)) },
                isError = showQueryError,
                supportingText = if (showQueryError) {
                    { Text(stringResource(R.string.finance_stock_empty_ticker)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(stringResource(R.string.finance_stock_provider), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                ScreeningProvider.entries.forEach { provider ->
                    FilterChip(
                        selected = selectedProvider == provider,
                        onClick = { selectedProvider = provider },
                        label = { Text(provider.label.pick(isArabic)) },
                    )
                }
            }
        }
        item {
            FinanceNoticeCard(selectedProvider.availability.pick(isArabic))
        }
        item {
            Text(stringResource(R.string.finance_stock_provider_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(
                onClick = {
                    if (query.trim().isBlank()) showQueryError = true
                    else openScreeningProvider(context, selectedProvider, query)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.finance_stock_open_provider))
            }
        }
        item {
            FinanceNoticeCard(stringResource(R.string.finance_stock_disclaimer))
        }
    }
}

@Composable
private fun DebtLedgerContent(state: IslamicFinanceUiState, viewModel: IslamicFinanceViewModel) {
    var partyName by rememberSaveable { mutableStateOf("") }
    var direction by rememberSaveable { mutableStateOf(DebtDirection.Receivable) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("USD") }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var notes by rememberSaveable { mutableStateOf("") }
    var showValidationError by rememberSaveable { mutableStateOf(false) }
    val formatter = remember { NumberFormat.getNumberInstance(Locale.ENGLISH) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FinanceIntroCard(
                icon = Icons.Filled.AccountBalanceWallet,
                title = stringResource(R.string.finance_debts_title),
                text = stringResource(R.string.finance_debts_intro),
            )
        }
        item { FinanceNoticeCard(stringResource(R.string.finance_debts_quran_note)) }
        item {
            OutlinedTextField(
                value = partyName,
                onValueChange = { partyName = it; showValidationError = false },
                label = { Text(stringResource(R.string.finance_debt_party)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(stringResource(R.string.finance_debt_direction), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                FilterChip(
                    selected = direction == DebtDirection.Receivable,
                    onClick = { direction = DebtDirection.Receivable },
                    label = { Text(stringResource(R.string.finance_debt_receivable)) },
                )
                FilterChip(
                    selected = direction == DebtDirection.Payable,
                    onClick = { direction = DebtDirection.Payable },
                    label = { Text(stringResource(R.string.finance_debt_payable)) },
                )
            }
        }
        item {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = Digits.toWesternDigits(it).replace(',', '.').filter { char -> char.isDigit() || char == '.' }
                    showValidationError = false
                },
                label = { Text(stringResource(R.string.finance_debt_amount)) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it.uppercase().take(8) },
                label = { Text(stringResource(R.string.finance_debt_currency)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it; showValidationError = false },
                label = { Text(stringResource(R.string.finance_debt_due_date)) },
                placeholder = { Text(stringResource(R.string.finance_debt_due_date_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.finance_debt_reminder), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.finance_debt_reminder_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.finance_debt_notes)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showValidationError) {
            item { Text(stringResource(R.string.finance_debt_invalid), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
        if (state.reminderUnavailable) {
            item {
                Text(
                    stringResource(R.string.finance_debt_reminder_unavailable),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        item {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (partyName.isBlank() || amount == null || amount <= 0.0 || !viewModel.isValidDate(dueDate)) {
                        showValidationError = true
                    } else {
                        viewModel.saveDebt(partyName, direction, amount, currency, dueDate, reminderEnabled, notes)
                        partyName = ""
                        direction = DebtDirection.Receivable
                        amountText = ""
                        currency = "USD"
                        dueDate = ""
                        reminderEnabled = false
                        notes = ""
                        showValidationError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.finance_debt_save))
            }
        }
        item { DebtSummaryCard(state, formatter) }
        item { Text(stringResource(R.string.finance_debt_saved), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (state.debts.isEmpty()) {
            item { Text(stringResource(R.string.finance_debt_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.debts, key = DebtEntry::id) { entry ->
                DebtCard(entry, formatter, onDelete = { viewModel.deleteDebt(entry) })
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(state: IslamicFinanceUiState, formatter: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            DebtSummaryRows(stringResource(R.string.finance_debt_summary_receivable), state.receivableByCurrency, formatter)
            Spacer(Modifier.height(8.dp))
            DebtSummaryRows(stringResource(R.string.finance_debt_summary_payable), state.payableByCurrency, formatter)
        }
    }
}

@Composable
private fun DebtSummaryRows(label: String, amounts: Map<String, Double>, formatter: NumberFormat) {
    Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    if (amounts.isEmpty()) {
        Text("—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        amounts.toSortedMap().forEach { (currency, total) ->
            Text("${formatter.format(total)} $currency", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DebtCard(entry: DebtEntry, formatter: NumberFormat, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(
                    if (entry.direction == DebtDirection.Receivable) Icons.Filled.TrendingUp else Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.padding(9.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.partyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${formatter.format(entry.amount)} ${entry.currency}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                entry.dueDate?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (entry.reminderEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.finance_debt_reminder), style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (entry.notes.isNotBlank()) Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.finance_debt_delete))
            }
        }
    }
}

@Composable
private fun FinanceIntroCard(icon: ImageVector, title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun FinanceNoticeCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun LocalizedFinanceText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun openScreeningProvider(context: Context, provider: ScreeningProvider, query: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("stock_ticker", query.trim()))
    context.startActivity(Intent(Intent.ACTION_VIEW, provider.url.toUri()))
}
