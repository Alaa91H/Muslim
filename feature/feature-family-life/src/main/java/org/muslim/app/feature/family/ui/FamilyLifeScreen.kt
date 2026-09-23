package org.muslim.app.feature.family.ui

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import org.muslim.app.core.ui.theme.IslamicCard
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimStateTone
import org.muslim.app.feature.family.R
import org.muslim.app.feature.family.domain.AqiqahCalculator
import org.muslim.app.feature.family.domain.AqiqahReminderDay
import org.muslim.app.feature.family.domain.BabyNameGender
import org.muslim.app.feature.family.domain.FamilyTopicCategory
import org.muslim.app.feature.family.domain.FamilyLifeContent
import org.muslim.app.feature.family.domain.IslamicBabyName
import org.muslim.app.feature.family.domain.LocalizedFamilyText
import org.muslim.app.feature.family.domain.RuqyahAudioTrack
import org.muslim.app.feature.family.domain.RuqyahPassage
import org.muslim.app.feature.family.domain.RuqyahSupplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class FamilySection {
    Home,
    Guide,
    Ruqyah,
    Names,
    Aqiqah,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyLifeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FamilyLifeViewModel = hiltViewModel(),
) {
    var sectionName by rememberSaveable { mutableStateOf(FamilySection.Home.name) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var articleId by rememberSaveable { mutableStateOf<String?>(null) }
    val section = FamilySection.entries.firstOrNull { it.name == sectionName } ?: FamilySection.Home
    val selectedCategory = categoryName?.let { saved ->
        FamilyTopicCategory.entries.firstOrNull { it.name == saved }
    }
    val selectedArticle = articleId?.let(FamilyLifeContent::articleById)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isArabic = AppLanguage.isArabicUi()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigate(target: FamilySection, category: FamilyTopicCategory? = null) {
        sectionName = target.name
        categoryName = category?.name
        articleId = null
    }

    fun navigateBack() {
        when {
            articleId != null -> articleId = null
            section != FamilySection.Home -> navigate(FamilySection.Home)
            else -> onBack()
        }
    }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FamilyLifeTopBar(
                title = selectedArticle?.title?.pick(isArabic) ?: section.title(),
                onBack = ::navigateBack,
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            IslamicDecorationBand(
                tint = MaterialTheme.colorScheme.tertiary,
                compact = true,
            )
            IslamicDecorationDivider(
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            when {
                selectedArticle != null -> FamilyArticleDetailContent(
                    article = selectedArticle,
                    isArabic = isArabic,
                )
                section == FamilySection.Home -> FamilyHubContent(
                    isArabic = isArabic,
                    onOpenCategory = { category -> navigate(FamilySection.Guide, category) },
                    onOpenRuqyah = { navigate(FamilySection.Ruqyah) },
                    onOpenNames = { navigate(FamilySection.Names) },
                    onOpenAqiqah = { navigate(FamilySection.Aqiqah) },
                )
                section == FamilySection.Guide -> FamilyGuideCatalogContent(
                    isArabic = isArabic,
                    initialCategory = selectedCategory,
                    onOpenArticle = { articleId = it },
                )
                section == FamilySection.Ruqyah -> RuqyahContent(
                    isArabic = isArabic,
                    onPlay = { url -> openAudio(context = context, url = url) },
                    onAudioFailure = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
                section == FamilySection.Names -> BabyNamesContent(isArabic = isArabic)
                section == FamilySection.Aqiqah -> AqiqahContent(
                    state = state,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyLifeTopBar(
    title: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.learn_back),
                )
            }
        },
    )
}

@Composable
private fun FamilySection.title(): String = when (this) {
    FamilySection.Home -> stringResource(R.string.family_life_title)
    FamilySection.Guide -> stringResource(R.string.family_guide_all_title)
    FamilySection.Ruqyah -> stringResource(R.string.family_tab_ruqyah)
    FamilySection.Names -> stringResource(R.string.family_tab_names)
    FamilySection.Aqiqah -> stringResource(R.string.family_tab_aqiqah)
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
        item { RuqyahIntroAndGuidance(isArabic) }
        item { FamilySectionHeading(stringResource(R.string.family_ruqyah_passages_title)) }
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
        item { FamilySectionHeading(stringResource(R.string.family_ruqyah_supplications_title)) }
        items(FamilyLifeContent.ruqyahSupplications, key = { it.id }) { supplication ->
            RuqyahSupplicationCard(supplication = supplication, isArabic = isArabic)
        }
        item { FamilySectionHeading(stringResource(R.string.family_ruqyah_audio_title)) }
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
private fun RuqyahIntroAndGuidance(isArabic: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FamilyIntroCard(
            icon = Icons.Filled.Security,
            title = stringResource(R.string.family_ruqyah_method_title),
            text = stringResource(R.string.family_ruqyah_method_intro),
        )
        NoticeCard(
            icon = Icons.Filled.Info,
            text = stringResource(R.string.family_ruqyah_health_notice),
        )
        FamilySectionHeading(stringResource(R.string.family_ruqyah_steps_title))
        FamilyLifeContent.ruqyahGuidance.forEach { guidance ->
            Text(
                text = guidance.pick(isArabic),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun FamilySectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun RuqyahPassageCard(
    passage: RuqyahPassage,
    isArabic: Boolean,
    onPlay: () -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
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
private fun RuqyahSupplicationCard(
    supplication: RuqyahSupplication,
    isArabic: Boolean,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = supplication.title.pick(isArabic),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = supplication.arabic,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = supplication.meaning.pick(isArabic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = supplication.reference.pick(isArabic),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AudioTrackCard(
    track: RuqyahAudioTrack,
    isArabic: Boolean,
    onPlay: () -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
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
                item {
                    MuslimStateSurface(
                        title = stringResource(R.string.family_names_empty),
                        tone = MuslimStateTone.Neutral,
                        icon = Icons.Filled.Search,
                    )
                }
            }
        }
    }
}

@Composable
private fun BabyNameCard(name: IslamicBabyName, isArabic: Boolean) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                name.origin?.let { origin ->
                    Text(
                        text = stringResource(R.string.family_name_origin, origin.pick(isArabic)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                name.note?.let { note ->
                    Text(
                        text = note.pick(isArabic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
    val daysUntil = birthDate?.let {
        AqiqahCalculator.daysUntil(it, state.aqiqahReminderDay, LocalDate.now())
    }
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
            AqiqahBirthDateEditor(
                value = birthDateText,
                parseError = parseError,
                onValueChange = {
                    birthDateText = it
                    parseError = false
                },
                onSave = {
                    if (birthDate == null) parseError = true else viewModel.setBirthDate(birthDate)
                },
            )
        }
        schedule?.let { calculated ->
            item { AqiqahDatesCard(schedule = calculated) }
        }
        item {
            AqiqahReminderDaySelector(
                selectedDay = state.aqiqahReminderDay,
                onSelected = viewModel::setAqiqahReminderDay,
            )
        }
        item {
            AqiqahReminderCard(
                birthDate = birthDate,
                reminderDay = state.aqiqahReminderDay,
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
private fun AqiqahBirthDateEditor(
    value: String,
    parseError: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DigitNormalizedOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
        IslamicSecondaryButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.family_aqiqah_apply))
        }
    }
}

@Composable
private fun AqiqahReminderDaySelector(
    selectedDay: AqiqahReminderDay,
    onSelected: (AqiqahReminderDay) -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.family_aqiqah_choose_reminder_day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AqiqahReminderDay.entries.forEach { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { onSelected(day) },
                    label = { Text(stringResource(R.string.family_aqiqah_day_number, day.offsetDays)) },
                )
            }
        }
    }
}

@Composable
private fun AqiqahReminderCard(
    birthDate: LocalDate?,
    reminderDay: AqiqahReminderDay,
    reminderAvailable: Boolean,
    reminderEnabled: Boolean,
    onToggleReminder: (Boolean) -> Unit,
) {
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.family_aqiqah_reminder), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (birthDate == null) stringResource(R.string.family_aqiqah_set_birth_first)
                    else if (reminderAvailable) {
                        stringResource(R.string.family_aqiqah_reminder_desc, reminderDay.offsetDays)
                    } else {
                        stringResource(R.string.family_aqiqah_selected_date_passed, reminderDay.offsetDays)
                    },
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
    schedule: org.muslim.app.feature.family.domain.AqiqahSchedule,
) {
    val dates = listOf(
        R.string.family_aqiqah_day_seven to schedule.seventhDay,
        R.string.family_aqiqah_day_fourteen to schedule.fourteenthDay,
        R.string.family_aqiqah_day_twenty_one to schedule.twentyFirstDay,
    )
    IslamicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
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
private fun FamilyIntroCard(icon: ImageVector, title: String, text: String) {
    MuslimStateSurface(
        title = title,
        supportingText = text,
        tone = MuslimStateTone.Positive,
        icon = icon,
    )
}

@Composable
private fun NoticeCard(icon: ImageVector, text: String) {
    MuslimStateSurface(
        title = text,
        tone = MuslimStateTone.Warning,
        icon = icon,
    )
}

private fun LocalizedFamilyText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun openAudio(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
