package org.muslim.app.core.common.prayer

/**
 * Juristic method used to calculate the Asr prayer time.
 *
 * - [Standard] (الجمهور): shadow length factor 1 — used by Shafi'i, Maliki and Hanbali schools.
 * - [Hanafi] (الحنفي): shadow length factor 2.
 */
enum class AsrMethod(val shadowLength: Double) {
    Standard(1.0),
    Hanafi(2.0),
}
