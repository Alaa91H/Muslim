package org.muslim.app.feature.zakat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.zakat.R
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.MuslimCenteredStatus
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.zakat.domain.CountryCurrency
import java.text.NumberFormat
import java.time.LocalDate

/**
 * Zakat calculator (PROJECT_PROMPT.md §6 Phase 7): money zakat with nisab
 * check, fitr zakat and a yearly history. Global by country/currency — gold
 * and silver prices can be fetched live (gold-api.com + open.er-api.com) or
 * entered manually, and everything is cached for offline use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ZakatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Always western digits (never Arabic-Indic), regardless of the device
    // locale — this is a project-wide rule (see Digits in core-common).
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.ENGLISH)

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
            CountrySection(state, viewModel)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            MuslimSectionHeader(title = stringResource(R.string.zakat_money_section))
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
private fun CountrySection(state: ZakatUiState, viewModel: ZakatViewModel) {
    MuslimSectionHeader(title = stringResource(R.string.zakat_country_section))
    Spacer(Modifier.height(8.dp))
    CountryDropdown(
        selected = state.selectedCountry,
        countries = state.countries,
        onSelected = viewModel::selectCountry,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.zakat_auto_prices),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.zakat_auto_prices_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = state.autoPrices,
            onCheckedChange = viewModel::setAutoPrices,
        )
    }
    Spacer(Modifier.height(8.dp))
    IslamicPrimaryButton(
        onClick = viewModel::fetchPrices,
        enabled = state.selectedCountry != null && !state.isFetching,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isFetching) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.zakat_fetching))
        } else {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.zakat_fetch_now))
        }
    }
    if (state.fetchFailed) {
        Spacer(Modifier.height(6.dp))
        MuslimStateSurface(
            title = stringResource(R.string.zakat_fetch_failed),
            tone = MuslimStateTone.Critical,
        )
    }
    if (state.lastUpdatedAt != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.zakat_last_updated, formatUpdatedAt(state.lastUpdatedAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (state.currencyCode.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.zakat_currency,
                "${state.currencySymbol} ${state.currencyCode}".trim(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.zakat_source_note),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    selected: CountryCurrency?,
    countries: List<CountryCurrency>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { countryDisplayName(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.zakat_country)) },
            placeholder = { Text(stringResource(R.string.zakat_country_placeholder)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            countries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(countryDisplayName(option))
                            Text(
                                text = "${option.currency} · ${option.symbol}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelected(option.code)
                    },
                )
            }
        }
    }
}

@Composable
private fun countryDisplayName(country: CountryCurrency): String {
    val configuration = LocalConfiguration.current
    val arabic = configuration.locales[0]?.language == "ar"
    return if (arabic) country.nameArabic else country.nameEnglish
}

private fun formatUpdatedAt(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching {
        java.time.OffsetDateTime.parse(raw).toLocalDateTime().toString().replace('T', ' ')
    }.getOrElse {
        runCatching {
            java.time.Instant.parse(raw)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
                .replace('T', ' ')
        }.getOrDefault(raw)
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
            // Normalize Arabic-Indic/Persian digits (produced by some locale
            // keyboards) to western ones first, then keep only digits and a
            // single decimal point. Without this, typing in Arabic locales
            // produced digits that toDoubleOrNull() rejected and the field
            // silently dropped characters.
            val western = org.muslim.app.core.common.text.Digits.toWesternDigits(raw)
            val cleaned = western.replace(',', '.').filter { it.isDigit() || it == '.' }
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
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (result.nisabExceeded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column {
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
                IslamicPrimaryButton(onClick = viewModel::saveCalculation) {
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
    MuslimSectionHeader(title = stringResource(R.string.zakat_fitr_section))
    Spacer(Modifier.height(8.dp))
    NumberField(stringResource(R.string.zakat_fitr_saa_value), state.fitrSaaValue, formatter, viewModel::setFitrSaaValue)
    OutlinedTextField(
        value = state.fitrPersons.toString(),
        onValueChange = { raw ->
            org.muslim.app.core.common.text.Digits.toWesternDigits(raw)
                .filter { it.isDigit() }
                .toIntOrNull()
                ?.let(viewModel::setFitrPersons)
        },
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
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
        MuslimCenteredStatus(text = stringResource(R.string.zakat_history_empty))
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
