package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.QuranAyahIndex

/** A valid on-device queue played through the existing shared recitation engine. */
internal fun newAndroidAutoSessionIntent(
    reciterId: String,
    surahNumber: Int,
    globalNumbers: List<Int>,
): RecitationSessionIntent? {
    if (reciterId.isBlank() || surahNumber !in 1..114 || globalNumbers.isEmpty()) return null
    if (globalNumbers.any { QuranAyahIndex.surahOf(it) != surahNumber }) return null
    if (globalNumbers.zipWithNext().any { (first, next) -> first >= next }) return null

    return RecitationSessionIntent(
        reciterId = reciterId,
        surahNumber = surahNumber,
        globalNumbers = globalNumbers,
        repeatCount = 1,
        continuous = false,
        advanceToNext = false,
        toEndOfQuran = false,
    )
}

/** Validated snapshot needed to resume the existing Quran queue from Android Auto. */
internal data class AndroidAutoResumePlan(
    val intent: RecitationSessionIntent,
    val positionMs: Long,
    val remainingRepeats: Int,
)

/** Rejects resume requests that cannot be restored exactly from local audio. */
internal fun buildAndroidAutoResumePlan(
    saved: PersistedRecitationSession?,
    knownReciterIds: Set<String>,
    isAyahAvailableOffline: (Int) -> Boolean,
): AndroidAutoResumePlan? {
    val restorable = saved?.asRestorableOrNull() ?: return null
    if (restorable.intent.reciterId !in knownReciterIds) return null

    val remainingGlobals = restorable.remainingGlobalNumbers()
    if (remainingGlobals.isEmpty() || remainingGlobals.any { !isAyahAvailableOffline(it) }) return null

    return AndroidAutoResumePlan(
        intent = restorable.intent.copy(globalNumbers = remainingGlobals),
        positionMs = restorable.positionMs,
        remainingRepeats = restorable.remainingRepeats,
    )
}
