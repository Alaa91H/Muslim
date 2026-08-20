package org.muslim.app.feature.adhkar.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.adhkarPrefsDataStore by preferencesDataStore(name = "adhkar_prefs")

/**
 * User preferences for the adhkar experience (PROJECT_PROMPT.md §6 Phase 4):
 * the floating overlay message (duration, on/off), which adhkar the user
 * chose to hide, and the optional morning/evening reminders.
 */
data class AdhkarPrefs(
    /** Show adhkar as a floating message above all apps. */
    val overlayEnabled: Boolean = true,
    /** Seconds the floating message stays visible (default 5). */
    val overlayDurationSeconds: Int = 5,
    /** Overlay card background colour (ARGB; alpha baked in). */
    val overlayBackgroundColor: Int = 0xE6282830.toInt(),
    /** Overlay card background alpha 0..255 (default 230, matching the original card). */
    val overlayBackgroundAlpha: Int = 230,
    /** Overlay card corner radius in dp (0 = square, 40 = fully rounded). */
    val overlayCornerRadiusDp: Int = 20,
    /** Overlay Arabic text size in sp. */
    val overlayFontSizeSp: Int = 22,
    /** Dhikr ids the user disabled; an absent id means "enabled". */
    val disabledDhikrIds: Set<Long> = emptySet(),
    /** Dhikr ids the user pinned as favorites (shown at the top of the list). */
    val favoriteDhikrIds: Set<Long> = emptySet(),
    /** Daily morning adhkar reminder (default 06:00). */
    val morningReminderEnabled: Boolean = false,
    val morningHour: Int = 6,
    val morningMinute: Int = 0,
    /** Daily evening adhkar reminder (default 18:00). */
    val eveningReminderEnabled: Boolean = false,
    val eveningHour: Int = 18,
    val eveningMinute: Int = 0,
    /** Periodic floating reminder (PROJECT_PROMPT.md Phase 4) — repeats every interval. */
    val periodicReminderEnabled: Boolean = false,
    /** Interval in minutes between reminders (15/30/60/120/180). */
    val periodicReminderIntervalMinutes: Int = 60,
    /** Category for the periodic reminder; null = random across all enabled adhkar. */
    val periodicReminderCategoryId: String? = null,
    /** Prefer compact one- or two-line adhkar in floating reminders. */
    val shortDhikrOnly: Boolean = true,
    /** Restrict the periodic reminder to a daily time window (e.g. work hours). */
    val periodicReminderWindowEnabled: Boolean = false,
    val periodicReminderWindowStartHour: Int = 9,
    val periodicReminderWindowStartMinute: Int = 0,
    val periodicReminderWindowEndHour: Int = 17,
    val periodicReminderWindowEndMinute: Int = 0,
) {
    fun isDhikrEnabled(id: Long): Boolean = id !in disabledDhikrIds

    fun isDhikrFavorite(id: Long): Boolean = id in favoriteDhikrIds

    /**
     * True when at least one of the daily morning/evening adhkar reminders is
     * enabled. This is the single master switch shown on the adhkar home
     * screen; the underlying per-slot flags keep their individual settings.
     */
    val morningEveningReminderEnabled: Boolean
        get() = morningReminderEnabled || eveningReminderEnabled
}

@Singleton
class AdhkarPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val prefs: Flow<AdhkarPrefs> = context.adhkarPrefsDataStore.data
        .map { p ->
        AdhkarPrefs(
            overlayEnabled = p[Keys.OVERLAY_ENABLED] ?: true,
            overlayDurationSeconds = (p[Keys.OVERLAY_DURATION] ?: 5).coerceIn(1, 600),
            overlayBackgroundColor = (p[Keys.OVERLAY_BG_COLOR] ?: 0xE6282830.toInt()).let { stored ->
                // Backward compatible: alpha comes from its own key, falling back
                // to the alpha baked into the stored ARGB colour.
                val alpha = p[Keys.OVERLAY_BG_ALPHA] ?: ((stored ushr 24) and 0xFF)
                (alpha shl 24) or (stored and 0xFFFFFF)
            },
            overlayBackgroundAlpha = (p[Keys.OVERLAY_BG_ALPHA]
                ?: ((p[Keys.OVERLAY_BG_COLOR] ?: 0xE6282830.toInt()) ushr 24) and 0xFF)
                .coerceIn(0, 255),
            overlayCornerRadiusDp = (p[Keys.OVERLAY_CORNER_RADIUS] ?: 20).coerceIn(0, 48),
            overlayFontSizeSp = (p[Keys.OVERLAY_FONT_SIZE] ?: 22).coerceIn(14, 36),
            disabledDhikrIds = (p[Keys.DISABLED_DHIKR_IDS] ?: emptySet())
                .mapNotNull { it.toLongOrNull() }
                .toSet(),
            favoriteDhikrIds = (p[Keys.FAVORITE_DHIKR_IDS] ?: emptySet())
                .mapNotNull { it.toLongOrNull() }
                .toSet(),
            morningReminderEnabled = p[Keys.MORNING_ENABLED] ?: false,
            morningHour = (p[Keys.MORNING_HOUR] ?: 6).coerceIn(0, 23),
            morningMinute = (p[Keys.MORNING_MINUTE] ?: 0).coerceIn(0, 59),
            eveningReminderEnabled = p[Keys.EVENING_ENABLED] ?: false,
            eveningHour = (p[Keys.EVENING_HOUR] ?: 18).coerceIn(0, 23),
            eveningMinute = (p[Keys.EVENING_MINUTE] ?: 0).coerceIn(0, 59),
            periodicReminderEnabled = p[Keys.PERIODIC_ENABLED] ?: false,
            periodicReminderIntervalMinutes = (p[Keys.PERIODIC_INTERVAL] ?: 60).coerceIn(5, 1_440),
            periodicReminderCategoryId = p[Keys.PERIODIC_CATEGORY],
            shortDhikrOnly = p[Keys.SHORT_DHIKR_ONLY] ?: true,
            periodicReminderWindowEnabled = p[Keys.PERIODIC_WINDOW_ENABLED] ?: false,
            periodicReminderWindowStartHour = (p[Keys.PERIODIC_WINDOW_START_HOUR] ?: 9).coerceIn(0, 23),
            periodicReminderWindowStartMinute = (p[Keys.PERIODIC_WINDOW_START_MINUTE] ?: 0).coerceIn(0, 59),
            periodicReminderWindowEndHour = (p[Keys.PERIODIC_WINDOW_END_HOUR] ?: 17).coerceIn(0, 23),
            periodicReminderWindowEndMinute = (p[Keys.PERIODIC_WINDOW_END_MINUTE] ?: 0).coerceIn(0, 59),
        )
        }
        // Corrupt persisted data (e.g. written by an older version) must never
        // crash the adhkar screen on entry; fall back to the defaults instead.
        .catch { emit(AdhkarPrefs()) }

    suspend fun setOverlayEnabled(enabled: Boolean) = edit { it[Keys.OVERLAY_ENABLED] = enabled }

    suspend fun setOverlayDurationSeconds(seconds: Int) =
        edit { it[Keys.OVERLAY_DURATION] = seconds.coerceIn(1, 600) }

    suspend fun setOverlayBackgroundColor(rgb: Int) = edit { prefs ->
        // The picker supplies an opaque RGB; keep the user's current alpha.
        val alpha = prefs[Keys.OVERLAY_BG_ALPHA] ?: 230
        prefs[Keys.OVERLAY_BG_COLOR] = (alpha shl 24) or (rgb and 0xFFFFFF)
    }

    suspend fun setOverlayBackgroundAlpha(alpha: Int) = edit { prefs ->
        val a = alpha.coerceIn(0, 255)
        val argb = prefs[Keys.OVERLAY_BG_COLOR] ?: 0xE6282830.toInt()
        prefs[Keys.OVERLAY_BG_ALPHA] = a
        prefs[Keys.OVERLAY_BG_COLOR] = (a shl 24) or (argb and 0xFFFFFF)
    }

    /** Restores the original overlay card look (colour, radius, font size). */
    suspend fun resetOverlayAppearance() = edit { prefs ->
        prefs.remove(Keys.OVERLAY_BG_COLOR)
        prefs.remove(Keys.OVERLAY_BG_ALPHA)
        prefs.remove(Keys.OVERLAY_CORNER_RADIUS)
        prefs.remove(Keys.OVERLAY_FONT_SIZE)
    }

    suspend fun setOverlayCornerRadiusDp(radius: Int) =
        edit { it[Keys.OVERLAY_CORNER_RADIUS] = radius.coerceIn(0, 48) }

    suspend fun setOverlayFontSizeSp(size: Int) =
        edit { it[Keys.OVERLAY_FONT_SIZE] = size.coerceIn(14, 36) }

    suspend fun setDhikrEnabled(id: Long, enabled: Boolean) = edit { prefs ->
        val current = prefs[Keys.DISABLED_DHIKR_IDS] ?: emptySet()
        prefs[Keys.DISABLED_DHIKR_IDS] =
            if (enabled) current - id.toString() else current + id.toString()
    }

    suspend fun setDhikrFavorite(id: Long, favorite: Boolean) = edit { prefs ->
        val current = prefs[Keys.FAVORITE_DHIKR_IDS] ?: emptySet()
        prefs[Keys.FAVORITE_DHIKR_IDS] =
            if (favorite) current + id.toString() else current - id.toString()
    }

    suspend fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) = edit {
        it[Keys.MORNING_ENABLED] = enabled
        it[Keys.MORNING_HOUR] = hour.coerceIn(0, 23)
        it[Keys.MORNING_MINUTE] = minute.coerceIn(0, 59)
    }

    suspend fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) = edit {
        it[Keys.EVENING_ENABLED] = enabled
        it[Keys.EVENING_HOUR] = hour.coerceIn(0, 23)
        it[Keys.EVENING_MINUTE] = minute.coerceIn(0, 59)
    }

    suspend fun setPeriodicReminderEnabled(enabled: Boolean) = edit {
        it[Keys.PERIODIC_ENABLED] = enabled
    }

    suspend fun setPeriodicReminderInterval(minutes: Int) = edit {
        it[Keys.PERIODIC_INTERVAL] = minutes.coerceIn(5, 1_440)
    }

    suspend fun setPeriodicReminderCategory(categoryId: String?) = edit {
        if (categoryId == null) it.remove(Keys.PERIODIC_CATEGORY) else it[Keys.PERIODIC_CATEGORY] = categoryId
    }

    suspend fun setShortDhikrOnly(enabled: Boolean) = edit {
        it[Keys.SHORT_DHIKR_ONLY] = enabled
    }

    suspend fun setPeriodicReminderWindow(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) = edit {
        it[Keys.PERIODIC_WINDOW_ENABLED] = enabled
        it[Keys.PERIODIC_WINDOW_START_HOUR] = startHour.coerceIn(0, 23)
        it[Keys.PERIODIC_WINDOW_START_MINUTE] = startMinute.coerceIn(0, 59)
        it[Keys.PERIODIC_WINDOW_END_HOUR] = endHour.coerceIn(0, 23)
        it[Keys.PERIODIC_WINDOW_END_MINUTE] = endMinute.coerceIn(0, 59)
    }

    private suspend fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.adhkarPrefsDataStore.edit { transform(it) }
    }

    private object Keys {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val OVERLAY_DURATION = intPreferencesKey("overlay_duration_seconds")
        val OVERLAY_BG_COLOR = intPreferencesKey("overlay_bg_color")
        val OVERLAY_BG_ALPHA = intPreferencesKey("overlay_bg_alpha")
        val OVERLAY_CORNER_RADIUS = intPreferencesKey("overlay_corner_radius_dp")
        val OVERLAY_FONT_SIZE = intPreferencesKey("overlay_font_size_sp")
        val DISABLED_DHIKR_IDS = stringSetPreferencesKey("disabled_dhikr_ids")
        val FAVORITE_DHIKR_IDS = stringSetPreferencesKey("favorite_dhikr_ids")
        val MORNING_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_HOUR = intPreferencesKey("morning_reminder_hour")
        val MORNING_MINUTE = intPreferencesKey("morning_reminder_minute")
        val EVENING_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_HOUR = intPreferencesKey("evening_reminder_hour")
        val EVENING_MINUTE = intPreferencesKey("evening_reminder_minute")
        val PERIODIC_ENABLED = booleanPreferencesKey("periodic_reminder_enabled")
        val PERIODIC_INTERVAL = intPreferencesKey("periodic_reminder_interval_minutes")
        val PERIODIC_CATEGORY = androidx.datastore.preferences.core.stringPreferencesKey("periodic_reminder_category")
        val SHORT_DHIKR_ONLY = booleanPreferencesKey("short_dhikr_only")
        val PERIODIC_WINDOW_ENABLED = booleanPreferencesKey("periodic_window_enabled")
        val PERIODIC_WINDOW_START_HOUR = intPreferencesKey("periodic_window_start_hour")
        val PERIODIC_WINDOW_START_MINUTE = intPreferencesKey("periodic_window_start_minute")
        val PERIODIC_WINDOW_END_HOUR = intPreferencesKey("periodic_window_end_hour")
        val PERIODIC_WINDOW_END_MINUTE = intPreferencesKey("periodic_window_end_minute")
    }
}
