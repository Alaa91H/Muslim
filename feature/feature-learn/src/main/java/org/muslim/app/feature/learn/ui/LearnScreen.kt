package org.muslim.app.feature.learn.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimSectionHeader
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.LearnContent
import org.muslim.app.feature.learn.domain.LearnTopic
import org.muslim.app.feature.learn.domain.LearningAssessmentCatalog
import org.muslim.app.feature.learn.domain.LearningFeatureDestination
import org.muslim.app.feature.learn.domain.LearningProgressPlanner

private val topicIcons = mapOf(
    "pillars_islam" to Icons.Filled.Mosque,
    "pillars_iman" to Icons.Filled.Book,
    "faith_tawhid_worship" to Icons.Filled.Mosque,
    "faith_knowing_allah" to Icons.Filled.Star,
    "faith_angels" to Icons.Filled.VerifiedUser,
    "faith_books" to Icons.AutoMirrored.Filled.MenuBook,
    "faith_messengers" to Icons.Filled.AutoStories,
    "faith_last_day" to Icons.Filled.Schedule,
    "faith_qadar" to Icons.Filled.Loop,
    "faith_questions" to Icons.Filled.Checklist,
    "tahara_intro" to Icons.Filled.AutoStories,
    "water_impurity" to Icons.Filled.WaterDrop,
    "restroom_etiquette" to Icons.Filled.Checklist,
    "wudu_nullifiers" to Icons.Filled.Warning,
    "wiping_footwear" to Icons.Filled.BeachAccess,
    "menstruation_postpartum" to Icons.Filled.LocalFlorist,
    "excused_person" to Icons.Filled.VerifiedUser,
    "prayer_intro" to Icons.Filled.AutoStories,
    "qibla_niyyah" to Icons.Filled.Place,
    "sujud_sahw" to Icons.Filled.Loop,
    "congregation_imamah" to Icons.Filled.Mosque,
    "traveler_prayer" to Icons.Filled.BeachAccess,
    "sick_prayer" to Icons.Filled.VerifiedUser,
    "jumuah" to Icons.Filled.Mosque,
    "wudu" to Icons.Filled.WaterDrop,
    "ghusl" to Icons.Filled.Bathtub,
    "tayammum" to Icons.Filled.BeachAccess,
    "salah" to Icons.Filled.AutoStories,
    "salah_arkan" to Icons.Filled.Checklist,
    "salah_times" to Icons.Filled.Schedule,
    "adhan" to Icons.AutoMirrored.Filled.VolumeUp,
    "shurut" to Icons.Filled.VerifiedUser,
    "nullifiers" to Icons.Filled.Warning,
    "rawatib" to Icons.Filled.Loop,
    "rakats" to Icons.Filled.FormatListNumbered,
    "special" to Icons.AutoMirrored.Filled.MenuBook,
    "fasting" to Icons.Filled.Restaurant,
    "fasting_day" to Icons.Filled.Schedule,
    "fasting_nullifiers" to Icons.Filled.Warning,
    "fasting_exemptions" to Icons.Filled.VerifiedUser,
    "fasting_women" to Icons.Filled.LocalFlorist,
    "fasting_travel_illness" to Icons.Filled.BeachAccess,
    "ramadan_sunnahs" to Icons.Filled.AutoStories,
    "laylat_qadr_itikaf" to Icons.Filled.Star,
    "voluntary_fasting" to Icons.Filled.Loop,
    "zakat" to Icons.Filled.AccountBalance,
    "zakat_nisab_haul" to Icons.Filled.FormatListNumbered,
    "zakat_cash_metals" to Icons.Filled.AccountBalance,
    "zakat_business_investments" to Icons.Filled.AccountBalance,
    "zakat_debts_jewelry" to Icons.Filled.Warning,
    "zakat_recipients" to Icons.Filled.ChildCare,
    "zakat_crops_livestock" to Icons.Filled.LocalFlorist,
    "zakat_fitr" to Icons.Filled.Restaurant,
    "zakat_calculator_guide" to Icons.Filled.Checklist,
    "quran_intro" to Icons.AutoMirrored.Filled.MenuBook,
    "quran_etiquette" to Icons.Filled.AutoStories,
    "quran_structure" to Icons.Filled.FormatListNumbered,
    "quran_understanding" to Icons.Filled.Book,
    "quran_learning_plan" to Icons.Filled.Checklist,
    "tajweed_intro" to Icons.AutoMirrored.Filled.VolumeUp,
    "tajweed_makharij" to Icons.Filled.VerifiedUser,
    "tajweed_noon_meem" to Icons.Filled.AutoStories,
    "tajweed_madd" to Icons.Filled.Schedule,
    "tajweed_qalqalah" to Icons.Filled.Warning,
    "tajweed_waqf" to Icons.Filled.Place,
    "tajweed_practice" to Icons.Filled.Loop,
    "sunnah_intro" to Icons.AutoMirrored.Filled.MenuBook,
    "hadith_anatomy" to Icons.Filled.FormatListNumbered,
    "hadith_grades" to Icons.Filled.VerifiedUser,
    "hadith_verification" to Icons.Filled.Checklist,
    "hadith_understanding" to Icons.Filled.AutoStories,
    "hadith_library_guide" to Icons.Filled.Book,
    "seerah_method" to Icons.Filled.Checklist,
    "seerah_early_life" to Icons.Filled.LocalFlorist,
    "seerah_revelation_makkah" to Icons.AutoMirrored.Filled.MenuBook,
    "seerah_hijrah" to Icons.Filled.Place,
    "seerah_madinah" to Icons.Filled.Mosque,
    "seerah_major_events" to Icons.Filled.Star,
    "seerah_character_legacy" to Icons.Filled.VerifiedUser,
    "ethics_foundation" to Icons.Filled.Book,
    "ethics_speech" to Icons.AutoMirrored.Filled.VolumeUp,
    "ethics_family" to Icons.Filled.ChildCare,
    "ethics_neighbours" to Icons.Filled.Mosque,
    "ethics_conflict" to Icons.Filled.Warning,
    "ethics_privacy" to Icons.Filled.VerifiedUser,
    "ethics_work_digital" to Icons.Filled.Checklist,
    "new_muslim_welcome" to Icons.Filled.Star,
    "new_muslim_belief" to Icons.Filled.Book,
    "new_muslim_prayer" to Icons.Filled.Mosque,
    "new_muslim_purification" to Icons.Filled.WaterDrop,
    "new_muslim_quran" to Icons.AutoMirrored.Filled.MenuBook,
    "new_muslim_daily_life" to Icons.Filled.LocalFlorist,
    "new_muslim_roadmap" to Icons.Filled.Schedule,
    "family_intro" to Icons.Filled.ChildCare,
    "family_spouse_selection" to Icons.Filled.VerifiedUser,
    "family_marriage_contract" to Icons.Filled.Checklist,
    "family_marital_life" to Icons.Filled.LocalFlorist,
    "family_parenting" to Icons.Filled.ChildCare,
    "family_kinship" to Icons.Filled.Mosque,
    "family_conflict_separation" to Icons.Filled.Warning,
    "finance_intro" to Icons.Filled.AccountBalance,
    "finance_sale_contracts" to Icons.Filled.Checklist,
    "finance_riba" to Icons.Filled.Warning,
    "finance_debt_loans" to Icons.Filled.AccountBalance,
    "finance_ecommerce" to Icons.Filled.Restaurant,
    "finance_business_investing" to Icons.Filled.FormatListNumbered,
    "finance_tools_guide" to Icons.Filled.Book,
    "funeral" to Icons.Filled.LocalFlorist,
    "madhhab" to Icons.Filled.ChildCare,
)

private enum class LearnSpecialDestination {
    Names,
    Hajj,
}

@Composable
private fun categoryTitleRes(category: String): Int = when (category) {
    LearnContent.CATEGORY_FAITH -> R.string.learn_category_faith
    LearnContent.CATEGORY_TAHARA -> R.string.learn_category_tahara
    LearnContent.CATEGORY_SALAH -> R.string.learn_category_salah
    LearnContent.CATEGORY_IBADAH -> R.string.learn_category_ibadah
    LearnContent.CATEGORY_QURAN -> R.string.learn_category_quran
    LearnContent.CATEGORY_SUNNAH -> R.string.learn_category_sunnah
    LearnContent.CATEGORY_SEERAH -> R.string.learn_category_seerah
    LearnContent.CATEGORY_ETHICS -> R.string.learn_category_ethics
    LearnContent.CATEGORY_NEW_MUSLIM -> R.string.learn_category_new_muslim
    LearnContent.CATEGORY_FAMILY -> R.string.learn_category_family
    LearnContent.CATEGORY_FINANCE -> R.string.learn_category_finance
    LearnContent.CATEGORY_REFERENCE -> R.string.learn_category_reference
    else -> R.string.learn_category_reference
}

/**
 * Learning hub (PROJECT_PROMPT.md §6 Phase 5): a complete, indexed learning
 * reference — the pillars of Islam and faith, purification (wudu / ghusl /
 * tayammum), the prayer in full detail (conditions, times, adhan, pillars,
 * nullifiers, rawatib, special prayers, rak'ah table), fasting, zakat,
 * funerals and a neutral madhhab differences overview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onBack: () -> Unit,
    onOpenFeature: (LearningFeatureDestination) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LearnViewModel = hiltViewModel(),
) {
    var selected by remember { mutableStateOf<LearnTopic?>(null) }
    var specialDestination by remember { mutableStateOf<LearnSpecialDestination?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showMistakes by remember { mutableStateOf(false) }
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val completedLessonIds by viewModel.completedLessonIds.collectAsStateWithLifecycle()
    val lastOpenedLessonId by viewModel.lastOpenedLessonId.collectAsStateWithLifecycle()
    val quizAnswers by viewModel.quizAnswers.collectAsStateWithLifecycle()
    val topic = selected
    val continueTopic = remember(lastOpenedLessonId, completedLessonIds) {
        LearningProgressPlanner.continueLessonId(
            lastOpenedLessonId = lastOpenedLessonId,
            completedLessonIds = completedLessonIds,
        )?.let { id -> LearnContent.topics.firstOrNull { it.id == id } }
    }
    val mistakeCount = remember(quizAnswers) {
        LearningAssessmentCatalog.incorrectEntries(quizAnswers).size
    }

    LaunchedEffect(topic?.id) {
        topic?.id?.let(viewModel::openLesson)
    }

    // Back always resolves the inner learning destination before returning to More.
    BackHandler(enabled = showMistakes) { showMistakes = false }
    BackHandler(enabled = specialDestination != null) { specialDestination = null }
    BackHandler(enabled = topic != null) { selected = null }

    when (specialDestination) {
        LearnSpecialDestination.Names -> {
            NamesOfAllahScreen(onBack = { specialDestination = null }, modifier = modifier)
            return
        }
        LearnSpecialDestination.Hajj -> {
            HajjUmrahScreen(onBack = { specialDestination = null }, modifier = modifier)
            return
        }
        null -> Unit
    }

    if (showMistakes) {
        LearningMistakesScreen(
            quizAnswers = quizAnswers,
            onBack = { showMistakes = false },
            onOpenLesson = { lessonTopic ->
                showMistakes = false
                selected = lessonTopic
            },
            modifier = modifier,
        )
        return
    }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (topic == null) R.string.learn_title else topic.titleRes)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (topic == null) onBack() else selected = null }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
                actions = {
                    if (topic != null) {
                        val isFav = topic.id in favoriteIds
                        IconButton(onClick = { viewModel.toggleFavorite(topic.id) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(
                                    if (isFav) R.string.learn_remove_favorite else R.string.learn_add_favorite
                                ),
                                tint = if (isFav) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (topic == null) {
            TopicList(
                favoriteIds = favoriteIds,
                completedLessonIds = completedLessonIds,
                searchQuery = searchQuery,
                continueTopic = continueTopic,
                mistakeCount = mistakeCount,
                onSearchQueryChange = { searchQuery = it },
                onToggleFavorite = viewModel::toggleFavorite,
                onReviewMistakes = { showMistakes = true },
                modifier = Modifier.padding(innerPadding),
                onOpen = { selected = it },
                onOpenSpecial = { specialDestination = it },
            )
        } else {
            LearningLessonReader(
                topic = topic,
                completed = topic.id in completedLessonIds,
                quizAnswers = quizAnswers,
                onSetCompleted = { completed ->
                    viewModel.setLessonCompleted(topic.id, completed)
                },
                onAnswerQuiz = { quizId, optionId ->
                    viewModel.answerQuiz(topic.id, quizId, optionId)
                },
                onOpenFeature = onOpenFeature,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun TopicList(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (LearnTopic) -> Unit,
    onOpenSpecial: (LearnSpecialDestination) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "islamic-decoration") {
            IslamicDecorationBand(
                tint = MaterialTheme.colorScheme.tertiary,
                compact = true,
            )
        }
        specialDestinationItems(onOpenSpecial)
        favouriteTopicItems(favoriteIds, onToggleFavorite, onOpen)
        categoryTopicItems(favoriteIds, onToggleFavorite, onOpen)
    }
}

private fun LazyListScope.specialDestinationItems(
    onOpenSpecial: (LearnSpecialDestination) -> Unit,
) {
    specialDestinationItem(
        key = "names_of_allah",
        titleRes = R.string.learn_topic_names_of_allah,
        subtitleRes = R.string.learn_topic_names_of_allah_sub,
        icon = Icons.Filled.Star,
        destination = LearnSpecialDestination.Names,
        onOpenSpecial = onOpenSpecial,
    )
    specialDestinationItem(
        key = "hajj_umrah",
        titleRes = R.string.learn_topic_hajj,
        subtitleRes = R.string.learn_topic_hajj_sub,
        icon = Icons.Filled.Place,
        destination = LearnSpecialDestination.Hajj,
        onOpenSpecial = onOpenSpecial,
    )
}

private fun LazyListScope.specialDestinationItem(
    key: String,
    titleRes: Int,
    subtitleRes: Int,
    icon: ImageVector,
    destination: LearnSpecialDestination,
    onOpenSpecial: (LearnSpecialDestination) -> Unit,
) {
    item(key = key) {
        IslamicCard(
            modifier = Modifier.fillMaxWidth().clickable { onOpenSpecial(destination) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                SpecialDestinationIcon(icon)
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(subtitleRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialDestinationIcon(icon: ImageVector) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(8.dp).size(20.dp),
        )
    }
}

private fun LazyListScope.favouriteTopicItems(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpen: (LearnTopic) -> Unit,
) {
    val favorites = LearnContent.topics.filter { it.id in favoriteIds }
    if (favorites.isEmpty()) return
    item(key = "favorites_header") { CategoryHeader(title = stringResource(R.string.learn_favorites_header)) }
    topicRows(favorites, { true }, onToggleFavorite, onOpen, keyPrefix = "favorite_")
}

private fun LazyListScope.categoryTopicItems(
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onOpen: (LearnTopic) -> Unit,
) {
    val byCategory = LearnContent.topics.groupBy { it.category }
    LearnContent.categoryOrder.forEach { category ->
        val topics = byCategory[category].orEmpty()
        if (topics.isEmpty()) return@forEach
        item(key = "category_$category") { CategoryHeader(title = stringResource(categoryTitleRes(category))) }
        topicRows(topics, { topic -> topic.id in favoriteIds }, onToggleFavorite, onOpen)
    }
}

private fun LazyListScope.topicRows(
    topics: List<LearnTopic>,
    isFavorite: (LearnTopic) -> Boolean,
    onToggleFavorite: (String) -> Unit,
    onOpen: (LearnTopic) -> Unit,
    keyPrefix: String = "",
) {
    items(topics, key = { "$keyPrefix${it.id}" }) { topic ->
        TopicListItem(topic, isFavorite(topic), onToggleFavorite, onOpen)
    }
}

@Composable
private fun TopicListItem(
    topic: LearnTopic,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onOpen: (LearnTopic) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(topic) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = topicIcons[topic.id] ?: Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = stringResource(topic.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(topic.subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onToggleFavorite(topic.id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.learn_remove_favorite else R.string.learn_add_favorite
                    ),
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    MuslimSectionHeader(
        title = title,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun GuideContent(
    topic: LearnTopic,
    onOpenFeature: (LearningFeatureDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lesson = remember(topic.id) { LearningAcademyCatalog.lessonFor(topic) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        lesson.estimatedMinutes?.let { minutes ->
            item(key = "${lesson.id}_metadata") {
                LearningLessonMetadataCard(
                    estimatedMinutes = minutes,
                    contentVersion = lesson.contentVersion,
                )
            }
        }

        lesson.sections.forEach { section ->
            item(key = "${lesson.id}_${section.id}_header") {
                CategoryHeader(title = section.title)
            }

            section.blocks.forEachIndexed { blockIndex, block ->
                when (block) {
                    is LearningContentBlock.Steps -> {
                        itemsIndexed(
                            items = block.items,
                            key = { stepIndex, _ ->
                                "${lesson.id}_${section.id}_${blockIndex}_step_${stepIndex}"
                            },
                        ) { stepIndex, step ->
                            LearningStepCard(
                                index = stepIndex,
                                step = step,
                            )
                        }
                    }

                    else -> {
                        item(key = "${lesson.id}_${section.id}_block_${blockIndex}") {
                            LearningBlockCard(block = block)
                        }
                    }
                }
            }
        }

        lesson.featureLink?.let { link ->
            item(key = "${lesson.id}_feature_link") {
                LearningFeatureLinkCard(
                    link = link,
                    onOpenFeature = onOpenFeature,
                )
            }
        }

        if (lesson.references.isNotEmpty()) {
            item(key = "${lesson.id}_references_header") {
                CategoryHeader(title = stringResource(R.string.learn_references))
            }
            items(
                items = lesson.references,
                key = { reference -> "${lesson.id}_reference_${reference.id}" },
            ) { reference ->
                LearningReferenceCard(reference)
            }
        }
    }
}

@Composable
private fun LearningLessonMetadataCard(
    estimatedMinutes: Int,
    contentVersion: Int,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.learn_estimated_minutes, estimatedMinutes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.learn_content_version, contentVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.learn_scholar_review_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LearningFeatureLinkCard(
    link: LearningFeatureLink,
    onOpenFeature: (LearningFeatureDestination) -> Unit,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(link.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(link.bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = { onOpenFeature(link.destination) }) {
                Text(stringResource(link.actionRes))
            }
        }
    }
}

@Composable
private fun LearningReferenceCard(reference: LearningReference) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = reference.citation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            reference.locator?.let { locator ->
                Text(
                    text = locator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            reference.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LearningStepCard(
    index: Int,
    step: LearningStepItem,
) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                step.supplementalText?.let { supplemental ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = supplemental,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningBlockCard(block: LearningContentBlock) {
    when (block) {
        is LearningContentBlock.Paragraph -> LearningParagraphCard(block)
        is LearningContentBlock.Callout -> LearningCalloutCard(block)
        is LearningContentBlock.Evidence -> LearningEvidenceCard(block)
        is LearningContentBlock.Comparison -> LearningComparisonCard(block)
        is LearningContentBlock.QuestionAnswer -> LearningQuestionAnswerCard(block)
        is LearningContentBlock.Quiz -> LearningQuizCard(block)
        is LearningContentBlock.Steps -> Unit
    }
}

@Composable
private fun LearningParagraphCard(block: LearningContentBlock.Paragraph) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LearningCalloutCard(block: LearningContentBlock.Callout) {
    val containerColor = when (block.tone) {
        LearningCalloutTone.INFO -> MaterialTheme.colorScheme.secondaryContainer
        LearningCalloutTone.IMPORTANT -> MaterialTheme.colorScheme.primaryContainer
        LearningCalloutTone.WARNING -> MaterialTheme.colorScheme.errorContainer
        LearningCalloutTone.DIFFERENCE_OF_OPINION -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = when (block.tone) {
        LearningCalloutTone.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
        LearningCalloutTone.IMPORTANT -> MaterialTheme.colorScheme.onPrimaryContainer
        LearningCalloutTone.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        LearningCalloutTone.DIFFERENCE_OF_OPINION -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
            Text(
                text = block.body,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun LearningEvidenceCard(block: LearningContentBlock.Evidence) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.heading?.let { heading ->
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun LearningComparisonCard(block: LearningContentBlock.Comparison) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            block.intro?.let { intro ->
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            block.items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningQuestionAnswerCard(block: LearningContentBlock.QuestionAnswer) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = block.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = block.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LearningQuizCard(block: LearningContentBlock.Quiz) {
    IslamicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = block.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            block.options.forEach { option ->
                Text(
                    text = "• ${option.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

