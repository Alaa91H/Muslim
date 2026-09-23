package org.muslim.app.wear

import android.app.Activity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.common.wear.WearPrayerSnapshot
import java.text.DateFormat
import java.util.Date

/**
 * A compact, paired-phone Wear OS companion. It never calculates prayer times
 * or stores location: it renders the latest snapshot the phone explicitly
 * synchronized and relays a single tasbih increment when tapped.
 */
class WearMainActivity : ComponentActivity() {

    private val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearCompanionApp() }
    }

    override fun onResume() {
        super.onResume()
        refreshJob?.cancel()
        refreshJob = connectionScope.launch {
            WearConnectionManager.refreshFromPhone(applicationContext)
        }
    }

    override fun onDestroy() {
        connectionScope.cancel()
        super.onDestroy()
    }
}

@Suppress("LongMethod")
@Composable
private fun WearCompanionApp() {
    val context = LocalContext.current
    val actionScope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf(WearSnapshotStore.read(context)) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var hapticsEnabled by remember {
        mutableStateOf(
            context.getSharedPreferences(HAPTICS_FILE, Activity.MODE_PRIVATE)
                .getBoolean(HAPTICS_ENABLED, true),
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            snapshot = WearSnapshotStore.read(context)
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WearOrnamentBand(
                style = snapshot?.ornamentStyle ?: AppOrnamentStyle.Geometry,
                intensity = snapshot?.ornamentIntensity ?: OrnamentIntensity.Balanced,
            )
            Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_next_prayer),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            PrayerOverview(snapshot = snapshot, nowMillis = nowMillis)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_tasbih),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            snapshot?.let { state ->
                Text(
                    text = state.tasbihPhrase,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        org.muslim.app.wear.R.string.wear_tasbih_count,
                        state.tasbihCount,
                        state.tasbihTarget,
                    ),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = {
                        if (hapticsEnabled) {
                            (context as? Activity)?.window?.decorView?.performHapticFeedback(
                                HapticFeedbackConstants.CONFIRM,
                            )
                        }
                        actionScope.launch { WearConnectionManager.sendTasbihIncrement(context) }
                    },
                ) {
                    Text(text = stringResource(org.muslim.app.wear.R.string.wear_increment))
                }
            } ?: Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_sync_waiting),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    hapticsEnabled = !hapticsEnabled
                    context.getSharedPreferences(HAPTICS_FILE, Activity.MODE_PRIVATE).edit {
                        putBoolean(HAPTICS_ENABLED, hapticsEnabled)
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        if (hapticsEnabled) {
                            org.muslim.app.wear.R.string.wear_vibration_on
                        } else {
                            org.muslim.app.wear.R.string.wear_vibration_off
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun WearOrnamentBand(
    style: AppOrnamentStyle,
    intensity: OrnamentIntensity,
) {
    if (intensity == OrnamentIntensity.Off) return
    val baseColor = MaterialTheme.colorScheme.primary
    val alpha = when (intensity) {
        OrnamentIntensity.Off -> 0f
        OrnamentIntensity.Subtle -> 0.22f
        OrnamentIntensity.Balanced -> 0.34f
        OrnamentIntensity.Rich -> 0.48f
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
    ) {
        val color = baseColor.copy(alpha = alpha)
        val centerY = size.height / 2f
        val centerX = size.width / 2f
        val unit = size.height * 0.34f
        val stroke = 1.dp.toPx()

        when (style) {
            AppOrnamentStyle.Geometry,
            AppOrnamentStyle.Andalusian,
            AppOrnamentStyle.Mashrabiya -> listOf(centerX - size.height, centerX, centerX + size.height).forEach { x ->
                drawLine(color, Offset(x, centerY - unit), Offset(x + unit, centerY), stroke)
                drawLine(color, Offset(x + unit, centerY), Offset(x, centerY + unit), stroke)
                drawLine(color, Offset(x, centerY + unit), Offset(x - unit, centerY), stroke)
                drawLine(color, Offset(x - unit, centerY), Offset(x, centerY - unit), stroke)
            }

            AppOrnamentStyle.Stars,
            AppOrnamentStyle.Royal -> listOf(centerX - size.height, centerX, centerX + size.height).forEach { x ->
                drawCircle(color = color, radius = unit * 0.48f, center = Offset(x, centerY))
                drawLine(color, Offset(x - unit, centerY), Offset(x + unit, centerY), stroke)
                drawLine(color, Offset(x, centerY - unit), Offset(x, centerY + unit), stroke)
            }

            AppOrnamentStyle.Arabesque,
            AppOrnamentStyle.Ottoman -> {
                drawCircle(
                    color = color,
                    radius = unit,
                    center = Offset(centerX - unit * 0.72f, centerY),
                    style = Stroke(width = stroke),
                )
                drawCircle(
                    color = color,
                    radius = unit,
                    center = Offset(centerX + unit * 0.72f, centerY),
                    style = Stroke(width = stroke),
                )
            }

            AppOrnamentStyle.Mushaf,
            AppOrnamentStyle.Minimal -> {
                drawLine(
                    color = color,
                    start = Offset(centerX - size.width * 0.22f, centerY),
                    end = Offset(centerX + size.width * 0.22f, centerY),
                    strokeWidth = stroke,
                )
                drawCircle(color = color, radius = unit * 0.45f, center = Offset(centerX, centerY))
            }
        }
    }
}

@Composable
private fun PrayerOverview(snapshot: WearPrayerSnapshot?, nowMillis: Long) {
    val nextPrayerName = snapshot?.nextPrayerName
    val nextPrayerAt = snapshot?.nextPrayerAtEpochMillis
    if (nextPrayerName == null || nextPrayerAt == null) {
        Text(text = stringResource(org.muslim.app.wear.R.string.wear_no_prayer))
        return
    }
    Text(
        text = nextPrayerName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(nextPrayerAt)),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        text = stringResource(
            org.muslim.app.wear.R.string.wear_countdown,
            formatCountdown((nextPrayerAt - nowMillis).coerceAtLeast(0L)),
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun formatCountdown(millis: Long): String {
    val totalSeconds = millis / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    return if (hours > 0) "%d:%02d".format(hours, minutes) else "%d:%02d".format(minutes, totalSeconds % 60L)
}

private const val HAPTICS_FILE = "wear_haptics"
private const val HAPTICS_ENABLED = "enabled"
