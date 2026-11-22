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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.PathMeasure
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
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.accessibility.AccessibilityDarkColors
import org.muslim.app.core.ui.accessibility.AccessibilityLightColors
import org.muslim.app.core.ui.accessibility.LocalAccessibilityVisuals

/** Central Android/Compose adaptation of the liquid-glass specification. */
object LiquidGlassTokens {
    const val SurfaceAlpha = 0.16f
    const val BorderAlpha = 0.28f
    const val DividerAlpha = 0.20f
    const val SelectedSegmentAlpha = 0.12f
    const val SelectedChipAlpha = 0.34f
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
    val PopupFill = Color(0xF228245A)
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
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val baseColors = MaterialTheme.colorScheme
    val useGlass = enabled && !LocalAccessibilityVisuals.current.readingModeEnabled && !baseColors.isAccessibilityPalette()
    val typography = MaterialTheme.typography
    if (!useGlass) {
        CompositionLocalProvider(LocalLiquidGlassMode provides false, content = content)
        return
    }

    CompositionLocalProvider(LocalLiquidGlassMode provides true) {
        MaterialTheme(
            colorScheme = baseColors.liquidGlassColors(),
            typography = typography,
            shapes = liquidGlassShapes(),
        ) {
            Box(modifier = modifier.fillMaxSize().background(LiquidGlassTokens.FallbackBackground)) {
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
    surface = LiquidGlassTokens.PopupFill,
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
    primary = Color.White,
    onPrimary = LiquidGlassTokens.FallbackBackground,
    primaryContainer = LiquidGlassTokens.SurfaceFill,
    onPrimaryContainer = Color.White,
    secondary = Color.White,
    onSecondary = LiquidGlassTokens.FallbackBackground,
    secondaryContainer = LiquidGlassTokens.SurfaceFill,
    onSecondaryContainer = Color.White,
    tertiary = Color.White,
    onTertiary = LiquidGlassTokens.FallbackBackground,
    tertiaryContainer = LiquidGlassTokens.SurfaceFill,
    onTertiaryContainer = Color.White,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF5F1412),
    errorContainer = Color(0xFF5F1412),
    onErrorContainer = Color.White,
    surfaceTint = Color.Transparent,
)

private fun liquidGlassShapes() = Shapes(
    small = RoundedCornerShape(LiquidGlassTokens.SmallRadius),
    medium = RoundedCornerShape(LiquidGlassTokens.LargeRadius),
    large = RoundedCornerShape(LiquidGlassTokens.LargeRadius),
    extraLarge = RoundedCornerShape(LiquidGlassTokens.LargeRadius),
)

/** Compose corner curve approximation; it does not claim native continuous-corner parity. */
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
        border = BorderStroke(1.dp, LiquidGlassTokens.GlassBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(IslamicSpacing.Small),
            content = content,
        )
    }
}

/** One-pixel separator using the shared 20% white glass token. */
@Composable
fun LiquidGlassDivider(modifier: Modifier = Modifier) {
    val color = if (LocalLiquidGlassMode.current) LiquidGlassTokens.GlassDivider else MaterialTheme.colorScheme.outlineVariant
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (!LocalLiquidGlassMode.current) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)
        return
    }
    var focused by remember { mutableStateOf(false) }
    val reduceMotion = LocalMuslimMotionPreferences.current.reduceMotion
    val thumbOffset by animateDpAsState(
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
                    interactionSource = remember { MutableInteractionSource() },
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
                        .offset(x = thumbOffset)
                        .size(LiquidGlassTokens.SwitchThumbSize)
                        .background(Color.White, CircleShape),
                )
            }
        }
    }
}

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
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            var focused by remember(index) { mutableStateOf(false) }
            val selected = selectedIndex == index
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 72.dp, minHeight = LiquidGlassTokens.TouchTarget)
                    .background(if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedSegmentAlpha) else Color.Transparent, shape)
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
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

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
            .background(if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedChipAlpha) else Color.Transparent, shape)
            .border(
                if (focused) 2.dp else 1.dp,
                if (focused) Color.White else if (selected) Color.White.copy(alpha = LiquidGlassTokens.SelectedChipBorderAlpha) else LiquidGlassTokens.GlassBorder,
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
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, maxLines = 1, softWrap = false)
    }
}

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

private data class MeasuredOrnament(val path: Path, val measure: PathMeasure, val length: Float, val trace: Path)

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
    val transition = rememberInfiniteTransition(label = "aurora-ornament-trace")
    val animation: State<Float>? = if (reduceMotion) null else transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora-ornament-progress",
    )
    Box(modifier = modifier.drawWithCache {
        val cellWidth = 168.dp.toPx()
        val cellHeight = 148.dp.toPx()
        val radius = min(cellWidth, cellHeight) * 0.19f
        val columns = ceil(size.width / (cellWidth * 2f)).toInt() + 1
        val rows = ceil(size.height / cellHeight).toInt() + 2
        val motifs = buildList {
            for (row in -1 until rows) {
                val y = row * cellHeight + cellHeight / 2f
                for (column in 0..columns) {
                    add(measuredMotif(size.width / 2f - column * cellWidth, y, -1f, radius, cellWidth, cellHeight))
                    if (column > 0) add(measuredMotif(size.width / 2f + column * cellWidth, y, 1f, radius, cellWidth, cellHeight))
                }
            }
        }
        val totalLength = motifs.sumOf { it.length.toDouble() }.toFloat().coerceAtLeast(1f)
        onDrawBehind {
            val progress = animation?.value ?: 1f
            val traceProgress = if (reduceMotion) 1f else (progress / 0.84f).coerceIn(0f, 1f)
            val traceAlpha = if (reduceMotion) 0f else if (progress > 0.84f) ((1f - progress) / 0.16f).coerceIn(0f, 1f) else 1f
            val baseAlpha = if (reduceMotion) alpha else alpha * 0.18f
            motifs.forEach { drawPath(it.path, Color.White.copy(alpha = baseAlpha), style = Stroke(width = 1.dp.toPx())) }
            if (!reduceMotion) {
                var remaining = totalLength * traceProgress
                val traceColor = Color.White.copy(alpha = alpha * traceAlpha)
                motifs.forEach { motif ->
                    motif.trace.reset()
                    val segmentLength = remaining.coerceIn(0f, motif.length)
                    if (segmentLength > 0f) {
                        motif.measure.getSegment(0f, segmentLength, motif.trace, startWithMoveTo = true)
                        drawPath(motif.trace, traceColor, style = Stroke(width = 1.dp.toPx()))
                    }
                    remaining -= motif.length
                }
            }
        }
    })
}

private fun measuredMotif(
    centerX: Float,
    centerY: Float,
    direction: Float,
    radius: Float,
    cellWidth: Float,
    cellHeight: Float,
): MeasuredOrnament {
    val path = Path()
    for (point in 0..16) {
        val angle = -PI / 2.0 + point * PI / 8.0
        val pointRadius = if (point % 2 == 0) radius else radius * 0.45f
        val x = centerX + (cos(angle) * pointRadius * direction).toFloat()
        val y = centerY + (sin(angle) * pointRadius).toFloat()
        if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    val wing = cellWidth * 0.43f
    val rise = cellHeight * 0.34f
    path.cubicTo(centerX + direction * wing * 0.35f, centerY - rise, centerX + direction * wing, centerY - rise, centerX + direction * wing, centerY)
    path.cubicTo(centerX + direction * wing, centerY + rise, centerX + direction * wing * 0.35f, centerY + rise, centerX, centerY + radius)
    val measure = PathMeasure().apply { setPath(path, forceClosed = false) }
    return MeasuredOrnament(path, measure, measure.length, Path())
}
