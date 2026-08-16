package org.example.islamicapp.feature.quran.domain

import org.example.islamicapp.feature.quran.R

/**
 * Supported reciters for on-demand audio downloads (PROJECT_PROMPT.md §6
 * Phase 2: "مكتبة قرّاء متعددة عالميًا، تُنزَّل عند الطلب سورة سورة").
 *
 * [id] is the slug used by the islamic.network audio CDN (the same content
 * provider as the bundled mushaf text — see README.md "مصادر المحتوى").
 * Verified live for every entry below.
 */
enum class Reciter(
    val id: String,
    @androidx.annotation.StringRes val displayNameRes: Int,
) {
    Alafasy("ar.alafasy", R.string.reciter_alafasy),
    Husary("ar.husary", R.string.reciter_husary),
    Minshawi("ar.minshawi", R.string.reciter_minshawi),
    AhmedAjam("ar.ahmedajamy", R.string.reciter_ahmedajam),
    MaherMuaiqly("ar.mahermuaiqly", R.string.reciter_mahermuaiqly),
}
