package org.muslim.app.feature.tasbih.data

import org.muslim.app.feature.tasbih.domain.TasbihPhrase

/**
 * Versioned codec for persisted tasbih state.
 *
 * v1 stored enum ordinals. v2 stores explicit stable ids so reordering phrases
 * can never move a user's count to a different dhikr.
 */
internal object TasbihPersistenceCodec {
    const val CURRENT_VERSION = 2

    fun encodeCounts(counts: Map<TasbihPhrase, Int>): String =
        counts.entries
            .filter { it.value > 0 }
            .joinToString(";") { (phrase, count) -> "${phrase.storageId}:${count.coerceAtLeast(0)}" }

    /**
     * Reads both v2 stable ids and the legacy v1 ordinal format.
     * Invalid or negative values are ignored instead of poisoning the state.
     */
    fun decodeCounts(raw: String?): Map<TasbihPhrase, Int> =
        raw.orEmpty()
            .split(";")
            .mapNotNull { entry ->
                if (entry.isBlank()) return@mapNotNull null
                val separator = entry.lastIndexOf(':')
                if (separator <= 0 || separator == entry.lastIndex) return@mapNotNull null

                val key = entry.substring(0, separator)
                val count = entry.substring(separator + 1).toIntOrNull()
                    ?.takeIf { it >= 0 }
                    ?: return@mapNotNull null
                val phrase = resolvePhrase(key) ?: return@mapNotNull null
                phrase to count
            }
            .toMap()

    fun resolveSelectedPhrase(
        stableId: String?,
        legacyOrdinal: Int?,
        fallback: TasbihPhrase = TasbihPhrase.SubhanAllah,
    ): TasbihPhrase =
        TasbihPhrase.fromStorageId(stableId)
            ?: legacyOrdinal?.let(TasbihPhrase.entries::getOrNull)
            ?: fallback

    private fun resolvePhrase(key: String): TasbihPhrase? =
        TasbihPhrase.fromStorageId(key)
            ?: key.toIntOrNull()?.let(TasbihPhrase.entries::getOrNull)
}
