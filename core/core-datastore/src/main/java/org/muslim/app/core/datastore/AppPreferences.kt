package org.muslim.app.core.datastore

/**
 * App-wide (non-prayer) user preferences, persisted in DataStore
 * (PROJECT_PROMPT.md §4.1 + §5: theme mode, dynamic color, UI language).
 */
data class AppPreferences(
    val themeMode: AppThemeMode = AppThemeMode.System,
    val dynamicColor: Boolean = true,
    /** BCP-47 language tag; "system" = follow the device language. */
    val languageCode: String = SYSTEM_LANGUAGE,
    /** Respect the system "remove animations" accessibility setting. */
    val reduceAnimations: Boolean = false,
) {
    companion object {
        const val SYSTEM_LANGUAGE = "system"
    }
}

/** Theme selection (PROJECT_PROMPT.md §4.1: فاتح / داكن / حسب النظام). */
enum class AppThemeMode {
    System,
    Light,
    Dark,
}
