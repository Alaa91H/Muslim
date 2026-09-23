package org.muslim.app.feature.tasbih.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.ui.theme.WidgetOrnamentSpec
import org.muslim.app.core.ui.theme.widgetOrnamentSpec
import org.muslim.app.feature.tasbih.R
import org.muslim.app.feature.tasbih.data.TasbihRepository

private val DayBackground = Color(0xFF0E3B2A)
private val DayForeground = Color(0xFFFFFFFF)
private val DayAccent = Color(0xFF9FD8C0)

/**
 * Home-screen misbaha widget (PROJECT_PROMPT.md §6 Phase 4: "Widget
 * للمسبحة"): shows today's count and target; tapping the widget counts one
 * more via [MisbahaIncrementAction].
 */
class MisbahaWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            MisbahaWidgetEntryPoint::class.java,
        )
        val repository = entryPoint.tasbihRepository()
        val state = repository.state.first()
        val appPreferences = entryPoint.appPreferencesRepository().preferences.first()
        val ornament = widgetOrnamentSpec(
            style = appPreferences.ornamentStyle,
            intensity = appPreferences.ornamentIntensity,
        )
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(DayBackground))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
                    .clickable(actionRunCallback<MisbahaIncrementAction>()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MisbahaOrnamentAccent(ornament)
                Text(
                    text = state.phrase.text,
                    style = TextStyle(color = ColorProvider(DayAccent), fontSize = 12.sp),
                    maxLines = 1,
                )
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    text = state.count.toString(),
                    style = TextStyle(color = ColorProvider(DayForeground), fontSize = 34.sp, fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "/ ${state.target.toString()}",
                    style = TextStyle(color = ColorProvider(DayAccent), fontSize = 13.sp),
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = LocalContext.current.getString(R.string.misbaha_widget_tap_hint),
                    style = TextStyle(color = ColorProvider(DayAccent), fontSize = 11.sp),
                    maxLines = 1,
                )
            }
        }
    }

    companion object {
        suspend fun update(context: Context) = MisbahaWidget().updateAll(context)
    }
}

suspend fun refreshMisbahaWidgets(context: Context) {
    MisbahaWidget.update(context)
}

@androidx.compose.runtime.Composable
private fun MisbahaOrnamentAccent(ornament: WidgetOrnamentSpec?) {
    if (ornament == null) return
    Image(
        provider = ImageProvider(ornament.drawableRes),
        contentDescription = null,
        colorFilter = ColorFilter.tint(
            ColorProvider(Color(0xFFB49A62).copy(alpha = ornament.tintAlpha)),
        ),
        modifier = GlanceModifier.size(12.dp),
    )
    Spacer(GlanceModifier.height(3.dp))
}

/** Tap action: count one more and re-render every widget instance. */
class MisbahaIncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            MisbahaWidgetEntryPoint::class.java,
        ).tasbihRepository()
        val state = repository.state.first()
        repository.increment(state.phrase)
        MisbahaWidget.update(context)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MisbahaWidgetEntryPoint {
    fun tasbihRepository(): TasbihRepository
    fun appPreferencesRepository(): AppPreferencesRepository
}
