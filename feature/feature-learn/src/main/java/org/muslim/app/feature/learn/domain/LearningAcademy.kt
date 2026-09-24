package org.muslim.app.feature.learn.domain

/**
 * Scalable content model for the Learn academy.
 *
 * The original LearnContent model is intentionally kept intact while the app
 * migrates. Existing topics are adapted into this hierarchy so favourites,
 * navigation ids and released content remain stable during the transition.
 *
 * Religious content must not be marked [LearningReviewStatus.REVIEWED] until
 * its references and wording have completed the project's scholarly review.
 */
enum class LearningReviewStatus {
    DRAFT,
    NEEDS_SCHOLAR_REVIEW,
    REVIEWED,
}

enum class LearningReferenceKind {
    QURAN,
    HADITH,
    FIQH,
    BOOK,
    OTHER,
}

data class LearningReference(
    val id: String,
    val kind: LearningReferenceKind,
    val citation: String,
    val locator: String? = null,
    val note: String? = null,
)

enum class LearningCalloutTone {
    INFO,
    IMPORTANT,
    WARNING,
    DIFFERENCE_OF_OPINION,
}

data class LearningStepItem(
    val title: String,
    val body: String,
    val supplementalText: String? = null,
)

data class LearningComparisonItem(
    val label: String,
    val body: String,
    val referenceIds: List<String> = emptyList(),
)

data class LearningQuizOption(
    val id: String,
    val text: String,
)

sealed interface LearningContentBlock {
    data class Paragraph(
        val text: String,
    ) : LearningContentBlock

    data class Steps(
        val items: List<LearningStepItem>,
    ) : LearningContentBlock

    data class Callout(
        val body: String,
        val title: String? = null,
        val tone: LearningCalloutTone = LearningCalloutTone.INFO,
    ) : LearningContentBlock

    data class Evidence(
        val text: String,
        val referenceIds: List<String>,
        val heading: String? = null,
    ) : LearningContentBlock

    data class Comparison(
        val items: List<LearningComparisonItem>,
        val intro: String? = null,
    ) : LearningContentBlock

    data class QuestionAnswer(
        val question: String,
        val answer: String,
        val referenceIds: List<String> = emptyList(),
    ) : LearningContentBlock

    data class Quiz(
        val question: String,
        val options: List<LearningQuizOption>,
        val correctOptionId: String,
        val explanation: String? = null,
        val referenceIds: List<String> = emptyList(),
    ) : LearningContentBlock
}

data class LearningSection(
    val id: String,
    val title: String,
    val blocks: List<LearningContentBlock>,
)

data class LearningLesson(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val sections: List<LearningSection>,
    val references: List<LearningReference> = emptyList(),
    val estimatedMinutes: Int? = null,
    val contentVersion: Int = 1,
    val reviewStatus: LearningReviewStatus = LearningReviewStatus.NEEDS_SCHOLAR_REVIEW,
)

data class LearningModule(
    val id: String,
    val lessonIds: List<String>,
)

data class LearningCourse(
    val id: String,
    val moduleIds: List<String>,
)

data class LearningPath(
    val id: String,
    val courseIds: List<String>,
)

/**
 * Compatibility catalog used while legacy LearnTopic content is migrated into
 * complete courses lesson-by-lesson.
 */
object LearningAcademyCatalog {

    private const val MODULE_SUFFIX = "_core"

    val lessons: List<LearningLesson> = LearnContent.topics.map(::fromLegacyTopic)

    val modules: List<LearningModule> = LearnContent.categoryOrder.map { category ->
        LearningModule(
            id = category + MODULE_SUFFIX,
            lessonIds = LearnContent.topics
                .filter { it.category == category }
                .map { it.id },
        )
    }

    val courses: List<LearningCourse> = LearnContent.categoryOrder.map { category ->
        LearningCourse(
            id = category,
            moduleIds = listOf(category + MODULE_SUFFIX),
        )
    }

    val paths: List<LearningPath> = listOf(
        LearningPath(
            id = "foundations",
            courseIds = listOf(
                LearnContent.CATEGORY_FAITH,
                LearnContent.CATEGORY_TAHARA,
                LearnContent.CATEGORY_SALAH,
            ),
        ),
        LearningPath(
            id = "worship",
            courseIds = listOf(LearnContent.CATEGORY_IBADAH),
        ),
        LearningPath(
            id = "reference",
            courseIds = listOf(LearnContent.CATEGORY_REFERENCE),
        ),
    )

    private val lessonsById = lessons.associateBy { it.id }

    fun lessonFor(topic: LearnTopic): LearningLesson =
        requireNotNull(lessonsById[topic.id]) {
            "No academy lesson registered for legacy topic '${topic.id}'."
        }

    private fun fromLegacyTopic(topic: LearnTopic): LearningLesson {
        val sections = buildList {
            add(
                LearningSection(
                    id = "quick_summary",
                    title = "الملخص السريع",
                    blocks = listOf(
                        LearningContentBlock.Steps(
                            items = topic.steps.map { step ->
                                LearningStepItem(
                                    title = step.title,
                                    body = step.description,
                                    supplementalText = step.dua,
                                )
                            },
                        ),
                    ),
                ),
            )
            topic.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                add(
                    LearningSection(
                        id = "important_notes",
                        title = "ملاحظات مهمة",
                        blocks = listOf(
                            LearningContentBlock.Callout(
                                body = notes,
                                tone = LearningCalloutTone.IMPORTANT,
                            ),
                        ),
                    ),
                )
            }
        }

        return LearningLesson(
            id = topic.id,
            titleRes = topic.titleRes,
            subtitleRes = topic.subtitleRes,
            sections = sections,
        )
    }
}
