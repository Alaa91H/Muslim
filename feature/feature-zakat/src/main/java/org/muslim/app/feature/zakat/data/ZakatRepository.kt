package org.muslim.app.feature.zakat.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.feature.zakat.domain.ZakatInput
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.zakatDataStore by preferencesDataStore(name = "zakat_prefs")

/** One saved calculation, for the history list. */
@Serializable
data class ZakatHistoryEntry(
    val date: String,
    val zakatableAmount: Double,
    val zakatDue: Double,
)

/** Persisted zakat inputs (last used prices + saved history). */
data class ZakatPreferences(
    val lastInput: ZakatInput = ZakatInput(),
    val fitrSaaValue: Double = 25.0,
    val history: List<ZakatHistoryEntry> = emptyList(),
)

/**
 * Persists zakat inputs, the manual gold/silver prices (offline-first per
 * §6 Phase 7 — network updates are optional) and the yearly history.
 */
@Singleton
class ZakatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }

    val preferences: Flow<ZakatPreferences> = context.zakatDataStore.data.map { prefs ->
        ZakatPreferences(
            lastInput = ZakatInput(
                cash = prefs[Keys.CASH] ?: 0.0,
                goldGrams = prefs[Keys.GOLD_GRAMS] ?: 0.0,
                goldPricePerGram = prefs[Keys.GOLD_PRICE] ?: 0.0,
                silverGrams = prefs[Keys.SILVER_GRAMS] ?: 0.0,
                silverPricePerGram = prefs[Keys.SILVER_PRICE] ?: 0.0,
                tradeGoods = prefs[Keys.TRADE] ?: 0.0,
                investments = prefs[Keys.INVESTMENTS] ?: 0.0,
                debtsOwed = prefs[Keys.DEBTS] ?: 0.0,
            ),
            fitrSaaValue = prefs[Keys.FITR_SAA] ?: 25.0,
            history = runCatching {
                json.decodeFromString<List<ZakatHistoryEntry>>(prefs[Keys.HISTORY] ?: "[]")
            }.getOrDefault(emptyList()),
        )
    }

    suspend fun saveInput(input: ZakatInput) {
        context.zakatDataStore.edit { prefs ->
            prefs[Keys.CASH] = input.cash
            prefs[Keys.GOLD_GRAMS] = input.goldGrams
            prefs[Keys.GOLD_PRICE] = input.goldPricePerGram
            prefs[Keys.SILVER_GRAMS] = input.silverGrams
            prefs[Keys.SILVER_PRICE] = input.silverPricePerGram
            prefs[Keys.TRADE] = input.tradeGoods
            prefs[Keys.INVESTMENTS] = input.investments
            prefs[Keys.DEBTS] = input.debtsOwed
        }
    }

    suspend fun saveFitrSaaValue(value: Double) {
        context.zakatDataStore.edit { prefs -> prefs[Keys.FITR_SAA] = value }
    }

    suspend fun addHistoryEntry(zakatableAmount: Double, zakatDue: Double) {
        context.zakatDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<ZakatHistoryEntry>>(prefs[Keys.HISTORY] ?: "[]")
            }.getOrDefault(emptyList())
            val entry = ZakatHistoryEntry(LocalDate.now().toString(), zakatableAmount, zakatDue)
            prefs[Keys.HISTORY] = json.encodeToString(
                kotlinx.serialization.serializer<List<ZakatHistoryEntry>>(),
                (listOf(entry) + current).take(50),
            )
        }
    }

    suspend fun clearHistory() {
        context.zakatDataStore.edit { prefs -> prefs[Keys.HISTORY] = "[]" }
    }

    private object Keys {
        val CASH = doublePreferencesKey("cash")
        val GOLD_GRAMS = doublePreferencesKey("gold_grams")
        val GOLD_PRICE = doublePreferencesKey("gold_price")
        val SILVER_GRAMS = doublePreferencesKey("silver_grams")
        val SILVER_PRICE = doublePreferencesKey("silver_price")
        val TRADE = doublePreferencesKey("trade")
        val INVESTMENTS = doublePreferencesKey("investments")
        val DEBTS = doublePreferencesKey("debts")
        val FITR_SAA = doublePreferencesKey("fitr_saa")
        val HISTORY = stringPreferencesKey("history")
    }
}
