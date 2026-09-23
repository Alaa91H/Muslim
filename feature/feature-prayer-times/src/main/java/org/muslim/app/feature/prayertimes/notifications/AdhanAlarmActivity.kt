package org.muslim.app.feature.prayertimes.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.ui.prayerLabelRes

/**
 * Lightweight alarm surface shown while the Adhan is active.
 *
 * It deliberately owns hardware volume-key dismissal only while this surface is
 * visible. The app never installs an accessibility service or intercepts volume
 * keys globally.
 */
class AdhanAlarmActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockScreenPresentation()
        render(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        render(intent)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode in STOP_KEY_CODES) {
            stopAdhanAndClose()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun configureLockScreenPresentation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun render(intent: Intent?) {
        val prayer = intent?.getStringExtra(EXTRA_PRAYER)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
            ?: Prayer.Fajr

        val accent = getColor(R.color.adhan_accent)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_LOCALE
            setPadding(dp(32), dp(48), dp(32), dp(48))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(accent, Color.rgb(8, 25, 22)),
            )
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.adhan_notification_title)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        }, matchWrap())

        root.addView(TextView(this).apply {
            text = getString(prayerLabelRes(prayer))
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 38f)
            setPadding(0, dp(12), 0, dp(10))
        }, matchWrap())

        root.addView(TextView(this).apply {
            text = getString(
                R.string.adhan_notification_big_text,
                getString(prayerLabelRes(prayer)),
            )
            gravity = Gravity.CENTER
            setTextColor(Color.argb(220, 255, 255, 255))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(0, 0, 0, dp(32))
        }, matchWrap())

        root.addView(Button(this).apply {
            text = getString(R.string.adhan_notification_stop)
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(accent)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            minHeight = dp(56)
            setPadding(dp(28), 0, dp(28), 0)
            setOnClickListener { stopAdhanAndClose() }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))

        setContentView(root)
    }

    private fun stopAdhanAndClose() {
        AdhanPlaybackService.stop(applicationContext)
        finishAndRemoveTask()
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()

    companion object {
        private const val EXTRA_PRAYER = "extra_prayer"

        private val STOP_KEY_CODES = setOf(
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE,
        )

        fun intent(context: Context, prayer: Prayer): Intent =
            Intent(context, AdhanAlarmActivity::class.java)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
    }
}
