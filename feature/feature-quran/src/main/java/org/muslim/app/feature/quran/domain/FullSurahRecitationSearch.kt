package org.muslim.app.feature.quran.domain

/** Search helper for the separate catalogue of recordings that contain complete surahs. */
object FullSurahRecitationSearch {
    private val arabicMarks = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

    fun filter(recordings: List<FullSurahRecitation>, query: String): List<FullSurahRecitation> {
        val terms = normalize(query).split(Regex("\\s+")).filter(String::isNotBlank)
        if (terms.isEmpty()) return recordings

        return recordings.filter { recording ->
            val searchable = normalize(
                "${recording.reciterName} ${recording.rewayaName} ${recording.id} " +
                    "${recording.reciterId} ${recording.recordingId}",
            )
            terms.all(searchable::contains)
        }
    }

    private fun normalize(value: String): String = value
        .lowercase(java.util.Locale.ROOT)
        .replace(arabicMarks, "")
        .replace('\u0640', ' ')
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
}
