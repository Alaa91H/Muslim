package org.muslim.app.feature.scholarlibrary.data

import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarBookmark
import org.muslim.app.feature.scholarlibrary.domain.ScholarCategory
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarHighlight
import org.muslim.app.feature.scholarlibrary.domain.ScholarHighlightStyle
import org.muslim.app.feature.scholarlibrary.domain.ScholarNote
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarReadingStatus
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPlan
import org.muslim.app.feature.scholarlibrary.domain.StudyFlashcard

internal fun ScholarBookEntity.toDomain() = ScholarBook(
    id = id,
    title = title,
    author = author,
    category = ScholarCategory.fromId(category),
    authorDeathYearHijri = authorDeathYearHijri,
    description = description,
    sourceName = sourceName,
    sourceUrl = sourceUrl,
    licenseSummary = licenseSummary,
    imported = imported,
    subtitle = subtitle,
    language = language,
    difficulty = ScholarDifficulty.fromId(difficulty),
    publisher = publisher,
    edition = edition,
    editor = editor,
    publicationYear = publicationYear,
    volumeCount = volumeCount,
    keywords = keywords.split(KEYWORD_SEPARATOR).filter { it.isNotBlank() },
)

internal fun ScholarPassageEntity.toDomain() = ScholarPassage(
    id = id,
    bookId = bookId,
    chapter = chapter,
    volume = volume,
    page = page,
    text = text,
    section = section,
    orderIndex = orderIndex,
)

internal fun ScholarNoteEntity.toDomain() = ScholarNote(id, passageId, text, createdAtEpochMillis)

internal fun ScholarFlashcardEntity.toDomain() = StudyFlashcard(
    id = id,
    passageId = passageId,
    front = front,
    back = back,
    reviewCount = reviewCount,
    dueAtEpochMillis = dueAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
)

internal fun ScholarBookmarkEntity.toDomain() = ScholarBookmark(
    passageId = passageId,
    createdAtEpochMillis = createdAtEpochMillis,
)

internal fun ScholarHighlightEntity.toDomain() = ScholarHighlight(
    id = id,
    passageId = passageId,
    quote = quote,
    note = note,
    style = ScholarHighlightStyle.fromId(style),
    createdAtEpochMillis = createdAtEpochMillis,
)

internal fun ScholarReadingProgressEntity.toDomain() = ScholarReadingProgress(
    bookId = bookId,
    lastPassageId = lastPassageId,
    status = ScholarReadingStatus.fromId(status),
    progressPercent = progressPercent.coerceIn(0, 100),
    updatedAtEpochMillis = updatedAtEpochMillis,
)

internal fun ScholarStudyPlanEntity.toDomain() = ScholarStudyPlan(
    id = id,
    pathId = pathId,
    sessionsPerWeek = sessionsPerWeek,
    minutesPerSession = minutesPerSession,
    targetPassagesPerSession = targetPassagesPerSession,
    active = active,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

private const val KEYWORD_SEPARATOR = "\u001F"
