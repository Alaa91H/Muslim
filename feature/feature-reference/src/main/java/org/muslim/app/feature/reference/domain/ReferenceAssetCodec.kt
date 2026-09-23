package org.muslim.app.feature.reference.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Decoder for versioned, bundled reference-library content packs. */
object ReferenceAssetCodec {

    private const val SUPPORTED_SCHEMA_VERSION = 2

    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = false
    }

    fun decode(rawJson: String): ReferenceBook {
        val pack = json.decodeFromString<ReferencePackDto>(rawJson)
        require(pack.schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported reference schema version: ${pack.schemaVersion}"
        }
        return pack.book.toDomain()
    }
}

@Serializable
private data class ReferencePackDto(
    val schemaVersion: Int,
    val book: ReferenceBookDto,
)

@Serializable
private data class ReferenceBookDto(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val contentRevision: Int = 1,
    val chapters: List<ReferenceChapterDto> = emptyList(),
    val topics: List<ReferenceTopicDto>,
) {
    fun toDomain() = ReferenceBook(
        id = id,
        titleAr = titleAr,
        titleEn = titleEn,
        subtitleAr = subtitleAr,
        subtitleEn = subtitleEn,
        topics = topics.map(ReferenceTopicDto::toDomain),
        chapters = chapters.map(ReferenceChapterDto::toDomain),
        contentRevision = contentRevision,
    )
}

@Serializable
private data class ReferenceChapterDto(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val topicIds: List<String>,
    val summaryAr: String = "",
    val summaryEn: String = "",
) {
    fun toDomain() = RefChapter(
        id = id,
        titleAr = titleAr,
        titleEn = titleEn,
        topicIds = topicIds,
        summaryAr = summaryAr,
        summaryEn = summaryEn,
    )
}

@Serializable
private data class ReferenceTopicDto(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val summaryAr: String,
    val summaryEn: String,
    val sections: List<ReferenceSectionDto>,
    val keywordsAr: List<String> = emptyList(),
    val keywordsEn: List<String> = emptyList(),
    val relatedTopicIds: List<String> = emptyList(),
    val citations: List<ReferenceCitationDto> = emptyList(),
    val reviewStatus: String = "NeedsReview",
    val lastReviewed: String? = null,
) {
    fun toDomain() = RefTopic(
        id = id,
        titleAr = titleAr,
        titleEn = titleEn,
        summaryAr = summaryAr,
        summaryEn = summaryEn,
        sections = sections.map(ReferenceSectionDto::toDomain),
        keywordsAr = keywordsAr,
        keywordsEn = keywordsEn,
        relatedTopicIds = relatedTopicIds,
        citations = citations.map(ReferenceCitationDto::toDomain),
        reviewStatus = ReferenceReviewStatus.valueOf(reviewStatus),
        lastReviewed = lastReviewed,
    )
}

@Serializable
private data class ReferenceSectionDto(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val paragraphs: List<ReferenceParagraphDto>,
    val citationIds: List<String> = emptyList(),
) {
    fun toDomain() = RefSection(
        id = id,
        titleAr = titleAr,
        titleEn = titleEn,
        paragraphs = paragraphs.map(ReferenceParagraphDto::toDomain),
        citationIds = citationIds,
    )
}

@Serializable
private data class ReferenceParagraphDto(
    val ar: String,
    val en: String,
    val citationIds: List<String> = emptyList(),
) {
    fun toDomain() = RefParagraph(
        ar = ar,
        en = en,
        citationIds = citationIds,
    )
}

@Serializable
private data class ReferenceCitationDto(
    val id: String,
    val kind: String,
    val titleAr: String,
    val titleEn: String,
    val locator: String,
    val url: String? = null,
    val noteAr: String? = null,
    val noteEn: String? = null,
) {
    fun toDomain() = ReferenceCitation(
        id = id,
        kind = ReferenceSourceKind.valueOf(kind),
        titleAr = titleAr,
        titleEn = titleEn,
        locator = locator,
        url = url,
        noteAr = noteAr,
        noteEn = noteEn,
    )
}
