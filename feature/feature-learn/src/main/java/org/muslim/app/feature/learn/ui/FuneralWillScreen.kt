package org.muslim.app.feature.learn.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.FuneralContent
import org.muslim.app.feature.learn.domain.FuneralGuideSection
import org.muslim.app.feature.learn.domain.LocalizedFuneralText
import org.muslim.app.feature.learn.domain.WillDraft

private enum class FuneralWillTab(val icon: ImageVector) {
    Will(Icons.Filled.Notes),
    FuneralGuide(Icons.Filled.AutoStories),
}

private val WillDraftSaver: Saver<WillDraft, List<String>> = Saver(
    save = { draft ->
        listOf(
            draft.fullName,
            draft.executorName,
            draft.executorContact,
            draft.debtsAndRights,
            draft.funeralWishes,
            draft.guardianshipNotes,
            draft.charitableBequests,
            draft.additionalNotes,
        )
    },
    restore = { values ->
        if (values.size == 8) {
            WillDraft(
                fullName = values[0],
                executorName = values[1],
                executorContact = values[2],
                debtsAndRights = values[3],
                funeralWishes = values[4],
                guardianshipNotes = values[5],
                charitableBequests = values[6],
                additionalNotes = values[7],
            )
        } else {
            WillDraft()
        }
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuneralWillScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FuneralWillViewModel = hiltViewModel(),
) {
    val isArabic = AppLanguage.isArabicUi()
    val storedDraft by viewModel.draft.collectAsStateWithLifecycle()
    var draft by rememberSaveable(stateSaver = WillDraftSaver) { mutableStateOf(WillDraft()) }
    var selectedTab by rememberSaveable { mutableIntStateOf(FuneralWillTab.Will.ordinal) }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(storedDraft) {
        draft = storedDraft
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.funeral_will_clear_dialog_title)) },
            text = { Text(stringResource(R.string.funeral_will_clear_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        draft = WillDraft()
                        viewModel.clear()
                        showClearConfirmation = false
                    },
                ) { Text(stringResource(R.string.funeral_will_clear_confirm)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.funeral_will_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.funeral_will_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.funeral_will_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                FuneralWillTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = {
                            Text(
                                when (tab) {
                                    FuneralWillTab.Will -> stringResource(R.string.funeral_will_tab_will)
                                    FuneralWillTab.FuneralGuide -> stringResource(R.string.funeral_will_tab_guide)
                                },
                            )
                        },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
            when (FuneralWillTab.entries[selectedTab]) {
                FuneralWillTab.Will -> WillDraftContent(
                    draft = draft,
                    isArabic = isArabic,
                    onDraftChange = { draft = it },
                    onSave = { viewModel.save(draft) },
                    onShare = { shareWillDraft(context, draft, isArabic) },
                    onClear = { showClearConfirmation = true },
                )
                FuneralWillTab.FuneralGuide -> FuneralGuideContent(isArabic = isArabic)
            }
        }
    }
}

@Composable
private fun WillDraftContent(
    draft: WillDraft,
    isArabic: Boolean,
    onDraftChange: (WillDraft) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        willDraftIntroduction(isArabic)
        willDraftFields(draft, onDraftChange)
        willDraftActions(draft, onSave, onShare, onClear)
    }
}

private fun LazyListScope.willDraftIntroduction(isArabic: Boolean) {
    item {
        IntroCard(
            icon = Icons.Filled.Security,
            title = stringResource(R.string.funeral_will_draft_title),
            text = stringResource(R.string.funeral_will_draft_intro),
        )
    }
    item { NoticeCard(Icons.Filled.Info, stringResource(R.string.funeral_will_legal_notice)) }
    item { NoticeCard(Icons.Filled.Security, stringResource(R.string.funeral_will_privacy_notice)) }
    item {
        Text(
            text = stringResource(R.string.funeral_will_checklist_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    items(FuneralContent.willChecklist) { checklistItem -> ChecklistRow(checklistItem.pick(isArabic)) }
    item {
        Text(
            text = FuneralContent.willReferences.pick(isArabic),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    item {
        Text(
            text = stringResource(R.string.funeral_will_form_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun LazyListScope.willDraftFields(draft: WillDraft, onDraftChange: (WillDraft) -> Unit) {
    item { WillField(draft.fullName, { onDraftChange(draft.copy(fullName = it)) }, stringResource(R.string.funeral_will_full_name), singleLine = true) }
    item { WillField(draft.executorName, { onDraftChange(draft.copy(executorName = it)) }, stringResource(R.string.funeral_will_executor_name), stringResource(R.string.funeral_will_executor_name_hint), true) }
    item { WillField(draft.executorContact, { onDraftChange(draft.copy(executorContact = it)) }, stringResource(R.string.funeral_will_executor_contact), stringResource(R.string.funeral_will_executor_contact_hint), true) }
    item { WillField(draft.debtsAndRights, { onDraftChange(draft.copy(debtsAndRights = it)) }, stringResource(R.string.funeral_will_debts), stringResource(R.string.funeral_will_debts_hint)) }
    item { WillField(draft.funeralWishes, { onDraftChange(draft.copy(funeralWishes = it)) }, stringResource(R.string.funeral_will_funeral_wishes), stringResource(R.string.funeral_will_funeral_wishes_hint)) }
    item { WillField(draft.guardianshipNotes, { onDraftChange(draft.copy(guardianshipNotes = it)) }, stringResource(R.string.funeral_will_guardianship), stringResource(R.string.funeral_will_guardianship_hint)) }
    item { WillField(draft.charitableBequests, { onDraftChange(draft.copy(charitableBequests = it)) }, stringResource(R.string.funeral_will_charity), stringResource(R.string.funeral_will_charity_hint)) }
    item { WillField(draft.additionalNotes, { onDraftChange(draft.copy(additionalNotes = it)) }, stringResource(R.string.funeral_will_additional_notes)) }
}

private fun LazyListScope.willDraftActions(
    draft: WillDraft,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit,
) {
    item {
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_save))
        }
    }
    item {
        OutlinedButton(onClick = onShare, enabled = !draft.isEmpty(), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_share))
        }
    }
    item {
        OutlinedButton(onClick = onClear, enabled = !draft.isEmpty(), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.funeral_will_clear))
        }
    }
}

@Composable
private fun WillField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String? = null,
    singleLine: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FuneralGuideContent(isArabic: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            IntroCard(
                icon = Icons.Filled.HealthAndSafety,
                title = stringResource(R.string.funeral_will_guide_title),
                text = stringResource(R.string.funeral_will_guide_intro),
            )
        }
        item {
            NoticeCard(
                icon = Icons.Filled.Info,
                text = stringResource(R.string.funeral_will_guide_notice),
            )
        }
        items(FuneralContent.guideSections, key = { it.id }) { section ->
            FuneralGuideCard(section = section, isArabic = isArabic)
        }
    }
}

@Composable
private fun FuneralGuideCard(section: FuneralGuideSection, isArabic: Boolean) {
    var expanded by rememberSaveable(section.id) { mutableStateOf(false) }
    val icon = when (section.iconKey) {
        "care" -> Icons.Filled.FavoriteBorder
        "wash" -> Icons.Filled.WaterDrop
        "shroud" -> Icons.Filled.AutoStories
        "prayer" -> Icons.Filled.HealthAndSafety
        else -> Icons.Filled.FavoriteBorder
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(section.title.pick(isArabic), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(section.intro.pick(isArabic), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                section.steps.forEachIndexed { index, step ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = step.pick(isArabic),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = section.reference.pick(isArabic),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun IntroCard(icon: ImageVector, title: String, text: String) {
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

@Composable
private fun ChecklistRow(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Filled.Checklist,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

private fun LocalizedFuneralText.pick(isArabic: Boolean): String = if (isArabic) arabic else english

private fun shareWillDraft(context: Context, draft: WillDraft, isArabic: Boolean) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, draft.toShareText(isArabic))
    }
    context.startActivity(Intent.createChooser(intent, if (isArabic) "مشاركة مسودة الوصية" else "Share will draft"))
}
