package org.muslim.app.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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

/** A quiet surface: subtle outline, restrained elevation and semantic Material colour. */
@Composable
fun IslamicCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(IslamicSpacing.Medium),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = IslamicElevation.Resting),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = { content() },
        )
    }
}

/** Semantic prominence for user-facing empty, loading, unavailable and recovery states. */
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
        border = BorderStroke(1.dp, colors.outlineVariant),
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
                    )
                }
                if (icon != null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(IslamicSpacing.Small))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            supportingText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.82f),
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

/** A section title with optional supporting context and a single trailing action. */
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
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
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

/** Primary action with an explicit accessible minimum height. */
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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
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
