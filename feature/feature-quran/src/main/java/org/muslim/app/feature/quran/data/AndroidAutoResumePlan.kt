package org.muslim.app.feature.quran.data

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
