package org.muslim.app.wear

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the effective language synchronized from the phone app to the Wear
 * context. The phone resolves its "System" setting before transport, so the
 * watch follows the phone application's effective language even when the watch
 * itself is configured in another language.
 */
internal fun Context.withSyncedWearLocale(languageTag: String?): Context {
    if (languageTag.isNullOrBlank()) return this

    val parsed = Locale.forLanguageTag(languageTag)
    if (parsed.language.isBlank() || parsed.language == "und") return this

    val locale = Locale.Builder()
        .setLocale(parsed)
        .setUnicodeLocaleKeyword("nu", "latn")
        .build()

    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
