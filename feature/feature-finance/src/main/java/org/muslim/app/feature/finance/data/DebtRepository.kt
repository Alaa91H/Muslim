package org.muslim.app.feature.finance.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.muslim.app.feature.finance.domain.DebtEntry
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financeDataStore by preferencesDataStore(name = "islamic_finance_prefs")

/**
 * Local-only ledger for debt records. No financial information is uploaded by
 * this repository; the user controls any sharing outside of the app.
 */
@Singleton
class DebtRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val debts: Flow<List<DebtEntry>> = context.financeDataStore.data.map { prefs ->
        decode(prefs[Keys.DEBTS]).sortedBy { it.dueDate.orEmpty() }
    }

    suspend fun save(entry: DebtEntry) {
        context.financeDataStore.edit { prefs ->
            val updated = decode(prefs[Keys.DEBTS])
                .filterNot { it.id == entry.id }
                .plus(entry)
                .sortedBy { it.dueDate.orEmpty() }
                .take(MAX_DEBTS)
            prefs[Keys.DEBTS] = json.encodeToString(ListSerializer(DebtEntry.serializer()), updated)
        }
    }

    suspend fun delete(id: String) {
        context.financeDataStore.edit { prefs ->
            val updated = decode(prefs[Keys.DEBTS]).filterNot { it.id == id }
            prefs[Keys.DEBTS] = json.encodeToString(ListSerializer(DebtEntry.serializer()), updated)
        }
    }

    private fun decode(raw: String?): List<DebtEntry> = runCatching {
        json.decodeFromString(ListSerializer(DebtEntry.serializer()), raw ?: "[]")
    }.getOrDefault(emptyList())

    private object Keys {
        val DEBTS = stringPreferencesKey("debt_entries")
    }

    private companion object {
        const val MAX_DEBTS = 200
    }
}
