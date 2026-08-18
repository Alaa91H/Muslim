package org.muslim.app.feature.zakat.domain

/**
 * A country with its local currency, used to price gold/silver nisab in the
 * user's own money (PROJECT_PROMPT.md §6 Phase 7: زكاة عالمية).
 *
 * @param code ISO 3166-1 alpha-2 country code
 * @param currency ISO 4217 currency code (what the FX provider keys on)
 * @param symbol the currency symbol shown next to amounts
 * @param nameArabic Arabic country name
 * @param nameEnglish English country name
 */
data class CountryCurrency(
    val code: String,
    val currency: String,
    val symbol: String,
    val nameArabic: String,
    val nameEnglish: String,
)

/**
 * Curated global catalog of the currencies users most often hold wealth in.
 * Grouped by region; the ISO currency codes match the FX provider's keys
 * (open.er-api.com, which serves daily ECB/Open Exchange Rates data).
 */
object CountryCurrencies {

    val ALL: List<CountryCurrency> = listOf(
        // الخليج والشرق الأوسط
        CountryCurrency("SA", "SAR", "ر.س", "السعودية", "Saudi Arabia"),
        CountryCurrency("AE", "AED", "د.إ", "الإمارات", "United Arab Emirates"),
        CountryCurrency("KW", "KWD", "د.ك", "الكويت", "Kuwait"),
        CountryCurrency("QA", "QAR", "ر.ق", "قطر", "Qatar"),
        CountryCurrency("BH", "BHD", "د.ب", "البحرين", "Bahrain"),
        CountryCurrency("OM", "OMR", "ر.ع", "عُمان", "Oman"),
        CountryCurrency("YE", "YER", "ر.ي", "اليمن", "Yemen"),
        CountryCurrency("IQ", "IQD", "د.ع", "العراق", "Iraq"),
        CountryCurrency("JO", "JOD", "د.أ", "الأردن", "Jordan"),
        CountryCurrency("PS", "ILS", "₪", "فلسطين", "Palestine"),
        CountryCurrency("LB", "LBP", "ل.ل", "لبنان", "Lebanon"),
        CountryCurrency("SY", "SYP", "ل.س", "سوريا", "Syria"),
        // شمال أفريقيا
        CountryCurrency("EG", "EGP", "ج.م", "مصر", "Egypt"),
        CountryCurrency("LY", "LYD", "د.ل", "ليبيا", "Libya"),
        CountryCurrency("TN", "TND", "د.ت", "تونس", "Tunisia"),
        CountryCurrency("DZ", "DZD", "د.ج", "الجزائر", "Algeria"),
        CountryCurrency("MA", "MAD", "د.م", "المغرب", "Morocco"),
        CountryCurrency("SD", "SDG", "ج.س", "السودان", "Sudan"),
        // تركيا وإيران
        CountryCurrency("TR", "TRY", "₺", "تركيا", "Türkiye"),
        CountryCurrency("IR", "IRR", "﷼", "إيران", "Iran"),
        // جنوب آسيا
        CountryCurrency("PK", "PKR", "ر.ب", "باكستان", "Pakistan"),
        CountryCurrency("IN", "INR", "₹", "الهند", "India"),
        CountryCurrency("BD", "BDT", "৳", "بنغلاديش", "Bangladesh"),
        // شرق وجنوب شرق آسيا
        CountryCurrency("ID", "IDR", "ر.إ", "إندونيسيا", "Indonesia"),
        CountryCurrency("MY", "MYR", "ر.م", "ماليزيا", "Malaysia"),
        CountryCurrency("SG", "SGD", "S$", "سنغافورة", "Singapore"),
        CountryCurrency("CN", "CNY", "¥", "الصين", "China"),
        CountryCurrency("JP", "JPY", "¥", "اليابان", "Japan"),
        // أوروبا
        CountryCurrency("GB", "GBP", "£", "بريطانيا", "United Kingdom"),
        CountryCurrency("DE", "EUR", "€", "ألمانيا", "Germany"),
        CountryCurrency("FR", "EUR", "€", "فرنسا", "France"),
        CountryCurrency("ES", "EUR", "€", "إسبانيا", "Spain"),
        CountryCurrency("IT", "EUR", "€", "إيطاليا", "Italy"),
        CountryCurrency("NL", "EUR", "€", "هولندا", "Netherlands"),
        CountryCurrency("BE", "EUR", "€", "بلجيكا", "Belgium"),
        CountryCurrency("SE", "SEK", "kr", "السويد", "Sweden"),
        CountryCurrency("NO", "NOK", "kr", "النرويج", "Norway"),
        CountryCurrency("CH", "CHF", "₣", "سويسرا", "Switzerland"),
        CountryCurrency("RU", "RUB", "₽", "روسيا", "Russia"),
        // الأمريكتان
        CountryCurrency("US", "USD", "$", "الولايات المتحدة", "United States"),
        CountryCurrency("CA", "CAD", "C$", "كندا", "Canada"),
        CountryCurrency("MX", "MXN", "Mex$", "المكسيك", "Mexico"),
        CountryCurrency("BR", "BRL", "R$", "البرازيل", "Brazil"),
        // أفريقيا جنوب الصحراء
        CountryCurrency("NG", "NGN", "₦", "نيجيريا", "Nigeria"),
        CountryCurrency("KE", "KES", "KSh", "كينيا", "Kenya"),
        CountryCurrency("ZA", "ZAR", "ر.ج", "جنوب أفريقيا", "South Africa"),
        // أوقيانوسيا
        CountryCurrency("AU", "AUD", "A$", "أستراليا", "Australia"),
    )

    /** Looks up a catalog entry by country code; null for an unknown code. */
    fun byCountry(code: String): CountryCurrency? = ALL.firstOrNull { it.code == code }

    /** Looks up a catalog entry by currency code (first match). */
    fun byCurrency(currency: String): CountryCurrency? = ALL.firstOrNull { it.currency == currency }
}