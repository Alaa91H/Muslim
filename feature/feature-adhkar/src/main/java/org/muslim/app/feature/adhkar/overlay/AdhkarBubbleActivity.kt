package org.muslim.app.feature.adhkar.overlay

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.muslim.app.core.common.lang.AppLanguage
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
        val backgroundColor = intent?.getIntExtra(EXTRA_BG_COLOR, DEFAULT_BG_COLOR) ?: DEFAULT_BG_COLOR
        val cornerRadiusDp = (intent?.getIntExtra(EXTRA_CORNER_RADIUS_DP, DEFAULT_CORNER_RADIUS_DP)
            ?: DEFAULT_CORNER_RADIUS_DP).coerceIn(0, 48)
        val fontSizeSp = (intent?.getIntExtra(EXTRA_FONT_SIZE_SP, DEFAULT_FONT_SIZE_SP) ?: DEFAULT_FONT_SIZE_SP)
            .coerceIn(14, 36)

        if (arabic.isBlank()) {
            finish()
            return
        }

        val card = buildCard(arabic, translation, source, backgroundColor, cornerRadiusDp, fontSizeSp).apply {
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

    private fun buildCard(
        arabic: String,
        translation: String,
        source: String,
        backgroundColor: Int,
        cornerRadiusDp: Int,
        fontSizeSp: Int,
    ): LinearLayout {
        val density = resources.displayMetrics.density
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = cornerRadiusDp * density
                setColor(backgroundColor)
                setStroke((1 * density).toInt(), 0x33FFFFFF.toInt())
            }
        }
        val padding = (16 * density).toInt()
        column.setPadding(padding, padding, padding, padding)

        column.addView(TextView(this).apply {
            text = arabic
            textSize = fontSizeSp.toFloat()
            setTextColor(Color.WHITE)
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

    companion object {
        const val DEFAULT_DURATION_SECONDS = 5
        val DEFAULT_BG_COLOR: Int = 0xE6282830.toInt()
        const val DEFAULT_CORNER_RADIUS_DP = 20
        const val DEFAULT_FONT_SIZE_SP = 22
        const val EXTRA_ARABIC = "extra_arabic"
        const val EXTRA_TRANSLATION = "extra_translation"
        const val EXTRA_SOURCE = "extra_source"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_BG_COLOR = "extra_bg_color"
        const val EXTRA_CORNER_RADIUS_DP = "extra_corner_radius_dp"
        const val EXTRA_FONT_SIZE_SP = "extra_font_size_sp"
    }
}
