package org.example.islamicapp.feature.tasbih.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import org.example.islamicapp.feature.tasbih.R
import org.example.islamicapp.feature.tasbih.data.TasbihRepository

/**
 * Home-screen tasbih widget (PROJECT_PROMPT.md §6 Phase 4): tap anywhere to
 * count — the total is persisted to the same DataStore the app screen reads,
 * so phone and widget always agree.
 */
class TasbihWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext, TasbihWidgetEntryPoint::class.java,
        ).tasbihRepository()
        val stats = repository.stats.first()

        provideContent {
            TasbihWidgetContent(today = stats.today, target = stats.target)
        }
    }

    @Composable
    private fun TasbihWidgetContent(today: Int, target: Int) {
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(ColorProvider(android.graphics.Color.TRANSPARENT))
                .clickable(actionRunCallback<TasbihTapAction>())
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = today.toString(),
                style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = context.getString(R.string.tasbih_of_target, target),
                style = TextStyle(fontSize = 12.sp),
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = context.getString(R.string.tasbih_today_widget),
                style = TextStyle(fontSize = 11.sp),
            )
        }
    }

    companion object {
        /** Re-renders every widget instance (called after each tap). */
        suspend fun refresh(context: Context) {
            TasbihWidget().updateAll(context)
        }
    }
}

/** Counts one tasbih right on the home screen without opening the app. */
class TasbihTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext, TasbihWidgetEntryPoint::class.java,
        ).tasbihRepository()
        repository.addToToday(1)
        TasbihWidget().update(context, glanceId)
    }
}

class TasbihWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasbihWidget()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TasbihWidgetEntryPoint {
    fun tasbihRepository(): TasbihRepository
}
