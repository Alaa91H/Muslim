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
 */
fun Context.withAppLocale(languageCode: String): Context {
    val locale = if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
        Locale.getDefault()
    } else {
        Locale.forLanguageTag(languageCode)
    }
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
