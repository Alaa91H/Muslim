package org.muslim.app.feature.adhkar.overlay

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.domain.Dhikr

/**
 * Floating adhkar reminder shown ABOVE all apps ([WindowManager] overlay,
 * [android.Manifest.permission.SYSTEM_ALERT_WINDOW]). It auto-dismisses after
 * the user-configured duration (default 5 seconds) and dismisses instantly on
 * tap. Runs as a [Service] so it keeps showing while the app is in the
 * background.
 */
@AndroidEntryPoint
class AdhkarOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())

    private val dismissRunnable = Runnable { stopSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val arabic = intent?.getStringExtra(EXTRA_ARABIC).orEmpty()
        val translation = intent?.getStringExtra(EXTRA_TRANSLATION).orEmpty()
        val source = intent?.getStringExtra(EXTRA_SOURCE).orEmpty()
        val durationSeconds = (intent?.getIntExtra(EXTRA_DURATION_SECONDS, DEFAULT_DURATION_SECONDS) ?: DEFAULT_DURATION_SECONDS)
            .coerceIn(1, 600)

        NotificationChannels.create(this)
        startForeground(NOTIFICATION_ID, foregroundNotification(arabic))

        if (arabic.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay(arabic, translation, source)
        handler.removeCallbacks(dismissRunnable)
        handler.postDelayed(dismissRunnable, durationSeconds * 1_000L)
        return START_NOT_STICKY
    }

    /** Renders the dhikr card and attaches it to the window manager. */
    private fun showOverlay(arabic: String, translation: String, source: String) {
        removeOverlay()
        val view = buildOverlayView(arabic, translation, source)
        overlayView = view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (60 * resources.displayMetrics.density).toInt()
        }
        runCatching { windowManager.addView(view, params) }
            .onFailure { stopSelf() }
    }

    private fun buildOverlayView(arabic: String, translation: String, source: String): View {
        val density = resources.displayMetrics.density
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.adhkar_overlay_bg)
            setOnClickListener { stopSelf() }
        }
        val padding = (16 * density).toInt()
        column.setPadding(padding, padding, padding, padding)

        column.addView(TextView(this).apply {
            text = arabic
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

        if (translation.isNotBlank()) {
            column.addView(TextView(this).apply {
                text = translation
                textSize = 14f
                setTextColor(0xFFB8BEC9.toInt())
                gravity = Gravity.CENTER
                setPadding(0, (6 * density).toInt(), 0, 0)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        if (source.isNotBlank()) {
            column.addView(TextView(this).apply {
                text = source
                textSize = 12f
                setTextColor(0xFF8A93A3.toInt())
                gravity = Gravity.CENTER
                setPadding(0, (8 * density).toInt(), 0, 0)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        return column
    }

    private fun foregroundNotification(arabic: String?): Notification =
        NotificationCompat.Builder(this, NotificationChannels.ADHKAR)
            .setSmallIcon(R.drawable.ic_adhkar_notification)
            .setContentTitle(getString(R.string.adhkar_overlay_notification_title))
            .setContentText(arabic ?: getString(R.string.adhkar_overlay_notification_text))
            .setOngoing(false)
            .setAutoCancel(false)
            .build()

    private fun removeOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
            overlayView = null
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(dismissRunnable)
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 3001
        const val DEFAULT_DURATION_SECONDS = 5

        private const val EXTRA_ARABIC = "extra_arabic"
        private const val EXTRA_TRANSLATION = "extra_translation"
        private const val EXTRA_SOURCE = "extra_source"
        private const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"

        /** Shows [dhikr] above all apps for [durationSeconds]; dismisses on tap. */
        fun start(context: Context, dhikr: Dhikr, durationSeconds: Int) {
            val intent = Intent(context, AdhkarOverlayService::class.java)
                .putExtra(EXTRA_ARABIC, dhikr.arabic)
                .putExtra(EXTRA_TRANSLATION, dhikr.translation)
                .putExtra(EXTRA_SOURCE, dhikr.source)
                .putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            context.startForegroundService(intent)
        }
    }
}
