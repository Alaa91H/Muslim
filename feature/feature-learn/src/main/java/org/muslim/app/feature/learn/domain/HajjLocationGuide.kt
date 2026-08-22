package org.muslim.app.feature.learn.domain

import kotlin.math.abs

/** Named sacred-site region used to choose contextual guidance offline. */
enum class SacredSite { KAABA, SAFA_MARWA, ARAFAH, MUZDALIFAH, MINA, JAMARAT, HARAMAIN }

data class SacredSiteGuidance(
    val site: SacredSite,
    val title: String,
    val supplication: String,
    val note: String,
)

data class SacredSiteLocation(
    val site: SacredSite,
    val latitude: Double,
    val longitude: Double,
    val label: String,
)

object HajjLocationGuide {
    private const val MAKKAH_LAT = 21.4225
    private const val MAKKAH_LON = 39.8262

    /** Offline coordinates for the principal sites shown on the map. */
    val locations: List<SacredSiteLocation> = listOf(
        SacredSiteLocation(SacredSite.KAABA, 21.4225, 39.8262, "الكعبة المشرفة"),
        SacredSiteLocation(SacredSite.SAFA_MARWA, 21.4230, 39.8260, "الصفا والمروة"),
        SacredSiteLocation(SacredSite.MINA, 21.4133, 39.8932, "منى"),
        SacredSiteLocation(SacredSite.JAMARAT, 21.4177, 39.8736, "الجمرات"),
        SacredSiteLocation(SacredSite.MUZDALIFAH, 21.3891, 39.9388, "مزدلفة"),
        SacredSiteLocation(SacredSite.ARAFAH, 21.3549, 39.9841, "عرفات"),
    )

    private val guidance = mapOf(
        SacredSite.KAABA to SacredSiteGuidance(
            SacredSite.KAABA,
            "رؤية الكعبة والطواف",
            "اللهم زد هذا البيت تشريفًا وتعظيمًا ومهابةً وأمنًا، وزد من حجّه أو اعتمره تشريفًا وتعظيمًا.",
            "اذكر الله وادع بما تيسر، ولا تخص الحجر الأسود بدعاء ثابت عند كل شوط.",
        ),
        SacredSite.SAFA_MARWA to SacredSiteGuidance(
            SacredSite.SAFA_MARWA,
            "الصفا والمروة",
            "إن الصفا والمروة من شعائر الله، أبدأ بما بدأ الله به. لا إله إلا الله وحده لا شريك له...",
            "يبدأ السعي من الصفا وينتهي بالمروة، والشوط ذهاب واحد.",
        ),
        SacredSite.ARAFAH to SacredSiteGuidance(
            SacredSite.ARAFAH,
            "عرفة",
            "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.",
            "أكثر من الدعاء والذكر من بعد الزوال إلى غروب الشمس.",
        ),
        SacredSite.MUZDALIFAH to SacredSiteGuidance(
            SacredSite.MUZDALIFAH,
            "مزدلفة",
            "لبيك اللهم لبيك، لبيك لا شريك لك لبيك.",
            "صل المغرب والعشاء جمعًا، واذكر الله عند المشعر الحرام.",
        ),
        SacredSite.MINA to SacredSiteGuidance(
            SacredSite.MINA,
            "منى",
            "الله أكبر، الله أكبر، الله أكبر، لا إله إلا الله والله أكبر.",
            "أكثر من الذكر في أيام التشريق وارم الجمرات في أوقاتها.",
        ),
        SacredSite.JAMARAT to SacredSiteGuidance(
            SacredSite.JAMARAT,
            "الجمرات",
            "الله أكبر مع كل حصاة.",
            "بعد الصغرى والوسطى ادع طويلًا مستقبل القبلة، ولا دعاء مخصوصًا بعد الكبرى.",
        ),
    )

    fun guidanceFor(site: SacredSite): SacredSiteGuidance = guidance.getValue(site)

    /** Offline approximation; GPS is only used to select a nearby region. */
    fun siteAt(latitude: Double, longitude: Double): SacredSite? {
        if (abs(latitude - MAKKAH_LAT) > 0.25 || abs(longitude - MAKKAH_LON) > 0.25) return null
        return when {
            latitude > 21.45 -> SacredSite.MINA
            latitude < 21.35 -> SacredSite.ARAFAH
            longitude < 39.80 -> SacredSite.SAFA_MARWA
            else -> SacredSite.KAABA
        }
    }
}
