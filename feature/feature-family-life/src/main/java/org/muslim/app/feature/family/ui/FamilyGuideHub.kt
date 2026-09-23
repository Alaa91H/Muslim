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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.family.R
import org.muslim.app.feature.family.domain.FamilyGuideArticle
import org.muslim.app.feature.family.domain.FamilyLifeContent
import org.muslim.app.feature.family.domain.FamilyTopicCategory
import org.muslim.app.feature.family.domain.LocalizedFamilyText

@Composable
internal fun FamilyHubContent(
    isArabic: Boolean,
    onOpenCategory: (FamilyTopicCategory) -> Unit,
    onOpenRuqyah: () -> Unit,
    onOpenNames: () -> Unit,
    onOpenAqiqah: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MuslimStateSurface(
                title = stringResource(R.string.family_hub_intro_title),
                supportingText = stringResource(R.string.family_hub_intro_text),
                tone = MuslimStateTone.Positive,
                icon = Icons.Filled.FamilyRestroom,
            )
        }
        item {
            Text(
                text = stringResource(R.string.family_hub_paths_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        items(FamilyTopicCategory.entries, key = { it.name }) { category ->
            FamilyCategoryCard(
                category = category,
                isArabic = isArabic,
                articleCount = FamilyLifeContent.articlesFor(category).size,
                onClick = { onOpenCategory(category) },
            )
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.family_hub_tools_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            FamilyToolCard(
                icon = Icons.Filled.HealthAndSafety,
                title = stringResource(R.string.family_tab_ruqyah),
                description = stringResource(R.string.family_hub_ruqyah_desc),
                onClick = onOpenRuqyah,
            )
        }
        item {
            FamilyToolCard(
                icon = Icons.Filled.Translate,
                title = stringResource(R.string.family_tab_names),
                description = stringResource(R.string.family_hub_names_desc),
                onClick = onOpenNames,
            )
        }
        item {
            FamilyToolCard(
                icon = Icons.Filled.ChildCare,
                title = stringResource(R.string.family_tab_aqiqah),
                description = stringResource(R.string.family_hub_aqiqah_desc),
                onClick = onOpenAqiqah,
            )
        }
    }
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
    onOpenArticle: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable(initialCategory) { mutableStateOf(initialCategory?.name) }
    val category = categoryName?.let { name ->
        FamilyTopicCategory.entries.firstOrNull { it.name == name }
    }
    val results = remember(query, categoryName) {
        FamilyLifeContent.searchArticles(query = query, category = category)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MuslimStateSurface(
                title = category?.title(isArabic) ?: stringResource(R.string.family_guide_all_title),
                supportingText = stringResource(R.string.family_guide_intro),
                tone = MuslimStateTone.Information,
                icon = category?.icon() ?: Icons.AutoMirrored.Filled.MenuBook,
            )
        }
        item {
            DigitNormalizedOutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.family_articles_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = category == null,
                        onClick = { categoryName = null },
                        label = { Text(stringResource(R.string.family_guide_filter_all)) },
                    )
                }
                items(FamilyTopicCategory.entries, key = { it.name }) { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { categoryName = item.name },
                        label = { Text(item.title(isArabic)) },
                    )
                }
            }
        }
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
                onClick = { onOpenArticle(article.id) },
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
}

@Composable
private fun FamilyGuideResultCard(
    article: FamilyGuideArticle,
    isArabic: Boolean,
    onClick: () -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column {
            Text(
                text = article.title.pick(isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
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
) {
    val category = FamilyLifeContent.categoryFor(article.id)
    val sensitive = category == FamilyTopicCategory.ConflictResolution ||
        category == FamilyTopicCategory.Separation

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { FamilyArticleHeader(article, category, isArabic) }
        if (sensitive) {
            item { FamilySensitiveNotice() }
        }
        items(article.sections, key = { it.title.arabic + it.title.english }) { section ->
            FamilyArticleSectionCard(section.title, section.paragraphs, isArabic)
        }
        if (article.references.isNotEmpty()) {
            item { FamilyReferencesHeading() }
            items(article.references, key = { it.citation }) { reference ->
                FamilyReferenceCard(reference.title, reference.citation, isArabic)
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
) {
    Text(
        text = article.title.pick(isArabic),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )
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
    title: LocalizedFamilyText,
    citation: String,
    isArabic: Boolean,
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
                    text = title.pick(isArabic),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = citation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
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
