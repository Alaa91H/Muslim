package org.example.islamicapp.feature.prayertimes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * System entry point for the prayer-times home-screen widget. Also kicks off
 * the periodic (15-minute) background refresh when the widget is added.
 */
class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = PrayerTimesWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefreshWorker.enqueue(context)
    }
}
