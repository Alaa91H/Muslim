package org.muslim.app.feature.learn.ui

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.ui.text.DigitNormalizedOutlinedTextField
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.AqiqahCalculator
import org.muslim.app.feature.learn.domain.BabyNameGender
import org.muslim.app.feature.learn.domain.FamilyGuideArticle
import org.muslim.app.feature.learn.domain.FamilyLifeContent
import org.muslim.app.feature.learn.domain.IslamicBabyName
import org.muslim.app.feature.learn.domain.LocalizedFamilyText
import org.muslim.app.feature.learn.domain.RuqyahAudioTrack
import org.muslim.app.feature.learn.domain.RuqyahPassage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class FamilyTab(val icon: ImageVector) {
    Ruqyah(Icons.Filled.HealthAndSafety),
    Names(Icons.Filled.ChildCare),
    Aqiqah(Icons.Filled.DateRange),
    Marriage(Icons.Filled.FamilyRestroom),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyLifeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FamilyLifeViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(FamilyTab.Ruqyah.ordinal) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isArabic = AppLanguage.isArabicUi()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_life_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.learn_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
            ) {
                FamilyTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = {
                            Text(
                                when (tab) {
                                    FamilyTab.Ruqyah -> stringResource(R.string.family_tab_ruqyah)
                                    FamilyTab.Names -> stringResource(R.string.family_tab_names)
                                    FamilyTab.Aqiqah -> stringResource(R.string.family_tab_aqiqah)
                                    FamilyTab.Marriage -> stringResource(R.string.family_tab_marriage)
                                },
                            )
                        },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
            when (FamilyTab.entries[selectedTab]) {
                FamilyTab.Ruqyah -> RuqyahContent(
                    isArabic = isArabic,
                    onPlay = { url -> openAudio(context = context, url = url) },
                    onAudioFailure = {
                        scope.launch { snackbarHostState.showSnackbar(it) }
                    },
                )
                FamilyTab.Names -> BabyNamesContent(isArabic = isArabic)
                FamilyTab.Aqiqah -> AqiqahContent(
                    state = state,
                    viewModel = viewModel,
                )
                FamilyTab.Marriage -> MarriageContent(isArabic = isArabic)
            }
        }
    }
}

@Composable
private fun RuqyahContent(
    isArabic: Boolean,
    onPlay: (String) -> Unit,
    onAudioFailure: (String) -> Unit,
) {
    val audioUnavailableMessage = stringResource(R.string.family_audio_unavailable)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FamilyIntroCard(
                icon = Icons.Filled.Security,
                title = stringResource(R.string.family_ruqyah_method_title),
                text = stringResource(R.string.family_ruqyah_method_intro),
            )
        }
        item {
            NoticeCard(
                icon = Icons.Filled.Info,
                text = stringResource(R.string.family_ruqyah_health_notice),
            )
        }
        item {
            Text(
                text = stringResource(R.string.family_ruqyah_steps_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        items(FamilyLifeContent.ruqyahGuidance) { guidance ->
            Text(
                text = guidance.pick(isArabic),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.family_ruqyah_passages_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        items(FamilyLifeContent.ruqyahPassages, key = { it.id }) { passage ->
            RuqyahPassageCard(
                passage = passage,
                isArabic = isArabic,
                onPlay = {
                    if (FamilyLifeContent.isSafeAudioUrl(passage.audioUrl)) onPlay(passage.audioUrl)
                    else onAudioFailure(audioUnavailableMessage)
                },
            )
        }
        item {
            Text(
                text = stringResource(R.string.family_ruqyah_audio_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        items(FamilyLifeContent.ruqyahAudio, key = { it.id }) { track ->
            AudioTrackCard(
                track = track,
                isArabic = isArabic,
                onPlay = {
                    if (FamilyLifeContent.isSafeAudioUrl(track.url)) onPlay(track.url)
                    else onAudioFailure(audioUnavailableMessage)
                },
            )
        }
    }
}

@Composable
private fun RuqyahPassageCard(
    passage: RuqyahPassage,
    isArabic: Boolean,
    onPlay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = passage.title.pick(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onPlay) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = stringResource(R.string.family_audio_play))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = passage.text.pick(isArabic),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = passage.reference.pick(isArabic),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AudioTrackCard(
    track: RuqyahAudioTrack,
    isArabic: Boolean,
    onPlay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(9.dp).size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title.pick(isArabic), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(track.description.pick(isArabic), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onPlay) { Text(stringResource(R.string.family_audio_play)) }
        }
    }
}

@Composable
private fun BabyNamesContent(isArabic: Boolean) {
    var query by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf<BabyNameGender?>(null) }
    val results = remember(query, gender) { FamilyLifeContent.searchNames(query, gender) }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FamilyIntroCard(
                    icon = Icons.Filled.ChildCare,
                    title = stringResource(R.string.family_names_title),
                    text = stringResource(R.string.family_names_intro),
                )
            }
            item {
                DigitNormalizedOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.family_names_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == null,
                        onClick = { gender = null },
                        label = { Text(stringResource(R.string.family_names_all)) },
                    )
                    FilterChip(
                        selected = gender == BabyNameGender.Boy,
                        onClick = { gender = BabyNameGender.Boy },
                        label = { Text(stringResource(R.string.family_names_boys)) },
                    )
                    FilterChip(
                        selected = gender == BabyNameGender.Girl,
                        onClick = { gender = BabyNameGender.Girl },
                        label = { Text(stringResource(R.string.family_names_girls)) },
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.family_names_count, results.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(results, key = { it.id }) { name -> BabyNameCard(name, isArabic) }
            if (results.isEmpty()) {
                item { Text(stringResource(R.string.family_names_empty), modifier = Modifier.padding(20.dp)) }
            }
        }
    }
}

@Composable
private fun BabyNameCard(name: IslamicBabyName, isArabic: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = name.nameArabic.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name.nameArabic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(name.transliteration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (isArabic) name.meaningArabic else name.meaningEnglish,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AqiqahContent(
    state: FamilyLifeUiState,
    viewModel: FamilyLifeViewModel,
) {
    var birthDateText by rememberSaveable { mutableStateOf(state.birthDate?.toString().orEmpty()) }
    var parseError by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.birthDate) {
        if (state.birthDate != null) birthDateText = state.birthDate.toString()
    }
    val birthDate = birthDateText.trim().let { text ->
        if (text.isEmpty()) null else runCatching { LocalDate.parse(text) }.getOrNull()
    }
    val schedule = birthDate?.let(AqiqahCalculator::schedule)
    val daysUntil = birthDate?.let { AqiqahCalculator.daysUntilFirst(it, LocalDate.now()) }
    val reminderAvailable = daysUntil != null && daysUntil >= 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FamilyIntroCard(
                icon = Icons.Filled.DateRange,
                title = stringResource(R.string.family_aqiqah_title),
                text = stringResource(R.string.family_aqiqah_intro),
            )
        }
        item {
            DigitNormalizedOutlinedTextField(
                value = birthDateText,
                onValueChange = {
                    birthDateText = it
                    parseError = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.family_aqiqah_birth_date)) },
                placeholder = { Text(stringResource(R.string.family_aqiqah_date_hint)) },
                isError = parseError,
                supportingText = if (parseError) {
                    { Text(stringResource(R.string.family_aqiqah_invalid_date)) }
                } else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
        item {
            OutlinedButton(
                onClick = {
                    if (birthDate == null) parseError = true else viewModel.setBirthDate(birthDate)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.family_aqiqah_apply))
            }
        }
        if (schedule != null) {
            item {
                AqiqahDatesCard(schedule = schedule)
            }
        }
        item {
            AqiqahReminderCard(
                birthDate = birthDate,
                reminderAvailable = reminderAvailable,
                reminderEnabled = state.aqiqahReminderEnabled,
                onToggleReminder = { viewModel.setAqiqahReminderEnabled(it) },
            )
        }
        item {
            NoticeCard(
                icon = Icons.Filled.Info,
                text = stringResource(R.string.family_aqiqah_fiqh_note),
            )
        }
    }
}

@Composable
private fun AqiqahReminderCard(
    birthDate: LocalDate?,
    reminderAvailable: Boolean,
    reminderEnabled: Boolean,
    onToggleReminder: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.family_aqiqah_reminder), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (birthDate == null) stringResource(R.string.family_aqiqah_set_birth_first)
                    else if (reminderAvailable) stringResource(R.string.family_aqiqah_reminder_desc)
                    else stringResource(R.string.family_aqiqah_date_passed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = reminderEnabled,
                enabled = reminderAvailable,
                onCheckedChange = onToggleReminder,
            )
        }
    }
}

@Composable
private fun AqiqahDatesCard(
    schedule: org.muslim.app.feature.learn.domain.AqiqahSchedule,
) {
    val dates = listOf(
        R.string.family_aqiqah_day_seven to schedule.seventhDay,
        R.string.family_aqiqah_day_fourteen to schedule.fourteenthDay,
        R.string.family_aqiqah_day_twenty_one to schedule.twentyFirstDay,
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.family_aqiqah_dates_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            dates.forEach { (labelRes, date) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(labelRes), modifier = Modifier.weight(1f))
                    Text(date.format(DateTimeFormatter.ISO_LOCAL_DATE), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MarriageContent(isArabic: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            FamilyIntroCard(
                icon = Icons.Filled.FamilyRestroom,
                title = stringResource(R.string.family_marriage_title),
                text = stringResource(R.string.family_marriage_intro),
            )
        }
        items(FamilyLifeContent.familyArticles, key = { it.id }) { article ->
            FamilyArticleCard(article, isArabic)
        }
    }
}

@Composable
private fun FamilyArticleCard(article: FamilyGuideArticle, isArabic: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(article.title.pick(isArabic), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(article.summary.pick(isArabic), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            article.sections.forEach { section ->
                Spacer(Modifier.height(12.dp))
                Text(section.title.pick(isArabic), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                section.paragraphs.forEach { paragraph ->
                    Spacer(Modifier.height(6.dp))
                    Text(paragraph.pick(isArabic), style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight)
                }
            }
        }
    }
}

@Composable
private fun FamilyIntroCard(icon: ImageVector, title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun NoticeCard(icon: ImageVector, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun LocalizedFamilyText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun openAudio(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
