package org.muslim.app.feature.scholarlibrary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import org.muslim.app.feature.scholarlibrary.domain.ScholarPassage
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySession
import org.muslim.app.feature.scholarlibrary.domain.ScholarStudySessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarStudySessionScreen(
    pathId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScholarLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session = state.selectedStudySession
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pathId) {
        viewModel.loadStudySession(pathId)
    }
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
                title = { Text(stringResource(R.string.scholar_library_study_session)) },
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
            state.loading || state.catalogMetadataLoading -> SessionLoading(padding)
            session == null -> SessionUnavailable(padding)
            else -> StudySessionContent(
                session = session,
                passages = state.selectedSessionPassages,
                bookTitle = state.books.firstOrNull { it.id == session.bookId }?.title.orEmpty(),
                padding = padding,
                onCompletePassage = viewModel::completeNextStudySessionPassage,
                onStartNextSession = viewModel::startNextStudySession,
            )
        }
    }
}

@Composable
private fun SessionLoading(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SessionUnavailable(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.scholar_library_session_unavailable),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun StudySessionContent(
    session: ScholarStudySession,
    passages: List<ScholarPassage>,
    bookTitle: String,
    padding: PaddingValues,
    onCompletePassage: (String) -> Unit,
    onStartNextSession: () -> Unit,
) {
    val completedIds = session.completedPassageIds.toSet()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SessionHeader(session = session, bookTitle = bookTitle)
        }
        items(passages, key = { it.id }) { passage ->
            SessionPassageCard(
                passage = passage,
                completed = passage.id in completedIds,
                isNext = session.nextPassageId == passage.id,
                onComplete = { onCompletePassage(passage.id) },
            )
        }
        if (session.status == ScholarStudySessionStatus.Completed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            stringResource(R.string.scholar_library_session_completed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Button(onClick = onStartNextSession) {
                            Text(stringResource(R.string.scholar_library_start_next_session))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(
    session: ScholarStudySession,
    bookTitle: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                bookTitle.ifBlank { stringResource(R.string.scholar_library_book) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { session.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(
                    R.string.scholar_library_session_progress,
                    session.completedPassageIds.size,
                    session.targetPassageIds.size,
                    session.plannedMinutes,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SessionPassageCard(
    passage: ScholarPassage,
    completed: Boolean,
    isNext: Boolean,
    onComplete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(passage.chapter, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            passage.section?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            passage.page?.takeIf { it.isNotBlank() }?.let {
                Text(
                    stringResource(R.string.scholar_library_page_label, it),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(passage.text, style = MaterialTheme.typography.bodyLarge)
            when {
                completed -> Text(
                    stringResource(R.string.scholar_library_session_passage_completed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                isNext -> Button(onClick = onComplete) {
                    Text(stringResource(R.string.scholar_library_complete_session_passage))
                }
                else -> Text(
                    stringResource(R.string.scholar_library_session_waiting_previous),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
