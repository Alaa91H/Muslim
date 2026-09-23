package org.muslim.app.feature.reference.data

import android.content.Context
data class ReferenceReaderLocation(
    val bookId: String,
    val topicId: String,
    val scrollIndex: Int = 0,
)

internal object ReferenceReaderKeyCodec {
    private const val SEPARATOR = "|"

    fun topicKey(bookId: String, topicId: String): String = "${bookId}/${topicId}"

    fun encodeLocation(location: ReferenceReaderLocation): String =
        listOf(
            location.bookId,
            location.topicId,
            location.scrollIndex.coerceAtLeast(0).toString(),
        ).joinToString(SEPARATOR)

    fun decodeLocation(raw: String?): ReferenceReaderLocation? {
        if (raw.isNullOrBlank()) return null
        val parts = raw.split(SEPARATOR)
        if (parts.size != 3) return null
        val bookId = parts[0].trim()
        val topicId = parts[1].trim()
        val scrollIndex = parts[2].toIntOrNull()?.coerceAtLeast(0) ?: return null
        if (bookId.isBlank() || topicId.isBlank()) return null
        return ReferenceReaderLocation(bookId, topicId, scrollIndex)
    }

    fun clampFontStep(value: Int): Int = value.coerceIn(0, 3)
}

/**
 * Device-local preferences for the reference reader.
 *
 * The data is intentionally small and privacy-preserving: topic identifiers,
 * the last visible list item, bookmark identifiers, and a four-step font size.
 */
class ReferenceReaderPreferences(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE,
    )

    fun bookmarkKeys(): Set<String> =
        preferences.getStringSet(KEY_BOOKMARKS, emptySet()).orEmpty().toSet()

    fun isBookmarked(bookId: String, topicId: String): Boolean =
        ReferenceReaderKeyCodec.topicKey(bookId, topicId) in bookmarkKeys()

    fun setBookmarked(bookId: String, topicId: String, bookmarked: Boolean): Set<String> {
        val key = ReferenceReaderKeyCodec.topicKey(bookId, topicId)
        val updated = bookmarkKeys().toMutableSet().apply {
            if (bookmarked) add(key) else remove(key)
        }.toSet()
        preferences.edit().putStringSet(KEY_BOOKMARKS, updated).apply()
        return updated
    }

    fun lastRead(): ReferenceReaderLocation? =
        ReferenceReaderKeyCodec.decodeLocation(preferences.getString(KEY_LAST_READ, null))

    fun saveLastRead(location: ReferenceReaderLocation) {
        preferences.edit()
            .putString(KEY_LAST_READ, ReferenceReaderKeyCodec.encodeLocation(location))
            .apply()
    }

    fun savedScrollIndex(bookId: String, topicId: String): Int =
        preferences.getInt(scrollKey(bookId, topicId), 0).coerceAtLeast(0)

    fun saveScrollIndex(bookId: String, topicId: String, scrollIndex: Int) {
        val safeIndex = scrollIndex.coerceAtLeast(0)
        preferences.edit()
            .putInt(scrollKey(bookId, topicId), safeIndex)
            .putString(
                KEY_LAST_READ,
                ReferenceReaderKeyCodec.encodeLocation(
                    ReferenceReaderLocation(bookId, topicId, safeIndex),
                ),
            )
            .apply()
    }

    fun fontStep(): Int =
        ReferenceReaderKeyCodec.clampFontStep(preferences.getInt(KEY_FONT_STEP, DEFAULT_FONT_STEP))

    fun setFontStep(step: Int): Int {
        val normalized = ReferenceReaderKeyCodec.clampFontStep(step)
        preferences.edit().putInt(KEY_FONT_STEP, normalized).apply()
        return normalized
    }

    private fun scrollKey(bookId: String, topicId: String): String =
        "scroll_${ReferenceReaderKeyCodec.topicKey(bookId, topicId)}"

    private companion object {
        const val FILE_NAME = "reference_reader"
        const val KEY_BOOKMARKS = "bookmarks"
        const val KEY_LAST_READ = "last_read"
        const val KEY_FONT_STEP = "font_step"
        const val DEFAULT_FONT_STEP = 1
    }
}
