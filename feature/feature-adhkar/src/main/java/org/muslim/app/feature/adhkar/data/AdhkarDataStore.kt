package org.muslim.app.feature.adhkar.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single process-wide DataStore backing all adhkar preferences and counters.
 *
 * DataStore enforces one active instance per backing file. Keeping the delegate
 * in exactly one top-level property prevents AdhkarRepository and
 * AdhkarPrefsRepository from creating competing instances for adhkar_prefs.
 */
internal val Context.adhkarDataStore by preferencesDataStore(name = "adhkar_prefs")
