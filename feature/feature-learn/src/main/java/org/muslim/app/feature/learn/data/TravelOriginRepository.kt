package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.learn.domain.TravelPoint
import javax.inject.Inject
import javax.inject.Singleton

private val Context.travelDataStore by preferencesDataStore(name = "traveler_prefs")

/**
 * Stores only the user-selected departure coordinate on this device. The app
 * never starts background tracking and never uploads this travel reference.
 */
@Singleton
class TravelOriginRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val origin: Flow<TravelPoint?> = context.travelDataStore.data.map { prefs ->
        val latitude = prefs[Keys.ORIGIN_LATITUDE]
        val longitude = prefs[Keys.ORIGIN_LONGITUDE]
        if (latitude == null || longitude == null) null else TravelPoint(latitude, longitude)
    }

    suspend fun save(point: TravelPoint) {
        context.travelDataStore.edit { prefs ->
            prefs[Keys.ORIGIN_LATITUDE] = point.latitude
            prefs[Keys.ORIGIN_LONGITUDE] = point.longitude
        }
    }

    suspend fun clear() {
        context.travelDataStore.edit { prefs ->
            prefs.remove(Keys.ORIGIN_LATITUDE)
            prefs.remove(Keys.ORIGIN_LONGITUDE)
        }
    }

    private object Keys {
        val ORIGIN_LATITUDE = doublePreferencesKey("travel_origin_latitude")
        val ORIGIN_LONGITUDE = doublePreferencesKey("travel_origin_longitude")
    }
}
