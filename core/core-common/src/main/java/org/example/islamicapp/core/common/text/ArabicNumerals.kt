package org.example.islamicapp.core.common.text

private const val ARABIC_INDIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

/** Formats [this] using Arabic-Indic numerals (٠١٢٣...), as used in mushafs. */
fun Int.toArabicIndic(): String =
    toString().map { ARABIC_INDIC_DIGITS[it - '0'] }.joinToString("")

/** Formats [this] using Arabic-Indic numerals. */
fun String.toArabicIndic(): String =
    map { if (it in '0'..'9') ARABIC_INDIC_DIGITS[it - '0'] else it }.joinToString("")
