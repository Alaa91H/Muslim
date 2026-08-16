package org.example.islamicapp.feature.prayertimes.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.example.islamicapp.feature.prayertimes.R

private const val TAG = "WidgetPreview"

/**
 * Dev-only screen (launched with `adb shell am start -n
 * org.example.islamicapp/.feature.prayertimes.widget.WidgetPreviewActivity`)
 * that verifies the home-screen widget end-to-end on a device/emulator:
 *
 *  - renders the widget content at the three responsive bucket sizes
 *    ([PrayerTimesWidget.SIZE_COMPACT/MEDIUM/LARGE]) using Glance's
 *    headless `GlanceRemoteViews.compose` (the same path the real widget
 *    takes), with a live countdown that ticks every second;
 *  - a button that pins the widget to the home screen through the launcher
 *    (`AppWidgetManager.requestPinAppWidget`), avoiding a manual drag.
 */
class WidgetPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dev-only: `adb shell am start ... --ez auto_pin true` pins the widget
        // on launch (for headless verification when the display is unavailable).
        if (intent?.getBooleanExtra(EXTRA_AUTO_PIN, false) == true) {
            pinToHome()
        }
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WidgetPreviewScreen(onPinToHome = ::pinToHome)
                }
            }
        }
    }

    private companion object {
        const val EXTRA_AUTO_PIN = "auto_pin"
    }

    /** Asks the launcher to place the widget on the home screen (no drag needed). */
    private fun pinToHome() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = AppWidgetManager.getInstance(this)
        val component = ComponentName(this, PrayerTimesWidgetReceiver::class.java)
        // The 3-arg overload (with a success callback) is the only one exposed by
        // the SDK jars in this project's toolchain; a null callback is allowed.
        manager.requestPinAppWidget(component, Bundle(), null)
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
private fun WidgetPreviewScreen(onPinToHome: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Tick every second so the countdown visibly updates.
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    val data = remember(now) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext, PrayerTimesWidgetEntryPoint::class.java,
            )
            val settings = runBlocking { entryPoint.settingsRepository().settings.first() }
            PrayerTimesWidgetData.compute(
                settings = settings,
                calculator = entryPoint.calculator(),
                nowMillis = now,
            )
        }.getOrNull()
    }

    // Dev-only heartbeat so the live countdown can be verified over logcat even
    // when the emulator display is unavailable.
    LaunchedEffect(data) {
        val label = data?.let {
            "next=${it.nextPrayer} at=${it.nextPrayerAt} countdown=${formatCountdown(it.countdownSeconds)}"
        } ?: "no-data"
        android.util.Log.d(TAG, "tick $now -> $label")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = context.getString(R.string.widget_preview_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = context.getString(R.string.widget_preview_subtitle),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onPinToHome,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(context.getString(R.string.widget_preview_pin))
        }

        if (data == null) {
            Text(
                text = context.getString(R.string.widget_preview_no_data),
                style = MaterialTheme.typography.bodyMedium,
            )
            return@Column
        }

        val previews = listOf(
            PreviewSpec(
                label = context.getString(R.string.widget_preview_size_compact),
                size = PrayerTimesWidget.SIZE_COMPACT,
                side = 110.dp,
            ),
            PreviewSpec(
                label = context.getString(R.string.widget_preview_size_medium),
                size = PrayerTimesWidget.SIZE_MEDIUM,
                side = 180.dp,
            ),
            PreviewSpec(
                label = context.getString(R.string.widget_preview_size_large),
                size = PrayerTimesWidget.SIZE_LARGE,
                side = 250.dp,
            ),
        )

        previews.forEach { spec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = spec.label,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    AndroidView(
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    with(density) { spec.side.roundToPx() },
                                    with(density) { spec.side.roundToPx() },
                                )
                            }
                        },
                        update = { frame ->
                            val dataNow = data
                            frame.removeAllViews()
                            val result = runBlocking {
                                GlanceRemoteViews().compose(
                                    context = context,
                                    size = spec.size,
                                    content = { PrayerTimesWidgetContent(dataNow) },
                                )
                            }
                            val view = result.remoteViews.apply(context, frame)
                            frame.addView(view)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onPinToHome,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(context.getString(R.string.widget_preview_pin))
                    }
                }
            }
        }
    }
}

private data class PreviewSpec(
    val label: String,
    val size: DpSize,
    val side: Dp,
)
