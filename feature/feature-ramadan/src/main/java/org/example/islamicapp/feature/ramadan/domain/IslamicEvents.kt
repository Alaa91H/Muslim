package org.example.islamicapp.feature.ramadan.domain

import org.example.islamicapp.core.common.time.HijriDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Islamic occasions (PROJECT_PROMPT.md §6 Phase 8 "التقويم والمناسبات
 * الإسلامية") computed from the app-wide Hijri calendar, each with the next
 * Gregorian occurrence and a day countdown.
 */
data class IslamicEvent(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val nextDate: LocalDate,
    val daysUntil: Long,
)

object IslamicEvents {

    /** All occasions tracked by the app (hijri month/day pairs). */
    private data class Definition(
        val id: String,
        val titleAr: String,
        val titleEn: String,
        val month: Int,
        val day: Int,
    )

    private val definitions = listOf(
        Definition("ramadan_start", "بداية شهر رمضان", "Start of Ramadan", 9, 1),
        Definition("laylat_al_qadr_hint", "الليالي العشر الأواخر", "Last ten nights", 9, 21),
        Definition("eid_al_fitr", "عيد الفطر", "Eid al-Fitr", 10, 1),
        Definition("day_of_arafah", "يوم عرفة", "Day of Arafah", 12, 9),
        Definition("eid_al_adha", "عيد الأضحى", "Eid al-Adha", 12, 10),
        Definition("islamic_new_year", "رأس السنة الهجرية", "Islamic New Year", 1, 1),
        Definition("ashura", "يوم عاشوراء", "Day of Ashura", 1, 10),
        Definition("mawlid", "المولد النبوي", "Mawlid an-Nabawi", 3, 12),
        Definition("isra_miraj", "الإسراء والمعراج", "Isra and Mi'raj", 7, 27),
    )

    /**
     * Upcoming occasions (today included) within the next [horizonDays]
     * days, sorted by date.
     */
    fun upcoming(
        today: LocalDate = LocalDate.now(),
        hijriAdjustment: Int = 0,
        horizonDays: Long = 400,
    ): List<IslamicEvent> {
        val todayHijri = HijriDate.from(today, hijriAdjustment)
        // Check this Hijri year and the next so events early next year appear.
        return (todayHijri.year..todayHijri.year + 1).flatMap { year ->
            definitions.mapNotNull { def ->
                val date = runCatching {
                    HijriDate.of(year, def.month, def.day, hijriAdjustment).gregorian
                }.getOrNull() ?: return@mapNotNull null
                val days = ChronoUnit.DAYS.between(today, date)
                if (days in 0..horizonDays) {
                    IslamicEvent(def.id, def.titleAr, def.titleEn, def.month, def.day, date, days)
                } else {
                    null
                }
            }
        }.sortedBy { it.nextDate }
    }
}
