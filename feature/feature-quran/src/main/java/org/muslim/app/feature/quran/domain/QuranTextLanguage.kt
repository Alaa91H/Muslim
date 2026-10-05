package org.muslim.app.feature.quran.domain

import java.util.Locale

/**
 * Matches Quran text editions by BCP-47 language tag without mixing explicitly
 * selected scripts or regional editions. A base language (for example `pt`)
 * may use an installed regional edition (such as `pt-BR`) when no base edition
 * exists.
 */
internal fun <T> selectQuranTextLanguage(
    items: List<T>,
    preferredTag: String,
    languageTag: (T) -> String,
): List<T> {
    val preferred = Locale.forLanguageTag(preferredTag.trim())
    if (preferred.language.isBlank() || preferred.language.equals("und", ignoreCase = true)) return emptyList()
    val exact = items.filter { item ->
        Locale.forLanguageTag(languageTag(item).trim()).toLanguageTag()
            .equals(preferred.toLanguageTag(), ignoreCase = true)
    }
    if (exact.isNotEmpty()) return exact

    val preferredIsQualified = preferred.script.isNotBlank() || preferred.country.isNotBlank() ||
        preferred.variant.isNotBlank()
    return items.filter { item ->
        val content = Locale.forLanguageTag(languageTag(item).trim())
        content.language.equals(preferred.language, ignoreCase = true) &&
            (!preferredIsQualified || (content.script.isBlank() && content.country.isBlank() && content.variant.isBlank()))
    }
}
