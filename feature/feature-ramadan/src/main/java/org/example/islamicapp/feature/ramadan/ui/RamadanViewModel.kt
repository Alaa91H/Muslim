package org.example.islamicapp.feature.ramadan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.islamicapp.core.prayer.CalculationMethod
import org.example.islamicapp.core.prayer.Coordinates
import org.example.islamicapp.core.prayer.Prayer
import org.example.islamicapp.core.prayer.PrayerParameters
import org.example.islamicapp.core.prayer.PrayerTimesCalculator
import org.example.islamicapp.feature.ramadan.data.FastingTrackerRepository
import org.example.islamicapp.feature.ramadan.domain.IslamicEvent
import org.example.islamicapp.feature.ramadan.domain.IslamicEvents
import org.example.islamicapp.feature.ramadan.domain.RamadanInfo
import org.example.islamicapp.feature.ramadan.domain.RamadanSeason
import org.example.islamicapp.feature.ramadan.domain.SunnahFast
import org.example.islamicapp.feature.ramadan.domain.SunnahFasting
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class RamadanUiState(
    val ramadan: RamadanInfo = RamadanSeason.info(),
    /** Suhoor (Fajr) and iftar (Maghrib) datetimes today; null without location. */
    val suhoorAt: LocalDateTime? = null,
    val iftarAt: LocalDateTime? = null,
    val fastedDays: Set<String> = emptySet(),
    val qadaRemaining: Int = 0,
    val events: List<IslamicEvent> = emptyList(),
    val sunnahFasts: List<SunnahFast> = emptyList(),
)

/**
 * Ramadan hub (PROJECT_PROMPT.md §6 Phase 6 + Phase 8 occasions/sunnah
 * fasting). Suhoor/iftar use the shared prayer engine with the Umm al-Qura
 * method (matching the app default) and refresh as the day progresses.
 */
@HiltViewModel
class RamadanViewModel @Inject constructor(
    private val tracker: FastingTrackerRepository,
) : ViewModel() {

    private val location = MutableStateFlow<Pair<Double, Double>?>(null)
    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<RamadanUiState> = combine(
        combine(location, refresh) { loc, _ -> loc },
        tracker.fastedDays,
        tracker.qadaRemaining,
    ) { loc, fasted, qada ->
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        buildState(loc, today, now, fasted, qada)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RamadanUiState())

    /** Called by the navigation shell with the selected location (may be null). */
    fun setLocation(latitude: Double?, longitude: Double?) {
        location.value = if (latitude != null && longitude != null) latitude to longitude else null
    }

    fun toggleTodayFasted() {
        viewModelScope.launch { tracker.toggleDay(LocalDate.now()) }
    }

    fun markRamadanDay(date: LocalDate, fasted: Boolean) {
        viewModelScope.launch { tracker.setDay(date, fasted) }
    }

    fun adjustQada(delta: Int) {
        viewModelScope.launch { tracker.adjustQada(delta) }
    }

    private fun buildState(
        loc: Pair<Double, Double>?,
        today: LocalDate,
        now: LocalDateTime,
        fasted: Set<String>,
        qada: Int,
    ): RamadanUiState {
        var suhoor: LocalDateTime? = null
        var iftar: LocalDateTime? = null
        if (loc != null) {
            val result = PrayerTimesCalculator().compute(
                date = today,
                coordinates = Coordinates(latitude = loc.first, longitude = loc.second),
                parameters = PrayerParameters.of(CalculationMethod.UmmAlQura),
                timeZone = java.time.ZoneId.systemDefault(),
            )
            result.timeFor(Prayer.Fajr)?.let { suhoor = LocalDateTime.of(today, it) }
            result.timeFor(Prayer.Maghrib)?.let { iftar = LocalDateTime.of(today, it) }
        }
        return RamadanUiState(
            ramadan = RamadanSeason.info(today),
            suhoorAt = suhoor,
            iftarAt = iftar,
            fastedDays = fasted,
            qadaRemaining = qada,
            events = IslamicEvents.upcoming(today),
            sunnahFasts = SunnahFasting.upcoming(today),
        )
    }
}

/** Formats a remaining duration as "HH:MM" for the countdown cards. */
fun Duration.formatHm(): String {
    val total = maxOf(seconds, 0)
    return "%02d:%02d".format(total / 3600, (total % 3600) / 60)
}
