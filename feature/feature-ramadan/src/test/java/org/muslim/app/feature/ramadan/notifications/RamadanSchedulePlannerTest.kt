package org.muslim.app.feature.ramadan.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verifies the core scheduling contract: iftar/suhoor alarms are stopped
 * outside Ramadan by default, resume automatically inside it, and honour the
 * "notify outside Ramadan" opt-in.
 */
class RamadanSchedulePlannerTest {

    private val now = 1_800_000_000_000L // fixed instant
    private val maghribToday = now + 60 * 60_000L          // 1h from now
    private val maghribTomorrow = now + 24 * 60 * 60_000L  // tomorrow
    private val fajrTomorrow = now + 20 * 60 * 60_000L     // tomorrow fajr

    // ------------------------------------------------------------------
    // Outside Ramadan (default: no outside-enabled flag)
    // ------------------------------------------------------------------

    @Test
    fun `no alarms outside ramadan by default`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now,
            todayInRamadan = false,
            tomorrowInRamadan = false,
            outsideEnabled = false,
            suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday,
            tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.any).isFalse()
        assertThat(plan.iftarAtMillis).isNull()
        assertThat(plan.suhoorAtMillis).isNull()
    }

    @Test
    fun `no alarms outside ramadan even with valid times`() {
        // Times exist but the window is outside Ramadan -> nothing fires.
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = false, tomorrowInRamadan = false,
            outsideEnabled = false, suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.any).isFalse()
    }

    // ------------------------------------------------------------------
    // Inside Ramadan (automatic resume)
    // ------------------------------------------------------------------

    @Test
    fun `iftar fires at today maghrib during ramadan`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = true, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.iftarAtMillis).isEqualTo(maghribToday)
    }

    @Test
    fun `suhoor fires at tomorrow fajr minus lead time during ramadan`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = true, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.suhoorAtMillis).isEqualTo(fajrTomorrow - 30 * 60_000L)
    }

    @Test
    fun `suhoor lead time is clamped to zero`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = true, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = -5,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.suhoorAtMillis).isEqualTo(fajrTomorrow)
    }

    // ------------------------------------------------------------------
    // Transition days (the alarm must not leak outside Ramadan)
    // ------------------------------------------------------------------

    @Test
    fun `last ramadan day after maghrib does not fall forward to non-ramadan tomorrow`() {
        // Today is the last day of Ramadan and its Maghrib already passed;
        // tomorrow is not Ramadan -> no iftar at all (and no suhoor).
        val plan = RamadanSchedulePlanner.plan(
            now = now,
            todayInRamadan = true,
            tomorrowInRamadan = false,
            outsideEnabled = false,
            suhoorMinutesBefore = 30,
            todayMaghrib = now - 1, // already passed
            tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.any).isFalse()
        assertThat(plan.iftarAtMillis).isNull()
        assertThat(plan.suhoorAtMillis).isNull()
    }

    @Test
    fun `first ramadan day schedules suhoor for the fast`() {
        // Today is Ramadan 1 -> iftar today; tomorrow is Ramadan 2 -> suhoor fires.
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = true, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = 45,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.iftarAtMillis).isEqualTo(maghribToday)
        assertThat(plan.suhoorAtMillis).isEqualTo(fajrTomorrow - 45 * 60_000L)
    }

    @Test
    fun `day before ramadan schedules no iftar but no suhoor either`() {
        // Today: the day before Ramadan. Tomorrow is Ramadan 1 so suhoor
        // fires, but today's iftar must not (today is not a fasting day).
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = false, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.iftarAtMillis).isNull()
        assertThat(plan.suhoorAtMillis).isEqualTo(fajrTomorrow - 30 * 60_000L)
    }

    // ------------------------------------------------------------------
    // Year-round opt-in
    // ------------------------------------------------------------------

    @Test
    fun `outside-enabled keeps alarms running all year`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = false, tomorrowInRamadan = false,
            outsideEnabled = true, suhoorMinutesBefore = 30,
            todayMaghrib = maghribToday, tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = fajrTomorrow,
        )
        assertThat(plan.iftarAtMillis).isEqualTo(maghribToday)
        assertThat(plan.suhoorAtMillis).isEqualTo(fajrTomorrow - 30 * 60_000L)
    }

    @Test
    fun `outside-enabled still respects past times`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = false, tomorrowInRamadan = false,
            outsideEnabled = true, suhoorMinutesBefore = 30,
            todayMaghrib = now - 1, // passed
            tomorrowMaghrib = maghribTomorrow,
            tomorrowFajr = now - 1, // passed
        )
        assertThat(plan.iftarAtMillis).isEqualTo(maghribTomorrow)
        assertThat(plan.suhoorAtMillis).isNull()
    }

    // ------------------------------------------------------------------
    // Missing prayer times
    // ------------------------------------------------------------------

    @Test
    fun `missing maghrib or fajr silently skips that alarm`() {
        val plan = RamadanSchedulePlanner.plan(
            now = now, todayInRamadan = true, tomorrowInRamadan = true,
            outsideEnabled = false, suhoorMinutesBefore = 30,
            todayMaghrib = null, tomorrowMaghrib = null, tomorrowFajr = null,
        )
        assertThat(plan.any).isFalse()
    }
}
