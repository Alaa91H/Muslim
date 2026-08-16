package org.example.islamicapp.feature.prayertimes.ui.times

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.example.islamicapp.core.common.time.HijriDate
import org.example.islamicapp.core.datastore.prayer.PrayerSettings
import org.example.islamicapp.core.datastore.prayer.PrayerSettingsRepository
import org.example.islamicapp.core.common.prayer.Prayer
import org.example.islamicapp.core.common.prayer.PrayerParameters
import org.example.islamicapp.core.datastore.prayer.toPrayerParameters
import org.example.islamicapp.feature.prayertimes.domain.Coordinates
import org.example.islamicapp.feature.prayertimes.domain.PrayerTimesCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PrayerTimesViewModel @Inject constructor(
    settingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerTimesCalculator,
) : ViewModel() {

    data class DayTimes(
        val date: LocalDate,
        val hijriDay: Int,
        val hijriMonth: Int,
        val hijriYear: Int,
        val fajr: LocalTime?,
        val maghrib: LocalTime?,
    )

    data class UiState(
        val settings: PrayerSettings = PrayerSettings(),
        val selectedDate: LocalDate = LocalDate.now(),
        val hijri: HijriDate? = null,
        val times: Map<Prayer, LocalTime> = emptyMap(),
        val isValid: Boolean = false,
        val monthly: Boolean = false,
        val month: YearMonth = YearMonth.now(),
        val monthDays: List<DayTimes> = emptyList(),
    )

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val monthly = MutableStateFlow(false)

    val uiState: StateFlow<UiState> =
        combine(settingsRepository.settings, selectedDate, monthly) { settings, date, isMonthly ->
            val location = settings.location
            if (location == null) {
                UiState(settings = settings, selectedDate = date, monthly = isMonthly, month = YearMonth.from(date))
            } else {
                val zone = ZoneId.of(location.timeZone)
                val coordinates = Coordinates(location.latitude, location.longitude)
                val params = settings.toPrayerParameters()
                val result = calculator.compute(date, coordinates, params, zone, settings.asrMethod, settings.adjustments)
                val monthDays = if (isMonthly) {
                    monthGrid(YearMonth.from(date), coordinates, params, zone, settings)
                } else {
                    emptyList()
                }
                UiState(
                    settings = settings,
                    selectedDate = date,
                    hijri = runCatching { HijriDate.from(date, settings.hijriAdjustment) }.getOrNull(),
                    times = result.times,
                    isValid = result.isValid,
                    monthly = isMonthly,
                    month = YearMonth.from(date),
                    monthDays = monthDays,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun previousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    fun toggleMonthly() {
        monthly.value = !monthly.value
    }

    private fun monthGrid(
        month: YearMonth,
        coordinates: Coordinates,
        params: PrayerParameters,
        zone: ZoneId,
        settings: PrayerSettings,
    ): List<DayTimes> {
        return (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val result = calculator.compute(date, coordinates, params, zone, settings.asrMethod, settings.adjustments)
            val hijri = runCatching { HijriDate.from(date, settings.hijriAdjustment) }.getOrNull()
            DayTimes(
                date = date,
                hijriDay = hijri?.day ?: day,
                hijriMonth = hijri?.month ?: 1,
                hijriYear = hijri?.year ?: 0,
                fajr = result.timeFor(Prayer.Fajr),
                maghrib = result.timeFor(Prayer.Maghrib),
            )
        }
    }
}
