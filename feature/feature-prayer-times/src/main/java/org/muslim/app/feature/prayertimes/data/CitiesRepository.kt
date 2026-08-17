package org.muslim.app.feature.prayertimes.data

import org.muslim.app.feature.prayertimes.domain.City

/**
 * Embedded, offline city database (a curated set of major cities) so prayer
 * times work without internet or location permission. Extend [ALL] freely.
 */
object CitiesRepository {

    private val CITIES = listOf(
        // Middle East
        City("Mecca", "مكة المكرمة", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
        City("Medina", "المدينة المنورة", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh"),
        City("Riyadh", "الرياض", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        City("Jeddah", "جدة", "Saudi Arabia", 21.4858, 39.1925, "Asia/Riyadh"),
        City("Dubai", "دبي", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai"),
        City("Abu Dhabi", "أبوظبي", "United Arab Emirates", 24.4539, 54.3773, "Asia/Dubai"),
        City("Doha", "الدوحة", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        City("Kuwait City", "مدينة الكويت", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
        City("Manama", "المنامة", "Bahrain", 26.2285, 50.5860, "Asia/Bahrain"),
        City("Muscat", "مسقط", "Oman", 23.5880, 58.3829, "Asia/Muscat"),
        City("Sana'a", "صنعاء", "Yemen", 15.3694, 44.1910, "Asia/Aden"),
        City("Amman", "عمّان", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        City("Jerusalem", "القدس", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem"),
        City("Damascus", "دمشق", "Syria", 33.5138, 36.2765, "Asia/Damascus"),
        City("Beirut", "بيروت", "Lebanon", 33.8938, 35.5018, "Asia/Beirut"),
        City("Baghdad", "بغداد", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
        City("Cairo", "القاهرة", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        City("Alexandria", "الإسكندرية", "Egypt", 31.2001, 29.9187, "Africa/Cairo"),
        // North Africa
        City("Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, "Africa/Algiers"),
        City("Tunis", "تونس", "Tunisia", 36.8065, 10.1815, "Africa/Tunis"),
        City("Tripoli", "طرابلس", "Libya", 32.8872, 13.1913, "Africa/Tripoli"),
        City("Casablanca", "الدار البيضاء", "Morocco", 33.5731, -7.5898, "Africa/Casablanca"),
        City("Rabat", "الرباط", "Morocco", 34.0209, -6.8416, "Africa/Casablanca"),
        // Africa
        City("Khartoum", "الخرطوم", "Sudan", 15.5007, 32.5599, "Africa/Khartoum"),
        City("Addis Ababa", "أديس أبابا", "Ethiopia", 9.0192, 38.7525, "Africa/Addis_Ababa"),
        City("Lagos", "لاغوس", "Nigeria", 6.5244, 3.3792, "Africa/Lagos"),
        City("Nairobi", "نيروبي", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
        City("Dakar", "داكار", "Senegal", 14.7167, -17.4677, "Africa/Dakar"),
        City("Mogadishu", "مقديشو", "Somalia", 2.0469, 45.3182, "Africa/Mogadishu"),
        // Asia
        City("Istanbul", "إسطنبول", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        City("Ankara", "أنقرة", "Turkey", 39.9334, 32.8597, "Europe/Istanbul"),
        City("Tehran", "طهران", "Iran", 35.6892, 51.3890, "Asia/Tehran"),
        City("Mashhad", "مشهد", "Iran", 36.2605, 59.6168, "Asia/Tehran"),
        City("Islamabad", "إسلام آباد", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
        City("Karachi", "كراتشي", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        City("Lahore", "لاهور", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
        City("Dhaka", "دكا", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        City("Kuala Lumpur", "كوالالمبور", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        City("Jakarta", "جاكرتا", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        City("Singapore", "سنغافورة", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        City("Mumbai", "مومباي", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        City("Delhi", "دلهي", "India", 28.7041, 77.1025, "Asia/Kolkata"),
        City("Tashkent", "طشقند", "Uzbekistan", 41.2995, 69.2401, "Asia/Tashkent"),
        // Europe
        City("London", "لندن", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        City("Paris", "باريس", "France", 48.8566, 2.3522, "Europe/Paris"),
        City("Berlin", "برلين", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        City("Sarajevo", "سراييفو", "Bosnia and Herzegovina", 43.8563, 18.4131, "Europe/Sarajevo"),
        City("Moscow", "موسكو", "Russia", 55.7558, 37.6173, "Europe/Moscow"),
        // Americas
        City("New York", "نيويورك", "United States", 40.7128, -74.0060, "America/New_York"),
        City("Chicago", "شيكاغو", "United States", 41.8781, -87.6298, "America/Chicago"),
        City("Houston", "هيوستن", "United States", 29.7604, -95.3698, "America/Chicago"),
        City("Los Angeles", "لوس أنجلوس", "United States", 34.0522, -118.2437, "America/Los_Angeles"),
        City("Toronto", "تورونتو", "Canada", 43.6532, -79.3832, "America/Toronto"),
        // Oceania
        City("Sydney", "سيدني", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
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
