package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicElevation
import org.muslim.app.core.designsystem.IslamicIconSize
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.designsystem.MuslimTouchTarget

/** A quiet surface: subtle outline, restrained elevation and semantic Material colour.
 *
 * Professional polish: animateContentSize for responsive layout changes, low
 * resting elevation with a gentle pressed elevation when clickable, and a
 * 48 dp-assured surface for accessibility. The card remains reading-first:
 * no heavy shadow, no competing ornament.
 */
@Composable
fun IslamicCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(IslamicSpacing.Medium),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clickable = if (onClick != null) {
        Modifier.clickable(
            role = androidx.compose.ui.semantics.Role.Button,
            onClick = onClick,
        )
    } else Modifier
    Card(
        modifier = modifier.then(clickable),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = IslamicElevation.Resting,
            pressedElevation = IslamicElevation.Raised,
            focusedElevation = IslamicElevation.Raised,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .then(
                    if (onClick != null) Modifier.defaultMinSize(minHeight = 24.dp) else Modifier
                ),
            content = { content() },
        )
    }
}

/** Semantic prominence for user-facing empty, loading, unavailable and recovery states.
 *  Each tone maps to a Material container role so colour is never the sole signal:
 *  title + icon + container + action together communicate the state.
 */
enum class MuslimStateTone {
    Neutral,
    Information,
    Positive,
    Warning,
    Critical,
}

/**
 * A concise state surface with a clear hierarchy and an optional recovery action.
 * Feature modules own their state logic; this component only standardises presentation.
 */
@Composable
fun MuslimStateSurface(
    title: String,
    supportingText: String? = null,
    modifier: Modifier = Modifier,
    tone: MuslimStateTone = MuslimStateTone.Neutral,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val container = when (tone) {
        MuslimStateTone.Neutral -> colors.surfaceContainerLow
        MuslimStateTone.Information -> colors.secondaryContainer
        MuslimStateTone.Positive -> colors.primaryContainer
        MuslimStateTone.Warning -> colors.tertiaryContainer
        MuslimStateTone.Critical -> colors.errorContainer
    }
    val content = when (tone) {
        MuslimStateTone.Neutral -> colors.onSurface
        MuslimStateTone.Information -> colors.onSecondaryContainer
        MuslimStateTone.Positive -> colors.onPrimaryContainer
        MuslimStateTone.Warning -> colors.onTertiaryContainer
        MuslimStateTone.Critical -> colors.onErrorContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.85f)),
        tonalElevation = if (tone == MuslimStateTone.Neutral) 0.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(IslamicSpacing.Comfortable),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = iconContentDescription,
                        modifier = Modifier.size(IslamicIconSize.Standard),
                        tint = content,
                    )
                }
                if (icon != null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(IslamicSpacing.Small))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = content,
                )
            }
            supportingText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.84f),
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.defaultMinSize(minHeight = MuslimTouchTarget.Min),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/** A section title with optional supporting context and a single trailing action.
 *  Uses a clear hierarchy (titleLarge + bodySmall) and keeps the trailing slot
 *  aligned to the title baseline so settings and more screens feel calm.
 */
@Composable
fun MuslimSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}

/**
 * A reusable feature/list row for secondary navigation and settings surfaces.
 * The entire row is one accessible target with a stable title/subtitle hierarchy.
 */
@Composable
fun IslamicListItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    trailingContentDescription: String? = null,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 64.dp)
        .then(
            if (onClick != null) {
                Modifier
                    .clickable(
                        enabled = enabled,
                        role = androidx.compose.ui.semantics.Role.Button,
                        onClick = onClick,
                    )
            } else {
                Modifier
            },
        )
    Surface(
        modifier = rowModifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = IslamicSpacing.Medium, vertical = IslamicSpacing.Compact),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IslamicSpacing.Compact),
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = iconContentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IslamicIconSize.Standard),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                subtitle?.takeIf(String::isNotBlank)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = trailingContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Primary action with an explicit accessible minimum height and a calm tonal elevation. */
@Composable
fun IslamicPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MuslimTouchTarget.Min),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
        ),
        content = { content() },
    )
}

/** Secondary action with the same touch ergonomics and restrained outline. */
@Composable
fun IslamicSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MuslimTouchTarget.Min),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        content = { content() },
    )
}

/** Centres a short state label without relying on a screen-local empty-state layout. */
@Composable
fun MuslimCenteredStatus(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
