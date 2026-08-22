package org.muslim.app.feature.learn.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.ArabicLetter
import org.muslim.app.feature.learn.domain.BeginnerLanguage
import org.muslim.app.feature.learn.domain.MakhrajGroup
import org.muslim.app.feature.learn.domain.NewMuslimStep
import org.muslim.app.feature.learn.domain.NooraniContent
import org.muslim.app.feature.learn.domain.ReadingStage

/**
 * A beginner-friendly starter section. The visual cues are simplified memory
 * aids; individual makharij and Quranic recitation require teacher review.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NooraniNewMuslimScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val speaker = remember(context) { ArabicSpeechController(context) }

    DisposableEffect(speaker) {
        onDispose(speaker::release)
    }
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.noorani_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.learn_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NooraniTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            when (selectedTab) {
                0 -> LetterLesson(speaker = speaker)
                1 -> ReadingBasics(speaker = speaker)
                else -> NewMuslimCorner()
            }
        }
    }
}

@Composable
private fun NooraniTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val titles = listOf(
        stringResource(R.string.noorani_tab_letters),
        stringResource(R.string.noorani_tab_reading),
        stringResource(R.string.noorani_tab_new_muslim),
    )
    TabRow(selectedTabIndex = selectedTab) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedTab,
                onClick = { onSelect(index) },
                text = { Text(text = title, maxLines = 2, textAlign = TextAlign.Center) },
            )
        }
    }
}

@Composable
private fun LetterLesson(speaker: ArabicSpeechController) {
    var selected by remember { mutableStateOf(NooraniContent.letters.first()) }
    val isArabic = isArabicLocale()
    LazyColumn(
        contentPadding = PaddingValues(bottom = 28.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { LearningNotice(speakerReady = speaker.isReady) }
        item { SelectedLetterPanel(letter = selected, isArabic = isArabic, speaker = speaker) }
        item { SectionLabel(stringResource(R.string.noorani_choose_letter)) }
        items(NooraniContent.letters.chunked(4), key = { row -> row.first().id }) { row ->
            LetterRow(
                letters = row,
                selected = selected,
                onSelect = { selected = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun LearningNotice(speakerReady: Boolean) {
    val message = if (speakerReady) {
        stringResource(R.string.noorani_audio_ready)
    } else {
        stringResource(R.string.noorani_audio_unavailable)
    }
    NoticeCard(
        text = stringResource(R.string.noorani_intro),
        icon = Icons.Filled.Info,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    NoticeCard(
        text = message,
        icon = Icons.AutoMirrored.Filled.VolumeUp,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

@Composable
private fun SelectedLetterPanel(
    letter: ArabicLetter,
    isArabic: Boolean,
    speaker: ArabicSpeechController,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = letter.display,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = letter.name.resolve(isArabic),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            MakhrajVisual(group = letter.group, isArabic = isArabic)
            Text(
                text = letter.example.resolve(isArabic),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                enabled = speaker.isReady,
                onClick = { speaker.speak(letter.spokenArabic) },
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.noorani_listen))
            }
        }
    }
}

@Composable
private fun MakhrajVisual(group: MakhrajGroup, isArabic: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.size(84.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(color = outline, radius = size.minDimension * 0.43f, style = Stroke(width = 4f))
            val marker = groupMarker(group, size)
            drawCircle(color = primary, radius = size.minDimension * 0.11f, center = marker)
            drawLine(color = primary, start = center, end = marker, strokeWidth = 5f)
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = group.title.resolve(isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = group.cue.resolve(isArabic),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun groupMarker(group: MakhrajGroup, size: Size): Offset = when (group) {
    MakhrajGroup.THROAT -> Offset(size.width * 0.30f, size.height * 0.50f)
    MakhrajGroup.TONGUE -> Offset(size.width * 0.57f, size.height * 0.62f)
    MakhrajGroup.LIPS -> Offset(size.width * 0.82f, size.height * 0.50f)
    MakhrajGroup.OPEN_MOUTH -> Offset(size.width * 0.62f, size.height * 0.35f)
}

@Composable
private fun LetterRow(
    letters: List<ArabicLetter>,
    selected: ArabicLetter,
    onSelect: (ArabicLetter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        letters.forEach { letter ->
            val isSelected = letter.id == selected.id
            val color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .clickable { onSelect(letter) },
            ) {
                Text(
                    text = letter.display,
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        repeat(4 - letters.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun ReadingBasics(speaker: ArabicSpeechController) {
    val isArabic = isArabicLocale()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            NoticeCard(
                text = stringResource(R.string.noorani_reading_intro),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        items(NooraniContent.stages, key = { it.id }) { stage ->
            ReadingStageCard(stage = stage, isArabic = isArabic, speaker = speaker)
        }
    }
}

@Composable
private fun ReadingStageCard(
    stage: ReadingStage,
    isArabic: Boolean,
    speaker: ArabicSpeechController,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(stage.title.resolve(isArabic), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                stage.description.resolve(isArabic),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                stage.samples.forEach { sample ->
                    AssistChip(
                        enabled = speaker.isReady,
                        onClick = { speaker.speak(sample) },
                        label = { Text(sample, style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewMuslimCorner() {
    var language by remember { mutableStateOf(BeginnerLanguage.ARABIC) }
    val guide = NooraniContent.guide(language)
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { NewMuslimHeader() }
        item { LanguageChoices(selected = language, onSelect = { language = it }) }
        item { WelcomeCard(welcome = guide.welcome) }
        items(guide.steps.withIndex().toList(), key = { it.index }) { indexed ->
            GuideStepCard(index = indexed.index + 1, step = indexed.value)
        }
        item { ReviewNotice(text = guide.reviewNote) }
    }
}

@Composable
private fun NewMuslimHeader() {
    Column {
        Text(
            text = stringResource(R.string.new_muslim_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.new_muslim_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun LanguageChoices(selected: BeginnerLanguage, onSelect: (BeginnerLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BeginnerLanguage.entries.forEach { language ->
            AssistChip(
                onClick = { onSelect(language) },
                label = { Text(language.label) },
                leadingIcon = if (language == selected) {
                    { Icon(Icons.Filled.Translate, contentDescription = null) }
                } else {
                    null
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (language == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun WelcomeCard(welcome: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Text(
            text = welcome,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Composable
private fun GuideStepCard(index: Int, step: NewMuslimStep) {
    Card {
        Row(modifier = Modifier.padding(18.dp)) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 5.dp),
                )
                step.arabicPhrase?.let { phrase ->
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewNotice(text: String) {
    NoticeCard(
        text = text,
        icon = Icons.Filled.Info,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun NoticeCard(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    contentColor: Color,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun isArabicLocale(): Boolean = LocalConfiguration.current.locales[0]?.language == "ar"
