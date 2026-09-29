@file:Suppress("LongParameterList")

package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import org.muslim.app.core.designsystem.IslamicContentAlpha
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.designsystem.IslamicStroke
import org.muslim.app.core.designsystem.MuslimSemanticTypography
import org.muslim.app.core.designsystem.MuslimTouchTarget

/** Standard progress heading for learning, downloads and multi-step tasks. */
@Composable
fun MuslimProgressHeader(
    title: String,
    progress: Float,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        Text(text = title, style = MuslimSemanticTypography.SectionTitle)
        supportingText?.takeIf(String::isNotBlank)?.let { text ->
            Text(
                text = text,
                style = MuslimSemanticTypography.Supporting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { boundedProgress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** One prominent value with a concise label and optional supporting context. */
@Composable
fun MuslimMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XXSmall),
    ) {
        Text(
            text = label,
            style = MuslimSemanticTypography.Metadata,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MuslimSemanticTypography.Hero,
            color = MaterialTheme.colorScheme.onSurface,
        )
        supportingText?.takeIf(String::isNotBlank)?.let { text ->
            Text(
                text = text,
                style = MuslimSemanticTypography.Supporting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Consistent progressive-disclosure surface for advanced or infrequent controls. */
@Composable
fun MuslimExpandableSection(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(IslamicStroke.Standard, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = MuslimTouchTarget.Min)
                    .clickable { onExpandedChange(!expanded) }
                    .padding(
                        horizontal = IslamicSpacing.Medium,
                        vertical = IslamicSpacing.Compact,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XXSmall),
                ) {
                    Text(text = title, style = MuslimSemanticTypography.ItemTitle)
                    supportingText?.takeIf(String::isNotBlank)?.let { text ->
                        Text(
                            text = text,
                            style = MuslimSemanticTypography.Supporting,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = IslamicSpacing.Medium,
                        end = IslamicSpacing.Medium,
                        bottom = IslamicSpacing.Medium,
                    ),
                    verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
                    content = content,
                )
            }
        }
    }
}

/**
 * Non-animated placeholder block.
 *
 * Animation is intentionally omitted so skeletons remain calm and automatically
 * respect reduced-motion intent without requiring per-screen shimmer logic.
 */
@Composable
fun MuslimSkeleton(
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
            alpha = IslamicContentAlpha.Secondary,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth())
    }
}
