package org.muslim.app.feature.prayertimes.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator

/**
 * Home-screen widget (Glance) showing the next prayer and a live countdown,
 * in three responsive sizes (PROJECT_PROMPT.md §6: "Widgets متعددة الأحجام
 * ... عدّاد تنازلي مباشر للصلاة القادمة").
 *
 * Refresh strategy (Android does not deliver `TIME_TICK` to manifest
 * receivers, so the widget re-renders on every event below):
 *  - every 15 minutes via [WidgetRefreshWorker] (WorkManager),
 *  - at every prayer boundary (the exact adhan alarm refreshes it),
 *  - on boot / timezone / clock / date changes ([WidgetRefreshReceiver]),
 *  - whenever settings or the location change, and every time the app opens.
 */
class PrayerTimesWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode =
        SizeMode.Responsive(setOf(SIZE_COMPACT, SIZE_MEDIUM, SIZE_LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, PrayerTimesWidgetEntryPoint::class.java,
        )
        val settings = entryPoint.settingsRepository().settings.first()
        val data = PrayerTimesWidgetData.compute(
            settings = settings,
            calculator = entryPoint.calculator(),
            nowMillis = System.currentTimeMillis(),
        )
        provideContent {
            WidgetRoot(data)
        }
    }

    companion object {
        /** 2×2 cells — next prayer + countdown. */
        val SIZE_COMPACT = DpSize(110.dp, 110.dp)

        /** 3×3 cells — adds a compact list of today's five times. */
        val SIZE_MEDIUM = DpSize(180.dp, 180.dp)

        /** 4×4 cells — full five-prayer table with the next one highlighted. */
        val SIZE_LARGE = DpSize(250.dp, 250.dp)

        /** Opens the app's main screen when the widget is tapped. */
        internal fun openAppIntent(context: Context): Intent? =
            context.packageManager.getLaunchIntentForPackage(context.packageName)
    }
}

/**
 * Re-renders every widget instance. Thin wrapper over `updateAll` so that
 * callers (including the app module) don't need the Glance classpath.
 */
suspend fun refreshPrayerTimesWidgets(context: Context) {
    PrayerTimesWidget().updateAll(context)
}

/** Hilt entry point so the widget can read settings outside the app UI. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrayerTimesWidgetEntryPoint {
    fun settingsRepository(): PrayerSettingsRepository
    fun calculator(): PrayerTimesCalculator
}

// ---- Colors (deep-green Muslim surface + mint accent, see Color.kt) ----

private val WidgetBg = ColorProvider(Color(0xFF00513E))
private val WidgetOnBg = ColorProvider(Color(0xFFFFFFFF))
private val WidgetAccent = ColorProvider(Color(0xFF9EF2D2))
private val WidgetMuted = ColorProvider(Color(0xFFB2CCC0))
private val WidgetHighlight = ColorProvider(Color(0x33FFFFFF))

private val PrayerNames = listOf(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha)

// ---- Root: picks the layout for the current bucket size ----

@Composable
private fun WidgetRoot(data: PrayerTimesWidgetData) {
    val size = LocalSize.current
    val openApp = PrayerTimesWidget.openAppIntent(LocalContext.current)
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(WidgetBg)
        .padding(12.dp)
        .then(if (openApp != null) GlanceModifier.clickable(actionStartActivity(openApp)) else GlanceModifier)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!data.hasLocation) {
            NoLocationContent()
        } else if (size.height >= 200.dp) {
            LargeContent(data)
        } else if (size.width >= 150.dp) {
            MediumContent(data)
        } else {
            CompactContent(data)
        }
    }
}

@Composable
private fun NoLocationContent() {
    val context = LocalContext.current
    Column(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = context.getString(R.string.widget_no_location_title),
            style = TextStyle(
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg, textAlign = TextAlign.Center,
            ),
            maxLines = 2,
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = context.getString(R.string.widget_no_location_hint),
            style = TextStyle(fontSize = 11.sp, color = WidgetMuted, textAlign = TextAlign.Center),
            maxLines = 2,
        )
    }
}

// ---- Compact (110×110): next prayer name, time, countdown ----

@Composable
private fun CompactContent(data: PrayerTimesWidgetData) {
    val context = LocalContext.current
    Column(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = data.nextPrayer?.let { context.getString(prayerNameRes(it)) }
                ?: context.getString(R.string.widget_no_times),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg, textAlign = TextAlign.Center),
            maxLines = 1,
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = data.nextPrayerAt?.let { TimeFormats.short(it) } ?: "—",
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WidgetAccent, textAlign = TextAlign.Center),
            maxLines = 1,
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = formatCountdown(data.countdownSeconds),
            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg, textAlign = TextAlign.Center),
            maxLines = 1,
        )
    }
}

// ---- Medium (180×180): adds location + a compact five-times grid ----

@Composable
private fun MediumContent(data: PrayerTimesWidgetData) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = context.getString(R.string.widget_next_prayer),
                style = TextStyle(fontSize = 11.sp, color = WidgetMuted),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = data.locationName,
                style = TextStyle(fontSize = 11.sp, color = WidgetMuted, textAlign = TextAlign.End),
                maxLines = 1,
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = data.nextPrayer?.let { context.getString(prayerNameRes(it)) } ?: "—",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = data.nextPrayerAt?.let { TimeFormats.short(it) } ?: "—",
                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WidgetAccent),
                maxLines = 1,
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = formatCountdown(data.countdownSeconds),
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg),
            maxLines = 1,
        )
        Spacer(GlanceModifier.height(8.dp))
        PrayerTimesGrid(data)
    }
}

// ---- Large (250×250): full table with the next prayer highlighted ----

@Composable
private fun LargeContent(data: PrayerTimesWidgetData) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.widget_next_prayer),
                    style = TextStyle(fontSize = 12.sp, color = WidgetMuted),
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = data.nextPrayer?.let { context.getString(prayerNameRes(it)) } ?: "—",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg),
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = data.nextPrayerAt?.let { TimeFormats.short(it) } ?: "—",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WidgetAccent),
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = formatCountdown(data.countdownSeconds),
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WidgetOnBg),
                    maxLines = 1,
                )
            }
        }
        Spacer(GlanceModifier.height(14.dp))
        PrayerTimesTable(data)
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = data.locationName,
            style = TextStyle(fontSize = 11.sp, color = WidgetMuted, textAlign = TextAlign.End),
            maxLines = 1,
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}

// ---- Shared pieces ----

/** Compact two-row grid of today's five times (used by the medium size). */
@Composable
private fun PrayerTimesGrid(data: PrayerTimesWidgetData) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        PrayerTimesGridRow(context, data, PrayerNames.subList(0, 3))
        Spacer(GlanceModifier.height(4.dp))
        PrayerTimesGridRow(context, data, PrayerNames.subList(3, 5))
    }
}

@Composable
private fun PrayerTimesGridRow(context: Context, data: PrayerTimesWidgetData, prayers: List<Prayer>) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        prayers.forEach { prayer ->
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = context.getString(prayerNameRes(prayer)),
                    style = TextStyle(fontSize = 10.sp, color = WidgetMuted),
                    maxLines = 1,
                )
                Text(
                    text = data.times[prayer]?.let { TimeFormats.short(it) } ?: "—",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = WidgetOnBg),
                    maxLines = 1,
                )
            }
        }
    }
}

/** Full five-row table, highlighting the next prayer (used by the large size). */
@Composable
private fun PrayerTimesTable(data: PrayerTimesWidgetData) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        PrayerNames.forEach { prayer ->
            val isNext = prayer == data.nextPrayer
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .then(if (isNext) GlanceModifier.background(WidgetHighlight).cornerRadius(8.dp) else GlanceModifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = context.getString(prayerNameRes(prayer)),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNext) WidgetOnBg else WidgetMuted,
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    text = data.times[prayer]?.let { TimeFormats.short(it) } ?: "—",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNext) WidgetAccent else WidgetOnBg,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

/** Maps a [Prayer] to its localized display-name resource. */
internal fun prayerNameRes(prayer: Prayer): Int = when (prayer) {
    Prayer.Fajr -> R.string.prayer_fajr
    Prayer.Sunrise -> R.string.prayer_sunrise
    Prayer.Dhuhr -> R.string.prayer_dhuhr
    Prayer.Asr -> R.string.prayer_asr
    Prayer.Maghrib -> R.string.prayer_maghrib
    Prayer.Isha -> R.string.prayer_isha
}

/** Short "HH:mm" formatting for a prayer time. */
internal object TimeFormats {
    private val shortFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    fun short(time: java.time.LocalTime): String = time.format(shortFormatter)
}
