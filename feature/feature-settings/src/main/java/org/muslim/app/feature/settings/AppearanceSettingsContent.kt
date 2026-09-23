package org.muslim.app.feature.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.muslim.app.core.common.appearance.AppColorPalette
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.CardCornerStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppThemeMode
import org.muslim.app.core.ui.theme.AppTheme
import org.muslim.app.core.ui.theme.IslamicDecorationPreview
import org.muslim.app.core.ui.theme.previewColorsForPalette

internal data class AppearanceThemeActions(
    val onModeChanged: (AppThemeMode) -> Unit,
    val onDynamicColorChanged: (Boolean) -> Unit,
    val onAmoledBlackChanged: (Boolean) -> Unit,
)

internal data class AppearanceShapeActions(
    val onPaletteChanged: (AppColorPalette) -> Unit,
    val onCornerStyleChanged: (CardCornerStyle) -> Unit,
)

internal data class AppearanceOrnamentActions(
    val onStyleChanged: (AppOrnamentStyle) -> Unit,
    val onIntensityChanged: (OrnamentIntensity) -> Unit,
)

internal data class AppearanceSettingsActions(
    val theme: AppearanceThemeActions,
    val shape: AppearanceShapeActions,
    val ornament: AppearanceOrnamentActions,
    val onReduceAnimationsChanged: (Boolean) -> Unit,
)

/**
 * Focused appearance editor extracted from the settings hub.
 *
 * All controls use visual previews and the live sample is rendered through the
 * real [AppTheme], so the settings screen and the rest of the app cannot drift
 * into different interpretations of a palette, card radius, or ornament.
 */
@Suppress("LongMethod")
@Composable
internal fun AppearanceSettingsContent(
    preferences: AppPreferences,
    actions: AppearanceSettingsActions,
) {
    val systemDark = isSystemInDarkTheme()
    val resolvedDark = when (preferences.themeMode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicActive = preferences.dynamicColor && dynamicSupported

    AppearanceLivePreview(
        darkTheme = resolvedDark,
        dynamicColor = dynamicActive,
        amoledBlack = preferences.amoledBlack,
        palette = preferences.colorPalette,
        cornerStyle = preferences.cardCornerStyle,
        ornamentStyle = preferences.ornamentStyle,
        ornamentIntensity = preferences.ornamentIntensity,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )

    Text(
        text = stringResource(R.string.settings_theme_mode),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    AppearanceThemeModeSelector(
        selected = preferences.themeMode,
        onSelect = actions.theme.onModeChanged,
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_amoled_black)) },
        supportingContent = { Text(stringResource(R.string.settings_amoled_black_desc)) },
        leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
        trailingContent = {
            Switch(
                checked = preferences.amoledBlack,
                onCheckedChange = actions.theme.onAmoledBlackChanged,
            )
        },
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
        supportingContent = {
            Text(
                stringResource(
                    if (dynamicSupported) {
                        R.string.settings_dynamic_color_desc
                    } else {
                        R.string.settings_dynamic_color_unavailable
                    },
                ),
            )
        },
        leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
        trailingContent = {
            Switch(
                checked = dynamicActive,
                enabled = dynamicSupported,
                onCheckedChange = actions.theme.onDynamicColorChanged,
            )
        },
    )

    if (!dynamicActive) {
        Text(
            text = stringResource(R.string.settings_color_palette),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.settings_color_palette_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
        )
        AppearancePaletteSelector(
            selected = preferences.colorPalette,
            darkTheme = resolvedDark,
            amoledBlack = preferences.amoledBlack,
            onSelect = actions.shape.onPaletteChanged,
        )
    }

    Text(
        text = stringResource(R.string.settings_card_corners),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    AppearanceCornerSelector(
        selected = preferences.cardCornerStyle,
        onSelect = actions.shape.onCornerStyleChanged,
    )

    Text(
        text = stringResource(R.string.settings_ornament),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    AppearanceOrnamentSelector(
        selected = preferences.ornamentStyle,
        intensity = preferences.ornamentIntensity,
        onSelect = actions.ornament.onStyleChanged,
    )

    Text(
        text = stringResource(R.string.settings_ornament_intensity),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    AppearanceOrnamentIntensitySelector(
        selected = preferences.ornamentIntensity,
        onSelect = actions.ornament.onIntensityChanged,
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_reduce_animations)) },
        supportingContent = { Text(stringResource(R.string.settings_reduce_animations_desc)) },
        trailingContent = {
            Switch(
                checked = preferences.reduceAnimations,
                onCheckedChange = actions.onReduceAnimationsChanged,
            )
        },
    )
}

@Suppress("LongMethod")
@Composable
private fun AppearanceLivePreview(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    amoledBlack: Boolean,
    palette: AppColorPalette,
    cornerStyle: CardCornerStyle,
    ornamentStyle: AppOrnamentStyle,
    ornamentIntensity: OrnamentIntensity,
    modifier: Modifier = Modifier,
) {
    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        amoledBlack = amoledBlack,
        colorPalette = palette,
        cardCornerStyle = cornerStyle,
        ornamentStyle = ornamentStyle,
        ornamentIntensity = ornamentIntensity,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
        ) {
            Box {
                IslamicDecorationPreview(
                    style = ornamentStyle,
                    intensity = if (ornamentIntensity == OrnamentIntensity.Off) {
                        OrnamentIntensity.Subtle
                    } else {
                        ornamentIntensity
                    },
                    selected = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp),
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_appearance_preview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Column(Modifier.weight(1f)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.55f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer),
                                )
                                Spacer(Modifier.height(7.dp))
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.82f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.48f),
                                        ),
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Box(Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AppearanceThemeMode(
    val mode: AppThemeMode,
    val labelRes: Int,
)

private val appearanceThemeModes = listOf(
    AppearanceThemeMode(AppThemeMode.System, R.string.settings_theme_system),
    AppearanceThemeMode(AppThemeMode.Light, R.string.settings_theme_light),
    AppearanceThemeMode(AppThemeMode.Dark, R.string.settings_theme_dark),
)

@Composable
private fun AppearanceThemeModeSelector(
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        appearanceThemeModes.forEach { option ->
            val icon = when (option.mode) {
                AppThemeMode.System -> Icons.Filled.Palette
                AppThemeMode.Light -> Icons.Filled.LightMode
                AppThemeMode.Dark -> Icons.Filled.DarkMode
            }
            FilterChip(
                selected = selected == option.mode,
                onClick = { onSelect(option.mode) },
                label = { Text(stringResource(option.labelRes)) },
                leadingIcon = { Icon(icon, contentDescription = null) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AppearancePaletteSelector(
    selected: AppColorPalette,
    darkTheme: Boolean,
    amoledBlack: Boolean,
    onSelect: (AppColorPalette) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppColorPalette.entries.chunked(2).forEach { rowPalettes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowPalettes.forEach { palette ->
                    AppearancePaletteCard(
                        palette = palette,
                        selected = selected == palette,
                        darkTheme = darkTheme,
                        amoledBlack = amoledBlack,
                        onClick = { onSelect(palette) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowPalettes.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AppearancePaletteCard(
    palette: AppColorPalette,
    selected: Boolean,
    darkTheme: Boolean,
    amoledBlack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = previewColorsForPalette(palette, darkTheme, amoledBlack)
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                PaletteSwatch(colors.primary)
                PaletteSwatch(colors.secondary)
                PaletteSwatch(colors.tertiary)
                PaletteSwatch(colors.surface)
                PaletteSwatch(colors.background)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .border(1.dp, colors.primary.copy(alpha = 0.22f), RoundedCornerShape(12.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.72f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(colors.surface),
                )
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 29.dp)
                        .size(width = 42.dp, height = 10.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = stringResource(palette.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

@Composable
private fun PaletteSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(17.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    )
}

@Composable
private fun AppearanceCornerSelector(
    selected: CardCornerStyle,
    onSelect: (CardCornerStyle) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CardCornerStyle.entries.forEach { style ->
            val shape = RoundedCornerShape(
                when (style) {
                    CardCornerStyle.Compact -> 10.dp
                    CardCornerStyle.Soft -> 18.dp
                    CardCornerStyle.Rounded -> 28.dp
                },
            )
            Card(
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                border = BorderStroke(
                    if (selected == style) 2.dp else 1.dp,
                    if (selected == style) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                    Text(
                        text = stringResource(style.labelRes()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected == style) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceOrnamentSelector(
    selected: AppOrnamentStyle,
    intensity: OrnamentIntensity,
    onSelect: (AppOrnamentStyle) -> Unit,
) {
    val previewIntensity = if (intensity == OrnamentIntensity.Off) {
        OrnamentIntensity.Balanced
    } else {
        intensity
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppOrnamentStyle.entries.chunked(2).forEach { styles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                styles.forEach { style ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = selected == style,
                                onClick = { onSelect(style) },
                            ),
                    ) {
                        IslamicDecorationPreview(
                            style = style,
                            intensity = previewIntensity,
                            selected = selected == style,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selected == style, onClick = null)
                            Text(
                                text = stringResource(style.labelRes()),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected == style) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                if (styles.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AppearanceOrnamentIntensitySelector(
    selected: OrnamentIntensity,
    onSelect: (OrnamentIntensity) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OrnamentIntensity.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { intensity ->
                    FilterChip(
                        selected = selected == intensity,
                        onClick = { onSelect(intensity) },
                        label = { Text(stringResource(intensity.labelRes())) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun AppColorPalette.labelRes(): Int = when (this) {
    AppColorPalette.Classic -> R.string.settings_palette_classic
    AppColorPalette.Emerald -> R.string.settings_palette_emerald
    AppColorPalette.Midnight -> R.string.settings_palette_midnight
    AppColorPalette.Sand -> R.string.settings_palette_sand
    AppColorPalette.RoyalBlue -> R.string.settings_palette_royal_blue
    AppColorPalette.Turquoise -> R.string.settings_palette_turquoise
    AppColorPalette.Olive -> R.string.settings_palette_olive
    AppColorPalette.Burgundy -> R.string.settings_palette_burgundy
    AppColorPalette.Amethyst -> R.string.settings_palette_amethyst
    AppColorPalette.Copper -> R.string.settings_palette_copper
    AppColorPalette.Slate -> R.string.settings_palette_slate
    AppColorPalette.Sepia -> R.string.settings_palette_sepia
}

private fun CardCornerStyle.labelRes(): Int = when (this) {
    CardCornerStyle.Compact -> R.string.settings_corners_compact
    CardCornerStyle.Soft -> R.string.settings_corners_soft
    CardCornerStyle.Rounded -> R.string.settings_corners_rounded
}

private fun AppOrnamentStyle.labelRes(): Int = when (this) {
    AppOrnamentStyle.Geometry -> R.string.settings_ornament_geometry
    AppOrnamentStyle.Arabesque -> R.string.settings_ornament_arabesque
    AppOrnamentStyle.Stars -> R.string.settings_ornament_stars
    AppOrnamentStyle.Andalusian -> R.string.settings_ornament_andalusian
    AppOrnamentStyle.Mashrabiya -> R.string.settings_ornament_mashrabiya
    AppOrnamentStyle.Ottoman -> R.string.settings_ornament_ottoman
    AppOrnamentStyle.Mushaf -> R.string.settings_ornament_mushaf
    AppOrnamentStyle.Royal -> R.string.settings_ornament_royal
    AppOrnamentStyle.Minimal -> R.string.settings_ornament_minimal
}

private fun OrnamentIntensity.labelRes(): Int = when (this) {
    OrnamentIntensity.Off -> R.string.settings_ornament_intensity_off
    OrnamentIntensity.Subtle -> R.string.settings_ornament_intensity_subtle
    OrnamentIntensity.Balanced -> R.string.settings_ornament_intensity_balanced
    OrnamentIntensity.Rich -> R.string.settings_ornament_intensity_rich
}
