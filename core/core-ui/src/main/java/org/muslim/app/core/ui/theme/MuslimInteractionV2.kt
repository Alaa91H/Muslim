@file:Suppress("LongParameterList")

package org.muslim.app.core.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.designsystem.MuslimSemanticTypography

/** Short option set for a single-choice segmented decision. */
@Composable
fun MuslimSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.ControlGap),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelectedIndexChange(index) },
                label = { Text(label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Shared search field with one predictable clear affordance. */
@Composable
fun MuslimSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = clearContentDescription,
                    )
                }
            }
        } else {
            null
        },
    )
}

data class MuslimFilterOption(
    val id: String,
    val label: String,
)

/** Horizontally scrollable filter row for longer optional filter sets. */
@Composable
fun MuslimFilterBar(
    options: List<MuslimFilterOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.ControlGap),
    ) {
        items(options, key = { it.id }) { option ->
            FilterChip(
                selected = option.id in selectedIds,
                onClick = { onToggle(option.id) },
                label = { Text(option.label) },
            )
        }
    }
}

/** Shared modal sheet for secondary or advanced configuration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuslimBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = IslamicSpacing.PageHorizontal,
                    end = IslamicSpacing.PageHorizontal,
                    bottom = IslamicSpacing.Large,
                ),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
        ) {
            title?.takeIf(String::isNotBlank)?.let { text ->
                Text(
                    text = text,
                    style = MuslimSemanticTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

data class MuslimActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/** Contextual action list used instead of permanently exposing secondary actions. */
@Composable
fun MuslimActionSheet(
    actions: List<MuslimActionItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    MuslimBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        title = title,
    ) {
        actions.forEach { action ->
            MuslimSettingsItem(
                title = action.label,
                icon = action.icon,
                enabled = action.enabled,
                onClick = {
                    onDismiss()
                    action.onClick()
                },
            )
        }
    }
}

data class MuslimMenuAction(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/** Standard overflow trigger and menu for low-frequency screen actions. */
@Composable
fun MuslimOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    contentDescription: String,
    actions: List<MuslimMenuAction>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = contentDescription,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    leadingIcon = action.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = action.enabled,
                    onClick = {
                        onExpandedChange(false)
                        action.onClick()
                    },
                )
            }
        }
    }
}
