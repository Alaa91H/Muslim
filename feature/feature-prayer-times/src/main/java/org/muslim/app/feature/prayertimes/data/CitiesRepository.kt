package org.muslim.app.feature.prayertimes.data

import org.muslim.app.feature.prayertimes.domain.City

/**
 * Embedded, offline city database (a curated set of major cities) so prayer
 * times work without internet or location permission. Extend [ALL] freely.
 */
object CitiesRepository {

    private val CITIES = listOf(
        // Middle East
        City("Mecca", "مكة المكرمة", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh", 277.0),
        City("Medina", "المدينة المنورة", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh", 608.0),
        City("Riyadh", "الرياض", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh", 612.0),
        City("Jeddah", "جدة", "Saudi Arabia", 21.4858, 39.1925, "Asia/Riyadh", 12.0),
        City("Dubai", "دبي", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai", 5.0),
        City("Abu Dhabi", "أبوظبي", "United Arab Emirates", 24.4539, 54.3773, "Asia/Dubai", 27.0),
        City("Doha", "الدوحة", "Qatar", 25.2854, 51.5310, "Asia/Qatar", 7.0),
        City("Kuwait City", "مدينة الكويت", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait", 5.0),
        City("Manama", "المنامة", "Bahrain", 26.2285, 50.5860, "Asia/Bahrain", 0.0),
        City("Muscat", "مسقط", "Oman", 23.5880, 58.3829, "Asia/Muscat", 15.0),
        City("Sana'a", "صنعاء", "Yemen", 15.3694, 44.1910, "Asia/Aden", 2250.0),
        City("Amman", "عمّان", "Jordan", 31.9454, 35.9284, "Asia/Amman", 777.0),
        City("Jerusalem", "القدس", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem", 754.0),
        City("Damascus", "دمشق", "Syria", 33.5138, 36.2765, "Asia/Damascus", 680.0),
        City("Beirut", "بيروت", "Lebanon", 33.8938, 35.5018, "Asia/Beirut", 77.0),
        City("Baghdad", "بغداد", "Iraq", 33.3152, 44.3661, "Asia/Baghdad", 34.0),
        City("Cairo", "القاهرة", "Egypt", 30.0444, 31.2357, "Africa/Cairo", 23.0),
        City("Alexandria", "الإسكندرية", "Egypt", 31.2001, 29.9187, "Africa/Cairo", 5.0),
        // North Africa
        City("Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, "Africa/Algiers", 120.0),
        City("Tunis", "تونس", "Tunisia", 36.8065, 10.1815, "Africa/Tunis", 41.0),
        City("Tripoli", "طرابلس", "Libya", 32.8872, 13.1913, "Africa/Tripoli", 81.0),
        City("Casablanca", "الدار البيضاء", "Morocco", 33.5731, -7.5898, "Africa/Casablanca", 27.0),
        City("Rabat", "الرباط", "Morocco", 34.0209, -6.8416, "Africa/Casablanca", 75.0),
        // Africa
        City("Khartoum", "الخرطوم", "Sudan", 15.5007, 32.5599, "Africa/Khartoum", 381.0),
        City("Addis Ababa", "أديس أبابا", "Ethiopia", 9.0192, 38.7525, "Africa/Addis_Ababa", 2355.0),
        City("Lagos", "لاغوس", "Nigeria", 6.5244, 3.3792, "Africa/Lagos", 41.0),
        City("Nairobi", "نيروبي", "Kenya", -1.2921, 36.8219, "Africa/Nairobi", 1795.0),
        City("Dakar", "داكار", "Senegal", 14.7167, -17.4677, "Africa/Dakar", 22.0),
        City("Mogadishu", "مقديشو", "Somalia", 2.0469, 45.3182, "Africa/Mogadishu", 9.0),
        // Asia
        City("Istanbul", "إسطنبول", "Turkey", 41.0082, 28.9784, "Europe/Istanbul", 40.0),
        City("Ankara", "أنقرة", "Turkey", 39.9334, 32.8597, "Europe/Istanbul", 938.0),
        City("Tehran", "طهران", "Iran", 35.6892, 51.3890, "Asia/Tehran", 1191.0),
        City("Mashhad", "مشهد", "Iran", 36.2605, 59.6168, "Asia/Tehran", 985.0),
        City("Islamabad", "إسلام آباد", "Pakistan", 33.6844, 73.0479, "Asia/Karachi", 540.0),
        City("Karachi", "كراتشي", "Pakistan", 24.8607, 67.0011, "Asia/Karachi", 8.0),
        City("Lahore", "لاهور", "Pakistan", 31.5204, 74.3587, "Asia/Karachi", 217.0),
        City("Dhaka", "دكا", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka", 4.0),
        City("Kuala Lumpur", "كوالالمبور", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur", 66.0),
        City("Jakarta", "جاكرتا", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta", 8.0),
        City("Singapore", "سنغافورة", "Singapore", 1.3521, 103.8198, "Asia/Singapore", 15.0),
        City("Mumbai", "مومباي", "India", 19.0760, 72.8777, "Asia/Kolkata", 14.0),
        City("Delhi", "دلهي", "India", 28.7041, 77.1025, "Asia/Kolkata", 216.0),
        City("Tashkent", "طشقند", "Uzbekistan", 41.2995, 69.2401, "Asia/Tashkent", 455.0),
        // Europe
        City("London", "لندن", "United Kingdom", 51.5074, -0.1278, "Europe/London", 11.0),
        City("Paris", "باريس", "France", 48.8566, 2.3522, "Europe/Paris", 35.0),
        City("Berlin", "برلين", "Germany", 52.5200, 13.4050, "Europe/Berlin", 34.0),
        City("Sarajevo", "سراييفو", "Bosnia and Herzegovina", 43.8563, 18.4131, "Europe/Sarajevo", 518.0),
        City("Moscow", "موسكو", "Russia", 55.7558, 37.6173, "Europe/Moscow", 156.0),
        // Americas
        City("New York", "نيويورك", "United States", 40.7128, -74.0060, "America/New_York", 10.0),
        City("Chicago", "شيكاغو", "United States", 41.8781, -87.6298, "America/Chicago", 182.0),
        City("Houston", "هيوستن", "United States", 29.7604, -95.3698, "America/Chicago", 12.0),
        City("Los Angeles", "لوس أنجلوس", "United States", 34.0522, -118.2437, "America/Los_Angeles", 71.0),
        City("Toronto", "تورونتو", "Canada", 43.6532, -79.3832, "America/Toronto", 76.0),
        // Oceania
        City("Sydney", "سيدني", "Australia", -33.8688, 151.2093, "Australia/Sydney", 58.0),
    )

    val all: List<City> get() = CITIES

    /** Case-insensitive search over name, Arabic name and country. */
    fun search(query: String, limit: Int = 40): List<City> {
        val q = query.trim()
        if (q.isEmpty()) return CITIES.take(limit)
        val lower = q.lowercase()
        return CITIES.filter {
            it.name.lowercase().contains(lower) ||
                it.nameArabic.contains(q) ||
                it.country.lowercase().contains(lower)
        }.take(limit)
    }
}
