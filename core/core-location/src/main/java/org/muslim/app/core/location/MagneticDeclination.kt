package org.muslim.app.core.location

import android.content.Context
import android.hardware.GeomagneticField
import java.util.Date

/**
 * Magnetic declination for a location/date, in degrees (east positive).
 *
 * The compass sensor reports magnetic north; [GeomagneticField] lets us
 * correct to true north so the Qibla needle points at the correct heading
 * (PROJECT_PROMPT.md §6 Phase 1: "تصحيح الانحراف المغناطيسي تلقائيًا").
 */
object MagneticDeclination {

    /**
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param date      the date the correction applies to
     * @return declination in degrees (east positive); 0 when unavailable
     */
    fun declinationDegrees(context: Context, latitude: Double, longitude: Double, date: Date = Date()): Float =
        try {
            GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, date.time).declination
        } catch (e: Exception) {
            0f
        }
}
