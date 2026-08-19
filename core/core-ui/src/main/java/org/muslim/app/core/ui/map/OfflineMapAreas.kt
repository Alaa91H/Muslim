package org.muslim.app.core.ui.map

import org.maplibre.android.geometry.LatLngBounds

/**
 * A predefined area the user can download for offline use.
 *
 * @param name     User-facing name (kept bilingual where practical).
 * @param kind     "city" or "country".
 * @param bounds   Bounding box that defines the download.
 */
data class OfflineMapArea(
    val name: String,
    val nameArabic: String,
    val kind: String,
    val bounds: LatLngBounds,
)

/**
 * Curated set of offline download presets: major cities and countries with
 * their bounding boxes. Cities use a tight box around the urban core
 * (≈ 0.5°), countries use their full national bounds.
 */
object OfflineMapAreas {

    /** Builds a bounding box from a center point and a half-span in degrees. */
    private fun box(lat: Double, lng: Double, halfSpan: Double): LatLngBounds =
        LatLngBounds.Builder()
            .include(org.maplibre.android.geometry.LatLng(lat - halfSpan, lng - halfSpan))
            .include(org.maplibre.android.geometry.LatLng(lat + halfSpan, lng + halfSpan))
            .build()

    private const val CITY_SPAN = 0.5

    val CITIES: List<OfflineMapArea> = listOf(
        OfflineMapArea("Mecca", "مكة المكرمة", "city", box(21.4225, 39.8262, CITY_SPAN)),
        OfflineMapArea("Medina", "المدينة المنورة", "city", box(24.4672, 39.6111, CITY_SPAN)),
        OfflineMapArea("Riyadh", "الرياض", "city", box(24.7136, 46.6753, CITY_SPAN)),
        OfflineMapArea("Jeddah", "جدة", "city", box(21.4858, 39.1925, CITY_SPAN)),
        OfflineMapArea("Dubai", "دبي", "city", box(25.2048, 55.2708, CITY_SPAN)),
        OfflineMapArea("Abu Dhabi", "أبوظبي", "city", box(24.4539, 54.3773, CITY_SPAN)),
        OfflineMapArea("Doha", "الدوحة", "city", box(25.2854, 51.5310, CITY_SPAN)),
        OfflineMapArea("Kuwait City", "مدينة الكويت", "city", box(29.3759, 47.9774, CITY_SPAN)),
        OfflineMapArea("Cairo", "القاهرة", "city", box(30.0444, 31.2357, CITY_SPAN)),
        OfflineMapArea("Alexandria", "الإسكندرية", "city", box(31.2001, 29.9187, CITY_SPAN)),
        OfflineMapArea("Amman", "عمّان", "city", box(31.9454, 35.9284, CITY_SPAN)),
        OfflineMapArea("Jerusalem", "القدس", "city", box(31.7683, 35.2137, CITY_SPAN)),
        OfflineMapArea("Damascus", "دمشق", "city", box(33.5138, 36.2765, CITY_SPAN)),
        OfflineMapArea("Beirut", "بيروت", "city", box(33.8938, 35.5018, CITY_SPAN)),
        OfflineMapArea("Baghdad", "بغداد", "city", box(33.3152, 44.3661, CITY_SPAN)),
        OfflineMapArea("Istanbul", "إسطنبول", "city", box(41.0082, 28.9784, CITY_SPAN)),
        OfflineMapArea("Ankara", "أنقرة", "city", box(39.9334, 32.8597, CITY_SPAN)),
        OfflineMapArea("Algiers", "الجزائر", "city", box(36.7538, 3.0588, CITY_SPAN)),
        OfflineMapArea("Tunis", "تونس", "city", box(36.8065, 10.1815, CITY_SPAN)),
        OfflineMapArea("Tripoli", "طرابلس", "city", box(32.8872, 13.1913, CITY_SPAN)),
        OfflineMapArea("Casablanca", "الدار البيضاء", "city", box(33.5731, -7.5898, CITY_SPAN)),
        OfflineMapArea("Rabat", "الرباط", "city", box(34.0209, -6.8416, CITY_SPAN)),
        OfflineMapArea("Khartoum", "الخرطوم", "city", box(15.5007, 32.5599, CITY_SPAN)),
        OfflineMapArea("Addis Ababa", "أديس أبابا", "city", box(9.0192, 38.7525, CITY_SPAN)),
        OfflineMapArea("Lagos", "لاغوس", "city", box(6.5244, 3.3792, CITY_SPAN)),
        OfflineMapArea("Nairobi", "نيروبي", "city", box(-1.2921, 36.8219, CITY_SPAN)),
        OfflineMapArea("London", "لندن", "city", box(51.5074, -0.1278, CITY_SPAN)),
        OfflineMapArea("Paris", "باريس", "city", box(48.8566, 2.3522, CITY_SPAN)),
        OfflineMapArea("Berlin", "برلين", "city", box(52.5200, 13.4050, CITY_SPAN)),
        OfflineMapArea("Madrid", "مدريد", "city", box(40.4168, -3.7038, CITY_SPAN)),
        OfflineMapArea("Rome", "روما", "city", box(41.9028, 12.4964, CITY_SPAN)),
        OfflineMapArea("Moscow", "موسكو", "city", box(55.7558, 37.6173, CITY_SPAN)),
        OfflineMapArea("New York", "نيويورك", "city", box(40.7128, -74.0060, CITY_SPAN)),
        OfflineMapArea("Los Angeles", "لوس أنجلوس", "city", box(34.0522, -118.2437, CITY_SPAN)),
        OfflineMapArea("Toronto", "تورونتو", "city", box(43.6532, -79.3832, CITY_SPAN)),
        OfflineMapArea("Mexico City", "مكسيكو سيتي", "city", box(19.4326, -99.1332, CITY_SPAN)),
        OfflineMapArea("Sao Paulo", "ساو باولو", "city", box(-23.5505, -46.6333, CITY_SPAN)),
        OfflineMapArea("Buenos Aires", "بوينس آيرس", "city", box(-34.6037, -58.3816, CITY_SPAN)),
        OfflineMapArea("Jakarta", "جاكرتا", "city", box(-6.2088, 106.8456, CITY_SPAN)),
        OfflineMapArea("Kuala Lumpur", "كوالالمبور", "city", box(3.1390, 101.6869, CITY_SPAN)),
        OfflineMapArea("Singapore", "سنغافورة", "city", box(1.3521, 103.8198, CITY_SPAN)),
        OfflineMapArea("Bangkok", "بانكوك", "city", box(13.7563, 100.5018, CITY_SPAN)),
        OfflineMapArea("Manila", "مانيلا", "city", box(14.5995, 120.9842, CITY_SPAN)),
        OfflineMapArea("Tokyo", "طوكيو", "city", box(35.6762, 139.6503, CITY_SPAN)),
        OfflineMapArea("Osaka", "أوساكا", "city", box(34.6937, 135.5023, CITY_SPAN)),
        OfflineMapArea("Seoul", "سيول", "city", box(37.5665, 126.9780, CITY_SPAN)),
        OfflineMapArea("Beijing", "بكين", "city", box(39.9042, 116.4074, CITY_SPAN)),
        OfflineMapArea("Shanghai", "شنغهاي", "city", box(31.2304, 121.4737, CITY_SPAN)),
        OfflineMapArea("Delhi", "دلهي", "city", box(28.7041, 77.1025, CITY_SPAN)),
        OfflineMapArea("Mumbai", "مومباي", "city", box(19.0760, 72.8777, CITY_SPAN)),
        OfflineMapArea("Karachi", "كراتشي", "city", box(24.8607, 67.0011, CITY_SPAN)),
        OfflineMapArea("Lahore", "لاهور", "city", box(31.5204, 74.3587, CITY_SPAN)),
        OfflineMapArea("Dhaka", "دكا", "city", box(23.8103, 90.4125, CITY_SPAN)),
        OfflineMapArea("Tehran", "طهران", "city", box(35.6892, 51.3890, CITY_SPAN)),
        OfflineMapArea("Sydney", "سيدني", "city", box(-33.8688, 151.2093, CITY_SPAN)),
        OfflineMapArea("Melbourne", "ملبورن", "city", box(-37.8136, 144.9631, CITY_SPAN)),
    )

    val COUNTRIES: List<OfflineMapArea> = listOf(
        country("Saudi Arabia", "السعودية", 16.3, 32.2, 34.5, 55.7),
        country("United Arab Emirates", "الإمارات", 22.6, 26.1, 51.5, 56.4),
        country("Qatar", "قطر", 24.4, 26.2, 50.7, 51.7),
        country("Kuwait", "الكويت", 28.5, 30.1, 46.5, 48.5),
        country("Bahrain", "البحرين", 25.6, 26.3, 50.4, 50.8),
        country("Oman", "عمان", 16.6, 26.4, 52.0, 59.9),
        country("Yemen", "اليمن", 12.1, 19.0, 42.5, 54.6),
        country("Jordan", "الأردن", 29.2, 33.4, 34.9, 39.3),
        country("Palestine", "فلسطين", 31.2, 32.6, 34.2, 35.6),
        country("Syria", "سوريا", 32.3, 37.3, 35.7, 42.4),
        country("Lebanon", "لبنان", 33.0, 34.7, 35.1, 36.6),
        country("Iraq", "العراق", 29.1, 37.4, 38.8, 48.6),
        country("Iran", "إيران", 25.1, 39.8, 44.0, 63.3),
        country("Turkey", "تركيا", 35.8, 42.1, 26.0, 44.8),
        country("Egypt", "مصر", 22.0, 31.7, 24.7, 37.0),
        country("Algeria", "الجزائر", 18.9, 37.1, -8.7, 12.0),
        country("Morocco", "المغرب", 27.7, 35.9, -13.2, -1.0),
        country("Tunisia", "تونس", 30.2, 37.5, 7.5, 11.6),
        country("Libya", "ليبيا", 19.5, 33.2, 9.3, 25.2),
        country("Sudan", "السودان", 8.7, 22.2, 21.8, 38.7),
        country("Ethiopia", "إثيوبيا", 3.4, 14.9, 33.0, 48.0),
        country("Nigeria", "نيجيريا", 4.3, 13.9, 2.7, 14.7),
        country("Kenya", "كينيا", -4.7, 5.5, 33.9, 41.9),
        country("South Africa", "جنوب أفريقيا", -34.8, -22.1, 16.5, 32.9),
        country("United Kingdom", "بريطانيا", 49.9, 60.9, -8.6, 1.8),
        country("France", "فرنسا", 41.3, 51.1, -5.1, 9.6),
        country("Germany", "ألمانيا", 47.3, 55.1, 5.9, 15.0),
        country("Spain", "إسبانيا", 36.0, 43.8, -9.3, 3.3),
        country("Italy", "إيطاليا", 36.6, 47.1, 6.6, 18.5),
        country("Russia", "روسيا", 41.2, 81.9, 19.6, 180.0),
        country("United States", "الولايات المتحدة", 24.5, 49.4, -125.0, -66.9),
        country("Canada", "كندا", 41.7, 83.1, -141.0, -52.6),
        country("Mexico", "المكسيك", 14.5, 32.7, -117.1, -86.7),
        country("Brazil", "البرازيل", -33.8, 5.3, -73.9, -34.8),
        country("Argentina", "الأرجنتين", -55.1, -21.8, -73.6, -53.6),
        country("Indonesia", "إندونيسيا", -11.0, 6.1, 95.0, 141.0),
        country("Malaysia", "ماليزيا", 0.9, 7.4, 99.6, 119.3),
        country("Thailand", "تايلاند", 5.6, 20.5, 97.3, 105.6),
        country("Philippines", "الفلبين", 4.6, 21.1, 116.9, 126.6),
        country("Japan", "اليابان", 30.4, 45.6, 129.4, 145.8),
        country("South Korea", "كوريا الجنوبية", 33.1, 38.6, 125.9, 129.6),
        country("China", "الصين", 18.2, 53.6, 73.5, 134.8),
        country("India", "الهند", 6.8, 35.5, 68.2, 97.4),
        country("Pakistan", "باكستان", 23.7, 37.1, 60.9, 77.8),
        country("Bangladesh", "بنغلاديش", 20.6, 26.6, 88.0, 92.7),
        country("Afghanistan", "أفغانستان", 29.4, 38.5, 60.5, 74.9),
        country("Australia", "أستراليا", -43.6, -10.7, 112.9, 153.6),
        country("New Zealand", "نيوزيلندا", -47.3, -34.4, 166.4, 178.6),
    )

    private fun country(
        name: String,
        nameArabic: String,
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
    ): OfflineMapArea = OfflineMapArea(
        name = name,
        nameArabic = nameArabic,
        kind = "country",
        bounds = LatLngBounds.Builder()
            .include(org.maplibre.android.geometry.LatLng(minLat, minLng))
            .include(org.maplibre.android.geometry.LatLng(maxLat, maxLng))
            .build(),
    )
}
