package org.muslim.app.feature.adhkar.overlay

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.muslim.app.core.common.lang.AppLanguage
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.adhkar.R
import org.muslim.app.feature.adhkar.data.AdhkarSpeechController
import org.muslim.app.feature.adhkar.domain.Dhikr

/**
 * Floating adhkar reminder shown ABOVE all apps ([WindowManager] overlay,
 * [android.Manifest.permission.SYSTEM_ALERT_WINDOW]). It auto-dismisses after
 * the user-configured duration (default 5 seconds) and dismisses instantly on
 * tap. Runs as a [Service] so it keeps showing while the app is in the
 * background. When read-aloud is enabled, the same dhikr is spoken
 * automatically and the overlay closes only after speech finishes.
 */
@AndroidEntryPoint
class AdhkarOverlayService : Service() {

    @Inject
    lateinit var speechController: AdhkarSpeechController

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var dismissRunnable: Runnable? = null
    private var speechJob: Job? = null
    private var activeSpeechUtteranceId: String? = null
    private var activeStartId: Int = 0

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
        val backgroundColor = intent?.getIntExtra(EXTRA_BG_COLOR, DEFAULT_BG_COLOR) ?: DEFAULT_BG_COLOR
        val cornerRadiusDp = (intent?.getIntExtra(EXTRA_CORNER_RADIUS_DP, DEFAULT_CORNER_RADIUS_DP)
            ?: DEFAULT_CORNER_RADIUS_DP).coerceIn(0, 48)
        val fontSizeSp = (intent?.getIntExtra(EXTRA_FONT_SIZE_SP, DEFAULT_FONT_SIZE_SP) ?: DEFAULT_FONT_SIZE_SP)
            .coerceIn(14, 36)
        val readAloud = intent?.getBooleanExtra(EXTRA_READ_ALOUD, false) ?: false
        val speechVoiceName = intent?.getStringExtra(EXTRA_SPEECH_VOICE_NAME)
        val speechRate = (intent?.getFloatExtra(EXTRA_SPEECH_RATE, DEFAULT_SPEECH_RATE) ?: DEFAULT_SPEECH_RATE)
            .coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        val speechAllowNetworkVoices =
            intent?.getBooleanExtra(EXTRA_SPEECH_ALLOW_NETWORK, false) ?: false

        activeStartId = startId
        clearDismissTimer()
        cancelSpeechForCurrentOverlay()

        NotificationChannels.create(this)
        startForeground(NOTIFICATION_ID, foregroundNotification(arabic))

        if (arabic.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay(arabic, translation, source, backgroundColor, cornerRadiusDp, fontSizeSp)
        if (readAloud) {
            startAutomaticReading(
                arabic = arabic,
                voiceName = speechVoiceName,
                speechRate = speechRate,
                allowNetworkVoices = speechAllowNetworkVoices,
                fallbackDurationSeconds = durationSeconds,
                startId = startId,
            )
        } else {
            scheduleDismiss(startId, durationSeconds * 1_000L)
        }
        return START_NOT_STICKY
    }

    /**
     * Waits briefly for a newly-created TTS engine, reads the exact dhikr shown
     * in the overlay, then closes this specific service start after playback.
     * Any TTS initialization/playback failure falls back to the configured
     * visual duration instead of leaving the overlay on screen.
     */
    private fun startAutomaticReading(
        arabic: String,
        voiceName: String?,
        speechRate: Float,
        allowNetworkVoices: Boolean,
        fallbackDurationSeconds: Int,
        startId: Int,
    ) {
        scheduleDismiss(startId, TTS_INIT_TIMEOUT_MS + fallbackDurationSeconds * 1_000L)
        speechJob = serviceScope.launch {
            val readiness = withTimeoutOrNull(TTS_INIT_TIMEOUT_MS) {
                combine(
                    speechController.ready,
                    speechController.initializationFailed,
                ) { ready, failed -> ready to failed }
                    .first { (ready, failed) -> ready || failed }
            }
            if (activeStartId != startId) return@launch
            if (readiness?.first != true) {
                scheduleDismiss(startId, fallbackDurationSeconds * 1_000L)
                return@launch
            }

            val utteranceId = "$OVERLAY_UTTERANCE_PREFIX$startId"
            activeSpeechUtteranceId = utteranceId
            val started = speechController.speak(
                text = arabic,
                voiceName = voiceName,
                rate = speechRate,
                allowNetworkVoices = allowNetworkVoices,
                utteranceId = utteranceId,
            )
            if (!started) {
                activeSpeechUtteranceId = null
                scheduleDismiss(startId, fallbackDurationSeconds * 1_000L)
                return@launch
            }

            // Speech now owns the lifetime of the overlay, so the fixed visual
            // timer must not hide the dhikr while it is still being recited.
            clearDismissTimer()
            speechController.activeUtteranceId.first { it != utteranceId }
            if (activeStartId != startId) return@launch

            activeSpeechUtteranceId = null
            delay(POST_SPEECH_DISMISS_DELAY_MS)
            if (activeStartId == startId) {
                stopSelfResult(startId)
            }
        }
    }

    private fun scheduleDismiss(startId: Int, delayMillis: Long) {
        clearDismissTimer()
        val runnable = Runnable {
            if (activeStartId == startId) {
                stopSelfResult(startId)
            }
        }
        dismissRunnable = runnable
        handler.postDelayed(runnable, delayMillis)
    }

    private fun clearDismissTimer() {
        dismissRunnable?.let(handler::removeCallbacks)
        dismissRunnable = null
    }

    private fun cancelSpeechForCurrentOverlay() {
        speechJob?.cancel()
        speechJob = null
        activeSpeechUtteranceId?.let(speechController::stop)
        activeSpeechUtteranceId = null
    }

    /** Renders the dhikr card and attaches it to the window manager. */
    private fun showOverlay(
        arabic: String,
        translation: String,
        source: String,
        backgroundColor: Int,
        cornerRadiusDp: Int,
        fontSizeSp: Int,
    ) {
        removeOverlay()
        val view = buildOverlayView(arabic, translation, source, backgroundColor, cornerRadiusDp, fontSizeSp)
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

    private fun buildOverlayView(
        arabic: String,
        translation: String,
        source: String,
        backgroundColor: Int,
        cornerRadiusDp: Int,
        fontSizeSp: Int,
    ): View {
        val density = resources.displayMetrics.density
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = cornerRadiusDp * density
                setColor(backgroundColor)
                setStroke((1 * density).toInt(), 0x33FFFFFF.toInt())
            }
            setOnClickListener { stopSelf() }
        }
        val padding = (16 * density).toInt()
        column.setPadding(padding, padding, padding, padding)

        column.addView(TextView(this).apply {
            text = arabic
            textSize = fontSizeSp.toFloat()
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ))

        // English fallback hidden for Arabic readers (each language shows its
        // own texts — never an English rendering of the Arabic original).
        if (AppLanguage.showEnglishFallback() && translation.isNotBlank()) {
            column.addView(TextView(this).apply {
                text = translation
                textSize = (fontSizeSp - 8).coerceAtLeast(12).toFloat()
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
                textSize = (fontSizeSp - 10).coerceAtLeast(10).toFloat()
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
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
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
        clearDismissTimer()
        cancelSpeechForCurrentOverlay()
        serviceScope.cancel()
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 3001
        const val DEFAULT_DURATION_SECONDS = 5
        val DEFAULT_BG_COLOR: Int = 0xE6282830.toInt()
        const val DEFAULT_CORNER_RADIUS_DP = 20
        const val DEFAULT_FONT_SIZE_SP = 22
        const val DEFAULT_SPEECH_RATE = 1.0f

        private const val MIN_SPEECH_RATE = 0.5f
        private const val MAX_SPEECH_RATE = 2.0f
        private const val TTS_INIT_TIMEOUT_MS = 4_000L
        private const val POST_SPEECH_DISMISS_DELAY_MS = 450L
        private const val OVERLAY_UTTERANCE_PREFIX = "adhkar-overlay-"

        private const val EXTRA_ARABIC = "extra_arabic"
        private const val EXTRA_TRANSLATION = "extra_translation"
        private const val EXTRA_SOURCE = "extra_source"
        private const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        private const val EXTRA_BG_COLOR = "extra_bg_color"
        private const val EXTRA_CORNER_RADIUS_DP = "extra_corner_radius_dp"
        private const val EXTRA_FONT_SIZE_SP = "extra_font_size_sp"
        private const val EXTRA_READ_ALOUD = "extra_read_aloud"
        private const val EXTRA_SPEECH_VOICE_NAME = "extra_speech_voice_name"
        private const val EXTRA_SPEECH_RATE = "extra_speech_rate"
        private const val EXTRA_SPEECH_ALLOW_NETWORK = "extra_speech_allow_network"

        /**
         * Shows [dhikr] above all apps. With [readAloud] disabled, the card
         * follows [durationSeconds]. With [readAloud] enabled, it reads the same
         * Arabic text automatically and closes after speech completes.
         */
        fun start(
            context: Context,
            dhikr: Dhikr,
            durationSeconds: Int,
            backgroundColor: Int = DEFAULT_BG_COLOR,
            cornerRadiusDp: Int = DEFAULT_CORNER_RADIUS_DP,
            fontSizeSp: Int = DEFAULT_FONT_SIZE_SP,
            readAloud: Boolean = false,
            speechVoiceName: String? = null,
            speechRate: Float = DEFAULT_SPEECH_RATE,
            speechAllowNetworkVoices: Boolean = false,
        ) {
            val intent = Intent(context, AdhkarOverlayService::class.java)
                .putExtra(EXTRA_ARABIC, dhikr.arabic)
                .putExtra(EXTRA_TRANSLATION, dhikr.translation)
                .putExtra(EXTRA_SOURCE, dhikr.source)
                .putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                .putExtra(EXTRA_BG_COLOR, backgroundColor)
                .putExtra(EXTRA_CORNER_RADIUS_DP, cornerRadiusDp)
                .putExtra(EXTRA_FONT_SIZE_SP, fontSizeSp)
                .putExtra(EXTRA_READ_ALOUD, readAloud)
                .putExtra(EXTRA_SPEECH_VOICE_NAME, speechVoiceName)
                .putExtra(EXTRA_SPEECH_RATE, speechRate)
                .putExtra(EXTRA_SPEECH_ALLOW_NETWORK, speechAllowNetworkVoices)
            context.startForegroundService(intent)
        }
    }
}
