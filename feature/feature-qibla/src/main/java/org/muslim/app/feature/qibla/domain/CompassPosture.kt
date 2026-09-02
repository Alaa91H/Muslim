package org.muslim.app.feature.qibla.domain

import kotlin.math.abs

/** Validates whether a phone posture can provide a meaningful flat compass heading. */
object CompassPosture {
    const val MAX_TILT_DEGREES = 60f

    fun isLevel(pitchDegrees: Float, rollDegrees: Float): Boolean =
        pitchDegrees.isFinite() && rollDegrees.isFinite() &&
            abs(pitchDegrees) <= MAX_TILT_DEGREES &&
            abs(rollDegrees) <= MAX_TILT_DEGREES
}
