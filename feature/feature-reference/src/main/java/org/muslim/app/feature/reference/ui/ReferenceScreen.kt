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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muslim.app.feature.reference.R
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.reference.domain.ReferenceBook
import org.muslim.app.feature.reference.domain.ReferenceLibrary
import org.muslim.app.feature.reference.domain.RefLang
import org.muslim.app.feature.reference.domain.RefTopic

private val bookIcons = mapOf(
    "islam" to Icons.Filled.AutoStories,
    "sira" to Icons.Filled.History,
    "prophets" to Icons.Filled.Groups,
)

/**
 * المرجعية الإسلامية (feature-reference): مكتبة مرجعية شاملة ومفهرسة تعرض
 * ثلاثة كتب — «التعريف بالإسلام»، «السيرة النبوية»، «قصص الأنبياء» — مع
 * بحث نصي في كل كتاب وتبديل لغة المحتوى (عربي/English).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            selectedTopic != null && book != null -> selectedTopic!!.title(lang)
                            book != null -> book.title(lang)
                            else -> stringResource(R.string.reference_title)
                        },
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            selectedTopic != null -> selectedTopic = null
                            book != null -> { selectedBook = null; query = "" }
                            else -> onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.reference_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { lang = if (lang == RefLang.Arabic) RefLang.English else RefLang.Arabic }) {
                        Text(
                            text = if (lang == RefLang.Arabic) "English" else "العربية",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            selectedTopic != null && book != null -> TopicContent(
                topic = selectedTopic!!,
                lang = lang,
                modifier = contentModifier,
            )
            book != null -> BookContent(
                book = book,
                lang = lang,
                query = query,
                onQueryChanged = { query = it },
                onOpenTopic = { selectedTopic = it },
                modifier = contentModifier,
            )
            else -> HubContent(
                lang = lang,
                onOpenBook = { selectedBook = it },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun HubContent(
    lang: RefLang,
    onOpenBook: (ReferenceBook) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(ReferenceLibrary.books, key = { it.id }) { book ->
            IslamicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onOpenBook(book) },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
    }
}

@Composable
private fun BookContent(
    book: ReferenceBook,
    lang: RefLang,
    query: String,
    onQueryChanged: (String) -> Unit,
    onOpenTopic: (RefTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val results = ReferenceLibrary.search(book, query, lang)
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
        if (results.isEmpty()) {
            MuslimStateSurface(
                title = stringResource(R.string.reference_no_results),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Search,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results, key = { it.id }) { topic ->
                    ListItem(
                        headlineContent = { Text(topic.title(lang), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(topic.summary(lang), maxLines = 2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTopic(topic) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TopicContent(
    topic: RefTopic,
    lang: RefLang,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shareText = buildString {
        append(topic.title(lang)).append("\n\n")
        append(topic.summary(lang)).append("\n\n")
        topic.sections.forEach { section ->
            append(section.title(lang)).append("\n")
            section.paragraphs.forEach { paragraph -> append(paragraph.text(lang)).append("\n") }
            append("\n")
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = topic.summary(lang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
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
        items(topic.sections, key = { it.id }) { section ->
            Text(
                text = section.title(lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
            section.paragraphs.forEach { paragraph ->
                IslamicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        text = paragraph.text(lang),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                        lineHeight = 28.sp,
                    )
                }
            }
        }
    }
}
