package org.muslim.app.feature.qibla.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.muslim.app.feature.qibla.R

/** Device regression for the semantic description of the Kaaba compass marker. */
class QiblaCompassAccessibilityInstrumentationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compass_exposes_kaaba_marker_description_to_screen_readers() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val markerDescription = context.getString(R.string.qibla_marker_description)

        composeRule.setContent {
            MaterialTheme {
                QiblaCompassContent(
                    gpsState = QiblaGpsState.Idle,
                    presentation = QiblaPresentation(
                        locationName = "Test location",
                        trueHeading = 42f,
                        bearing = 119.0,
                        distanceKm = 1_000.0,
                        bearingCardinal = "East",
                        headingCardinal = "Northeast",
                        facingQibla = false,
                        turnRight = true,
                        turnDegrees = 77.0,
                        needsCalibration = false,
                        needsFlatPosture = false,
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onGpsRefresh = {},
                )
            }
        }

        composeRule.onNode(
            hasContentDescription(markerDescription, substring = true),
        ).assertExists()
    }
}
