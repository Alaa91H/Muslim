package org.muslim.app.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.core.content.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import org.muslim.app.core.common.appearance.AppColorPalette
import org.muslim.app.core.common.appearance.CardCornerStyle
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import javax.inject.Singleton

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

/**
 * Persists the app-wide preferences ([AppPreferences]) in a DataStore on the
 * device. This is the single source for the UI theme, the app language and
 * accessibility-related toggles (PROJECT_PROMPT.md §4/§5/§6).
 */
@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val preferences: Flow<AppPreferences> = context.appPreferencesDataStore.data.map { prefs ->
        AppPreferences(
            themeMode = runCatching { AppThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: AppThemeMode.System.name) }
                .getOrDefault(AppThemeMode.System),
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            colorPalette = enumOr(prefs[Keys.COLOR_PALETTE], AppColorPalette.Classic),
            cardCornerStyle = enumOr(prefs[Keys.CARD_CORNER_STYLE], CardCornerStyle.Soft),
            ornamentStyle = enumOr(prefs[Keys.ORNAMENT_STYLE], AppOrnamentStyle.Geometry),
            languageCode = prefs[Keys.LANGUAGE] ?: AppPreferences.SYSTEM_LANGUAGE,
            reduceAnimations = prefs[Keys.REDUCE_ANIMATIONS] ?: false,
            startTab = prefs[Keys.START_TAB] ?: AppPreferences.START_TAB_HOME,
            timeFormat24h = prefs[Keys.TIME_FORMAT_24H] ?: false,
            accessibilityReadingMode = prefs[Keys.ACCESSIBILITY_READING_MODE] ?: false,
            informationDensity = enumOr(prefs[Keys.INFORMATION_DENSITY], AppInformationDensity.Comfortable),
            accessibilityHighContrast = prefs[Keys.ACCESSIBILITY_HIGH_CONTRAST] ?: false,
            voiceNavigationEnabled = prefs[Keys.VOICE_NAVIGATION_ENABLED] ?: false,
            wearCompanionEnabled = prefs[Keys.WEAR_COMPANION_ENABLED] ?: false,
            showPrayerTrackerOnHome = prefs[Keys.SHOW_PRAYER_TRACKER_ON_HOME] ?: false,
            smartHomeBridgeEnabled = prefs[Keys.SMART_HOME_BRIDGE_ENABLED] ?: false,
            smartHomeBridgeEndpoint = prefs[Keys.SMART_HOME_BRIDGE_ENDPOINT].orEmpty(),
            moreSectionOrder = AppPreferences.decodeSectionOrder(prefs[Keys.MORE_SECTION_ORDER]),
            hiddenMoreSections = AppPreferences.decodeHiddenSections(prefs[Keys.MORE_SECTION_HIDDEN]),
            updateCheckEnabled = prefs[Keys.UPDATE_CHECK_ENABLED] ?: false,
            updateCheckFrequency = prefs[Keys.UPDATE_CHECK_FREQUENCY] ?: AppPreferences.UPDATE_CHECK_DAILY,
            autoUpdateEnabled = prefs[Keys.AUTO_UPDATE_ENABLED] ?: false,
            lastUpdateCheckEpoch = prefs[Keys.LAST_UPDATE_CHECK] ?: 0L,
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) = edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = edit { prefs -> prefs[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setColorPalette(palette: AppColorPalette) = edit { prefs ->
        prefs[Keys.COLOR_PALETTE] = palette.name
    }

    suspend fun setCardCornerStyle(style: CardCornerStyle) = edit { prefs ->
        prefs[Keys.CARD_CORNER_STYLE] = style.name
    }

    suspend fun setOrnamentStyle(style: AppOrnamentStyle) = edit { prefs ->
        prefs[Keys.ORNAMENT_STYLE] = style.name
    }

    suspend fun setLanguage(languageCode: String) {
        edit { prefs -> prefs[Keys.LANGUAGE] = languageCode }
        // Synchronous mirror so attachBaseContext can apply the locale without
        // an async read (DataStore cannot be read synchronously).
        languageMirror.edit { putString(LOCALE_MIRROR_KEY, languageCode) }
    }

    suspend fun setReduceAnimations(enabled: Boolean) = edit { prefs -> prefs[Keys.REDUCE_ANIMATIONS] = enabled }

    suspend fun setStartTab(route: String) {
        edit { prefs -> prefs[Keys.START_TAB] = route }
        // Synchronous mirror so the chosen start tab can be read at launch
        // without an async DataStore read (same pattern as language/time).
        startTabMirror.edit { putString("start_tab", route) }
    }

    suspend fun setTimeFormat24h(use24h: Boolean) {
        edit { prefs -> prefs[Keys.TIME_FORMAT_24H] = use24h }
        // Synchronous mirror so background services/widgets can format without
        // an async DataStore read (same pattern as the language mirror).
        timeFormatMirror.edit { putBoolean(Keys.TIME_FORMAT_24H.name, use24h) }
    }

    suspend fun setAccessibilityReadingMode(enabled: Boolean) {
        edit { prefs -> prefs[Keys.ACCESSIBILITY_READING_MODE] = enabled }
    }

    /** Persists the preferred amount of supporting information in adaptable UI surfaces. */
    suspend fun setInformationDensity(density: AppInformationDensity) {
        edit { prefs -> prefs[Keys.INFORMATION_DENSITY] = density.name }
    }

    suspend fun setAccessibilityHighContrast(enabled: Boolean) {
        edit { prefs -> prefs[Keys.ACCESSIBILITY_HIGH_CONTRAST] = enabled }
    }

    suspend fun setVoiceNavigationEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.VOICE_NAVIGATION_ENABLED] = enabled }
    }

    suspend fun setWearCompanionEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.WEAR_COMPANION_ENABLED] = enabled }
    }

    suspend fun setShowPrayerTrackerOnHome(enabled: Boolean) {
        edit { prefs -> prefs[Keys.SHOW_PRAYER_TRACKER_ON_HOME] = enabled }
    }

    suspend fun setSmartHomeBridgeEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.SMART_HOME_BRIDGE_ENABLED] = enabled }
    }

    suspend fun setSmartHomeBridgeEndpoint(endpoint: String) {
        edit { prefs -> prefs[Keys.SMART_HOME_BRIDGE_ENDPOINT] = endpoint.trim() }
    }

    /** Persists the user-defined order of the "More" hub sections. */
    suspend fun setMoreSectionOrder(order: List<String>) {
        edit { prefs -> prefs[Keys.MORE_SECTION_ORDER] = order.joinToString(",") }
    }

    /** Persists the set of "More" hub sections the user chose to hide. */
    suspend fun setHiddenMoreSections(hidden: Set<String>) {
        edit { prefs ->
            if (hidden.isEmpty()) prefs.remove(Keys.MORE_SECTION_HIDDEN)
            else prefs[Keys.MORE_SECTION_HIDDEN] = hidden.joinToString(",")
        }
    }

    /**
     * Returns whether the initial-install permission flow has not yet been
     * shown. This is intentionally separate from each system grant state: a
     * user may decline a permission and later revisit the Permission Center.
     */
    suspend fun isInitialPermissionSetupPending(): Boolean =
        context.appPreferencesDataStore.data.first()[Keys.INITIAL_PERMISSION_SETUP_HANDLED] != true

    /** Marks the one-time first-install permission flow as handled. */
    suspend fun markInitialPermissionSetupHandled() {
        edit { prefs -> prefs[Keys.INITIAL_PERMISSION_SETUP_HANDLED] = true }
    }

    /** Turns the periodic update check on/off (off by default). */
    suspend fun setUpdateCheckEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.UPDATE_CHECK_ENABLED] = enabled }
    }

    /** Sets the update-check cadence (daily/weekly/monthly). */
    suspend fun setUpdateCheckFrequency(frequency: String) {
        edit { prefs -> prefs[Keys.UPDATE_CHECK_FREQUENCY] = frequency }
    }

    /** Turns the fully-automatic (Session API) update on/off (off by default). */
    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.AUTO_UPDATE_ENABLED] = enabled }
    }

    /** Records the timestamp of the last successful update check. */
    suspend fun setLastUpdateCheck(epochMillis: Long) {
        edit { prefs -> prefs[Keys.LAST_UPDATE_CHECK] = epochMillis }
    }

    /** Blocking read of the 24-hour flag, safe for services and widget workers. */
    fun readTimeFormat24hSync(): Boolean =
        timeFormatMirror.getBoolean(Keys.TIME_FORMAT_24H.name, false)

    /** Blocking read of the persisted language, safe for [android.app.Activity.attachBaseContext]. */
    fun readLanguageSync(): String =
        languageMirror.getString(LOCALE_MIRROR_KEY, AppPreferences.SYSTEM_LANGUAGE)
            ?: AppPreferences.SYSTEM_LANGUAGE

    /**
     * Blocking read of the chosen start tab, safe for cold start. The mirror
     * is deliberately read **once** per process so that changing the setting
     * from Settings only persists the choice instead of navigating there.
     */
    fun readStartTabSync(): String =
        startTabMirror.getString("start_tab", AppPreferences.START_TAB_HOME)
            ?: AppPreferences.START_TAB_HOME

    private fun <T : Enum<T>> enumOr(value: String?, default: T): T =
        value?.let { raw -> default::class.java.enumConstants?.firstOrNull { it.name == raw } } ?: default

    private suspend fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.appPreferencesDataStore.edit { prefs -> transform(prefs) }
    }

    private val languageMirror: SharedPreferences
        get() = context.getSharedPreferences(LOCALE_MIRROR_FILE, Context.MODE_PRIVATE)

    private val startTabMirror: SharedPreferences
        get() = context.getSharedPreferences("app_start_tab", Context.MODE_PRIVATE)

    private val timeFormatMirror: SharedPreferences
        get() = context.getSharedPreferences("app_time_format", Context.MODE_PRIVATE)

    companion object {
        /** Mirror file/key for the UI language (see [setLanguage]). */
        const val LOCALE_MIRROR_FILE = "app_locale"
        const val LOCALE_MIRROR_KEY = "language"
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val CARD_CORNER_STYLE = stringPreferencesKey("card_corner_style")
        val ORNAMENT_STYLE = stringPreferencesKey("ornament_style")
        val LANGUAGE = stringPreferencesKey("language")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val START_TAB = stringPreferencesKey("start_tab")
        val TIME_FORMAT_24H = booleanPreferencesKey("time_format_24h")
        val ACCESSIBILITY_READING_MODE = booleanPreferencesKey("accessibility_reading_mode")
        val INFORMATION_DENSITY = stringPreferencesKey("information_density")
        val ACCESSIBILITY_HIGH_CONTRAST = booleanPreferencesKey("accessibility_high_contrast")
        val VOICE_NAVIGATION_ENABLED = booleanPreferencesKey("voice_navigation_enabled")
        val WEAR_COMPANION_ENABLED = booleanPreferencesKey("wear_companion_enabled")
        val SHOW_PRAYER_TRACKER_ON_HOME = booleanPreferencesKey("show_prayer_tracker_on_home")
        val SMART_HOME_BRIDGE_ENABLED = booleanPreferencesKey("smart_home_bridge_enabled")
        val SMART_HOME_BRIDGE_ENDPOINT = stringPreferencesKey("smart_home_bridge_endpoint")
        val MORE_SECTION_ORDER = stringPreferencesKey("more_section_order")
        val MORE_SECTION_HIDDEN = stringPreferencesKey("more_section_hidden")
        val UPDATE_CHECK_ENABLED = booleanPreferencesKey("update_check_enabled")
        val UPDATE_CHECK_FREQUENCY = stringPreferencesKey("update_check_frequency")
        val AUTO_UPDATE_ENABLED = booleanPreferencesKey("auto_update_enabled")
        val LAST_UPDATE_CHECK = androidx.datastore.preferences.core.longPreferencesKey("last_update_check")
        val INITIAL_PERMISSION_SETUP_HANDLED = booleanPreferencesKey("initial_permission_setup_handled")
    }
}
