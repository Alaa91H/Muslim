package org.example.islamicapp.feature.prayertimes.domain

/**
 * A city from the embedded offline database (works with no internet).
 *
 * @param name     city name (English, also used as the search key)
 * @param nameArabic city name in Arabic
 * @param country  country name
 * @param latitude latitude in degrees
 * @param longitude longitude in degrees
 * @param timeZone IANA time-zone identifier used for prayer-time computation
 */
data class City(
    val name: String,
    val nameArabic: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
) {
    val displayName: String get() = nameArabic.ifBlank { name }
}
