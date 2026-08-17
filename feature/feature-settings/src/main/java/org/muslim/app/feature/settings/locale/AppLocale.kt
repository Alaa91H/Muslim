package org.muslim.app.feature.settings.locale

import android.content.Context
import android.content.res.Configuration
import org.muslim.app.core.datastore.AppPreferences
import java.util.Locale

/**
 * Applies the user-chosen UI language (PROJECT_PROMPT.md §5: إمكانية اختيار
 * لغة التطبيق بشكل مستقل عن لغة نظام الجهاز) to a context's resources.
 *
 * `"system"` follows the device language; any other value is a BCP-47 tag
 * (e.g. "ar", "en") resolved via [Locale.forLanguageTag].
 *
 * The applied locale always uses Western (Latin) digits — `nu-latn` — so times,
 * counts and distances never render Arabic-Indic numerals, regardless of
 * the device or app language (PROJECT_PROMPT.md §6: الأرقام الغربية).
 */
fun Context.withAppLocale(languageCode: String): Context {
    val base = if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
        Locale.getDefault()
    } else {
        Locale.forLanguageTag(languageCode)
    }
    val locale = base.withLatinNumerals()

    // Keep process-wide formatting (DateTimeFormatter, String.format) on the
    // same Latin-digit locale as the resources, so no Arabic-Indic digits can
    // leak through the default-locale number formatting paths.
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

/** Same language/region, but Western digits only (no Arabic-Indic digits). */
private fun Locale.withLatinNumerals(): Locale =
    Locale.Builder()
        .setLocale(this)
        .setUnicodeLocaleKeyword("nu", "latn")
        .build()
