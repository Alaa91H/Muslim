package org.muslim.app.feature.adhkar.overlay

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.muslim.app.feature.adhkar.R

/**
 * Small auto-dismissing window shown when the user taps the adhkar reminder
 * bubble (Android 11+). It presents the dhikr in a centered card, finishes on
 * tap, and folds away automatically after the configured duration.
 */
class AdhkarBubbleActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arabic = intent?.getStringExtra(EXTRA_ARABIC).orEmpty()
        val translation = intent?.getStringExtra(EXTRA_TRANSLATION).orEmpty()
        val source = intent?.getStringExtra(EXTRA_SOURCE).orEmpty()
        val durationSeconds = (intent?.getIntExtra(EXTRA_DURATION_SECONDS, DEFAULT_DURATION_SECONDS)
            ?: DEFAULT_DURATION_SECONDS).coerceIn(1, 600)

        if (arabic.isBlank()) {
            finish()
            return
        }

        val card = buildCard(arabic, translation, source).apply {
            setOnClickListener { finish() }
        }
        setContentView(
            FrameLayout(this).apply {
                setBackgroundColor(Color.argb(110, 0, 0, 0))
                addView(card, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ))
            },
        )
        handler.postDelayed({ finish() }, durationSeconds * 1_000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun buildCard(arabic: String, translation: String, source: String): LinearLayout {
        val density = resources.displayMetrics.density
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.adhkar_overlay_bg)
        }
        val padding = (16 * density).toInt()
        column.setPadding(padding, padding, padding, padding)

        column.addView(TextView(this).apply {
            text = arabic
            textSize = 22f
            setTextColor(Color.WHITE)
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

    companion object {
        const val DEFAULT_DURATION_SECONDS = 5
        const val EXTRA_ARABIC = "extra_arabic"
        const val EXTRA_TRANSLATION = "extra_translation"
        const val EXTRA_SOURCE = "extra_source"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }
}
