package org.muslim.app.feature.reference.ui

import android.content.ClipData
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.muslim.app.feature.reference.data.AndroidReferenceRepositoryFactory
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
    "mothers" to Icons.Filled.Groups,
    "ahl_al_bayt" to Icons.Filled.Groups,
    "rashidun" to Icons.Filled.History,
)

/**
 * المرجعية الإسلامية (feature-reference): مكتبة مرجعية شاملة ومفهرسة تعرض
 * مجموعة كتب مترابطة مع بحث شامل وبحث داخل كل كتاب وتبديل لغة المحتوى
 * (عربي/English).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appContext = LocalContext.current.applicationContext
    val repository = remember(appContext) { AndroidReferenceRepositoryFactory.create(appContext) }
    var lang by remember { mutableStateOf(RefLang.Arabic) }
    var selectedBook by remember { mutableStateOf<ReferenceBook?>(null) }
    var selectedTopic by remember { mutableStateOf<RefTopic?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    // System back steps out of the topic, then the book, then the screen
    // (mirrors the toolbar arrow) — never skips straight to the More root.
    BackHandler(enabled = selectedTopic != null || selectedBook != null) {
        when {
            selectedTopic != null -> selectedTopic = null
            selectedBook != null -> {
                selectedBook = null
                query = ""
            }
        }
    }

    val book = selectedBook

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
                onToggleLanguage = {
                    lang = if (lang == RefLang.Arabic) RefLang.English else RefLang.Arabic
                },
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
                onOpenTopic = { targetBook, targetTopic ->
                    selectedBook = targetBook
                    selectedTopic = targetTopic
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
                onOpenTopic = { selectedTopic = it },
                modifier = contentModifier,
            )
            else -> HubContent(
                repository = repository,
                lang = lang,
                onOpenBook = { selectedBook = it },
                onOpenTopic = { targetBook, targetTopic ->
                    selectedBook = targetBook
                    selectedTopic = targetTopic
                },
                modifier = contentModifier,
            )
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
    onOpenBook: (ReferenceBook) -> Unit,
    onOpenTopic: (ReferenceBook, RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(repository, query, lang) {
        if (query.isBlank()) emptyList() else repository.searchAll(query, lang, limit = 40)
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "reference-decoration") {
            IslamicDecorationBand(
                tint = MaterialTheme.colorScheme.tertiary,
                compact = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(key = "library-search") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = {
                    Text(
                        if (lang == RefLang.Arabic) {
                            "ابحث في جميع كتب المكتبة…"
                        } else {
                            "Search the entire library…"
                        },
                    )
                },
                singleLine = true,
            )
        }

        if (query.isBlank()) {
            items(repository.books, key = { it.id }) { book ->
                ReferenceBookCard(book = book, lang = lang, onOpenBook = onOpenBook)
            }
        } else if (results.isEmpty()) {
            item(key = "global-no-results") {
                MuslimStateSurface(
                    title = stringResource(R.string.reference_no_results),
                    tone = MuslimStateTone.Empty,
                    modifier = Modifier.padding(16.dp),
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
                            TopicListItem(topic = topic, lang = lang, onOpenTopic = onOpenTopic)
                        }
                    }
                } else {
                    items(results, key = { it.id }) { topic ->
                        TopicListItem(topic = topic, lang = lang, onOpenTopic = onOpenTopic)
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
    onOpenTopic: (RefTopic) -> Unit,
) {
    ListItem(
        headlineContent = { Text(topic.title(lang), fontWeight = FontWeight.Medium) },
        supportingContent = { Text(topic.summary(lang), maxLines = 2) },
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
    onOpenTopic: (ReferenceBook, RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
    ) {
        item(key = "topic-header") {
            TopicHeader(topic = topic, lang = lang)
        }
        items(topic.sections, key = { it.id }) { section ->
            TopicSectionContent(topic = topic, section = section, lang = lang)
        }
        if (topic.citations.isNotEmpty()) {
            item(key = "topic-sources") {
                TopicSources(topic = topic, lang = lang)
            }
        }
        val related = resolveRelatedTopics(repository, book, topic)
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
    }
}

@Composable
private fun TopicHeader(
    topic: RefTopic,
    lang: RefLang,
) {
    val context = LocalContext.current
    val shareText = remember(topic, lang) { buildTopicShareText(topic, lang) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = topic.summary(lang),
            style = MaterialTheme.typography.bodyLarge,
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
) {
    SectionLabel(text = section.title(lang))
    section.paragraphs.forEach { paragraph ->
        ParagraphCard(
            paragraph = paragraph,
            citations = paragraph.citationIds.mapNotNull { citationId ->
                topic.citations.firstOrNull { it.id == citationId }
            },
            lang = lang,
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
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                lineHeight = 28.sp,
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
            }
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
