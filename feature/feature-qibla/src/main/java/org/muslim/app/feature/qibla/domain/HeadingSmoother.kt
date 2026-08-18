package org.muslim.app.feature.qibla.domain

/**
 * Exponential smoother for a circular (angular) signal.
 *
 * A naive low-pass filter on degrees wraps badly at the 0°/360° boundary;
 * this smoother operates on the shortest angular arc instead, so a reading
 * of 359° followed by 1° moves the output by 2°, not 358°.
 *
 * The output converges on the raw reading: the higher [alpha] the faster the
 * dial catches up (and the more it jitters); the lower it is the calmer the
 * needle (at the cost of lag). The default balances the two for a compass.
 */
class HeadingSmoother(
    private val alpha: Double = DEFAULT_ALPHA,
) {
    private var hasValue = false
    private var smoothed = 0.0

    /** Feeds one raw reading (degrees, 0..360) and returns the smoothed value. */
    fun update(rawDegrees: Float): Float {
        val raw = normalize(rawDegrees.toDouble())
        if (!hasValue) {
            smoothed = raw
            hasValue = true
            return raw.toFloat()
        }
        // Shortest signed arc from the current output to the raw reading.
        var delta = raw - smoothed
        if (delta > 180.0) delta -= 360.0
        if (delta < -180.0) delta += 360.0
        smoothed = normalize(smoothed + alpha * delta)
        return smoothed.toFloat()
    }

    /** Resets the filter (e.g. after a location or reference change). */
    fun reset() {
        hasValue = false
        smoothed = 0.0
    }

    private fun normalize(degrees: Double): Double =
        ((degrees % 360.0) + 360.0) % 360.0

    companion object {
        const val DEFAULT_ALPHA: Double = 0.3
    }
}