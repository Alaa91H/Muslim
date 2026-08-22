package org.muslim.app.feature.learn.domain

import org.muslim.app.core.common.prayer.HighLatitudeRule
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A locally stored geographical point used only as a travel-distance reference. */
data class TravelPoint(
    val latitude: Double,
    val longitude: Double,
)

enum class TravelDistanceThreshold(val kilometres: Double) {
    EIGHTY(80.0),
    NINETY(90.0),
}

enum class TravelDistanceStatus {
    BELOW_REFERENCE,
    AT_OR_ABOVE_REFERENCE,
}

data class TravelDistanceAssessment(
    val distanceKm: Double,
    val threshold: TravelDistanceThreshold,
    val status: TravelDistanceStatus,
)

data class TravelText(
    val arabic: String,
    val english: String,
) {
    fun resolve(isArabic: Boolean): String = if (isArabic) arabic else english
}

data class TravelGuideSection(
    val id: String,
    val title: TravelText,
    val paragraphs: List<TravelText>,
)

data class HighLatitudeRuleInfo(
    val rule: HighLatitudeRule,
    val title: TravelText,
    val description: TravelText,
)

/**
 * Educational travel tools. They show a straight-line GPS reference and never
 * issue a personal ruling on qasr, jam', residency or the exact travel route.
 */
object TravelContent {
    val travelSections = listOf(
        TravelGuideSection(
            id = "before_departure",
            title = TravelText("قبل الانطلاق", "Before departure"),
            paragraphs = listOf(
                TravelText(
                    "ثبّت نقطة انطلاقك محلياً ثم حدّث موقعك عند الحاجة. المسافة هنا خط مستقيم بين نقطتي GPS وليست مسافة طريق أو تذكرة سفر.",
                    "Save a local departure point, then refresh your location when needed. This distance is a straight GPS line, not a road or ticket distance.",
                ),
                TravelText(
                    "تستعمل الأداة عتبة مرجعية قابلة للاختيار (80 أو 90 كم) لتوضيح أن تقدير مسافة السفر مختلف فيه؛ اسأل جهة علمية موثوقة عن حالتك ومسار رحلتك ومدة إقامتك.",
                    "The tool offers an 80 km or 90 km reference to make the scholarly distance difference visible. Ask a trusted local scholar about your journey, route, and intended stay.",
                ),
            ),
        ),
        TravelGuideSection(
            id = "qasr_and_jam",
            title = TravelText("القصر والجمع", "Shortening and combining"),
            paragraphs = listOf(
                TravelText(
                    "القصر والجمع من مسائل فقه السفر التي تختلف في شروطها ومدة الإقامة ونوع الصلاة. لا يحوّل تجاوز العتبة المرجعية وحده إلى فتوى شخصية.",
                    "Shortening and combining have travel-fiqh conditions that differ by school, intended stay, and prayer. Crossing a reference distance alone is not a personal fatwa.",
                ),
                TravelText(
                    "رتّب خطتك قبل الرحلة مع إمام أو جهة علمية تعرف واقع بلدك؛ واحرص على أوقات الصلاة كما تظهر في إعدادات التطبيق ومركزك الإسلامي المحلي.",
                    "Plan before travelling with an imam or scholarly organisation familiar with your context, and check the prayer times shown by your app settings and local mosque.",
                ),
            ),
        ),
    )

    val transportSections = listOf(
        TravelGuideSection(
            id = "water_and_wudu",
            title = TravelText("الوضوء والطهارة", "Wudu and purification"),
            paragraphs = listOf(
                TravelText(
                    "إن أمكن استعمال الماء بلا أذى أو مخالفة لتعليمات السلامة، فتوضأ بهدوء وبقدر الحاجة. لا تعطل الممرات ولا تضيّق على الآخرين.",
                    "When water can be used safely and within crew rules, make wudu calmly and use only what is needed. Do not block aisles or inconvenience others.",
                ),
                TravelText(
                    "عند فقد الماء أو تعذّر استعماله، فالتيمم وتفاصيل مادته وشروطه مسائل فقهية؛ اسأل معلماً موثوقاً قبل التطبيق، خصوصاً في الطائرة أو وسيلة مشتركة.",
                    "When water is unavailable or unusable, tayammum and its material and conditions are fiqh matters. Ask a trusted teacher before applying it, especially on an aircraft or shared transport.",
                ),
            ),
        ),
        TravelGuideSection(
            id = "prayer_on_transport",
            title = TravelText("الصلاة في الطائرة والقطار", "Prayer on a plane or train"),
            paragraphs = listOf(
                TravelText(
                    "تحقّق من وقت الصلاة وخطة التوقف أو مساحة الصلاة المسموح بها قبل السفر. اتبع تعليمات الطاقم والسلامة، واسألهم بلطف عن مكان مناسب إن وُجد.",
                    "Check the prayer time, stop plan, and permitted space before travel. Follow crew and safety instructions, and politely ask about a suitable area when available.",
                ),
                TravelText(
                    "استقبل القبلة قدر استطاعتك، واستخدم البوصلة المحلية في هذا القسم كمعين تقني. إذا تعذر الوقوف أو الركوع أو السجود أو ثبات الاتجاه، فالتطبيق العملي يحتاج سؤال أهل العلم بحسب القدرة والظرف.",
                    "Face the qibla to the best of your ability and use this section’s offline compass as a technical aid. If standing, bowing, prostration, or maintaining direction is not possible, ask qualified scholars about applying the guidance to your circumstances.",
                ),
            ),
        ),
    )

    val highLatitudeRules = listOf(
        HighLatitudeRuleInfo(
            rule = HighLatitudeRule.MiddleOfTheNight,
            title = TravelText("منتصف الليل", "Middle of the night"),
            description = TravelText(
                "يضبط الفجر والعشاء حول منتصف الفترة الليلية عند تعذر العلامات. هو خيار حسابي من الخيارات المعروضة، وليس ترجيحاً فقهياً عاماً.",
                "Bounds Fajr and Isha around the midpoint of the night when signs are unavailable. It is a calculation option, not a universal fiqh preference.",
            ),
        ),
        HighLatitudeRuleInfo(
            rule = HighLatitudeRule.SeventhOfTheNight,
            title = TravelText("سُبع الليل", "One-seventh of the night"),
            description = TravelText(
                "يقسم الليل إلى سبعة أجزاء لتقدير الفجر والعشاء. يظل اختيار الجهة العلمية أو المسجد المحلي مقدماً عند وجود تقويم معتمد.",
                "Divides the night into seven parts to estimate Fajr and Isha. A local mosque or scholarly authority’s adopted timetable should take priority where available.",
            ),
        ),
        HighLatitudeRuleInfo(
            rule = HighLatitudeRule.TwilightAngle,
            title = TravelText("قاعدة زاوية الشفق", "Twilight-angle rule"),
            description = TravelText(
                "يستعمل زاوية الفجر والعشاء المختارة في إعدادات الحساب كنسبة من الليل. تتغير الزوايا والمناهج بين الجهات والمناطق.",
                "Uses the selected Fajr/Isha angles as a fraction of the night. Angles and methods vary by authority and region.",
            ),
        ),
    )

    fun distanceKm(origin: TravelPoint, destination: TravelPoint): Double {
        validate(origin)
        validate(destination)
        val earthRadiusKm = 6371.0088
        val latitudeDelta = (destination.latitude - origin.latitude).toRadians()
        val longitudeDelta = (destination.longitude - origin.longitude).toRadians()
        val a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
            cos(origin.latitude.toRadians()) * cos(destination.latitude.toRadians()) *
            sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        return earthRadiusKm * 2 * asin(sqrt(a))
    }

    fun assessDistance(
        origin: TravelPoint,
        destination: TravelPoint,
        threshold: TravelDistanceThreshold,
    ): TravelDistanceAssessment {
        val distance = distanceKm(origin, destination)
        val status = if (distance >= threshold.kilometres) {
            TravelDistanceStatus.AT_OR_ABOVE_REFERENCE
        } else {
            TravelDistanceStatus.BELOW_REFERENCE
        }
        return TravelDistanceAssessment(distance, threshold, status)
    }

    private fun validate(point: TravelPoint) {
        require(point.latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(point.longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
    }

    private fun Double.toRadians(): Double = Math.toRadians(this)
}
