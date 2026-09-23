package org.muslim.app.feature.learn.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.muslim.app.core.ui.theme.IslamicDecorationBand
import org.muslim.app.core.ui.theme.IslamicDecorationDivider
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.data.FuneralWillIntroVisibility
import org.muslim.app.feature.learn.domain.FuneralContent
import org.muslim.app.feature.learn.domain.FuneralGuideSection
import org.muslim.app.feature.learn.domain.LocalizedFuneralText
import org.muslim.app.feature.learn.domain.WillDraft
import org.muslim.app.feature.learn.domain.WillEducationSection

private enum class FuneralWillTab(val icon: ImageVector) {
    Will(Icons.AutoMirrored.Filled.Notes),
    FuneralGuide(Icons.Filled.AutoStories),
}

private data class WillIntroActions(
    val dismissDraftIntro: () -> Unit,
    val dismissLegalNotice: () -> Unit,
    val dismissPrivacyNotice: () -> Unit,
    val restoreAll: () -> Unit,
)

private data class WillDraftActions(
    val onChange: (WillDraft) -> Unit,
    val onSave: () -> Unit,
    val onShare: () -> Unit,
    val onExportPdf: () -> Unit,
    val onClear: () -> Unit,
)

private data class WillDraftIntroState(
    val visibility: FuneralWillIntroVisibility,
    val query: String,
    val onQueryChange: (String) -> Unit,
    val actions: WillIntroActions,
)

private val WillDraftSaver: Saver<WillDraft, List<String>> = Saver(
    save = { draft ->
        listOf(
            draft.fullName,
            draft.documentLocation,
            draft.executorName,
            draft.executorContact,
            draft.trustedContacts,
            draft.debtsAndRights,
            draft.assetsAndAccounts,
            draft.entrustedProperty,
            draft.digitalAccessInstructions,
            draft.funeralWishes,
            draft.guardianshipNotes,
            draft.charitableBequests,
            draft.lastReviewDate,
            draft.additionalNotes,
        )
    },
    restore = { values ->
        when (values.size) {
            14 -> WillDraft(
                fullName = values[0],
                documentLocation = values[1],
                executorName = values[2],
                executorContact = values[3],
                trustedContacts = values[4],
                debtsAndRights = values[5],
                assetsAndAccounts = values[6],
                entrustedProperty = values[7],
                digitalAccessInstructions = values[8],
                funeralWishes = values[9],
                guardianshipNotes = values[10],
                charitableBequests = values[11],
                lastReviewDate = values[12],
                additionalNotes = values[13],
            )

            8 -> WillDraft(
                fullName = values[0],
                executorName = values[1],
                executorContact = values[2],
                debtsAndRights = values[3],
                funeralWishes = values[4],
                guardianshipNotes = values[5],
                charitableBequests = values[6],
                additionalNotes = values[7],
            )

            else -> WillDraft()
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
    val storageError by viewModel.storageError.collectAsStateWithLifecycle()
    val pdfExportStatus by viewModel.pdfExportStatus.collectAsStateWithLifecycle()
    val introVisibility by viewModel.introVisibility.collectAsStateWithLifecycle()
    var draft by rememberSaveable(stateSaver = WillDraftSaver) { mutableStateOf(WillDraft()) }
    var selectedTab by rememberSaveable { mutableIntStateOf(FuneralWillTab.Will.ordinal) }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    var showShareConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val exportPdf = rememberPdfExportAction(
        viewModel = viewModel,
        draft = draft,
        isArabic = isArabic,
        status = pdfExportStatus,
    )

    LaunchedEffect(storedDraft) {
        draft = storedDraft
    }

    WillDraftDialogs(
        showShare = showShareConfirmation,
        showClear = showClearConfirmation,
        onDismissShare = { showShareConfirmation = false },
        onDismissClear = { showClearConfirmation = false },
        onConfirmShare = {
            shareWillDraft(context, draft, isArabic)
            showShareConfirmation = false
        },
        onConfirmClear = {
            draft = WillDraft()
            viewModel.clear()
            showClearConfirmation = false
        },
    )

    FuneralWillScaffold(
        modifier = modifier,
        onBack = onBack,
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
        draft = draft,
        storedDraft = storedDraft,
        isArabic = isArabic,
        storageError = storageError,
        introVisibility = introVisibility,
        onDraftChange = { draft = it },
        onSave = { viewModel.save(draft) },
        onShare = { showShareConfirmation = true },
        onExportPdf = exportPdf,
        onClear = { showClearConfirmation = true },
        viewModel = viewModel,
    )
}

@Composable
private fun rememberPdfExportAction(
    viewModel: FuneralWillViewModel,
    draft: WillDraft,
    isArabic: Boolean,
    status: WillPdfExportStatus,
): () -> Unit {
    val context = LocalContext.current
    val filename = stringResource(R.string.funeral_will_pdf_filename)
    val successMessage = stringResource(R.string.funeral_will_export_pdf_success)
    val errorMessage = stringResource(R.string.funeral_will_export_pdf_error)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destination ->
        if (destination != null) {
            viewModel.exportPdf(destination, draft, isArabic)
        }
    }

    LaunchedEffect(status) {
        val message = when (status) {
            WillPdfExportStatus.Success -> successMessage
            WillPdfExportStatus.Error -> errorMessage
            WillPdfExportStatus.Idle -> null
        }
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumePdfExportStatus()
        }
    }

    return { launcher.launch(filename) }
}

@Composable
private fun WillDraftDialogs(
    showShare: Boolean,
    showClear: Boolean,
    onDismissShare: () -> Unit,
    onDismissClear: () -> Unit,
    onConfirmShare: () -> Unit,
    onConfirmClear: () -> Unit,
) {
    if (showShare) {
        ConfirmationDialog(
            title = stringResource(R.string.funeral_will_share_dialog_title),
            text = stringResource(R.string.funeral_will_share_dialog_text),
            confirmText = stringResource(R.string.funeral_will_share_confirm),
            onConfirm = onConfirmShare,
            onDismiss = onDismissShare,
        )
    }
    if (showClear) {
        ConfirmationDialog(
            title = stringResource(R.string.funeral_will_clear_dialog_title),
            text = stringResource(R.string.funeral_will_clear_dialog_text),
            confirmText = stringResource(R.string.funeral_will_clear_confirm),
            onConfirm = onConfirmClear,
            onDismiss = onDismissClear,
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.funeral_will_cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuneralWillScaffold(
    modifier: Modifier,
    onBack: () -> Unit,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    draft: WillDraft,
    storedDraft: WillDraft,
    isArabic: Boolean,
    storageError: Boolean,
    introVisibility: FuneralWillIntroVisibility,
    onDraftChange: (WillDraft) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
    onClear: () -> Unit,
    viewModel: FuneralWillViewModel,
) {
    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.funeral_will_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.funeral_will_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        FuneralWillBody(
            modifier = Modifier.padding(innerPadding),
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
            draft = draft,
            storedDraft = storedDraft,
            isArabic = isArabic,
            storageError = storageError,
            introVisibility = introVisibility,
            onDraftChange = onDraftChange,
            onSave = onSave,
            onShare = onShare,
            onExportPdf = onExportPdf,
            onClear = onClear,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun FuneralWillBody(
    modifier: Modifier,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    draft: WillDraft,
    storedDraft: WillDraft,
    isArabic: Boolean,
    storageError: Boolean,
    introVisibility: FuneralWillIntroVisibility,
    onDraftChange: (WillDraft) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
    onClear: () -> Unit,
    viewModel: FuneralWillViewModel,
) {
    Column(modifier.fillMaxSize()) {
        IslamicDecorationBand(
            tint = MaterialTheme.colorScheme.tertiary,
            compact = true,
        )
        FuneralWillTabs(
            selectedTab = selectedTab,
            onSelect = onSelectTab,
        )
        IslamicDecorationDivider(
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        when (FuneralWillTab.entries[selectedTab]) {
            FuneralWillTab.Will -> WillDraftContent(
                draft = draft,
                isArabic = isArabic,
                introVisibility = introVisibility,
                isDirty = draft != storedDraft,
                storageError = storageError,
                draftActions = WillDraftActions(
                    onChange = onDraftChange,
                    onSave = onSave,
                    onShare = onShare,
                    onExportPdf = onExportPdf,
                    onClear = onClear,
                ),
                introActions = WillIntroActions(
                    dismissDraftIntro = viewModel::dismissDraftIntro,
                    dismissLegalNotice = viewModel::dismissLegalNotice,
                    dismissPrivacyNotice = viewModel::dismissPrivacyNotice,
                    restoreAll = viewModel::restoreIntroCards,
                ),
            )

            FuneralWillTab.FuneralGuide -> FuneralGuideContent(isArabic = isArabic)
        }
    }
}

@Composable
private fun FuneralWillTabs(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
) {
    PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
        FuneralWillTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab.ordinal,
                onClick = { onSelect(tab.ordinal) },
                text = {
                    Text(
                        when (tab) {
                            FuneralWillTab.Will -> stringResource(R.string.funeral_will_tab_will)
                            FuneralWillTab.FuneralGuide -> stringResource(R.string.funeral_will_tab_guide)
                        },
                    )
                },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun WillDraftContent(
    draft: WillDraft,
    isArabic: Boolean,
    introVisibility: FuneralWillIntroVisibility,
    isDirty: Boolean,
    storageError: Boolean,
    draftActions: WillDraftActions,
    introActions: WillIntroActions,
) {
    var educationQuery by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        willDraftIntroduction(
            isArabic = isArabic,
            state = WillDraftIntroState(
                visibility = introVisibility,
                query = educationQuery,
                onQueryChange = { educationQuery = it },
                actions = introActions,
            ),
        )
        item {
            NoticeCard(
                icon = Icons.Filled.Security,
                text = stringResource(
                    if (storageError) {
                        R.string.funeral_will_encryption_error
                    } else {
                        R.string.funeral_will_encrypted_notice
                    },
                ),
            )
        }
        if (!storageError) {
            willDraftFields(draft, draftActions.onChange)
            willDraftActions(
                draft = draft,
                isDirty = isDirty,
                onSave = draftActions.onSave,
                onShare = draftActions.onShare,
                onExportPdf = draftActions.onExportPdf,
                onClear = draftActions.onClear,
            )
        }
    }
}

private fun LazyListScope.willDraftIntroduction(
    isArabic: Boolean,
    state: WillDraftIntroState,
) {
    willIntroNotices(
        visibility = state.visibility,
        actions = state.actions,
    )
    willChecklist(isArabic)
    willEducation(
        isArabic = isArabic,
        query = state.query,
        onQueryChange = state.onQueryChange,
    )
    item {
        Text(
            text = stringResource(R.string.funeral_will_form_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun LazyListScope.willIntroNotices(
    visibility: FuneralWillIntroVisibility,
    actions: WillIntroActions,
) {
    if (visibility.draftIntroVisible) {
        item {
            IntroCard(
                icon = Icons.Filled.Security,
                title = stringResource(R.string.funeral_will_draft_title),
                text = stringResource(R.string.funeral_will_draft_intro),
                onDismiss = actions.dismissDraftIntro,
            )
        }
    }
    if (visibility.legalNoticeVisible) {
        item {
            NoticeCard(
                icon = Icons.Filled.Info,
                text = stringResource(R.string.funeral_will_legal_notice),
                onDismiss = actions.dismissLegalNotice,
            )
        }
    }
    if (visibility.privacyNoticeVisible) {
        item {
            NoticeCard(
                icon = Icons.Filled.Security,
                text = stringResource(R.string.funeral_will_privacy_notice),
                onDismiss = actions.dismissPrivacyNotice,
            )
        }
    }
    if (visibility.hasDismissedCards) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = actions.restoreAll) {
                    Text(stringResource(R.string.funeral_will_restore_intro_cards))
                }
            }
        }
    }
}

private fun LazyListScope.willChecklist(isArabic: Boolean) {
    item {
        Text(
            text = stringResource(R.string.funeral_will_checklist_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    items(FuneralContent.willChecklist) { checklistItem ->
        ChecklistRow(checklistItem.pick(isArabic))
    }
    item {
        Text(
            text = FuneralContent.willReferences.pick(isArabic),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun LazyListScope.willEducation(
    isArabic: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    item {
        Column {
            Text(
                text = stringResource(R.string.funeral_will_education_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.funeral_will_education_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    item {
        FuneralWillSearchField(
            query = query,
            onQueryChange = onQueryChange,
        )
    }
    item {
        TopicIndexRow(
            labels = FuneralContent.willEducationSections.map {
                it.title.pick(isArabic)
            },
            onSelect = onQueryChange,
        )
    }

    val matchingEducation = FuneralContent.searchWillEducationSections(
        query = query,
        isArabic = isArabic,
    )
    if (query.isNotBlank()) {
        item {
            SearchResultSummary(count = matchingEducation.size)
        }
    }
    if (matchingEducation.isEmpty()) {
        item {
            SearchEmptyState()
        }
    } else {
        items(matchingEducation, key = { it.id }) { section ->
            WillEducationCard(section = section, isArabic = isArabic)
        }
    }
}

private fun LazyListScope.willDraftFields(
    draft: WillDraft,
    onDraftChange: (WillDraft) -> Unit,
) {
    willIdentityFields(draft, onDraftChange)
    willFinancialFields(draft, onDraftChange)
    willFamilyFields(draft, onDraftChange)
    willReviewFields(draft, onDraftChange)
}

private fun LazyListScope.willIdentityFields(
    draft: WillDraft,
    onDraftChange: (WillDraft) -> Unit,
) {
    item {
        WillFormSection(
            id = "identity",
            title = stringResource(R.string.funeral_will_section_identity),
            initiallyExpanded = true,
        ) {
            WillField(
                value = draft.fullName,
                onValueChange = { onDraftChange(draft.copy(fullName = it)) },
                label = stringResource(R.string.funeral_will_full_name),
                singleLine = true,
            )
            WillField(
                value = draft.documentLocation,
                onValueChange = { onDraftChange(draft.copy(documentLocation = it)) },
                label = stringResource(R.string.funeral_will_document_location),
                supportingText = stringResource(R.string.funeral_will_document_location_hint),
            )
            WillField(
                value = draft.executorName,
                onValueChange = { onDraftChange(draft.copy(executorName = it)) },
                label = stringResource(R.string.funeral_will_executor_name),
                supportingText = stringResource(R.string.funeral_will_executor_name_hint),
                singleLine = true,
            )
            WillField(
                value = draft.executorContact,
                onValueChange = { onDraftChange(draft.copy(executorContact = it)) },
                label = stringResource(R.string.funeral_will_executor_contact),
                supportingText = stringResource(R.string.funeral_will_executor_contact_hint),
                singleLine = true,
            )
            WillField(
                value = draft.trustedContacts,
                onValueChange = { onDraftChange(draft.copy(trustedContacts = it)) },
                label = stringResource(R.string.funeral_will_trusted_contacts),
                supportingText = stringResource(R.string.funeral_will_trusted_contacts_hint),
            )
        }
    }
}

private fun LazyListScope.willFinancialFields(
    draft: WillDraft,
    onDraftChange: (WillDraft) -> Unit,
) {
    item {
        WillFormSection(
            id = "financial",
            title = stringResource(R.string.funeral_will_section_financial),
        ) {
            WillField(
                value = draft.debtsAndRights,
                onValueChange = { onDraftChange(draft.copy(debtsAndRights = it)) },
                label = stringResource(R.string.funeral_will_debts),
                supportingText = stringResource(R.string.funeral_will_debts_hint),
            )
            WillField(
                value = draft.assetsAndAccounts,
                onValueChange = { onDraftChange(draft.copy(assetsAndAccounts = it)) },
                label = stringResource(R.string.funeral_will_assets_accounts),
                supportingText = stringResource(R.string.funeral_will_assets_accounts_hint),
            )
            WillField(
                value = draft.entrustedProperty,
                onValueChange = { onDraftChange(draft.copy(entrustedProperty = it)) },
                label = stringResource(R.string.funeral_will_entrusted_property),
                supportingText = stringResource(R.string.funeral_will_entrusted_property_hint),
            )
            NoticeCard(
                icon = Icons.Filled.Security,
                text = stringResource(R.string.funeral_will_sensitive_data_notice),
            )
            WillField(
                value = draft.digitalAccessInstructions,
                onValueChange = { onDraftChange(draft.copy(digitalAccessInstructions = it)) },
                label = stringResource(R.string.funeral_will_digital_access),
                supportingText = stringResource(R.string.funeral_will_digital_access_hint),
            )
        }
    }
}

private fun LazyListScope.willFamilyFields(
    draft: WillDraft,
    onDraftChange: (WillDraft) -> Unit,
) {
    item {
        WillFormSection(
            id = "family",
            title = stringResource(R.string.funeral_will_section_family),
        ) {
            WillField(
                value = draft.funeralWishes,
                onValueChange = { onDraftChange(draft.copy(funeralWishes = it)) },
                label = stringResource(R.string.funeral_will_funeral_wishes),
                supportingText = stringResource(R.string.funeral_will_funeral_wishes_hint),
            )
            WillField(
                value = draft.guardianshipNotes,
                onValueChange = { onDraftChange(draft.copy(guardianshipNotes = it)) },
                label = stringResource(R.string.funeral_will_guardianship),
                supportingText = stringResource(R.string.funeral_will_guardianship_hint),
            )
            WillField(
                value = draft.charitableBequests,
                onValueChange = { onDraftChange(draft.copy(charitableBequests = it)) },
                label = stringResource(R.string.funeral_will_charity),
                supportingText = stringResource(R.string.funeral_will_charity_hint),
            )
        }
    }
}

private fun LazyListScope.willReviewFields(
    draft: WillDraft,
    onDraftChange: (WillDraft) -> Unit,
) {
    item {
        WillFormSection(
            id = "review",
            title = stringResource(R.string.funeral_will_section_review),
        ) {
            WillField(
                value = draft.lastReviewDate,
                onValueChange = { onDraftChange(draft.copy(lastReviewDate = it)) },
                label = stringResource(R.string.funeral_will_last_review),
                supportingText = stringResource(R.string.funeral_will_last_review_hint),
                singleLine = true,
            )
            WillField(
                value = draft.additionalNotes,
                onValueChange = { onDraftChange(draft.copy(additionalNotes = it)) },
                label = stringResource(R.string.funeral_will_additional_notes),
            )
        }
    }
}

private fun LazyListScope.willDraftActions(
    draft: WillDraft,
    isDirty: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
    onClear: () -> Unit,
) {
    willDraftSaveState(draft, isDirty)
    item {
        WillDraftActionButton(
            label = stringResource(R.string.funeral_will_save),
            icon = Icons.Filled.Save,
            enabled = isDirty,
            primary = true,
            onClick = onSave,
        )
    }
    item {
        WillDraftActionButton(
            label = stringResource(R.string.funeral_will_share),
            icon = Icons.Filled.Share,
            enabled = !draft.isEmpty(),
            onClick = onShare,
        )
    }
    item {
        WillDraftActionButton(
            label = stringResource(R.string.funeral_will_export_pdf),
            icon = Icons.Filled.PictureAsPdf,
            enabled = !draft.isEmpty(),
            onClick = onExportPdf,
        )
    }
    item {
        WillDraftActionButton(
            label = stringResource(R.string.funeral_will_clear),
            icon = Icons.Filled.DeleteOutline,
            enabled = !draft.isEmpty(),
            onClick = onClear,
        )
    }
}

private fun LazyListScope.willDraftSaveState(
    draft: WillDraft,
    isDirty: Boolean,
) {
    if (!draft.isEmpty()) {
        item {
            Text(
                text = stringResource(
                    if (isDirty) {
                        R.string.funeral_will_unsaved_changes
                    } else {
                        R.string.funeral_will_saved_locally
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDirty) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WillDraftActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    if (primary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            WillDraftActionButtonContent(label, icon)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            WillDraftActionButtonContent(label, icon)
        }
    }
}

@Composable
private fun WillDraftActionButtonContent(
    label: String,
    icon: ImageVector,
) {
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
    Spacer(Modifier.width(8.dp))
    Text(label)
}

@Composable
private fun FuneralWillSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.funeral_will_search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.funeral_will_search_clear),
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TopicIndexRow(
    labels: List<String>,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.funeral_will_quick_topics),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp),
        ) {
            items(labels) { label ->
                AssistChip(
                    onClick = { onSelect(label) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultSummary(count: Int) {
    Text(
        text = stringResource(R.string.funeral_will_search_results, count),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SearchEmptyState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.funeral_will_search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun WillFormSection(
    id: String,
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable("will_form_" + id) {
        mutableStateOf(initiallyExpanded)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = null,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    content()
                }
            }
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
    var query by rememberSaveable { mutableStateOf("") }
    val matchingSections = FuneralContent.searchGuideSections(
        query = query,
        isArabic = isArabic,
    )

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
        item {
            FuneralWillSearchField(
                query = query,
                onQueryChange = { query = it },
            )
        }
        item {
            TopicIndexRow(
                labels = FuneralContent.guideSections.map {
                    it.title.pick(isArabic)
                },
                onSelect = { query = it },
            )
        }
        if (query.isNotBlank()) {
            item {
                SearchResultSummary(count = matchingSections.size)
            }
        }
        item {
            Column {
                Text(
                    text = stringResource(R.string.funeral_will_guide_quick_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.funeral_will_guide_quick_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(FuneralContent.quickActionSteps) { quickStep ->
            ChecklistRow(quickStep.pick(isArabic))
        }
        if (matchingSections.isEmpty()) {
            item {
                SearchEmptyState()
            }
        } else {
            items(matchingSections, key = { it.id }) { section ->
                FuneralGuideCard(section = section, isArabic = isArabic)
            }
        }
    }
}

@Composable
private fun FuneralGuideCard(
    section: FuneralGuideSection,
    isArabic: Boolean,
) {
    val icon = when (section.iconKey) {
        "care", "support" -> Icons.Filled.FavoriteBorder
        "wash" -> Icons.Filled.WaterDrop
        "shroud", "burial" -> Icons.Filled.AutoStories
        "prayer" -> Icons.Filled.HealthAndSafety
        "documents" -> Icons.Filled.Checklist
        else -> Icons.Filled.AutoStories
    }
    GuidanceCard(
        id = section.id,
        icon = icon,
        title = section.title.pick(isArabic),
        intro = section.intro.pick(isArabic),
        steps = section.steps.map { it.pick(isArabic) },
        reference = section.reference.pick(isArabic),
        isArabic = isArabic,
    )
}

@Composable
private fun WillEducationCard(
    section: WillEducationSection,
    isArabic: Boolean,
) {
    GuidanceCard(
        id = "will_" + section.id,
        icon = Icons.AutoMirrored.Filled.Notes,
        title = section.title.pick(isArabic),
        intro = section.intro.pick(isArabic),
        steps = section.steps.map { it.pick(isArabic) },
        reference = section.reference.pick(isArabic),
        isArabic = isArabic,
    )
}

@Composable
private fun GuidanceCard(
    id: String,
    icon: ImageVector,
    title: String,
    intro: String,
    steps: List<String>,
    reference: String,
    isArabic: Boolean,
) {
    var expanded by rememberSaveable(id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = intro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = null,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
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
                            text = step,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reference,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun IntroCard(
    icon: ImageVector,
    title: String,
    text: String,
    onDismiss: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.funeral_will_dismiss_card),
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(
    icon: ImageVector,
    text: String,
    onDismiss: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.funeral_will_dismiss_card),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Filled.Checklist,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun LocalizedFuneralText.pick(isArabic: Boolean): String =
    if (isArabic) arabic else english

private fun shareWillDraft(
    context: Context,
    draft: WillDraft,
    isArabic: Boolean,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, draft.toShareText(isArabic))
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            if (isArabic) "مشاركة مسودة الوصية" else "Share will draft",
        ),
    )
}
