package org.muslim.app.core.datastore

import org.muslim.app.core.common.appearance.AppColorPalette
import org.muslim.app.core.common.appearance.CardCornerStyle
import org.muslim.app.core.common.appearance.AppOrnamentStyle

/**
 * App-wide (non-prayer) user preferences, persisted in DataStore
 * (PROJECT_PROMPT.md §4.1 + §5: theme mode, dynamic color, UI language).
 */
data class AppPreferences(
    val themeMode: AppThemeMode = AppThemeMode.System,
    /** Dynamic wallpaper color is opt-in so the calm Islamic token palette remains the default identity. */
    val dynamicColor: Boolean = false,
    /** Curated palette family used when dynamic colour is disabled. */
    val colorPalette: AppColorPalette = AppColorPalette.Classic,
    /** Global corner softness for cards, sheets, and controls. */
    val cardCornerStyle: CardCornerStyle = CardCornerStyle.Soft,
    /** Decorative background motif for supported screens. */
    val ornamentStyle: AppOrnamentStyle = AppOrnamentStyle.Geometry,
    /** BCP-47 language tag; "system" = follow the device language. */
    val languageCode: String = SYSTEM_LANGUAGE,
    /** Respect the system "remove animations" accessibility setting. */
    val reduceAnimations: Boolean = false,
    /** Navigation route shown as the start destination (default: prayer-times home). */
    val startTab: String = START_TAB_HOME,
    /** 12-hour vs 24-hour clock for every time shown in the app (default 12h). */
    val timeFormat24h: Boolean = false,
    /** Uses a clearer bundled Arabic typeface and more generous Arabic reading spacing. */
    val accessibilityReadingMode: Boolean = false,
    /** Preferred amount of supporting detail in adaptable screens and configuration dialogs. */
    val informationDensity: AppInformationDensity = AppInformationDensity.Comfortable,
    /** Uses the high-contrast application colour scheme instead of dynamic wallpaper colours. */
    val accessibilityHighContrast: Boolean = false,
    /** Exposes the explicit, user-triggered microphone button for one-shot navigation commands. */
    val voiceNavigationEnabled: Boolean = false,
    /** Publishes only the opted-in prayer/tasbih snapshot to a paired Wear OS app. */
    val wearCompanionEnabled: Boolean = false,
    /** Shows the optional prayer-completion summary on the home screen. */
    val showPrayerTrackerOnHome: Boolean = false,
    /** Enables an explicit HTTPS event after a locally scheduled adhan begins. */
    val smartHomeBridgeEnabled: Boolean = false,
    /** HTTPS destination selected by the user; empty until they configure a bridge. */
    val smartHomeBridgeEndpoint: String = "",
    /**
     * Order in which the sections of the "More" hub are rendered, as a list of
     * [MORE_SECTION] ids. Default: worship → knowledge → tools → app.
     */
    val moreSectionOrder: List<String> = DEFAULT_MORE_SECTION_ORDER,
    /**
     * Sections of the "More" hub the user chose to hide, as a set of
     * [MORE_SECTION] ids. Hidden sections are skipped when [moreSectionOrder]
     * is rendered.
     */
    val hiddenMoreSections: Set<String> = emptySet(),
    /**
     * Whether the app checks the GitHub releases page for a newer version.
     * Off by default; the periodic [AppPreferences.updateCheckFrequency]
     * worker only runs while this is enabled (and always respects the unified
     * notification manager for the AppUpdate category).
     */
    val updateCheckEnabled: Boolean = false,
    /**
     * How often the update check runs when [updateCheckEnabled]: one of
     * [UPDATE_CHECK_DAILY], [UPDATE_CHECK_WEEKLY], [UPDATE_CHECK_MONTHLY].
     */
    val updateCheckFrequency: String = UPDATE_CHECK_DAILY,
    /**
     * Fully automatic updates: when enabled (after a one-time confirmation)
     * a newly-found release is downloaded and installed through the
     * PackageInstaller Session API with no further prompts. Default: off.
     */
    val autoUpdateEnabled: Boolean = false,
    /** Epoch millis of the last successful update check (0 = never checked). */
    val lastUpdateCheckEpoch: Long = 0L,
    /** Radius selected for the on-demand nearby-mosque search. */
    val nearbyMosqueSearchRadiusKm: Int = DEFAULT_NEARBY_MOSQUE_RADIUS_KM,
    /** Serialized on-device mosque cache. It contains places, not old user distances. */
    val nearbyMosqueCacheJson: String = "",
    /** Epoch millis when [nearbyMosqueCacheJson] was last refreshed (0 = never). */
    val nearbyMosqueCacheSavedAtEpochMillis: Long = 0L,
) {
    companion object {
        const val SYSTEM_LANGUAGE = "system"
        const val START_TAB_HOME = "home"
        const val DEFAULT_NEARBY_MOSQUE_RADIUS_KM = 5

        /** The four "More" hub section ids, in their default order. */
        val DEFAULT_MORE_SECTION_ORDER = listOf(
            MORE_SECTION_WORSHIP,
            MORE_SECTION_KNOWLEDGE,
            MORE_SECTION_TOOLS,
            MORE_SECTION_APP,
        )

        const val MORE_SECTION_WORSHIP = "worship"
        const val MORE_SECTION_KNOWLEDGE = "knowledge"
        const val MORE_SECTION_TOOLS = "tools"
        const val MORE_SECTION_APP = "app"

        /** Update-check cadence ids (see [updateCheckFrequency]). */
        const val UPDATE_CHECK_DAILY = "daily"
        const val UPDATE_CHECK_WEEKLY = "weekly"
        const val UPDATE_CHECK_MONTHLY = "monthly"

        /**
         * Decodes a persisted comma-separated section order into a full, valid
         * 4-item list. Unknown ids are dropped and any known section missing
         * from the stored value is appended, so a corrupt/old value still
         * yields a complete order.
         */
        fun decodeSectionOrder(raw: String?): List<String> {
            if (raw.isNullOrBlank()) return DEFAULT_MORE_SECTION_ORDER
            val parsed = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val result = parsed.filter { it in DEFAULT_MORE_SECTION_ORDER }.distinct().toMutableList()
            DEFAULT_MORE_SECTION_ORDER.forEach { if (it !in result) result.add(it) }
            return result
        }

        /** Decodes a persisted comma-separated set of hidden section ids. */
        fun decodeHiddenSections(raw: String?): Set<String> =
            if (raw.isNullOrBlank()) emptySet()
            else raw.split(',').map { it.trim() }.filter { it in DEFAULT_MORE_SECTION_ORDER }.toSet()
    }
}

/** User-selected information density for responsive application surfaces. */
enum class AppInformationDensity {
    Comfortable,
    Compact,
}

/** Theme selection (PROJECT_PROMPT.md §4.1: فاتح / داكن / حسب النظام). */
enum class AppThemeMode {
    System,
    Light,
    Dark,
}
