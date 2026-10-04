package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.CastPlaybackState
import org.muslim.app.feature.quran.domain.CastPrayerLocation
import org.muslim.app.feature.quran.domain.CastPrayerTime
import org.muslim.app.feature.quran.domain.CastText
import org.muslim.app.feature.quran.domain.QuranCastPayload
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation

data class QuranCastMappingInput(
    val ayah: Ayah?,
    val surah: Surah?,
    val localizedSurahName: String?,
    val reciter: Reciter,
    val sessionId: String,
    val sequence: Long,
    val timestampEpochMs: Long,
    val positionMs: Long,
    val playbackState: PlaybackState,
    val durationMs: Long,
    val snapshot: RecitationPlaybackSnapshot?,
    val translations: List<Translation>,
    val selectedLanguage: String,
    val tafsir: List<TafsirEntry>,
    val prayerLocation: CastPrayerLocation?,
    val prayerTimes: List<CastPrayerTime>,
)

/** Maps the single local Quran playback state into the versioned Cast contract. */
object QuranCastMapper {
    fun map(input: QuranCastMappingInput): QuranCastPayload? = with(input) {
        val activeAyah = ayah ?: return null
        val translation = selectTranslation(translations, selectedLanguage)
        val queue = queueMetadata(snapshot, activeAyah.globalNumber)

        return QuranCastPayload(
            sessionId = sessionId,
            sequence = sequence,
            timestampEpochMs = timestampEpochMs,
            languageTag = translation?.language ?: "und",
            surahNumber = activeAyah.surahNumber,
            surahArabicName = surah?.arabicName ?: "سورة ${activeAyah.surahNumber}",
            surahLocalizedName = localizedSurahName?.takeIf(String::isNotBlank)
                ?: surah?.englishName?.takeIf(String::isNotBlank)
                ?: "Surah ${activeAyah.surahNumber}",
            totalAyahs = surah?.takeIf { it.number == activeAyah.surahNumber }?.ayahCount ?: activeAyah.numberInSurah,
            revelationType = surah?.takeIf { it.number == activeAyah.surahNumber }?.revelationType,
            ayahNumber = activeAyah.numberInSurah,
            globalAyahNumber = activeAyah.globalNumber,
            reciterName = reciter.name,
            reciterId = reciter.id,
            audioUrl = reciter.urlFor(activeAyah.surahNumber, activeAyah.numberInSurah),
            durationMs = durationMs.takeIf { it > 0L },
            positionMs = positionMs.coerceAtLeast(0L),
            playbackState = playbackState.toCastPlaybackState(),
            repeatCount = queue.repeatCount,
            remainingRepeats = queue.remainingRepeats,
            queueGlobalNumbers = queue.globalNumbers,
            queueIndex = queue.index,
            arabicAyah = activeAyah.text,
            translation = translation?.let { CastText(it.text, it.language, it.language) },
            tafsir = mapTafsir(tafsir, activeAyah.globalNumber),
            prayerLocation = prayerLocation,
            prayerTimes = prayerTimes,
        )
    }

    private fun selectTranslation(translations: List<Translation>, language: String): Translation? =
        translations.firstOrNull { it.language.equals(language, ignoreCase = true) } ?: translations.firstOrNull()

    private fun queueMetadata(snapshot: RecitationPlaybackSnapshot?, globalAyahNumber: Int): CastQueueMetadata {
        val numbers = snapshot?.queue?.map(RecitationQueueItem::globalNumber)
        val index = snapshot?.queueIndex
        val hasCurrentItem = index != null && numbers?.getOrNull(index) == globalAyahNumber
        return CastQueueMetadata(
            globalNumbers = numbers.takeIf { hasCurrentItem }.orEmpty().ifEmpty { listOf(globalAyahNumber) },
            index = index?.takeIf { hasCurrentItem } ?: 0,
            repeatCount = snapshot?.repeatCount ?: 1,
            remainingRepeats = snapshot?.remainingRepeats ?: 1,
        )
    }

    private fun mapTafsir(entries: List<TafsirEntry>, globalAyahNumber: Int): List<CastText> =
        entries.asSequence()
            .filter { it.globalNumber == globalAyahNumber }
            .map { entry ->
                CastText(
                    text = entry.text,
                    source = listOf(entry.title.takeIf(String::isNotBlank) ?: entry.source, entry.translator, entry.publisher)
                        .filter(String::isNotBlank)
                        .distinct()
                        .joinToString(" · "),
                    languageTag = entry.language.takeIf { it.isNotBlank() && !it.equals("und", ignoreCase = true) }
                        ?: if (entry.source.contains("english", ignoreCase = true)) "en" else "ar",
                )
            }.toList()

    private fun PlaybackState.toCastPlaybackState(): CastPlaybackState = when (this) {
        PlaybackState.Playing -> CastPlaybackState.PLAYING
        PlaybackState.Paused -> CastPlaybackState.PAUSED
        PlaybackState.Idle -> CastPlaybackState.IDLE
    }

    private data class CastQueueMetadata(
        val globalNumbers: List<Int>,
        val index: Int,
        val repeatCount: Int,
        val remainingRepeats: Int,
    )
}
