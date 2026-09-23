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
import org.muslim.app.core.common.appearance.OrnamentIntensity
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
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            amoledBlack = prefs[Keys.AMOLED_BLACK] ?: false,
            colorPalette = enumOr(prefs[Keys.COLOR_PALETTE], AppColorPalette.Classic),
            cardCornerStyle = enumOr(prefs[Keys.CARD_CORNER_STYLE], CardCornerStyle.Soft),
            ornamentStyle = enumOr(prefs[Keys.ORNAMENT_STYLE], AppOrnamentStyle.Geometry),
            ornamentIntensity = enumOr(prefs[Keys.ORNAMENT_INTENSITY], OrnamentIntensity.Balanced),
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
            updateChannel = prefs[Keys.UPDATE_CHANNEL]
                ?.takeIf {
                    it == AppPreferences.UPDATE_CHANNEL_STABLE ||
                        it == AppPreferences.UPDATE_CHANNEL_BETA
                }
                ?: AppPreferences.UPDATE_CHANNEL_STABLE,
            autoUpdateEnabled = prefs[Keys.AUTO_UPDATE_ENABLED] ?: false,
            autoUpdateWifiOnly = prefs[Keys.AUTO_UPDATE_WIFI_ONLY] ?: true,
            lastUpdateCheckEpoch = prefs[Keys.LAST_UPDATE_CHECK] ?: 0L,
            lastNotifiedUpdateVersion = prefs[Keys.LAST_NOTIFIED_UPDATE_VERSION].orEmpty(),
            nearbyMosqueSearchRadiusKm = (prefs[Keys.NEARBY_MOSQUE_SEARCH_RADIUS_KM]
                ?: AppPreferences.DEFAULT_NEARBY_MOSQUE_RADIUS_KM)
                .takeIf { it in AppPreferences.NEARBY_MOSQUE_RADIUS_OPTIONS_KM }
                ?: AppPreferences.DEFAULT_NEARBY_MOSQUE_RADIUS_KM,
            nearbyMosqueCacheJson = prefs[Keys.NEARBY_MOSQUE_CACHE_JSON].orEmpty(),
            nearbyMosqueCacheSavedAtEpochMillis = prefs[Keys.NEARBY_MOSQUE_CACHE_SAVED_AT] ?: 0L,
            updateDownloadId = prefs[Keys.UPDATE_DOWNLOAD_ID] ?: -1L,
            updateDownloadVersion = prefs[Keys.UPDATE_DOWNLOAD_VERSION].orEmpty(),
            updateDownloadFileName = prefs[Keys.UPDATE_DOWNLOAD_FILE_NAME].orEmpty(),
            updateDownloadSha256 = prefs[Keys.UPDATE_DOWNLOAD_SHA256].orEmpty(),
            updateDownloadVersionCode = prefs[Keys.UPDATE_DOWNLOAD_VERSION_CODE] ?: 0L,
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) = edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = edit { prefs -> prefs[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setAmoledBlack(enabled: Boolean) = edit { prefs -> prefs[Keys.AMOLED_BLACK] = enabled }

    suspend fun setColorPalette(palette: AppColorPalette) = edit { prefs ->
        prefs[Keys.COLOR_PALETTE] = palette.name
    }

    suspend fun setCardCornerStyle(style: CardCornerStyle) = edit { prefs ->
        prefs[Keys.CARD_CORNER_STYLE] = style.name
    }

    suspend fun setOrnamentStyle(style: AppOrnamentStyle) = edit { prefs ->
        prefs[Keys.ORNAMENT_STYLE] = style.name
    }

    suspend fun setOrnamentIntensity(intensity: OrnamentIntensity) = edit { prefs ->
        prefs[Keys.ORNAMENT_INTENSITY] = intensity.name
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

    /** Selects stable-only or beta-inclusive GitHub release discovery. */
    suspend fun setUpdateChannel(channel: String) {
        require(
            channel == AppPreferences.UPDATE_CHANNEL_STABLE ||
                channel == AppPreferences.UPDATE_CHANNEL_BETA,
        ) { "Unsupported update channel: $channel" }
        edit { prefs -> prefs[Keys.UPDATE_CHANNEL] = channel }
    }

    /** Enables/disables automatic download of newly discovered releases. */
    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        edit { prefs -> prefs[Keys.AUTO_UPDATE_ENABLED] = enabled }
    }

    /** Restricts automatic update downloads to Wi-Fi. */
    suspend fun setAutoUpdateWifiOnly(enabled: Boolean) {
        edit { prefs -> prefs[Keys.AUTO_UPDATE_WIFI_ONLY] = enabled }
    }

    /** Records the timestamp of the last successful update check. */
    suspend fun setLastUpdateCheck(epochMillis: Long) {
        edit { prefs -> prefs[Keys.LAST_UPDATE_CHECK] = epochMillis }
    }

    /** Records the release version for which a notification was actually posted. */
    suspend fun setLastNotifiedUpdateVersion(version: String) {
        edit { prefs -> prefs[Keys.LAST_NOTIFIED_UPDATE_VERSION] = version.trim() }
    }

    /** Persists the DownloadManager record so download state survives process death. */
    suspend fun setUpdateDownload(
        id: Long,
        version: String,
        fileName: String,
        sha256: String?,
        versionCode: Long?,
    ) {
        edit { prefs ->
            prefs[Keys.UPDATE_DOWNLOAD_ID] = id
            prefs[Keys.UPDATE_DOWNLOAD_VERSION] = version.trim()
            prefs[Keys.UPDATE_DOWNLOAD_FILE_NAME] = fileName
            if (sha256.isNullOrBlank()) prefs.remove(Keys.UPDATE_DOWNLOAD_SHA256)
            else prefs[Keys.UPDATE_DOWNLOAD_SHA256] = sha256.trim().lowercase()
            if (versionCode == null || versionCode <= 0L) prefs.remove(Keys.UPDATE_DOWNLOAD_VERSION_CODE)
            else prefs[Keys.UPDATE_DOWNLOAD_VERSION_CODE] = versionCode
        }
    }

    /** Clears persisted update-download metadata after replacement/cancellation. */
    suspend fun clearUpdateDownload() {
        edit { prefs ->
            prefs.remove(Keys.UPDATE_DOWNLOAD_ID)
            prefs.remove(Keys.UPDATE_DOWNLOAD_VERSION)
            prefs.remove(Keys.UPDATE_DOWNLOAD_FILE_NAME)
            prefs.remove(Keys.UPDATE_DOWNLOAD_SHA256)
            prefs.remove(Keys.UPDATE_DOWNLOAD_VERSION_CODE)
        }
    }

    /** Persists a supported nearby-mosque radius and rejects corrupted values. */
    suspend fun setNearbyMosqueSearchRadiusKm(radiusKm: Int) {
        require(radiusKm in AppPreferences.NEARBY_MOSQUE_RADIUS_OPTIONS_KM) { "Unsupported mosque radius: $radiusKm" }
        edit { prefs -> prefs[Keys.NEARBY_MOSQUE_SEARCH_RADIUS_KM] = radiusKm }
    }

    /** Stores raw mosque places and their refresh timestamp; user distances are always recalculated. */
    suspend fun setNearbyMosqueCache(serializedPlaces: String, savedAtEpochMillis: Long) {
        edit { prefs ->
            prefs[Keys.NEARBY_MOSQUE_CACHE_JSON] = serializedPlaces
            prefs[Keys.NEARBY_MOSQUE_CACHE_SAVED_AT] = savedAtEpochMillis
        }
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
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val CARD_CORNER_STYLE = stringPreferencesKey("card_corner_style")
        val ORNAMENT_STYLE = stringPreferencesKey("ornament_style")
        val ORNAMENT_INTENSITY = stringPreferencesKey("ornament_intensity")
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
        val UPDATE_CHANNEL = stringPreferencesKey("update_channel")
        val AUTO_UPDATE_ENABLED = booleanPreferencesKey("auto_update_enabled")
        val AUTO_UPDATE_WIFI_ONLY = booleanPreferencesKey("auto_update_wifi_only")
        val LAST_UPDATE_CHECK = androidx.datastore.preferences.core.longPreferencesKey("last_update_check")
        val LAST_NOTIFIED_UPDATE_VERSION = stringPreferencesKey("last_notified_update_version")
        val UPDATE_DOWNLOAD_ID = androidx.datastore.preferences.core.longPreferencesKey("update_download_id")
        val UPDATE_DOWNLOAD_VERSION = stringPreferencesKey("update_download_version")
        val UPDATE_DOWNLOAD_FILE_NAME = stringPreferencesKey("update_download_file_name")
        val UPDATE_DOWNLOAD_SHA256 = stringPreferencesKey("update_download_sha256")
        val UPDATE_DOWNLOAD_VERSION_CODE = androidx.datastore.preferences.core.longPreferencesKey("update_download_version_code")
        val NEARBY_MOSQUE_SEARCH_RADIUS_KM = androidx.datastore.preferences.core.intPreferencesKey("nearby_mosque_search_radius_km")
        val NEARBY_MOSQUE_CACHE_JSON = stringPreferencesKey("nearby_mosque_cache_json")
        val NEARBY_MOSQUE_CACHE_SAVED_AT = androidx.datastore.preferences.core.longPreferencesKey("nearby_mosque_cache_saved_at")
        val INITIAL_PERMISSION_SETUP_HANDLED = booleanPreferencesKey("initial_permission_setup_handled")
    }

}
