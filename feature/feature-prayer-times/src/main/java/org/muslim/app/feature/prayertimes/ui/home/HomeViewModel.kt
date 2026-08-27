package org.muslim.app.feature.prayertimes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.time.HijriDate
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerCompletionRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.datastore.prayer.toPrayerCalculationProfile
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.Coordinates
import org.muslim.app.core.common.prayer.NextPrayer
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerCalculationProfile
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: PrayerSettingsRepository,
    private val completionRepository: PrayerCompletionRepository,
    private val calculator: PrayerTimesCalculator,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    /** The app-wide 12/24-hour clock chosen in Settings (default 12h). */
    val use24h: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.timeFormat24h }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Decorative background style selected in app appearance settings. */
    val ornamentStyle: StateFlow<AppOrnamentStyle> =
        appPreferencesRepository.preferences
            .map { it.ornamentStyle }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppOrnamentStyle.Geometry)

    /** Home display of the prayer checklist is an explicit, disabled-by-default choice. */
    val showPrayerTrackerOnHome: StateFlow<Boolean> =
        appPreferencesRepository.preferences
            .map { it.showPrayerTrackerOnHome }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** One day's condensed times for the monthly grid. */
    data class DayTimes(
        val date: LocalDate,
        val hijriDay: Int,
        val fajr: LocalTime?,
        val maghrib: LocalTime?,
    )

    /** Read-only alert presentation for a row; detailed editing remains in Prayer Settings. */
    data class PrayerAlert(
        val adhanEnabled: Boolean = true,
        val option: AdhanSoundOption = AdhanSoundOption.Default,
        val volume: Int = 100,
    )

    data class UiState(
        val hasLocation: Boolean = false,
        val locationName: String = "",
        val hijri: HijriDate? = null,
        val nextPrayer: Prayer? = null,
        val nextPrayerAt: LocalTime? = null,
        val countdownSeconds: Long = 0,
        /** Times of the day currently selected (today by default). */
        val times: Map<Prayer, LocalTime> = emptyMap(),
        val isValid: Boolean = false,
        /** Date whose times are shown; user can step through days. */
        val selectedDate: LocalDate = LocalDate.now(),
        /** Whether the monthly grid is shown instead of the daily list. */
        val monthly: Boolean = false,
        val month: YearMonth = YearMonth.now(),
        val monthDays: List<DayTimes> = emptyList(),
        /** Local-only prayer checklist for the selected day; sunrise is excluded. */
        val completedPrayers: Set<Prayer> = emptySet(),
        /** Per-prayer alert state shown beside the daily timetable. */
        val prayerAlerts: Map<Prayer, PrayerAlert> = emptyMap(),
    )

    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val monthly = MutableStateFlow(false)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val completionsForSelectedDate = selectedDate.flatMapLatest(completionRepository::completedPrayers)

    val uiState: StateFlow<UiState> =
        combine(
            settingsRepository.settings,
            clock,
            selectedDate,
            monthly,
            completionsForSelectedDate,
        ) { settings, now, date, isMonthly, completedPrayers ->
            compute(settings, now, date, isMonthly, completedPrayers)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private fun compute(
        settings: PrayerSettings,
        now: Long,
        date: LocalDate,
        isMonthly: Boolean,
        completedPrayers: Set<Prayer>,
    ): UiState {
        val location = settings.location ?: return UiState(selectedDate = date, monthly = isMonthly, month = YearMonth.from(date))
        val zone = ZoneId.of(location.timeZone)
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val coordinates = Coordinates(location.latitude, location.longitude, location.elevation)
        val profile = settings.toPrayerCalculationProfile()

        val result = calculator.compute(date, coordinates, profile, zone)

        // The countdown always tracks the REAL next prayer (from now), even
        // while the user browses a different day's times below.
        var next = NextPrayer.nextPrayer(result.epochMillis, now)
        if (next == null) {
            val tomorrowResult = calculator.compute(
                date = today.plusDays(1),
                coordinates = coordinates,
                profile = profile,
                timeZone = zone,
            )
            if (tomorrowResult.isValid) next = NextPrayer.nextPrayer(tomorrowResult.epochMillis, now)
        }
        // When the user browsed away from today, keep showing the true next
        // prayer by recomputing against "today" instead of the selected date.
        if (date != today) {
            val todayResult = calculator.compute(today, coordinates, profile, zone)
            next = NextPrayer.nextPrayer(todayResult.epochMillis, now)
                ?: calculator.compute(
                    date = today.plusDays(1),
                    coordinates = coordinates,
                    profile = profile,
                    timeZone = zone,
                ).let { NextPrayer.nextPrayer(it.epochMillis, now) }
        }

        return UiState(
            hasLocation = true,
            locationName = location.name,
            hijri = runCatching { HijriDate.from(date, settings.hijriAdjustment) }.getOrNull(),
            nextPrayer = next?.prayer,
            nextPrayerAt = next?.atEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
            countdownSeconds = next?.let { NextPrayer.countdownSeconds(it.atEpochMillis, now) } ?: 0,
            times = result.times,
            isValid = result.isValid,
            selectedDate = date,
            monthly = isMonthly,
            month = YearMonth.from(date),
            monthDays = if (isMonthly) {
                monthGrid(YearMonth.from(date), coordinates, profile, zone, settings)
            } else {
                emptyList()
            },
            completedPrayers = completedPrayers,
            prayerAlerts = Prayer.entries.associateWith { prayer ->
                PrayerAlert(
                    adhanEnabled = settings.adhanEnabled,
                    option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default,
                    volume = settings.adhanVolumeFor(prayer),
                )
            },
        )
    }

    fun previousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    fun toggleMonthly() {
        monthly.value = !monthly.value
    }

    fun togglePrayerCompletion(prayer: Prayer) {
        viewModelScope.launch {
            completionRepository.toggle(selectedDate.value, prayer)
        }
    }

    private fun monthGrid(
        month: YearMonth,
        coordinates: Coordinates,
        profile: PrayerCalculationProfile,
        zone: ZoneId,
        settings: PrayerSettings,
    ): List<DayTimes> {
        return (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val result = calculator.compute(date, coordinates, profile, zone)
            val hijri = runCatching { HijriDate.from(date, settings.hijriAdjustment) }.getOrNull()
            DayTimes(
                date = date,
                hijriDay = hijri?.day ?: day,
                fajr = result.timeFor(Prayer.Fajr),
                maghrib = result.timeFor(Prayer.Maghrib),
            )
        }
    }
}
