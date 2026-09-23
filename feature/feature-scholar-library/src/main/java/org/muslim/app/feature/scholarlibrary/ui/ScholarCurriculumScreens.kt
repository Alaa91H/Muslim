package org.muslim.app.feature.scholarlibrary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.feature.scholarlibrary.R
import org.muslim.app.feature.scholarlibrary.domain.ScholarAuthorSummary
import org.muslim.app.feature.scholarlibrary.domain.ScholarBook
import org.muslim.app.feature.scholarlibrary.domain.ScholarBookHierarchy
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarPathProgress
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPath
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPlan

internal fun LazyListScope.studyPathItems(
    paths: List<ScholarStudyPath>,
    progressByPath: Map<String, ScholarPathProgress>,
    onOpenPath: (String) -> Unit,
) {
    item {
        Text(
            stringResource(R.string.scholar_library_study_paths),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    items(paths, key = { it.id }) { path ->
        StudyPathSummaryCard(
            path = path,
            progress = progressByPath[path.id],
            onClick = { onOpenPath(path.id) },
        )
    }
}

@Composable
private fun StudyPathSummaryCard(
    path: ScholarStudyPath,
    progress: ScholarPathProgress?,
    onClick: () -> Unit,
) {
    val bookCount = path.stages.sumOf { it.bookIds.size }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.School, contentDescription = null)
                Text(
                    path.title,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(path.summary, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(
                    R.string.scholar_library_path_summary,
                    path.stages.size,
                    bookCount,
                    pathLevelLabel(path.level),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            progress?.let {
                LinearProgressIndicator(
                    progress = { it.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(
                        R.string.scholar_library_path_progress,
                        it.progressPercent,
                        it.completedBooks,
                        it.totalBooks,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarStudyPathScreen(
    pathId: String,
    onBack: () -> Unit,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val path = state.studyPaths.firstOrNull { it.id == pathId }
    val progress = state.pathProgress.firstOrNull { it.pathId == pathId }
    val activePlan = state.studyPlans.firstOrNull { it.pathId == pathId && it.active }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(path?.title ?: stringResource(R.string.scholar_library_study_paths)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scholar_library_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading || state.catalogMetadataLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            path == null -> {
                Text(
                    stringResource(R.string.scholar_library_path_not_found),
                    modifier = Modifier.padding(padding).padding(16.dp),
                )
            }
            else -> {
                StudyPathContent(
                    path = path,
                    books = state.books,
                    progress = progress,
                    activePlan = activePlan,
                    padding = padding,
                    actions = StudyPathActions(
                        onOpenBook = onOpenBook,
                        onDailyPlan = { viewModel.createDailyStudyPlan(path.id) },
                        onWeeklyPlan = { viewModel.createWeeklyStudyPlan(path.id) },
                        onDeletePlan = { id -> viewModel.deleteStudyPlan(id) },
                    ),
                )
            }
        }
    }
}

private data class StudyPathActions(
    val onOpenBook: (String) -> Unit,
    val onDailyPlan: () -> Unit,
    val onWeeklyPlan: () -> Unit,
    val onDeletePlan: (Long) -> Unit,
)

@Composable
private fun StudyPathContent(
    path: ScholarStudyPath,
    books: List<ScholarBook>,
    progress: ScholarPathProgress?,
    activePlan: ScholarStudyPlan?,
    padding: PaddingValues,
    actions: StudyPathActions,
) {
    val booksById = books.associateBy { it.id }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(path.summary, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.scholar_library_path_editorial_notice),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        item {
            PathProgressCard(progress = progress, books = books)
        }
        item {
            StudyPlanCard(
                plan = activePlan,
                onDailyPlan = actions.onDailyPlan,
                onWeeklyPlan = actions.onWeeklyPlan,
                onDeletePlan = actions.onDeletePlan,
            )
        }
        path.stages.forEachIndexed { index, stage ->
            item(key = "stage_${stage.id}") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.scholar_library_path_stage, index + 1, stage.title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(stage.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(stage.bookIds, key = { "path_${stage.id}_$it" }) { bookId ->
                booksById[bookId]?.let { book ->
                    CurriculumBookCard(book = book, onClick = { actions.onOpenBook(book.id) })
                }
            }
        }
    }
}

@Composable
private fun CurriculumBookCard(book: ScholarBook, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(book.author, style = MaterialTheme.typography.bodySmall)
                Text(book.category.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarAuthorsScreen(
    onBack: () -> Unit,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val booksById = state.books.associateBy { it.id }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scholar_library_authors)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scholar_library_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading || state.catalogMetadataLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.authors, key = { it.name }) { author ->
                    AuthorCard(
                        author = author,
                        booksById = booksById,
                        onOpenBook = onOpenBook,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthorCard(
    author: ScholarAuthorSummary,
    booksById: Map<String, ScholarBook>,
    onOpenBook: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(author.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            author.deathYearHijri?.let {
                Text(
                    stringResource(R.string.scholar_library_death_year, it),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                stringResource(R.string.scholar_library_author_book_count, author.bookCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            author.bookIds.mapNotNull(booksById::get).forEach { book ->
                Text(
                    text = "• " + book.title,
                    modifier = Modifier.fillMaxWidth().clickable { onOpenBook(book.id) }.padding(vertical = 3.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun BookHierarchyCard(hierarchy: ScholarBookHierarchy?) {
    val volumes = hierarchy?.volumes.orEmpty()
    if (volumes.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.scholar_library_book_hierarchy),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            volumes.take(HIERARCHY_VOLUME_PREVIEW_LIMIT).forEach { volume ->
                Text(
                    volume.label?.let { stringResource(R.string.scholar_library_volume_label, it) }
                        ?: stringResource(R.string.scholar_library_without_volume),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                volume.chapters.take(HIERARCHY_CHAPTER_PREVIEW_LIMIT).forEach { chapter ->
                    Text(
                        "• " + chapter.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    chapter.sections.take(HIERARCHY_SECTION_PREVIEW_LIMIT).forEach { section ->
                        Text(
                            stringResource(
                                R.string.scholar_library_hierarchy_section,
                                section.title ?: stringResource(R.string.scholar_library_section_unspecified),
                                section.passageIds.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathProgressCard(
    progress: ScholarPathProgress?,
    books: List<ScholarBook>,
) {
    val currentBook = progress?.currentBookId?.let { id -> books.firstOrNull { it.id == id } }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.scholar_library_path_progress_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            val percent = progress?.progressPercent ?: 0
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(
                    R.string.scholar_library_path_progress,
                    percent,
                    progress?.completedBooks ?: 0,
                    progress?.totalBooks ?: 0,
                ),
            )
            currentBook?.let {
                Text(
                    stringResource(R.string.scholar_library_path_next_book, it.title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StudyPlanCard(
    plan: ScholarStudyPlan?,
    onDailyPlan: () -> Unit,
    onWeeklyPlan: () -> Unit,
    onDeletePlan: (Long) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.scholar_library_study_plan),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (plan == null) {
                Text(stringResource(R.string.scholar_library_no_study_plan))
            } else {
                Text(
                    stringResource(
                        R.string.scholar_library_study_plan_summary,
                        plan.sessionsPerWeek,
                        plan.minutesPerSession,
                        plan.targetPassagesPerSession,
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDailyPlan) {
                    Text(stringResource(R.string.scholar_library_daily_plan))
                }
                OutlinedButton(onClick = onWeeklyPlan) {
                    Text(stringResource(R.string.scholar_library_weekly_plan))
                }
            }
            plan?.let {
                OutlinedButton(onClick = { onDeletePlan(it.id) }) {
                    Text(stringResource(R.string.scholar_library_delete_study_plan))
                }
            }
        }
    }
}

@Composable
private fun pathLevelLabel(level: ScholarDifficulty): String = when (level) {
    ScholarDifficulty.Unspecified -> stringResource(R.string.scholar_library_level_unspecified)
    ScholarDifficulty.Foundation -> stringResource(R.string.scholar_library_level_foundation)
    ScholarDifficulty.Intermediate -> stringResource(R.string.scholar_library_level_intermediate)
    ScholarDifficulty.Advanced -> stringResource(R.string.scholar_library_level_advanced)
}

private const val HIERARCHY_VOLUME_PREVIEW_LIMIT = 4
private const val HIERARCHY_CHAPTER_PREVIEW_LIMIT = 6
private const val HIERARCHY_SECTION_PREVIEW_LIMIT = 6
