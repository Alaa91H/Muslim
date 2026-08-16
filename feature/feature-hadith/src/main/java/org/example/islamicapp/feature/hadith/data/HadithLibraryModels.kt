package org.example.islamicapp.feature.hadith.data

import kotlinx.serialization.Serializable

/** Wire format of the open hadith-api JSON editions (fawazahmed0/hadith-api). */
@Serializable
data class BookFileDto(
    val metadata: MetadataDto = MetadataDto(),
    val hadiths: List<HadithDto> = emptyList(),
)

@Serializable
data class MetadataDto(
    val name: String = "",
    val sections: Map<String, String> = emptyMap(),
)

@Serializable
data class HadithDto(
    val hadithnumber: Int = 0,
    val arabicnumber: Int? = null,
    val text: String = "",
    val grades: List<GradeDto> = emptyList(),
    val reference: ReferenceDto? = null,
)

@Serializable
data class ReferenceDto(
    /** Section (kitab) number the hadith belongs to. */
    val book: Int? = null,
    val hadith: Int? = null,
)

@Serializable
data class GradeDto(
    val grade: String? = null,
    /** Grading authority name, e.g. "Al-Albani". */
    val attr: String? = null,
    val grader: String? = null,
)
