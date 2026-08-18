package org.muslim.app.feature.zakat.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.muslim.app.feature.zakat.domain.MetalsPriceCalculator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches live precious-metal spot prices and the user's FX rate
 * (PROJECT_PROMPT.md §6 Phase 7: جلب الأسعار تلقائيًا من مصدر موثوق).
 *
 * Offline-first: this is optional and user-triggered — the calculator keeps
 * the last known prices and always accepts manual entry when the fetch fails.
 */
@Singleton
class MetalsPriceRepository @Inject constructor(
    private val client: OkHttpClient,
) {

    sealed interface Result {
        data class Success(val metals: MetalsPriceCalculator.LocalMetals) : Result
        data object Failure : Result
    }

    /**
     * Fetches gold + silver (per troy ounce, USD) and the FX rate for
     * [currency], returning per-gram local-currency prices.
     */
    suspend fun fetchPrices(currency: String): Result = withContext(Dispatchers.IO) {
        runCatching {
            val goldBody = httpGet("https://api.gold-api.com/price/XAU")
            val silverBody = httpGet("https://api.gold-api.com/price/XAG")
            val fxBody = httpGet("https://open.er-api.com/v6/latest/USD")

            val goldPerGramUsd = MetalsPriceCalculator.parseMetalQuotePerGram(goldBody)
                ?: return@runCatching Result.Failure
            val silverPerGramUsd = MetalsPriceCalculator.parseMetalQuotePerGram(silverBody)
                ?: return@runCatching Result.Failure
            val usdRate = MetalsPriceCalculator.parseUsdRate(fxBody, currency)
                ?: return@runCatching Result.Failure

            Result.Success(
                MetalsPriceCalculator.LocalMetals(
                    goldPerGram = MetalsPriceCalculator.usdToLocal(goldPerGramUsd, usdRate),
                    silverPerGram = MetalsPriceCalculator.usdToLocal(silverPerGramUsd, usdRate),
                    updatedAtUtc = MetalsPriceCalculator.parseUpdatedAt(goldBody),
                    currency = currency,
                )
            )
        }.getOrDefault(Result.Failure)
    }

    private fun httpGet(url: String): String {
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} for $url")
            return response.body?.string() ?: error("Empty body for $url")
        }
    }
}
