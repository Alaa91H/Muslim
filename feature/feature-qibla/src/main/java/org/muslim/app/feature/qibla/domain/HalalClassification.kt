package org.muslim.app.feature.qibla.domain

/** Classification returned by a halal product lookup. */
enum class HalalStatus {
    HALAL,
    HARAM,
    QUESTIONABLE,
    UNKNOWN,
}

data class HalalIngredient(
    val code: String,
    val name: String,
    val status: HalalStatus,
    val reason: String? = null,
)

data class HalalProductResult(
    val barcode: String,
    val productName: String,
    val status: HalalStatus,
    val ingredients: List<HalalIngredient> = emptyList(),
    val source: String? = null,
)

/**
 * Conservative ingredient classifier. It never labels an unknown additive
 * halal; users are directed to verify certification and local scholarly
 * guidance instead.
 */
object HalalClassifier {
    private val haramCodes = setOf("E120", "E441", "E542", "E904", "E920", "E921")
    private val questionableCodes = setOf("E471", "E472", "E473", "E474", "E475", "E476", "E477", "E481", "E482")
    private val halalCodes = setOf("E100", "E101", "E160A", "E162", "E300", "E306", "E307", "E322", "E330", "E440")

    fun classify(code: String, name: String = code): HalalIngredient {
        val normalized = code.trim().uppercase().replace(" ", "")
        return when {
            normalized in haramCodes -> HalalIngredient(normalized, name, HalalStatus.HARAM, "Known animal-derived or otherwise prohibited additive; verify source.")
            normalized in questionableCodes -> HalalIngredient(normalized, name, HalalStatus.QUESTIONABLE, "Source may be plant- or animal-derived; certification is required.")
            normalized in halalCodes -> HalalIngredient(normalized, name, HalalStatus.HALAL)
            normalized.matches(Regex("E\\d{3,4}[A-Z]?")) -> HalalIngredient(normalized, name, HalalStatus.UNKNOWN, "Additive source is not determined from the code alone.")
            else -> HalalIngredient(normalized, name, HalalStatus.UNKNOWN, "Unrecognised ingredient code.")
        }
    }

    fun summarize(barcode: String, productName: String, ingredients: List<HalalIngredient>, source: String? = null): HalalProductResult {
        val status = when {
            ingredients.any { it.status == HalalStatus.HARAM } -> HalalStatus.HARAM
            ingredients.any { it.status == HalalStatus.QUESTIONABLE || it.status == HalalStatus.UNKNOWN } -> HalalStatus.QUESTIONABLE
            ingredients.isNotEmpty() -> HalalStatus.HALAL
            else -> HalalStatus.UNKNOWN
        }
        return HalalProductResult(barcode, productName, status, ingredients, source)
    }
}
