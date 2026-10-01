package org.muslim.app.core.cast

data class ByteRange(val start: Long, val endInclusive: Long, val totalLength: Long) {
    val length: Long get() = endInclusive - start + 1L

    companion object {
        fun parse(header: String?, totalLength: Long): ByteRange? {
            if (header == null || totalLength <= 0L || !header.startsWith("bytes=", ignoreCase = true)) return null
            val spec = header.substringAfter('=')
            if (',' in spec) return null
            val parts = spec.split('-', limit = 2)
            if (parts.size != 2) return null
            val start = parts[0].toLongOrNull()
            val requestedEnd = parts[1].takeIf(String::isNotEmpty)?.toLongOrNull()
            if (parts[0].isEmpty()) {
                val suffixLength = requestedEnd ?: return null
                if (suffixLength <= 0L) return null
                val suffixStart = (totalLength - suffixLength).coerceAtLeast(0L)
                return ByteRange(suffixStart, totalLength - 1L, totalLength)
            }
            val actualStart = start ?: return null
            val end = requestedEnd ?: (totalLength - 1L)
            if (actualStart < 0L || actualStart >= totalLength || end < actualStart) return null
            return ByteRange(actualStart, end.coerceAtMost(totalLength - 1L), totalLength)
        }
    }
}
