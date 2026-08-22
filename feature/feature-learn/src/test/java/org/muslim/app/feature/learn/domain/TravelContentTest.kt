package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TravelContentTest {
    @Test
    fun `distance calculator returns zero for the same local point`() {
        val point = TravelPoint(24.7136, 46.6753)

        assertThat(TravelContent.distanceKm(point, point)).isEqualTo(0.0)
    }

    @Test
    fun `distance calculator uses great circle kilometres`() {
        val distance = TravelContent.distanceKm(
            TravelPoint(0.0, 0.0),
            TravelPoint(0.0, 1.0),
        )

        assertThat(distance).isWithin(0.2).of(111.195)
    }

    @Test
    fun `reference assessment exposes both sides of the selected threshold`() {
        val origin = TravelPoint(0.0, 0.0)
        val shortTrip = TravelContent.assessDistance(
            origin,
            TravelPoint(0.0, 0.5),
            TravelDistanceThreshold.EIGHTY,
        )
        val longTrip = TravelContent.assessDistance(
            origin,
            TravelPoint(0.0, 1.0),
            TravelDistanceThreshold.NINETY,
        )

        assertThat(shortTrip.status).isEqualTo(TravelDistanceStatus.BELOW_REFERENCE)
        assertThat(longTrip.status).isEqualTo(TravelDistanceStatus.AT_OR_ABOVE_REFERENCE)
    }

    @Test
    fun `travel guide and high latitude choices retain review boundaries`() {
        val travelArabic = TravelContent.travelSections.flatMap { it.paragraphs }.joinToString(" ") { it.arabic }
        val transportEnglish = TravelContent.transportSections.flatMap { it.paragraphs }.joinToString(" ") { it.english }

        assertThat(travelArabic).contains("فتوى")
        assertThat(transportEnglish).contains("trusted teacher")
        assertThat(TravelContent.highLatitudeRules.map { it.rule.name })
            .containsExactly("MiddleOfTheNight", "SeventhOfTheNight", "TwilightAngle")
            .inOrder()
    }
}
