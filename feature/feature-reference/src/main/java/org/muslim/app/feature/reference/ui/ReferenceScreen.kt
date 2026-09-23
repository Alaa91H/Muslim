package org.muslim.app.feature.reference.ui

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muslim.app.feature.reference.R
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.muslim.app.feature.reference.data.AndroidReferenceRepositoryFactory
import org.muslim.app.feature.reference.data.ReferenceReaderKeyCodec
import org.muslim.app.feature.reference.data.ReferenceReaderLocation
import org.muslim.app.feature.reference.data.ReferenceReaderPreferences
import org.muslim.app.feature.reference.domain.ReferenceBook
import org.muslim.app.feature.reference.domain.ReferenceCitation
import org.muslim.app.feature.reference.domain.ReferenceRepository
import org.muslim.app.feature.reference.domain.ReferenceReviewStatus
import org.muslim.app.feature.reference.domain.RefLang
import org.muslim.app.feature.reference.domain.RefParagraph
import org.muslim.app.feature.reference.domain.RefSection
import org.muslim.app.feature.reference.domain.RefTopic

private val bookIcons = mapOf(
    "islam" to Icons.Filled.AutoStories,
    "sira" to Icons.Filled.History,
    "prophets" to Icons.Filled.Groups,
    "companions" to Icons.Filled.Groups,
    "mothers" to Icons.Filled.AutoStories,
    "ahl_al_bayt" to Icons.Filled.Groups,
    "rashidun" to Icons.Filled.History,
    "aqeedah" to Icons.Filled.AutoStories,
    "quran_sciences" to Icons.Filled.AutoStories,
    "hadith_sciences" to Icons.Filled.History,
    "fiqh_worship" to Icons.Filled.AutoStories,
    "ethics_life" to Icons.Filled.Groups,
    "history_civilization" to Icons.Filled.History,
    "places_landmarks" to Icons.Filled.Place,
    "islamic_glossary" to Icons.AutoMirrored.Filled.MenuBook,
    "faq_misconceptions" to Icons.Filled.Search,
)


private data class ReferenceReaderUiState(
    val preferences: ReferenceReaderPreferences,
    val bookmarkKeys: Set<String>,
    val fontStep: Int,
    val onBookmarkKeysChanged: (Set<String>) -> Unit,
    val onFontStepChanged: (Int) -> Unit,
    val onLastReadChanged: (ReferenceReaderLocation) -> Unit,
)

/**
 * المرجعية الإسلامية (feature-reference): مكتبة مرجعية شاملة ومفهرسة تعرض
 * كتب مترابطة تشمل التعريف بالإسلام والسيرة والأنبياء والصحابة وأمهات
 * المؤمنين وأهل البيت والخلفاء الراشدين والعقيدة وعلوم القرآن والحديث وفقه العبادات
 * والأخلاق والآداب وحياة المسلم والتاريخ والحضارة الإسلامية والأماكن والمعالم
 * والمعجم الإسلامي الموسع والأسئلة الشائعة والمفاهيم الخاطئة،
 * مع بحث نصي ثنائي اللغة على مستوى المكتبة كلها.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appContext = LocalContext.current.applicationContext
    val repository = rememberReferenceRepository()
    val readerPreferences = remember(appContext) { ReferenceReaderPreferences(appContext) }
    var lang by remember { mutableStateOf(RefLang.Arabic) }
    var selectedBook by remember { mutableStateOf<ReferenceBook?>(null) }
    var selectedTopic by remember { mutableStateOf<RefTopic?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var hubQuery by rememberSaveable { mutableStateOf("") }
    var bookmarkKeys by remember { mutableStateOf(readerPreferences.bookmarkKeys()) }
    var lastRead by remember { mutableStateOf(readerPreferences.lastRead()) }
    var fontStep by remember { mutableStateOf(readerPreferences.fontStep()) }

    fun openTopic(targetBook: ReferenceBook, targetTopic: RefTopic) {
        val location = ReferenceReaderLocation(targetBook.id, targetTopic.id)
        readerPreferences.saveLastRead(location)
        lastRead = location
        selectedBook = targetBook
        selectedTopic = targetTopic
    }

    ReferenceBackHandler(selectedTopic, selectedBook, { selectedTopic = null }) {
        selectedBook = null
        query = ""
    }

    val book = selectedBook
    val readerState = ReferenceReaderUiState(
        preferences = readerPreferences,
        bookmarkKeys = bookmarkKeys,
        fontStep = fontStep,
        onBookmarkKeysChanged = { bookmarkKeys = it },
        onFontStepChanged = { fontStep = readerPreferences.setFontStep(it) },
        onLastReadChanged = { lastRead = it },
    )

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ReferenceTopBar(
                lang = lang,
                book = book,
                topic = selectedTopic,
                onBack = {
                    when {
                        selectedTopic != null -> selectedTopic = null
                        book != null -> {
                            selectedBook = null
                            query = ""
                        }
                        else -> onBack()
                    }
                },
                onToggleLanguage = { lang = lang.toggled() },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            selectedTopic != null && book != null -> TopicContent(
                repository = repository,
                book = book,
                topic = selectedTopic!!,
                lang = lang,
                readerState = readerState,
                onOpenTopic = { targetBook, targetTopic ->
                    openTopic(targetBook, targetTopic)
                    query = ""
                },
                modifier = contentModifier,
            )
            book != null -> BookContent(
                repository = repository,
                book = book,
                lang = lang,
                query = query,
                onQueryChanged = { query = it },
                bookmarkKeys = readerState.bookmarkKeys,
                onOpenTopic = { openTopic(book, it) },
                modifier = contentModifier,
            )
            else -> HubContent(
                repository = repository,
                lang = lang,
                query = hubQuery,
                onQueryChanged = { hubQuery = it },
                bookmarkKeys = readerState.bookmarkKeys,
                lastRead = lastRead,
                onOpenBook = {
                    selectedBook = it
                    hubQuery = ""
                },
                onOpenTopic = { targetBook, targetTopic ->
                    openTopic(targetBook, targetTopic)
                    hubQuery = ""
                },
                modifier = contentModifier,
            )
        }
    }
}

private fun RefLang.toggled(): RefLang =
    if (this == RefLang.Arabic) RefLang.English else RefLang.Arabic

@Composable
private fun rememberReferenceRepository(): ReferenceRepository {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) { AndroidReferenceRepositoryFactory.create(appContext) }
}

@Composable
private fun ReferenceBackHandler(
    selectedTopic: RefTopic?,
    selectedBook: ReferenceBook?,
    onClearTopic: () -> Unit,
    onClearBook: () -> Unit,
) {
    BackHandler(enabled = selectedTopic != null || selectedBook != null) {
        if (selectedTopic != null) {
            onClearTopic()
        } else if (selectedBook != null) {
            onClearBook()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceTopBar(
    lang: RefLang,
    book: ReferenceBook?,
    topic: RefTopic?,
    onBack: () -> Unit,
    onToggleLanguage: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = when {
                    topic != null -> topic.title(lang)
                    book != null -> book.title(lang)
                    else -> stringResource(R.string.reference_title)
                },
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.reference_back),
                )
            }
        },
        actions = {
            TextButton(onClick = onToggleLanguage) {
                Text(
                    text = if (lang == RefLang.Arabic) "English" else "العربية",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun HubContent(
    repository: ReferenceRepository,
    lang: RefLang,
    query: String,
    onQueryChanged: (String) -> Unit,
    bookmarkKeys: Set<String>,
    lastRead: ReferenceReaderLocation?,
    onOpenBook: (ReferenceBook) -> Unit,
    onOpenTopic: (ReferenceBook, RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val results = remember(repository, query, lang) {
        repository.searchAll(query, lang, limit = 80)
    }
    val bookmarkedTopics = remember(repository, bookmarkKeys) {
        bookmarkKeys.mapNotNull { key -> resolveStoredTopic(repository, key) }
    }
    val lastReadTarget = remember(repository, lastRead) {
        lastRead?.let { resolveStoredTopic(repository, "${it.bookId}/${it.topicId}") }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "library-search") {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        if (lang == RefLang.Arabic) {
                            "ابحث في جميع كتب المكتبة…"
                        } else {
                            "Search the entire library…"
                        },
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (query.isBlank()) {
            item(key = "reference-decoration") {
                IslamicDecorationBand(
                    tint = MaterialTheme.colorScheme.tertiary,
                    compact = true,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            lastReadTarget?.let { (lastBook, lastTopic) ->
                item(key = "continue-reading") {
                    IslamicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onOpenTopic(lastBook, lastTopic) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column {
                            Text(
                                text = if (lang == RefLang.Arabic) "متابعة القراءة" else "Continue reading",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = lastTopic.title(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = lastBook.title(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
            if (bookmarkedTopics.isNotEmpty()) {
                item(key = "bookmarks-title") {
                    SectionLabel(
                        text = if (lang == RefLang.Arabic) "المفضلة" else "Bookmarks",
                    )
                }
                items(
                    items = bookmarkedTopics,
                    key = { (savedBook, savedTopic) -> "bookmark-${savedBook.id}/${savedTopic.id}" },
                ) { (savedBook, savedTopic) ->
                    ListItem(
                        headlineContent = {
                            Text(savedTopic.title(lang), fontWeight = FontWeight.Medium)
                        },
                        supportingContent = {
                            Text(savedBook.title(lang), maxLines = 1)
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Bookmark, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTopic(savedBook, savedTopic) },
                    )
                }
                item(key = "library-books-title") {
                    SectionLabel(
                        text = if (lang == RefLang.Arabic) "الكتب" else "Books",
                    )
                }
            }
            items(repository.books, key = { it.id }) { book ->
                ReferenceBookCard(book = book, lang = lang, onOpenBook = onOpenBook)
            }
        } else if (results.isEmpty()) {
            item(key = "library-no-results") {
                MuslimStateSurface(
                    title = stringResource(R.string.reference_no_results),
                    tone = MuslimStateTone.Neutral,
                    icon = Icons.Filled.Search,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
        } else {
            items(
                items = results,
                key = { result -> "${result.book.id}/${result.topic.id}" },
            ) { result ->
                ListItem(
                    headlineContent = {
                        Text(result.topic.title(lang), fontWeight = FontWeight.Medium)
                    },
                    supportingContent = {
                        Text(
                            text = "${result.book.title(lang)} • ${result.topic.summary(lang)}",
                            maxLines = 2,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(result.book, result.topic) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
private fun ReferenceBookCard(
    book: ReferenceBook,
    lang: RefLang,
    onOpenBook: (ReferenceBook) -> Unit,
) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onOpenBook(book) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = bookIcons[book.id] ?: Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp).size(24.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = book.title(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = book.subtitle(lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${book.topics.size} ${stringResource(R.string.reference_topics_count)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BookContent(
    repository: ReferenceRepository,
    book: ReferenceBook,
    lang: RefLang,
    query: String,
    onQueryChanged: (String) -> Unit,
    bookmarkKeys: Set<String>,
    onOpenTopic: (RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val results = repository.search(book, query, lang)
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text(stringResource(R.string.reference_search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        IslamicDecorationDivider(
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        if (results.isEmpty()) {
            MuslimStateSurface(
                title = stringResource(R.string.reference_no_results),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Search,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (query.isBlank() && book.chapters.isNotEmpty()) {
                    book.chapters.forEach { chapter ->
                        item(key = "chapter-header-${chapter.id}") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = chapter.title(lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                val summary = chapter.summary(lang)
                                if (summary.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        val chapterTopics = chapter.topicIds.mapNotNull { topicId ->
                            book.topics.firstOrNull { it.id == topicId }
                        }
                        items(
                            items = chapterTopics,
                            key = { topic -> "chapter-${chapter.id}-${topic.id}" },
                        ) { topic ->
                            TopicListItem(
                                topic = topic,
                                lang = lang,
                                bookmarked = ReferenceReaderKeyCodec.topicKey(book.id, topic.id) in bookmarkKeys,
                                onOpenTopic = onOpenTopic,
                            )
                        }
                    }
                } else {
                    items(results, key = { it.id }) { topic ->
                        TopicListItem(
                            topic = topic,
                            lang = lang,
                            bookmarked = ReferenceReaderKeyCodec.topicKey(book.id, topic.id) in bookmarkKeys,
                            onOpenTopic = onOpenTopic,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicListItem(
    topic: RefTopic,
    lang: RefLang,
    bookmarked: Boolean,
    onOpenTopic: (RefTopic) -> Unit,
) {
    ListItem(
        headlineContent = { Text(topic.title(lang), fontWeight = FontWeight.Medium) },
        supportingContent = { Text(topic.summary(lang), maxLines = 2) },
        trailingContent = {
            if (bookmarked) {
                Icon(Icons.Filled.Bookmark, contentDescription = null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTopic(topic) },
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun TopicContent(
    repository: ReferenceRepository,
    book: ReferenceBook,
    topic: RefTopic,
    lang: RefLang,
    readerState: ReferenceReaderUiState,
    onOpenTopic: (ReferenceBook, RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeInitialIndex = remember(book.id, topic.id) {
        readerState.preferences
            .savedScrollIndex(book.id, topic.id)
            .coerceAtMost(topic.sections.size + topic.relatedTopicIds.size + 8)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeInitialIndex)
    val scope = rememberCoroutineScope()
    val related = remember(repository, book.id, topic.id) {
        resolveRelatedTopics(repository, book, topic)
    }
    val topicIndex = remember(book.id, topic.id) {
        book.topics.indexOfFirst { it.id == topic.id }
    }
    val previousTopic = book.topics.getOrNull(topicIndex - 1)
    val nextTopic = book.topics.getOrNull(topicIndex + 1)
    val bookmarkKey = ReferenceReaderKeyCodec.topicKey(book.id, topic.id)
    val bookmarked = bookmarkKey in readerState.bookmarkKeys

    LaunchedEffect(listState, book.id, topic.id) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { scrollIndex ->
                readerState.preferences.saveScrollIndex(book.id, topic.id, scrollIndex)
                readerState.onLastReadChanged(
                    ReferenceReaderLocation(
                        bookId = book.id,
                        topicId = topic.id,
                        scrollIndex = scrollIndex,
                    ),
                )
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
    ) {
        item(key = "reader-controls") {
            ReaderControls(
                lang = lang,
                bookmarked = bookmarked,
                fontStep = readerState.fontStep,
                onToggleBookmark = {
                    readerState.onBookmarkKeysChanged(
                        readerState.preferences.setBookmarked(
                            bookId = book.id,
                            topicId = topic.id,
                            bookmarked = !bookmarked,
                        ),
                    )
                },
                onDecreaseFont = { readerState.onFontStepChanged(readerState.fontStep - 1) },
                onIncreaseFont = { readerState.onFontStepChanged(readerState.fontStep + 1) },
            )
        }
        item(key = "topic-header") {
            TopicHeader(topic = topic, lang = lang, fontStep = readerState.fontStep)
        }
        item(key = "topic-toc") {
            TopicTableOfContents(
                topic = topic,
                lang = lang,
                onJumpToSection = { sectionIndex ->
                    scope.launch {
                        listState.animateScrollToItem(3 + sectionIndex)
                    }
                },
            )
        }
        items(topic.sections, key = { it.id }) { section ->
            TopicSectionContent(
                topic = topic,
                section = section,
                lang = lang,
                fontStep = readerState.fontStep,
            )
        }
        if (topic.citations.isNotEmpty()) {
            item(key = "topic-sources") {
                TopicSources(topic = topic, lang = lang)
            }
        }
        if (related.isNotEmpty()) {
            item(key = "related-title") {
                SectionLabel(
                    text = if (lang == RefLang.Arabic) "موضوعات ذات صلة" else "Related topics",
                )
            }
            items(
                items = related,
                key = { (relatedBook, relatedTopic) -> "${relatedBook.id}/${relatedTopic.id}" },
            ) { (relatedBook, relatedTopic) ->
                ListItem(
                    headlineContent = {
                        Text(relatedTopic.title(lang), fontWeight = FontWeight.Medium)
                    },
                    supportingContent = {
                        Text(relatedTopic.summary(lang), maxLines = 2)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTopic(relatedBook, relatedTopic) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
        item(key = "reader-navigation") {
            TopicNavigation(
                lang = lang,
                previousTopic = previousTopic,
                nextTopic = nextTopic,
                onPrevious = { previousTopic?.let { onOpenTopic(book, it) } },
                onNext = { nextTopic?.let { onOpenTopic(book, it) } },
            )
        }
    }
}

@Composable
private fun ReaderControls(
    lang: RefLang,
    bookmarked: Boolean,
    fontStep: Int,
    onToggleBookmark: () -> Unit,
    onDecreaseFont: () -> Unit,
    onIncreaseFont: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    if (bookmarked) {
                        if (lang == RefLang.Arabic) "محفوظ" else "Saved"
                    } else {
                        if (lang == RefLang.Arabic) "حفظ" else "Save"
                    },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    enabled = fontStep > 0,
                    onClick = onDecreaseFont,
                ) {
                    Text("A−")
                }
                Text(
                    text = if (lang == RefLang.Arabic) "حجم النص" else "Text size",
                    style = MaterialTheme.typography.labelMedium,
                )
                TextButton(
                    enabled = fontStep < 3,
                    onClick = onIncreaseFont,
                ) {
                    Text("A+")
                }
            }
        }
    }
}

@Composable
private fun TopicTableOfContents(
    topic: RefTopic,
    lang: RefLang,
    onJumpToSection: (Int) -> Unit,
) {
    if (topic.sections.isEmpty()) return

    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Text(
                text = if (lang == RefLang.Arabic) "محتويات المقال" else "Article contents",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            topic.sections.forEachIndexed { index, section ->
                TextButton(onClick = { onJumpToSection(index) }) {
                    Text(
                        text = "${index + 1}. ${section.title(lang)}",
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicHeader(
    topic: RefTopic,
    lang: RefLang,
    fontStep: Int,
) {
    val context = LocalContext.current
    val shareText = remember(topic, lang) { buildTopicShareText(topic, lang) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = topic.summary(lang),
            fontSize = readerSummaryFontSize(fontStep),
            lineHeight = readerSummaryLineHeight(fontStep),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        if (topic.reviewStatus != ReferenceReviewStatus.Reviewed) {
            Text(
                text = if (lang == RefLang.Arabic) {
                    "المحتوى قيد المراجعة العلمية"
                } else {
                    "Content pending scholarly review"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                clipboard?.setPrimaryClip(ClipData.newPlainText(topic.title(lang), shareText))
            }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(4.dp))
                Text(stringResource(R.string.reference_copy))
            }
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                runCatching { context.startActivity(Intent.createChooser(intent, null)) }
            }) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(4.dp))
                Text(stringResource(R.string.reference_share))
            }
        }
    }
}

@Composable
private fun TopicSectionContent(
    topic: RefTopic,
    section: RefSection,
    lang: RefLang,
    fontStep: Int,
) {
    SectionLabel(text = section.title(lang))
    section.paragraphs.forEach { paragraph ->
        ParagraphCard(
            paragraph = paragraph,
            citations = paragraph.citationIds.mapNotNull { citationId ->
                topic.citations.firstOrNull { it.id == citationId }
            },
            lang = lang,
            fontStep = fontStep,
        )
    }
    val sectionCitations = section.citationIds.mapNotNull { citationId ->
        topic.citations.firstOrNull { it.id == citationId }
    }
    if (sectionCitations.isNotEmpty()) {
        CitationLine(citations = sectionCitations, lang = lang)
    }
}

@Composable
private fun ParagraphCard(
    paragraph: RefParagraph,
    citations: List<ReferenceCitation>,
    lang: RefLang,
    fontStep: Int,
) {
    IslamicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Text(
                text = paragraph.text(lang),
                fontSize = readerBodyFontSize(fontStep),
                lineHeight = readerBodyLineHeight(fontStep),
            )
            if (citations.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                CitationLine(citations = citations, lang = lang)
            }
        }
    }
}

@Composable
private fun CitationLine(
    citations: List<ReferenceCitation>,
    lang: RefLang,
) {
    Text(
        text = citations.joinToString(" • ") { citation ->
            "${citation.title(lang)} — ${citation.locator}"
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun TopicSources(
    topic: RefTopic,
    lang: RefLang,
) {
    val context = LocalContext.current
    SectionLabel(
        text = if (lang == RefLang.Arabic) "المصادر والمراجع" else "Sources and references",
    )
    topic.citations.forEachIndexed { index, citation ->
        IslamicCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column {
                Text(
                    text = "${index + 1}. ${citation.title(lang)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = citation.locator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                citation.note(lang)?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                citation.url?.takeIf { it.isNotBlank() }?.let { url ->
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        },
                    ) {
                        Text(if (lang == RefLang.Arabic) "فتح المصدر" else "Open source")
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicNavigation(
    lang: RefLang,
    previousTopic: RefTopic?,
    nextTopic: RefTopic?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            enabled = previousTopic != null,
            onClick = onPrevious,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(4.dp))
            Text(if (lang == RefLang.Arabic) "السابق" else "Previous")
        }
        Text(
            text = if (lang == RefLang.Arabic) {
                "التنقل بين موضوعات الكتاب"
            } else {
                "Navigate this book"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            enabled = nextTopic != null,
            onClick = onNext,
        ) {
            Text(if (lang == RefLang.Arabic) "التالي" else "Next")
            Spacer(Modifier.size(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

private fun readerBodyFontSize(step: Int) = when (ReferenceReaderKeyCodec.clampFontStep(step)) {
    0 -> 15.sp
    1 -> 17.sp
    2 -> 19.sp
    else -> 21.sp
}

private fun readerBodyLineHeight(step: Int) = when (ReferenceReaderKeyCodec.clampFontStep(step)) {
    0 -> 24.sp
    1 -> 28.sp
    2 -> 31.sp
    else -> 34.sp
}

private fun readerSummaryFontSize(step: Int) = when (ReferenceReaderKeyCodec.clampFontStep(step)) {
    0 -> 16.sp
    1 -> 18.sp
    2 -> 20.sp
    else -> 22.sp
}

private fun readerSummaryLineHeight(step: Int) = when (ReferenceReaderKeyCodec.clampFontStep(step)) {
    0 -> 24.sp
    1 -> 27.sp
    2 -> 30.sp
    else -> 33.sp
}

private fun buildTopicShareText(topic: RefTopic, lang: RefLang): String = buildString {
    append(topic.title(lang)).append("\n\n")
    append(topic.summary(lang)).append("\n\n")
    topic.sections.forEach { section ->
        append(section.title(lang)).append("\n")
        section.paragraphs.forEach { paragraph ->
            append(paragraph.text(lang)).append("\n")
        }
        append("\n")
    }
    if (topic.citations.isNotEmpty()) {
        append(if (lang == RefLang.Arabic) "المصادر والمراجع" else "Sources and references")
        append("\n")
        topic.citations.forEachIndexed { index, citation ->
            append(index + 1)
                .append(". ")
                .append(citation.title(lang))
                .append(" — ")
                .append(citation.locator)
                .append("\n")
        }
    }
}

private fun resolveStoredTopic(
    repository: ReferenceRepository,
    key: String,
): Pair<ReferenceBook, RefTopic>? {
    val separator = key.indexOf('/')
    if (separator <= 0 || separator == key.lastIndex) return null
    val bookId = key.substring(0, separator)
    val topicId = key.substring(separator + 1)
    val book = repository.byId(bookId) ?: return null
    val topic = book.topics.firstOrNull { it.id == topicId } ?: return null
    return book to topic
}

private fun resolveRelatedTopics(
    repository: ReferenceRepository,
    currentBook: ReferenceBook,
    topic: RefTopic,
): List<Pair<ReferenceBook, RefTopic>> = topic.relatedTopicIds.mapNotNull { relatedId ->
    val (bookId, topicId) = if ('/' in relatedId) {
        relatedId.substringBefore('/') to relatedId.substringAfter('/')
    } else {
        currentBook.id to relatedId
    }
    val relatedBook = repository.byId(bookId) ?: return@mapNotNull null
    val relatedTopic = relatedBook.topics.firstOrNull { it.id == topicId } ?: return@mapNotNull null
    relatedBook to relatedTopic
}
