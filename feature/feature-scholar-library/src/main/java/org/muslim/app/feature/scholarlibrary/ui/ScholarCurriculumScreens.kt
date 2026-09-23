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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.muslim.app.feature.scholarlibrary.domain.ScholarBookOutlineSection
import org.muslim.app.feature.scholarlibrary.domain.ScholarDifficulty
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudyPath

internal fun LazyListScope.studyPathItems(
    paths: List<ScholarStudyPath>,
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
        StudyPathSummaryCard(path = path, onClick = { onOpenPath(path.id) })
    }
}

@Composable
private fun StudyPathSummaryCard(path: ScholarStudyPath, onClick: () -> Unit) {
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            state.loading -> {
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
                    padding = padding,
                    onOpenBook = onOpenBook,
                )
            }
        }
    }
}

@Composable
private fun StudyPathContent(
    path: ScholarStudyPath,
    books: List<ScholarBook>,
    padding: PaddingValues,
    onOpenBook: (String) -> Unit,
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
                    CurriculumBookCard(book = book, onClick = { onOpenBook(book.id) })
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
        if (state.loading) {
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
internal fun BookOutlineCard(outline: List<ScholarBookOutlineSection>) {
    if (outline.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.scholar_library_book_outline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            outline.take(OUTLINE_PREVIEW_LIMIT).forEach { section ->
                val prefix = section.volume?.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.scholar_library_volume_label, it) + " — "
                }.orEmpty()
                Text(prefix + section.chapter, style = MaterialTheme.typography.bodyMedium)
            }
            if (outline.size > OUTLINE_PREVIEW_LIMIT) {
                Text(
                    stringResource(R.string.scholar_library_outline_more, outline.size - OUTLINE_PREVIEW_LIMIT),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

private const val OUTLINE_PREVIEW_LIMIT = 12
