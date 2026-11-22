package org.muslim.app.core.ui.theme

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.ui.accessibility.AccessibilityDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityLightColors
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals

/** Central Android/Compose adaptation of the liquid-glass specification. */
object LiquidGlassTokens {
    const val SurfaceAlpha = 0.16f
    const val BorderAlpha = 0.28f
    const val DividerAlpha = 0.20f
    // Reduced from 0.34f: repeated glass layers otherwise fail 4.5:1 for white text.
    const val SelectedSegmentAlpha = 0.12f
    const val UnselectedTextAlpha = 0.85f
    const val SelectedChipBorderAlpha = 0.60f
    const val SwitchOnAlpha = 0.90f
    const val SwitchAnimationMillis = 250

    val BackgroundBlur = 24.dp
    val LargeRadius = 26.dp
    val SmallRadius = 22.dp
    val SwitchWidth = 54.dp
    val SwitchHeight = 32.dp
    val SwitchThumbSize = 26.dp
    val SwitchThumbInset = 3.dp
    val SwitchTravel = 22.dp
    val TouchTarget = 44.dp
    val FallbackBackground = Color(0xFF28245A)
    val FallbackGlassFill = Color(0xD928245A)
    val SwitchOn = Color(0xFF28CD96).copy(alpha = SwitchOnAlpha)
    val GlassFill = Color.White.copy(alpha = SurfaceAlpha)
    val SurfaceFill: Color
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlassFill else FallbackGlassFill
    val GlassBorder = Color.White.copy(alpha = BorderAlpha)
    val GlassDivider = Color.White.copy(alpha = DividerAlpha)
}

val LocalLiquidGlassMode = staticCompositionLocalOf { false }

/** Opt-in screen scope. High-contrast and reading modes retain their existing visuals. */
@Composable
fun LiquidGlassScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val baseColors = MaterialTheme.colorScheme
    val readingMode = LocalAccessibilityVisuals.current.readingModeEnabled
    val highContrast = baseColors.isAccessibilityPalette()
    val enabled = !readingMode && !highContrast
    val typography = MaterialTheme.typography

    if (!enabled) {
        CompositionLocalProvider(LocalLiquidGlassMode provides false, content = content)
        return
    }

    CompositionLocalProvider(LocalLiquidGlassMode provides true) {
        MaterialTheme(
            colorScheme = baseColors.liquidGlassColors(),
            typography = typography,
            shapes = liquidGlassShapes(),
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(LiquidGlassTokens.FallbackBackground),
            ) {
                LiquidGlassAuroraBackground(Modifier.matchParentSize())
                content()
            }
        }
    }
}

private fun ColorScheme.isAccessibilityPalette(): Boolean =
    (background == AccessibilityLightColors.background && primary == AccessibilityLightColors.primary) ||
        (background == AccessibilityDarkColors.background && primary == AccessibilityDarkColors.primary)

private fun ColorScheme.liquidGlassColors() = copy(
    background = LiquidGlassTokens.FallbackBackground,
    onBackground = Color.White,
    surface = LiquidGlassTokens.SurfaceFill,
    onSurface = Color.White,
    surfaceVariant = LiquidGlassTokens.SurfaceFill,
    onSurfaceVariant = Color.White.copy(alpha = LiquidGlassTokens.UnselectedTextAlpha),
    surfaceDim = LiquidGlassTokens.FallbackBackground,
    surfaceBright = LiquidGlassTokens.SurfaceFill,
    surfaceContainerLowest = LiquidGlassTokens.SurfaceFill,
    surfaceContainerLow = LiquidGlassTokens.SurfaceFill,
    surfaceContainer = LiquidGlassTokens.SurfaceFill,
    surfaceContainerHigh = LiquidGlassTokens.SurfaceFill,
    surfaceContainerHighest = LiquidGlassTokens.SurfaceFill,
    outline = LiquidGlassTokens.GlassBorder,
    outlineVariant = LiquidGlassTokens.GlassBorder,
    primaryContainer = LiquidGlassTokens.SurfaceFill,
    onPrimaryContainer = Color.White,
    secondaryContainer = LiquidGlassTokens.SurfaceFill,
    onSecondaryContainer = Color.White,
    tertiaryContainer = LiquidGlassTokens.SurfaceFill,
    onTertiaryContainer = Color.White,
    surfaceTint = Color.Transparent,
)

private fun liquidGlassShapes() = Shapes(
    small = LiquidGlassShape(LiquidGlassTokens.SmallRadius),
    medium = LiquidGlassShape(LiquidGlassTokens.SmallRadius),
    large = LiquidGlassShape(LiquidGlassTokens.LargeRadius),
    extraLarge = LiquidGlassShape(LiquidGlassTokens.LargeRadius),
)

/** Smooth cubic corners approximate continuous corners with native Compose drawing. */
data class LiquidGlassShape(val radius: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: androidx.compose.ui.unit.Density): Outline {
        val r = with(density) { radius.toPx() }.coerceAtMost(min(size.width, size.height) / 2f)
        val c = r * 0.38f
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(size.width - r, 0f)
            cubicTo(size.width - r + c, 0f, size.width, r - c, size.width, r)
            lineTo(size.width, size.height - r)
            cubicTo(size.width, size.height - r + c, size.width - r + c, size.height, size.width - r, size.height)
            lineTo(r, size.height)
            cubicTo(r - c, size.height, 0f, size.height - r + c, 0f, size.height - r)
            lineTo(0f, r)
            cubicTo(0f, r - c, r - c, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Transparent surface primitive; it intentionally has no elevation or shadow. */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    large: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = LiquidGlassShape(if (large) LiquidGlassTokens.LargeRadius else LiquidGlassTokens.SmallRadius)
    val clickModifier = if (onClick == null) Modifier else Modifier
        .heightIn(min = LiquidGlassTokens.TouchTarget)
        .clickable(
            role = Role.Button,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        )

    Surface(
        modifier = modifier.then(clickModifier),
        shape = shape,
        color = LiquidGlassTokens.SurfaceFill,
        contentColor = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, LiquidGlassTokens.GlassBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/** Switch with fixed LTR geometry, native switch semantics and a 44dp hit target. */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val reduceMotion = LocalMuslimMotionPreferences.current.reduceMotion
    val offset by animateDpAsState(
        targetValue = if (checked) LiquidGlassTokens.SwitchTravel else 0.dp,
        animationSpec = tween(if (reduceMotion) 0 else LiquidGlassTokens.SwitchAnimationMillis),
        label = "glass-switch-thumb",
    )

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .widthIn(min = LiquidGlassTokens.SwitchWidth)
                .heightIn(min = LiquidGlassTokens.TouchTarget)
                .onFocusChanged { focused = it.isFocused }
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null,
                    onValueChange = onCheckedChange,
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            val trackShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .size(LiquidGlassTokens.SwitchWidth, LiquidGlassTokens.SwitchHeight)
                    .background(if (checked) LiquidGlassTokens.SwitchOn else LiquidGlassTokens.SurfaceFill, trackShape)
                    .border(if (focused) 2.dp else 1.dp, if (focused) Color.White else LiquidGlassTokens.GlassBorder, trackShape),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = LiquidGlassTokens.SwitchThumbInset)
                        .offset(x = offset)
                        .size(LiquidGlassTokens.SwitchThumbSize)
                        .background(Color.White, CircleShape),
                )
            }
        }
    }
}

/** Single-choice control with radio semantics. */
@Composable
fun LiquidGlassSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (options.isEmpty()) return
    val trackShape = RoundedCornerShape(15.dp)
    Row(
        modifier = modifier
            .background(LiquidGlassTokens.SurfaceFill, trackShape)
            .border(1.dp, LiquidGlassTokens.GlassBorder, trackShape)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            var focused by remember(index) { mutableStateOf(false) }
            val selected = selectedIndex == index
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = LiquidGlassTokens.TouchTarget)
                    .background(
                        if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedSegmentAlpha) else Color.Transparent,
                        shape,
                    )
                    .onFocusChanged { focused = it.isFocused }
                    .then(if (focused) Modifier.border(2.dp, Color.White, shape) else Modifier)
                    .selectable(
                        selected = selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onSelectedIndexChange(index) },
                    )
                    .padding(horizontal = 8.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else Color.White.copy(alpha = LiquidGlassTokens.UnselectedTextAlpha),
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Multi-choice chip with a checkmark slot reserved in both states. */
@Composable
fun LiquidGlassChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(20.dp)
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = LiquidGlassTokens.TouchTarget)
            .background(
                if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedSegmentAlpha) else Color.Transparent,
                shape,
            )
            .border(
                if (focused) 2.dp else 1.dp,
                if (focused) Color.White else if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedChipBorderAlpha)
                else LiquidGlassTokens.GlassBorder,
                shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .toggleable(
                value = selected,
                enabled = enabled,
                role = Role.Checkbox,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onValueChange = onSelectedChange,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(15.dp), contentAlignment = Alignment.Center) {
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One blurred aurora layer, with the specified solid glass fallback below API 31. */
@Composable
fun LiquidGlassAuroraBackground(modifier: Modifier = Modifier) {
    val reduceMotion = LocalMuslimMotionPreferences.current.reduceMotion
    Box(modifier = modifier.fillMaxSize().background(LiquidGlassTokens.FallbackBackground)) {
        Canvas(
            Modifier.matchParentSize().then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(LiquidGlassTokens.BackgroundBlur) else Modifier,
            ),
        ) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF12102D), Color(0xFF102631), Color(0xFF211A3C), Color(0xFF11152B)),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF527A9A).copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.17f, size.height * 0.24f),
                    radius = size.minDimension * 0.76f,
                ),
                radius = size.minDimension * 0.76f,
                center = Offset(size.width * 0.17f, size.height * 0.24f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6D579A).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.83f, size.height * 0.78f),
                    radius = size.minDimension * 0.68f,
                ),
                radius = size.minDimension * 0.68f,
                center = Offset(size.width * 0.83f, size.height * 0.78f),
            )
        }
        Canvas(Modifier.matchParentSize()) { drawRect(Color.Black.copy(alpha = 0.30f)) }
        LiquidGlassLineOrnaments(reduceMotion, Modifier.matchParentSize())
    }
}

/** Symmetric arabesque linework; cached paths animate only their opacity. */
@Composable
private fun LiquidGlassLineOrnaments(reduceMotion: Boolean, modifier: Modifier = Modifier) {
    val intensity = LocalIslamicDecoration.current.intensity
    if (intensity == OrnamentIntensity.Off) return
    val alpha = when (intensity) {
        OrnamentIntensity.Off -> 0f
        OrnamentIntensity.Subtle -> 0.06f
        OrnamentIntensity.Balanced -> 0.10f
        OrnamentIntensity.Rich -> 0.14f
    }
    val transition = rememberInfiniteTransition(label = "aurora-ornament-pulse")
    val animation: State<Float>? = if (reduceMotion) null else transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "aurora-ornament-opacity",
    )

    Box(modifier = modifier.drawWithCache {
        val moduleHeight = 104.dp.toPx()
        val reach = 40.dp.toPx()
        val starRadius = 13.dp.toPx()
        val rows = ceil(size.height / moduleHeight).toInt() + 2
        val modules = buildList {
            for (row in -1 until rows) {
                val centerY = row * moduleHeight + moduleHeight / 2f
                add(ornamentPaths(0f, centerY, 1f, reach, moduleHeight, starRadius))
                add(ornamentPaths(size.width, centerY, -1f, reach, moduleHeight, starRadius))
            }
        }
        onDrawBehind {
            val pulse = animation?.value ?: 1f
            modules.forEach { (curve, star) ->
                val color = Color.White.copy(alpha = alpha * pulse)
                drawPath(curve, color, style = Stroke(width = 1.dp.toPx()))
                drawPath(star, color, style = Stroke(width = 1.dp.toPx()))
            }
        }
    })
}

private fun ornamentPaths(
    edgeX: Float,
    centerY: Float,
    direction: Float,
    reach: Float,
    moduleHeight: Float,
    starRadius: Float,
): Pair<Path, Path> {
    val curve = Path().apply {
        moveTo(edgeX, centerY - moduleHeight * 0.48f)
        cubicTo(edgeX + direction * reach * 0.15f, centerY - moduleHeight * 0.44f,
            edgeX + direction * reach, centerY - moduleHeight * 0.22f,
            edgeX + direction * reach * 0.56f, centerY)
        cubicTo(edgeX + direction * reach * 1.08f, centerY + moduleHeight * 0.24f,
            edgeX + direction * reach * 0.18f, centerY + moduleHeight * 0.45f,
            edgeX, centerY + moduleHeight * 0.48f)
    }
    val star = Path().apply {
        val starCenterX = edgeX + direction * reach * 0.55f
        for (point in 0..16) {
            val angle = -PI / 2.0 + point * PI / 8.0
            val radius = if (point % 2 == 0) starRadius else starRadius * 0.46f
            val x = starCenterX + direction * (cos(angle) * radius).toFloat()
            val y = centerY + (sin(angle) * radius).toFloat()
            if (point == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    return curve to star
}
