package org.muslim.app.feature.zakat.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MetalsPriceCalculatorTest {

    @Test
    fun `perGram divides the troy ounce price by the troy ounce weight`() {
        assertThat(MetalsPriceCalculator.perGram(MetalsPriceCalculator.GRAMS_PER_TROY_OUNCE))
            .isWithin(1e-9)
            .of(1.0)
    }

    @Test
    fun `usdToLocal multiplies the usd price by the fx rate`() {
        assertThat(MetalsPriceCalculator.usdToLocal(100.0, 3.75)).isEqualTo(375.0)
    }

    @Test
    fun `parseMetalQuotePerGram reads the spot price and converts to grams`() {
        // 3110.34768 USD per troy ounce == 100 USD per gram.
        val perGram = MetalsPriceCalculator.parseMetalQuotePerGram(
            """{"price": 3110.34768, "updatedAt": "2026-08-18T00:00:00Z"}"""
        )
        assertThat(perGram).isNotNull()
        assertThat(perGram!!).isWithin(1e-6).of(100.0)
    }

    @Test
    fun `parseMetalQuotePerGram returns null on malformed input`() {
        assertThat(MetalsPriceCalculator.parseMetalQuotePerGram("not json")).isNull()
        assertThat(MetalsPriceCalculator.parseMetalQuotePerGram("""{"price": -5}""")).isNull()
    }

    @Test
    fun `parseUsdRate reads the requested currency rate`() {
        val rate = MetalsPriceCalculator.parseUsdRate(
            """{"result":"success","rates":{"USD":1.0,"SAR":3.75,"EUR":0.92}}""",
            "SAR",
        )
        assertThat(rate).isEqualTo(3.75)
    }

    @Test
    fun `parseUsdRate returns null when the currency is absent`() {
        val rate = MetalsPriceCalculator.parseUsdRate(
            """{"result":"success","rates":{"USD":1.0,"SAR":3.75}}""",
            "JPY",
        )
        assertThat(rate).isNull()
    }

    @Test
    fun `fxResponseOk requires success and the currency`() {
        val body = """{"result":"success","rates":{"USD":1.0,"SAR":3.75}}"""
        assertThat(MetalsPriceCalculator.fxResponseOk(body, "SAR")).isTrue()
        assertThat(MetalsPriceCalculator.fxResponseOk(body, "JPY")).isFalse()
        assertThat(MetalsPriceCalculator.fxResponseOk("""{"result":"error"}""", "SAR")).isFalse()
    }

    @Test
    fun `parseUpdatedAt extracts the timestamp when present`() {
        assertThat(
            MetalsPriceCalculator.parseUpdatedAt("""{"price": 1.0, "updatedAt": "2026-08-18T07:00:00Z"}""")
        ).isEqualTo("2026-08-18T07:00:00Z")
        assertThat(MetalsPriceCalculator.parseUpdatedAt("""{"price": 1.0}""")).isNull()
    }

    @Test
    fun `country catalog has unique codes and complete entries`() {
        val countries = CountryCurrencies.ALL
        assertThat(countries.map { it.code }.distinct().size).isEqualTo(countries.size)
        countries.forEach { country ->
            assertThat(country.code).isNotEmpty()
            assertThat(country.currency).isNotEmpty()
            assertThat(country.symbol).isNotEmpty()
            assertThat(country.nameArabic).isNotEmpty()
            assertThat(country.nameEnglish).isNotEmpty()
        }
    }

    @Test
    fun `country lookup resolves by code and currency`() {
        assertThat(CountryCurrencies.byCountry("SA")?.currency).isEqualTo("SAR")
        assertThat(CountryCurrencies.byCountry("ZZ")).isNull()
        assertThat(CountryCurrencies.byCurrency("SAR")?.code).isEqualTo("SA")
    }
}
