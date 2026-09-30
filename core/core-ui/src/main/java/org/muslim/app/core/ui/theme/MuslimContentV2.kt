@file:Suppress("LongParameterList")

package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import org.muslim.app.core.designsystem.IslamicContentAlpha
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicRadius
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.designsystem.IslamicStroke
import org.muslim.app.core.designsystem.MuslimSemanticTypography
import org.muslim.app.core.designsystem.MuslimTouchTarget

/** Dominant information surface for one current task or state. */
@Composable
fun MuslimHero(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(IslamicStroke.Standard, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(IslamicSpacing.Comfortable),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
                ) {
                    Text(
                        text = title,
                        style = MuslimSemanticTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = IslamicContentAlpha.Secondary,
                        ),
                    )
                    Text(
                        text = value,
                        style = MuslimSemanticTypography.Hero,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        modifier = Modifier.size(IslamicIconSize.Hero),
                    )
                }
            }
            supportingText?.takeIf(String::isNotBlank)?.let { text ->
                Text(
                    text = text,
                    style = MuslimSemanticTypography.Supporting,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                        alpha = IslamicContentAlpha.Secondary,
                    ),
                )
            }
            action?.let { slot ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.ControlGap),
                    content = slot,
                )
            }
        }
    }
}

/** Section hierarchy without forcing the section itself into a card. */
@Composable
fun MuslimSection(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
    ) {
        MuslimSectionHeader(
            title = title,
            supportingText = supportingText,
            action = action,
        )
        content()
    }
}

/** Quiet grouping primitive for related controls or information. */
@Composable
fun MuslimGroup(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = IslamicSpacing.Medium,
        vertical = IslamicSpacing.Small,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(IslamicStroke.Standard, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
            content = content,
        )
    }
}

/** Shared settings/navigation row; specialized trailing controls remain caller-owned. */
@Composable
fun MuslimSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MuslimTouchTarget.Min)
            .then(clickModifier)
            .padding(horizontal = IslamicSpacing.Medium, vertical = IslamicSpacing.Compact),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(IslamicIconSize.Standard),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = IslamicContentAlpha.Disabled)
                },
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.XXSmall),
        ) {
            Text(
                text = title,
                style = MuslimSemanticTypography.ItemTitle,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = IslamicContentAlpha.Disabled)
                },
            )
            supportingText?.takeIf(String::isNotBlank)?.let { text ->
                Text(
                    text = text,
                    style = MuslimSemanticTypography.Supporting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) IslamicContentAlpha.Full else IslamicContentAlpha.Disabled,
                    ),
                )
            }
        }
        trailing?.let { slot -> Row(content = slot) }
    }
}

/** Compact non-interactive status label. State meaning includes text/icon, never color alone. */
@Composable
fun MuslimStatusChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: MuslimStateTone = MuslimStateTone.Neutral,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    val colors = statusToneColors(tone)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(IslamicRadius.Pill),
        color = colors.first,
        contentColor = colors.second,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = IslamicSpacing.Compact,
                vertical = IslamicSpacing.XSmall,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.XSmall),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(IslamicIconSize.Supporting),
                )
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Compact contextual message for information, warning, success or error. */
@Composable
fun MuslimInlineMessage(
    message: String,
    modifier: Modifier = Modifier,
    tone: MuslimStateTone = MuslimStateTone.Information,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    val colors = statusToneColors(tone)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = colors.first,
        contentColor = colors.second,
    ) {
        Row(
            modifier = Modifier.padding(IslamicSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(IslamicIconSize.Supporting),
                )
            }
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MuslimSemanticTypography.Supporting,
            )
        }
    }
}

@Composable
private fun statusToneColors(tone: MuslimStateTone): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (tone) {
        MuslimStateTone.Neutral -> colors.surfaceContainerHigh to colors.onSurface
        MuslimStateTone.Information -> colors.secondaryContainer to colors.onSecondaryContainer
        MuslimStateTone.Positive -> colors.primaryContainer to colors.onPrimaryContainer
        MuslimStateTone.Warning -> colors.tertiaryContainer to colors.onTertiaryContainer
        MuslimStateTone.Critical -> colors.errorContainer to colors.onErrorContainer
    }
}
