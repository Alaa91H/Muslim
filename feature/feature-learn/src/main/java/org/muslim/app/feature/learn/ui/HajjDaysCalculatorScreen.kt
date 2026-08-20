package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.time.HijriDate
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.HajjDaysCalculator
import org.muslim.app.feature.learn.domain.HajjKeyDay
import org.muslim.app.feature.learn.domain.HajjKeyDayKind
import java.time.LocalDate

/**
 * Automatic Hajj-days calculator (PROJECT_PROMPT.md section Hajj): the user
 * enters any Hijri date and gets the season of that year — the Day of Arafah
 * (9 Dhul-Hijjah), the Day of An-Nahr (10 Dhul-Hijjah) and the three Days of
 * Tashreeq (11-13 Dhul-Hijjah) — each with its Gregorian equivalent and its
 * position relative to the entered date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HajjDaysCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hajj_calc_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        HajjDaysCalculatorContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun HajjDaysCalculatorContent(modifier: Modifier = Modifier) {
    val today = remember { HijriDate.today() }
    var yearText by remember { mutableStateOf(today.year.toString()) }
    var monthText by remember { mutableStateOf(today.month.toString()) }
    var dayText by remember { mutableStateOf(today.day.toString()) }

    val computed = remember(yearText, monthText, dayText) {
        val year = yearText.toIntOrNull()
        val month = monthText.toIntOrNull()
        val day = dayText.toIntOrNull()
        if (year == null || month == null || day == null) return@remember null
        val entered = HajjDaysCalculator.parse(year, month, day) ?: return@remember null
        entered to HajjDaysCalculator.seasonFor(entered)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Text(
                text = stringResource(R.string.hajj_calc_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(14.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = yearText,
                onValueChange = { yearText = org.muslim.app.core.common.text.Digits.onlyDigits(it).take(4) },
                labelRes = R.string.hajj_calc_year,
                modifier = Modifier.weight(1.3f),
            )
            NumberField(
                value = monthText,
                onValueChange = { monthText = org.muslim.app.core.common.text.Digits.onlyDigits(it).take(2) },
                labelRes = R.string.hajj_calc_month,
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = dayText,
                onValueChange = { dayText = org.muslim.app.core.common.text.Digits.onlyDigits(it).take(2) },
                labelRes = R.string.hajj_calc_day,
                modifier = Modifier.weight(1f),
            )
        }

        if (computed == null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.hajj_calc_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        computed?.let { (entered, days) ->
            Spacer(Modifier.height(16.dp))
            EnteredDateCard(entered)
            days.forEach { day ->
                Spacer(Modifier.height(8.dp))
                KeyDayCard(day = day, entered = entered)
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun EnteredDateCard(entered: HijriDate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.hajj_calc_entered),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = entered.formatArabicLong(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.hajj_calc_gregorian, formatGregorian(entered.gregorian)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun KeyDayCard(day: HajjKeyDay, entered: HijriDate) {
    val diff = day.daysFrom(entered)
    val highlight = diff == 0L
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dayTitle(day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                RelativeMarker(diff)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = day.hijri.formatArabicLong(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.hajj_calc_gregorian, formatGregorian(day.gregorian)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun dayTitle(day: HajjKeyDay): String = when (day.kind) {
    HajjKeyDayKind.ARAFAH -> stringResource(R.string.hajj_calc_arafah)
    HajjKeyDayKind.NAHR -> stringResource(R.string.hajj_calc_nahr)
    else -> stringResource(
        R.string.hajj_calc_tashreeq_day,
        day.hijri.day - HajjDaysCalculator.TASHREEQ_FIRST_DAY + 1,
    )
}

/** Small chip showing where the day sits relative to the entered date. */
@Composable
private fun RelativeMarker(diff: Long) {
    val textRes: Int = when {
        diff == 0L -> R.string.hajj_calc_today
        diff == 1L -> R.string.hajj_calc_tomorrow
        diff == -1L -> R.string.hajj_calc_yesterday
        diff > 1L -> R.string.hajj_calc_in_days
        else -> R.string.hajj_calc_ago_days
    }
    val count: Long? = when {
        diff > 1L -> diff
        diff < -1L -> -diff
        else -> null
    }
    val text = if (count != null) stringResource(textRes, count) else stringResource(textRes)
    Surface(
        shape = RoundedCornerShape(50),
        color = if (diff == 0L) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (diff == 0L) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Gregorian date with English month names and Western digits (locale-neutral). */
private fun formatGregorian(date: LocalDate): String =
    "${date.dayOfMonth} ${GREGORIAN_MONTHS[date.monthValue - 1]} ${date.year}"

private val GREGORIAN_MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
