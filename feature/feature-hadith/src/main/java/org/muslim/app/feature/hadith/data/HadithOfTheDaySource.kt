package org.muslim.app.feature.hadith.data

import org.muslim.app.feature.hadith.domain.Hadith

/**
 * The repository dependency of [HadithOfTheDayWorker]. Kept as a tiny
 * interface so unit tests can fake it without instantiating the real
 * repository (or the Room/Hilt graph it depends on).
 */
interface HadithOfTheDaySource {
    suspend fun hadithOfTheDay(): Hadith?

    /** True when the user has the daily hadith notification enabled. */
    suspend fun isDailyNotificationEnabled(): Boolean
}
