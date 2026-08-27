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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.Saver
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
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
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
    Stocks(Icons.AutoMirrored.Filled.TrendingUp),
    Debts(Icons.Filled.AccountBalanceWallet),
}

private data class DebtDraft(
    val partyName: String = "",
    val direction: DebtDirection = DebtDirection.Receivable,
    val amountText: String = "",
    val currency: String = "USD",
    val dueDate: String = "",
    val reminderEnabled: Boolean = false,
    val notes: String = "",
)

private val DebtDraftSaver: Saver<DebtDraft, List<Any>> = Saver(
    save = { draft ->
        listOf(
            draft.partyName,
            draft.direction.name,
            draft.amountText,
            draft.currency,
            draft.dueDate,
            draft.reminderEnabled,
            draft.notes,
        )
    },
    restore = { values ->
        DebtDraft(
            partyName = values[0] as String,
            direction = DebtDirection.valueOf(values[1] as String),
            amountText = values[2] as String,
            currency = values[3] as String,
            dueDate = values[4] as String,
            reminderEnabled = values[5] as Boolean,
            notes = values[6] as String,
        )
    },
)

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
    IslamicCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        containerColor = if (expanded) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Column {
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
                icon = Icons.AutoMirrored.Filled.TrendingUp,
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
            MuslimSectionHeader(title = stringResource(R.string.finance_stock_provider))
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
            IslamicPrimaryButton(
                onClick = {
                    if (query.trim().isBlank()) showQueryError = true
                    else openScreeningProvider(context, selectedProvider, query)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
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
    var draft by rememberSaveable(stateSaver = DebtDraftSaver) { mutableStateOf(DebtDraft()) }
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
            DebtEntryForm(
                draft = draft,
                onDraftChange = {
                    draft = it
                    showValidationError = false
                },
                showValidationError = showValidationError,
                reminderUnavailable = state.reminderUnavailable,
                onSave = {
                    val amount = draft.amountText.toDoubleOrNull()
                    if (draft.partyName.isBlank() || amount == null || amount <= 0.0 || !viewModel.isValidDate(draft.dueDate)) {
                        showValidationError = true
                    } else {
                        viewModel.saveDebt(
                            draft.partyName,
                            draft.direction,
                            amount,
                            draft.currency,
                            draft.dueDate,
                            draft.reminderEnabled,
                            draft.notes,
                        )
                        draft = DebtDraft()
                        showValidationError = false
                    }
                },
            )
        }
        item { DebtSummaryCard(state, formatter) }
        item {
            MuslimSectionHeader(title = stringResource(R.string.finance_debt_saved))
        }
        if (state.debts.isEmpty()) {
            item {
                MuslimStateSurface(
                    title = stringResource(R.string.finance_debt_empty),
                    tone = MuslimStateTone.Neutral,
                )
            }
        } else {
            items(state.debts, key = DebtEntry::id) { entry ->
                DebtCard(entry, formatter, onDelete = { viewModel.deleteDebt(entry) })
            }
        }
    }
}

@Composable
private fun DebtEntryForm(
    draft: DebtDraft,
    onDraftChange: (DebtDraft) -> Unit,
    showValidationError: Boolean,
    reminderUnavailable: Boolean,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DebtIdentityFields(draft, onDraftChange)
        DebtAmountAndDateFields(draft, onDraftChange)
        DebtReminderAndNotesFields(draft, onDraftChange)
        if (showValidationError) {
            MuslimStateSurface(
                title = stringResource(R.string.finance_debt_invalid),
                tone = MuslimStateTone.Critical,
            )
        }
        if (reminderUnavailable) {
            MuslimStateSurface(
                title = stringResource(R.string.finance_debt_reminder_unavailable),
                tone = MuslimStateTone.Warning,
            )
        }
        IslamicPrimaryButton(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.finance_debt_save))
        }
    }
}

@Composable
private fun DebtIdentityFields(draft: DebtDraft, onDraftChange: (DebtDraft) -> Unit) {
    OutlinedTextField(
        value = draft.partyName,
        onValueChange = { onDraftChange(draft.copy(partyName = it)) },
        label = { Text(stringResource(R.string.finance_debt_party)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        stringResource(R.string.finance_debt_direction),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = draft.direction == DebtDirection.Receivable,
            onClick = { onDraftChange(draft.copy(direction = DebtDirection.Receivable)) },
            label = { Text(stringResource(R.string.finance_debt_receivable)) },
        )
        FilterChip(
            selected = draft.direction == DebtDirection.Payable,
            onClick = { onDraftChange(draft.copy(direction = DebtDirection.Payable)) },
            label = { Text(stringResource(R.string.finance_debt_payable)) },
        )
    }
}

@Composable
private fun DebtAmountAndDateFields(draft: DebtDraft, onDraftChange: (DebtDraft) -> Unit) {
    OutlinedTextField(
        value = draft.amountText,
        onValueChange = { raw ->
            val amount = Digits.toWesternDigits(raw).replace(',', '.').filter { it.isDigit() || it == '.' }
            onDraftChange(draft.copy(amountText = amount))
        },
        label = { Text(stringResource(R.string.finance_debt_amount)) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = draft.currency,
        onValueChange = { onDraftChange(draft.copy(currency = it.uppercase(Locale.ROOT).take(8))) },
        label = { Text(stringResource(R.string.finance_debt_currency)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = draft.dueDate,
        onValueChange = { onDraftChange(draft.copy(dueDate = it)) },
        label = { Text(stringResource(R.string.finance_debt_due_date)) },
        placeholder = { Text(stringResource(R.string.finance_debt_due_date_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DebtReminderAndNotesFields(draft: DebtDraft, onDraftChange: (DebtDraft) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.finance_debt_reminder), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.finance_debt_reminder_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = draft.reminderEnabled,
            onCheckedChange = { onDraftChange(draft.copy(reminderEnabled = it)) },
        )
    }
    OutlinedTextField(
        value = draft.notes,
        onValueChange = { onDraftChange(draft.copy(notes = it)) },
        label = { Text(stringResource(R.string.finance_debt_notes)) },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DebtSummaryCard(state: IslamicFinanceUiState, formatter: NumberFormat) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column {
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
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(
                    if (entry.direction == DebtDirection.Receivable) Icons.AutoMirrored.Filled.TrendingUp else Icons.Filled.AccountBalanceWallet,
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
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(verticalAlignment = Alignment.Top) {
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
    MuslimStateSurface(
        title = text,
        tone = MuslimStateTone.Information,
        icon = Icons.Filled.Info,
    )
}

private fun LocalizedFinanceText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun openScreeningProvider(context: Context, provider: ScreeningProvider, query: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("stock_ticker", query.trim()))
    context.startActivity(Intent(Intent.ACTION_VIEW, provider.url.toUri()))
}
