package org.muslim.app.feature.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.family.R
import org.muslim.app.feature.family.domain.FamilyChecklist
import org.muslim.app.feature.family.domain.FamilyEvidenceReference
import org.muslim.app.feature.family.domain.FamilyEvidenceType
import org.muslim.app.feature.family.domain.FamilyGuideArticle
import org.muslim.app.feature.family.domain.FamilyLifeContent
import org.muslim.app.feature.family.domain.FamilyReferenceParser
import org.muslim.app.feature.family.domain.FamilyTopicCategory
import org.muslim.app.feature.family.domain.FamilyUtilityContent
import org.muslim.app.feature.family.domain.LocalizedFamilyText

internal enum class FamilyHubDestination {
    Search,
    Saved,
    Tools,
    Ruqyah,
    Names,
    Aqiqah,
    Quran,
    Hadith,
    Adhkar,
}

internal data class FamilyArticleReaderActions(
    val onToggleFavorite: () -> Unit,
    val onCopyArticle: () -> Unit,
    val onShareArticle: () -> Unit,
    val onOpenReference: (FamilyEvidenceReference) -> Unit,
    val onOpenArticle: (String) -> Unit,
)

private data class FamilyGuideFilterState(
    val category: FamilyTopicCategory?,
    val evidenceType: FamilyEvidenceType?,
    val favoritesOnly: Boolean,
    val query: String,
)

private data class FamilyGuideFilterActions(
    val onQueryChange: (String) -> Unit,
    val onCategoryChange: (FamilyTopicCategory?) -> Unit,
    val onEvidenceTypeChange: (FamilyEvidenceType?) -> Unit,
    val onFavoritesOnlyChange: (Boolean) -> Unit,
)

@Composable
internal fun FamilyHubContent(
    isArabic: Boolean,
    favoriteCount: Int,
    recentCount: Int,
    onOpenCategory: (FamilyTopicCategory) -> Unit,
    onOpenDestination: (FamilyHubDestination) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        familyHubIntro()
        familyGlobalSearchItem(onOpenDestination)
        familyCategoryItems(isArabic, onOpenCategory)
        familyLibraryItems(favoriteCount, recentCount, onOpenDestination)
        familyPracticalToolItems(onOpenDestination)
        familyCrossFeatureItems(onOpenDestination)
    }
}

private fun LazyListScope.familyHubIntro() {
    item {
        MuslimStateSurface(
            title = stringResource(R.string.family_hub_intro_title),
            supportingText = stringResource(R.string.family_hub_intro_text),
            tone = MuslimStateTone.Positive,
            icon = Icons.Filled.FamilyRestroom,
        )
    }
}

private fun LazyListScope.familyGlobalSearchItem(
    onOpenDestination: (FamilyHubDestination) -> Unit,
) {
    item {
        FamilyToolCard(
            icon = Icons.Filled.Search,
            title = stringResource(R.string.family_global_search_title),
            description = stringResource(R.string.family_global_search_hub_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Search) },
        )
    }
}

private fun LazyListScope.familyCategoryItems(
    isArabic: Boolean,
    onOpenCategory: (FamilyTopicCategory) -> Unit,
) {
    item { FamilyHubHeading(stringResource(R.string.family_hub_paths_title)) }
    items(FamilyTopicCategory.entries, key = { it.name }) { category ->
        FamilyCategoryCard(
            category = category,
            isArabic = isArabic,
            articleCount = FamilyLifeContent.articlesFor(category).size,
            onClick = { onOpenCategory(category) },
        )
    }
}

private fun LazyListScope.familyLibraryItems(
    favoriteCount: Int,
    recentCount: Int,
    onOpenDestination: (FamilyHubDestination) -> Unit,
) {
    item { FamilyHubHeading(stringResource(R.string.family_hub_library_title)) }
    item {
        FamilyToolCard(
            icon = Icons.Filled.Bookmark,
            title = stringResource(R.string.family_saved_title),
            description = stringResource(R.string.family_saved_summary, favoriteCount, recentCount),
            onClick = { onOpenDestination(FamilyHubDestination.Saved) },
        )
    }
    item {
        FamilyToolCard(
            icon = Icons.Filled.Checklist,
            title = stringResource(R.string.family_checklists_title),
            description = stringResource(R.string.family_checklists_summary),
            onClick = { onOpenDestination(FamilyHubDestination.Tools) },
        )
    }
}

private fun LazyListScope.familyPracticalToolItems(
    onOpenDestination: (FamilyHubDestination) -> Unit,
) {
    item { FamilyHubHeading(stringResource(R.string.family_hub_tools_title)) }
    item {
        FamilyToolCard(
            icon = Icons.Filled.HealthAndSafety,
            title = stringResource(R.string.family_tab_ruqyah),
            description = stringResource(R.string.family_hub_ruqyah_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Ruqyah) },
        )
    }
    item {
        FamilyToolCard(
            icon = Icons.Filled.Translate,
            title = stringResource(R.string.family_tab_names),
            description = stringResource(R.string.family_hub_names_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Names) },
        )
    }
    item {
        FamilyToolCard(
            icon = Icons.Filled.ChildCare,
            title = stringResource(R.string.family_tab_aqiqah),
            description = stringResource(R.string.family_hub_aqiqah_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Aqiqah) },
        )
    }
}

private fun LazyListScope.familyCrossFeatureItems(
    onOpenDestination: (FamilyHubDestination) -> Unit,
) {
    item { FamilyHubHeading(stringResource(R.string.family_hub_related_sections_title)) }
    item {
        FamilyToolCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.family_open_quran_title),
            description = stringResource(R.string.family_open_quran_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Quran) },
        )
    }
    item {
        FamilyToolCard(
            icon = Icons.Filled.AutoStories,
            title = stringResource(R.string.family_open_hadith_title),
            description = stringResource(R.string.family_open_hadith_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Hadith) },
        )
    }
    item {
        FamilyToolCard(
            icon = Icons.Filled.Favorite,
            title = stringResource(R.string.family_open_adhkar_title),
            description = stringResource(R.string.family_open_adhkar_desc),
            onClick = { onOpenDestination(FamilyHubDestination.Adhkar) },
        )
    }
}

@Composable
private fun FamilyHubHeading(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun FamilyCategoryCard(
    category: FamilyTopicCategory,
    isArabic: Boolean,
    articleCount: Int,
    onClick: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = category.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(10.dp).size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = category.title(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = category.subtitle(isArabic),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.family_hub_article_count, articleCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun FamilyToolCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
internal fun FamilyGuideCatalogContent(
    isArabic: Boolean,
    initialCategory: FamilyTopicCategory?,
    favoriteIds: Set<String>,
    onOpenArticle: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable(initialCategory) { mutableStateOf(initialCategory?.name) }
    var evidenceTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    val category = categoryName?.let { name ->
        FamilyTopicCategory.entries.firstOrNull { it.name == name }
    }
    val evidenceType = evidenceTypeName?.let { name ->
        FamilyEvidenceType.entries.firstOrNull { it.name == name }
    }
    val results = remember(
        query,
        categoryName,
        evidenceTypeName,
        favoritesOnly,
        favoriteIds,
    ) {
        FamilyLifeContent.searchArticles(
            query = query,
            category = category,
            evidenceType = evidenceType,
        ).filter { !favoritesOnly || it.id in favoriteIds }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FamilyGuideFilters(
                isArabic = isArabic,
                state = FamilyGuideFilterState(
                    category = category,
                    evidenceType = evidenceType,
                    favoritesOnly = favoritesOnly,
                    query = query,
                ),
                actions = FamilyGuideFilterActions(
                    onQueryChange = { query = it },
                    onCategoryChange = { categoryName = it?.name },
                    onEvidenceTypeChange = { evidenceTypeName = it?.name },
                    onFavoritesOnlyChange = { favoritesOnly = it },
                ),
            )
        }
        familyGuideResultItems(
            results = results,
            isArabic = isArabic,
            favoriteIds = favoriteIds,
            onOpenArticle = onOpenArticle,
            onToggleFavorite = onToggleFavorite,
        )
    }
}

private fun LazyListScope.familyGuideResultItems(
    results: List<FamilyGuideArticle>,
    isArabic: Boolean,
    favoriteIds: Set<String>,
    onOpenArticle: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    item {
        Text(
            text = stringResource(R.string.family_articles_count, results.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    items(results, key = { it.id }) { article ->
        FamilyGuideResultCard(
            article = article,
            isArabic = isArabic,
            isFavorite = article.id in favoriteIds,
            onClick = { onOpenArticle(article.id) },
            onToggleFavorite = { onToggleFavorite(article.id) },
        )
    }
    if (results.isEmpty()) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_articles_empty),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Search,
            )
        }
    }
}

@Composable
private fun FamilyGuideFilters(
    isArabic: Boolean,
    state: FamilyGuideFilterState,
    actions: FamilyGuideFilterActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MuslimStateSurface(
            title = state.category?.title(isArabic) ?: stringResource(R.string.family_guide_all_title),
            supportingText = stringResource(R.string.family_guide_intro),
            tone = MuslimStateTone.Information,
            icon = state.category?.icon() ?: Icons.AutoMirrored.Filled.MenuBook,
        )
        DigitNormalizedOutlinedTextField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FamilyUiTags.GUIDE_SEARCH_FIELD),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.family_articles_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = state.category == null,
                    onClick = { actions.onCategoryChange(null) },
                    label = { Text(stringResource(R.string.family_guide_filter_all)) },
                )
            }
            items(FamilyTopicCategory.entries, key = { it.name }) { item ->
                FilterChip(
                    selected = state.category == item,
                    onClick = { actions.onCategoryChange(item) },
                    label = { Text(item.title(isArabic)) },
                )
            }
        }
        FamilyEvidenceFilters(
            isArabic = isArabic,
            evidenceType = state.evidenceType,
            favoritesOnly = state.favoritesOnly,
            onEvidenceTypeChange = actions.onEvidenceTypeChange,
            onFavoritesOnlyChange = actions.onFavoritesOnlyChange,
        )
    }
}

@Composable
private fun FamilyEvidenceFilters(
    isArabic: Boolean,
    evidenceType: FamilyEvidenceType?,
    favoritesOnly: Boolean,
    onEvidenceTypeChange: (FamilyEvidenceType?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = evidenceType == null,
                onClick = { onEvidenceTypeChange(null) },
                label = { Text(stringResource(R.string.family_source_filter_all)) },
            )
        }
        items(FamilyEvidenceType.entries, key = { it.name }) { type ->
            FilterChip(
                selected = evidenceType == type,
                onClick = { onEvidenceTypeChange(type) },
                label = { Text(type.label(isArabic)) },
            )
        }
        item {
            FilterChip(
                selected = favoritesOnly,
                onClick = { onFavoritesOnlyChange(!favoritesOnly) },
                label = { Text(stringResource(R.string.family_filter_favorites)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (favoritesOnly) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun FamilyGuideResultCard(
    article: FamilyGuideArticle,
    isArabic: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.title.pick(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.family_remove_favorite else R.string.family_add_favorite,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = article.summary.pick(isArabic),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.family_guide_sections_count, article.sections.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.family_guide_open_article),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun FamilyArticleDetailContent(
    article: FamilyGuideArticle,
    isArabic: Boolean,
    isFavorite: Boolean,
    relatedArticles: List<FamilyGuideArticle>,
    actions: FamilyArticleReaderActions,
) {
    val category = FamilyLifeContent.categoryFor(article.id)
    val sensitive = category == FamilyTopicCategory.ConflictResolution ||
        category == FamilyTopicCategory.Separation

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(FamilyUiTags.ARTICLE_READER),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FamilyArticleHeader(
                article = article,
                category = category,
                isArabic = isArabic,
                isFavorite = isFavorite,
                onToggleFavorite = actions.onToggleFavorite,
            )
        }
        item {
            FamilyArticleActions(
                onCopyArticle = actions.onCopyArticle,
                onShareArticle = actions.onShareArticle,
            )
        }
        if (sensitive) {
            item { FamilySensitiveNotice() }
        }
        items(article.sections, key = { it.title.arabic + it.title.english }) { section ->
            FamilyArticleSectionCard(section.title, section.paragraphs, isArabic)
        }
        if (article.references.isNotEmpty()) {
            item { FamilyReferencesHeading() }
            items(article.references, key = { it.citation }) { reference ->
                FamilyReferenceCard(
                    reference = reference,
                    isArabic = isArabic,
                    onOpen = if (FamilyReferenceParser.canOpenInApp(reference)) {
                        { actions.onOpenReference(reference) }
                    } else {
                        null
                    },
                )
            }
        }
        if (relatedArticles.isNotEmpty()) {
            item { FamilyRelatedHeading() }
            items(relatedArticles, key = { it.id }) { related ->
                FamilyRelatedArticleCard(
                    article = related,
                    isArabic = isArabic,
                    onClick = { actions.onOpenArticle(related.id) },
                )
            }
        }
        item { FamilyEducationNotice() }
    }
}

@Composable
private fun FamilyArticleHeader(
    article: FamilyGuideArticle,
    category: FamilyTopicCategory?,
    isArabic: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = article.title.pick(isArabic),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.family_remove_favorite else R.string.family_add_favorite,
                ),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(
        text = article.summary.pick(isArabic),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    category?.let {
        Spacer(Modifier.height(8.dp))
        Text(
            text = it.title(isArabic),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FamilyArticleActions(
    onCopyArticle: () -> Unit,
    onShareArticle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(FamilyUiTags.ARTICLE_ACTIONS),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCopyArticle) {
            Icon(Icons.Filled.ContentCopy, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.family_copy_article))
        }
        TextButton(onClick = onShareArticle) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.family_share_article))
        }
    }
}

@Composable
private fun FamilySensitiveNotice() {
    MuslimStateSurface(
        title = stringResource(R.string.family_sensitive_notice_title),
        supportingText = stringResource(R.string.family_sensitive_notice_text),
        tone = MuslimStateTone.Warning,
        icon = Icons.Filled.Info,
    )
}

@Composable
private fun FamilyArticleSectionCard(
    title: LocalizedFamilyText,
    paragraphs: List<LocalizedFamilyText>,
    isArabic: Boolean,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.pick(isArabic),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        paragraphs.forEach { paragraph ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = paragraph.pick(isArabic),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun FamilyReferencesHeading() {
    Text(
        text = stringResource(R.string.family_references_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FamilyReferenceCard(
    reference: FamilyEvidenceReference,
    isArabic: Boolean,
    onOpen: (() -> Unit)?,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = reference.title.pick(isArabic),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = reference.type.label(isArabic),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = reference.citation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                reference.note?.let { note ->
                    Text(
                        text = note.pick(isArabic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onOpen != null) {
                    TextButton(onClick = onOpen) {
                        Text(
                            stringResource(
                                if (reference.type == FamilyEvidenceType.Quran) {
                                    R.string.family_open_quran_reference
                                } else {
                                    R.string.family_open_hadith_reference
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun FamilyRelatedHeading() {
    Text(
        text = stringResource(R.string.family_related_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FamilyRelatedArticleCard(
    article: FamilyGuideArticle,
    isArabic: Boolean,
    onClick: () -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = article.title.pick(isArabic),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = article.summary.pick(isArabic),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
internal fun FamilySavedContent(
    isArabic: Boolean,
    favoriteIds: Set<String>,
    recentArticleIds: List<String>,
    onOpenArticle: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    val favorites = FamilyLifeContent.familyArticles.filter { it.id in favoriteIds }
    val recent = recentArticleIds.mapNotNull(FamilyLifeContent::articleById)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_saved_title),
                supportingText = stringResource(R.string.family_saved_intro),
                tone = MuslimStateTone.Information,
                icon = Icons.Filled.Bookmark,
            )
        }
        item {
            FamilySectionLabel(
                title = stringResource(R.string.family_favorites_heading),
                count = favorites.size,
            )
        }
        if (favorites.isEmpty()) {
            item {
                FamilyEmptyLibraryCard(
                    text = stringResource(R.string.family_favorites_empty),
                    icon = Icons.Filled.BookmarkBorder,
                )
            }
        } else {
            items(favorites, key = { "favorite-" + it.id }) { article ->
                FamilySavedArticleCard(article, isArabic) { onOpenArticle(article.id) }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.family_history_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (recent.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.family_history_clear))
                    }
                }
            }
        }
        if (recent.isEmpty()) {
            item {
                FamilyEmptyLibraryCard(
                    text = stringResource(R.string.family_history_empty),
                    icon = Icons.Filled.History,
                )
            }
        } else {
            items(recent, key = { "recent-" + it.id }) { article ->
                FamilySavedArticleCard(article, isArabic) { onOpenArticle(article.id) }
            }
        }
    }
}

@Composable
internal fun FamilyToolsContent(
    isArabic: Boolean,
    completedItemIds: Set<String>,
    initialChecklistId: String? = null,
    onSetCompleted: (String, String, Boolean) -> Unit,
) {
    var selectedChecklistId by rememberSaveable(initialChecklistId) {
        mutableStateOf(initialChecklistId)
    }
    val selected = selectedChecklistId?.let(FamilyUtilityContent::checklistById)
    if (selected == null) {
        FamilyChecklistCatalog(
            isArabic = isArabic,
            completedItemIds = completedItemIds,
            onOpen = { selectedChecklistId = it },
        )
    } else {
        FamilyChecklistDetail(
            checklist = selected,
            isArabic = isArabic,
            completedItemIds = completedItemIds,
            onBack = { selectedChecklistId = null },
            onSetCompleted = onSetCompleted,
        )
    }
}

@Composable
private fun FamilyChecklistCatalog(
    isArabic: Boolean,
    completedItemIds: Set<String>,
    onOpen: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_checklists_title),
                supportingText = stringResource(R.string.family_checklists_intro),
                tone = MuslimStateTone.Positive,
                icon = Icons.Filled.Checklist,
            )
        }
        items(FamilyUtilityContent.checklists, key = { it.id }) { checklist ->
            val done = checklist.items.count { item ->
                FamilyUtilityContent.completionKey(checklist.id, item.id) in completedItemIds
            }
            IslamicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpen(checklist.id) },
            ) {
                Text(
                    text = checklist.title.pick(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = checklist.description.pick(isArabic),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.family_checklist_progress,
                        done,
                        checklist.items.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun FamilyChecklistDetail(
    checklist: FamilyChecklist,
    isArabic: Boolean,
    completedItemIds: Set<String>,
    onBack: () -> Unit,
    onSetCompleted: (String, String, Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.family_checklists_back))
            }
        }
        item {
            Text(
                text = checklist.title.pick(isArabic),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = checklist.description.pick(isArabic),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(checklist.items, key = { it.id }) { item ->
            val key = FamilyUtilityContent.completionKey(checklist.id, item.id)
            val checked = key in completedItemIds
            IslamicCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = {
                            onSetCompleted(checklist.id, item.id, it)
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.title.pick(isArabic),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_checklists_notice_title),
                supportingText = stringResource(R.string.family_checklists_notice_text),
                tone = MuslimStateTone.Neutral,
                icon = Icons.Filled.Info,
            )
        }
    }
}

@Composable
private fun FamilySavedArticleCard(
    article: FamilyGuideArticle,
    isArabic: Boolean,
    onClick: () -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(
            text = article.title.pick(isArabic),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = article.summary.pick(isArabic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FamilySectionLabel(title: String, count: Int) {
    Text(
        text = "$title ($count)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FamilyEmptyLibraryCard(
    text: String,
    icon: ImageVector,
) {
    MuslimStateSurface(
        title = text,
        tone = MuslimStateTone.Neutral,
        icon = icon,
    )
}

@Composable
private fun FamilyEducationNotice() {
    MuslimStateSurface(
        title = stringResource(R.string.family_education_notice_title),
        supportingText = stringResource(R.string.family_education_notice_text),
        tone = MuslimStateTone.Neutral,
        icon = Icons.Filled.Info,
    )
}

private fun LocalizedFamilyText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun FamilyEvidenceType.label(isArabic: Boolean): String = when (this) {
    FamilyEvidenceType.Quran -> if (isArabic) "القرآن الكريم" else "Quran"
    FamilyEvidenceType.Hadith -> if (isArabic) "الحديث" else "Hadith"
    FamilyEvidenceType.Fiqh -> if (isArabic) "فقه" else "Fiqh"
    FamilyEvidenceType.Legal -> if (isArabic) "قانوني" else "Legal"
    FamilyEvidenceType.Health -> if (isArabic) "صحي" else "Health"
    FamilyEvidenceType.Guidance -> if (isArabic) "إرشاد متخصص" else "Qualified guidance"
}

private fun FamilyTopicCategory.title(isArabic: Boolean): String = when (this) {
    FamilyTopicCategory.BeforeMarriage -> if (isArabic) "قبل الزواج" else "Before marriage"
    FamilyTopicCategory.Marriage -> if (isArabic) "عقد الزواج وحقوقه" else "Marriage contract & rights"
    FamilyTopicCategory.MaritalLife -> if (isArabic) "الحياة الزوجية" else "Married life"
    FamilyTopicCategory.ConflictResolution -> if (isArabic) "الخلاف والإصلاح" else "Conflict & reconciliation"
    FamilyTopicCategory.Separation -> if (isArabic) "الانفصال والطلاق" else "Separation & divorce"
    FamilyTopicCategory.Newborn -> if (isArabic) "المولود والعقيقة" else "Newborn & aqiqah"
    FamilyTopicCategory.Parenting -> if (isArabic) "تربية الأبناء" else "Parenting"
    FamilyTopicCategory.Kinship -> if (isArabic) "الوالدان وصلة الرحم" else "Parents & kinship"
    FamilyTopicCategory.DailyLife -> if (isArabic) "حياة المسلم في البيت" else "Daily Muslim life"
}

private fun FamilyTopicCategory.subtitle(isArabic: Boolean): String = when (this) {
    FamilyTopicCategory.BeforeMarriage -> if (isArabic) "الاختيار والخطبة والتوافق والاستعداد" else "Choosing, engagement, compatibility and preparation"
    FamilyTopicCategory.Marriage -> if (isArabic) "العقد والمهر والشروط والتوثيق" else "Contract, mahr, conditions and documentation"
    FamilyTopicCategory.MaritalLife -> if (isArabic) "الحقوق والتواصل والمال والخصوصية" else "Rights, communication, finances and privacy"
    FamilyTopicCategory.ConflictResolution -> if (isArabic) "الحوار والوساطة والسلامة" else "Dialogue, mediation and safety"
    FamilyTopicCategory.Separation -> if (isArabic) "مبادئ عامة تحفظ الحقوق وتمنع الفتوى الآلية" else "General guidance that protects rights without automated rulings"
    FamilyTopicCategory.Newborn -> if (isArabic) "استقبال المولود والرعاية والعقيقة" else "Welcoming a child, care and aqiqah"
    FamilyTopicCategory.Parenting -> if (isArabic) "الرحمة والقدوة والحماية" else "Mercy, example and protection"
    FamilyTopicCategory.Kinship -> if (isArabic) "البر والحدود والأسرة الممتدة" else "Kindness, boundaries and extended family"
    FamilyTopicCategory.DailyLife -> if (isArabic) "العبادة والخصوصية والتقنية داخل البيت" else "Worship, privacy and technology at home"
}

private fun FamilyTopicCategory.icon(): ImageVector = when (this) {
    FamilyTopicCategory.BeforeMarriage -> Icons.Filled.Favorite
    FamilyTopicCategory.Marriage -> Icons.Filled.Handshake
    FamilyTopicCategory.MaritalLife -> Icons.Filled.Home
    FamilyTopicCategory.ConflictResolution -> Icons.Filled.Forum
    FamilyTopicCategory.Separation -> Icons.Filled.Gavel
    FamilyTopicCategory.Newborn -> Icons.Filled.ChildCare
    FamilyTopicCategory.Parenting -> Icons.Filled.FamilyRestroom
    FamilyTopicCategory.Kinship -> Icons.Filled.Groups
    FamilyTopicCategory.DailyLife -> Icons.AutoMirrored.Filled.MenuBook
}
