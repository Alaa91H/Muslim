package org.muslim.app.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import org.muslim.app.R
import org.muslim.app.core.datastore.AppPreferences

/**
 * Lets the user customize the "More" hub: reorder its sections by drag & drop
 * and show/hide each section with a switch. Both are persisted in DataStore
 * and applied live by [MoreScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOrderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoreOrderViewModel = hiltViewModel(),
) {
    val order by viewModel.sectionOrder.collectAsStateWithLifecycle()
    val hidden by viewModel.hiddenSections.collectAsStateWithLifecycle()

    // Local reorderable copy, resynced whenever the persisted order changes
    // (e.g. after reset). Drag & drop mutates only this local list until the
    // drag ends, at which point it is persisted.
    val sections = remember { mutableStateListOf<String>() }
    LaunchedEffect(order) {
        if (sections.toList() != order) {
            sections.clear()
            sections.addAll(order)
        }
    }

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var dragTargetIndex by remember { mutableIntStateOf(-1) }
    val rowHeightPx = with(LocalDensity.current) { ROW_HEIGHT.toPx() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_order_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.more_order_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.more_order_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            sections.forEachIndexed { index, id ->
                key(id) {
                    val isDragging = draggingId == id
                    val translationY = if (isDragging && dragStartIndex >= 0) {
                        dragOffsetY - (dragTargetIndex - dragStartIndex) * rowHeightPx
                    } else {
                        0f
                    }
                    SectionRow(
                        title = stringResource(sectionTitleRes(id)),
                        shown = id !in hidden,
                        isDragging = isDragging,
                        translationY = translationY,
                        onToggle = { shown -> viewModel.setSectionHidden(id, !shown) },
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                this.translationY = translationY
                                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                            }
                            .pointerInput(id) {
                                val rowPx = ROW_HEIGHT.toPx()
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingId = id
                                        dragStartIndex = sections.indexOf(id)
                                        dragTargetIndex = dragStartIndex
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffsetY += amount.y
                                        val target = (dragStartIndex + (dragOffsetY / rowPx).roundToInt())
                                            .coerceIn(0, sections.lastIndex)
                                        val current = sections.indexOf(id)
                                        if (target != current && target in sections.indices) {
                                            sections.move(current, target)
                                        }
                                        dragTargetIndex = target
                                    },
                                    onDragEnd = {
                                        viewModel.setOrder(sections.toList())
                                        draggingId = null
                                        dragStartIndex = -1
                                        dragOffsetY = 0f
                                        dragTargetIndex = -1
                                    },
                                    onDragCancel = {
                                        viewModel.setOrder(sections.toList())
                                        draggingId = null
                                        dragStartIndex = -1
                                        dragOffsetY = 0f
                                        dragTargetIndex = -1
                                    },
                                )
                            },
                    )
                }
            }
            OutlinedButton(
                onClick = viewModel::reset,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.more_order_reset))
            }
        }
    }
}

@Composable
private fun SectionRow(
    title: String,
    shown: Boolean,
    isDragging: Boolean,
    translationY: Float,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isDragging) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isDragging) 4.dp else 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .height(ROW_HEIGHT)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (shown) 1f else 0.45f),
            )
            Switch(
                checked = shown,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to || from !in indices || to !in indices) return
    val item = removeAt(from)
    add(to, item)
}

/** Maps a persisted section id back to its display string resource. */
private fun sectionTitleRes(sectionId: String): Int = when (sectionId) {
    AppPreferences.MORE_SECTION_WORSHIP -> R.string.more_section_worship
    AppPreferences.MORE_SECTION_KNOWLEDGE -> R.string.more_section_knowledge
    AppPreferences.MORE_SECTION_TOOLS -> R.string.more_section_tools
    AppPreferences.MORE_SECTION_APP -> R.string.more_section_app
    else -> R.string.more_section_worship
}

private val ROW_HEIGHT = 64.dp
