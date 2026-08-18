package org.muslim.app.feature.zakat.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Converts the live metal quotes and FX rates into a per-gram local-currency
 * price, and parses the two provider payloads (kept framework-free so it runs
 * as a plain JVM unit test).
 *
 * Sources (both free, no API key, documented in the repository):
 *  - Gold/silver: gold-api.com — `GET /price/XAU` and `/price/XAG` return the
 *    spot price in USD **per troy ounce**.
 *  - FX: open.er-api.com — `GET /v6/latest/USD` returns `rates` as units of
 *    each currency per 1 USD.
 */
object MetalsPriceCalculator {

    /** Troy ounce in grams (the standard precious-metals weight). */
    const val GRAMS_PER_TROY_OUNCE = 31.1034768

    /** Result of a successful live fetch, converted to local money. */
    data class LocalMetals(
        val goldPerGram: Double,
        val silverPerGram: Double,
        val updatedAtUtc: String?,
        val currency: String,
    )

    /** Converts a per-troy-ounce price into a per-gram price. */
    fun perGram(perOunce: Double): Double = perOunce / GRAMS_PER_TROY_OUNCE

    /** Converts a USD-per-gram price into the local currency using [usdToLocal]. */
    fun usdToLocal(usdPerGram: Double, usdToLocal: Double): Double = usdPerGram * usdToLocal

    /**
     * Parses a gold-api.com quote body (e.g. `{"price": 4393.0, "updatedAt": ...}`)
     * and returns the price per gram. Returns null on malformed input.
     */
    fun parseMetalQuotePerGram(jsonBody: String): Double? {
        return runCatching {
            val root = Json.parseToJsonElement(jsonBody).jsonObject
            val price = root["price"]?.jsonPrimitive?.double ?: return null
            perGram(price)
        }.getOrNull()?.takeIf { it > 0.0 }
    }

    /**
     * Parses an open.er-api.com response and returns the rate of [currency]
     * units per 1 USD. Returns null when the body is malformed or the currency
     * is absent.
     */
    fun parseUsdRate(jsonBody: String, currency: String): Double? {
        return runCatching {
            val root = Json.parseToJsonElement(jsonBody).jsonObject
            val rates = root["rates"]?.jsonObject ?: return null
            rates[currency]?.jsonPrimitive?.double
        }.getOrNull()?.takeIf { it > 0.0 }
    }

    /** True when the FX response reports success and contains the currency. */
    fun fxResponseOk(jsonBody: String, currency: String): Boolean {
        return runCatching {
            val root = Json.parseToJsonElement(jsonBody).jsonObject
            root["result"]?.jsonPrimitive?.content == "success" &&
                root["rates"]?.jsonObject?.containsKey(currency) == true
        }.getOrDefault(false)
    }

    /** Extracts the updated-at timestamp from a metal quote, if present. */
    fun parseUpdatedAt(jsonBody: String): String? = runCatching {
        val root = Json.parseToJsonElement(jsonBody).jsonObject
        root["updatedAt"]?.jsonPrimitive?.content
    }.getOrNull()
}
