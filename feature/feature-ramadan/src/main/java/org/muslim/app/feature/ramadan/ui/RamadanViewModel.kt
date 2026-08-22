package org.muslim.app.feature.ramadan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.CalculationMethod
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerParameters
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.feature.ramadan.data.HabitTrackerRepository
import org.muslim.app.feature.ramadan.data.RamadanRepository
import org.muslim.app.feature.ramadan.data.RamadanSettings
import org.muslim.app.feature.ramadan.domain.HabitId
import org.muslim.app.feature.ramadan.domain.HabitSummary
import org.muslim.app.feature.ramadan.domain.HabitTrackerCalculator
import org.muslim.app.feature.ramadan.domain.HabitTrackerState
import org.muslim.app.feature.ramadan.domain.RamadanDates
import org.muslim.app.feature.ramadan.domain.RamadanInfo
import org.muslim.app.feature.ramadan.notifications.RamadanScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/** UI state for the Ramadan screen. */
data class RamadanUiState(
    val info: RamadanInfo,
    val today: LocalDate,
    val nowMillis: Long,
    val iftarTime: LocalTime?,
    val suhoorTime: LocalTime?,
    val nextIftarMillis: Long?,
    val nextSuhoorMillis: Long?,
    val fastingDays: Set<LocalDate>,
    val settings: RamadanSettings,
    val habitState: HabitTrackerState,
    val habitSummary: HabitSummary,
)

/** Pure computation of the iftar/suhoor instants (unit-testable). */
object RamadanTimes {

    data class Result(
        val iftarTime: LocalTime?,
        val suhoorTime: LocalTime?,
        val nextIftarMillis: Long?,
        val nextSuhoorMillis: Long?,
    )

    fun compute(
        prayerSettings: PrayerSettings,
        calculator: PrayerTimesCalculator,
        zone: ZoneId,
        nowMillis: Long,
        suhoorMinutesBefore: Int,
    ): Result {
        val location = prayerSettings.location ?: return Result(null, null, null, null)
        val coordinates = Coordinates(location.latitude, location.longitude, location.elevation)
        val params = if (prayerSettings.method == CalculationMethod.Custom) {
            PrayerParameters(
                method = CalculationMethod.Custom,
                fajrAngle = prayerSettings.customFajrAngle,
                ishaAngle = prayerSettings.customIshaAngle,
                highLatitudeRule = prayerSettings.highLatitudeRule,
            )
        } else {
            PrayerParameters.of(prayerSettings.method).copy(highLatitudeRule = prayerSettings.highLatitudeRule)
        }

        val today = LocalDate.now(zone)
        val todayResult = calculator.compute(today, coordinates, params, zone, prayerSettings.asrMethod, prayerSettings.adjustments)
        val tomorrowResult = calculator.compute(today.plusDays(1), coordinates, params, zone, prayerSettings.asrMethod, prayerSettings.adjustments)

        val iftarToday = todayResult.epochMillis[Prayer.Maghrib]
        val iftarTomorrow = tomorrowResult.epochMillis[Prayer.Maghrib]
        val fajrTomorrow = tomorrowResult.epochMillis[Prayer.Fajr]

        val nextIftar = when {
            iftarToday != null && iftarToday > nowMillis -> iftarToday
            else -> iftarTomorrow
        }
        val nextSuhoor = fajrTomorrow?.minus(suhoorMinutesBefore.coerceAtLeast(0) * 60_000L)

        return Result(
            iftarTime = todayResult.times[Prayer.Maghrib],
            suhoorTime = tomorrowResult.times[Prayer.Fajr],
            nextIftarMillis = nextIftar,
            nextSuhoorMillis = nextSuhoor?.takeIf { it > nowMillis },
        )
    }
}

@HiltViewModel
class RamadanViewModel @Inject constructor(
    private val prayerSettingsRepository: PrayerSettingsRepository,
    private val ramadanRepository: RamadanRepository,
    private val habitTrackerRepository: HabitTrackerRepository,
    private val calculator: PrayerTimesCalculator,
    private val scheduler: RamadanScheduler,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    val state: StateFlow<RamadanUiState> = combine(
        prayerSettingsRepository.settings,
        ramadanRepository.settings,
        habitTrackerRepository.state,
        ticker,
    ) { prayer, ramadan, habits, nowMillis ->
        val zone = prayer.location?.let { runCatching { ZoneId.of(it.timeZone) }.getOrNull() }
            ?: ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val times = RamadanTimes.compute(
            prayerSettings = prayer,
            calculator = calculator,
            zone = zone,
            nowMillis = nowMillis,
            suhoorMinutesBefore = ramadan.suhoorMinutesBefore,
        )
        RamadanUiState(
            info = RamadanDates.upcoming(today, prayer.hijriAdjustment),
            today = today,
            nowMillis = nowMillis,
            iftarTime = times.iftarTime,
            suhoorTime = times.suhoorTime,
            nextIftarMillis = times.nextIftarMillis,
            nextSuhoorMillis = times.nextSuhoorMillis,
            fastingDays = ramadan.fastingDays,
            settings = ramadan,
            habitState = habits,
            habitSummary = HabitTrackerCalculator.summary(habits, today),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), placeholder())

    private fun placeholder(): RamadanUiState {
        val today = LocalDate.now()
        return RamadanUiState(
            info = RamadanDates.upcoming(today),
            today = today,
            nowMillis = System.currentTimeMillis(),
            iftarTime = null,
            suhoorTime = null,
            nextIftarMillis = null,
            nextSuhoorMillis = null,
            fastingDays = emptySet(),
            settings = RamadanSettings(),
            habitState = HabitTrackerState(),
            habitSummary = HabitTrackerCalculator.summary(HabitTrackerState(), today),
        )
    }

    fun toggleFastingDay(date: LocalDate) {
        viewModelScope.launch { ramadanRepository.toggleFastingDay(date) }
    }

    fun toggleHabit(date: LocalDate, habit: HabitId) {
        viewModelScope.launch { habitTrackerRepository.toggleHabit(date, habit) }
    }

    fun setKhatmaJuz(juz: Int) {
        viewModelScope.launch { habitTrackerRepository.setKhatmaJuz(juz) }
    }

    fun toggleTaraweeh(date: LocalDate) {
        viewModelScope.launch { habitTrackerRepository.toggleTaraweeh(date) }
    }

    fun setItikafEnabled(enabled: Boolean) {
        viewModelScope.launch { habitTrackerRepository.setItikafEnabled(enabled) }
    }

    fun setIftarNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            ramadanRepository.setIftarNotificationEnabled(enabled)
            reschedule()
        }
    }

    fun setSuhoorReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            ramadanRepository.setSuhoorReminderEnabled(enabled)
            reschedule()
        }
    }

    fun setSuhoorMinutesBefore(minutes: Int) {
        viewModelScope.launch {
            ramadanRepository.setSuhoorMinutesBefore(minutes)
            reschedule()
        }
    }

    private suspend fun reschedule() {
        // Re-read the latest settings and re-schedule the alarms.
        val prayerSettings = prayerSettingsRepository.settings.first()
        val ramadan = ramadanRepository.settings.first()
        scheduler.schedule(prayerSettings, ramadan)
    }

    fun setNotifyOutsideRamadan(enabled: Boolean) {
        viewModelScope.launch {
            ramadanRepository.setNotifyOutsideRamadan(enabled)
            reschedule()
        }
    }
}
