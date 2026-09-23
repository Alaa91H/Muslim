package org.muslim.app.feature.learn.data

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muslim.app.feature.learn.domain.WillDraft
import org.muslim.app.feature.learn.domain.WillDraftDocument
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a private, user-chosen PDF copy of the will draft.
 *
 * The destination is supplied by Android's Storage Access Framework, so the
 * app does not need broad storage permissions and does not silently create
 * copies in shared storage.
 */
@Singleton
class WillDraftPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun export(
        destination: Uri,
        draft: WillDraft,
        isArabic: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val output = requireNotNull(
                context.contentResolver.openOutputStream(destination, "w"),
            )
            output.use {
                writeDocument(
                    output = it,
                    document = draft.toDocument(isArabic),
                    isArabic = isArabic,
                )
            }
        }.isSuccess
    }

    private fun writeDocument(
        output: OutputStream,
        document: WillDraftDocument,
        isArabic: Boolean,
    ) {
        val pdf = PdfDocument()
        try {
            WillDraftPdfRenderer(
                pdf = pdf,
                isArabic = isArabic,
            ).render(document)
            pdf.writeTo(output)
        } finally {
            pdf.close()
        }
    }
}

private class WillDraftPdfRenderer(
    private val pdf: PdfDocument,
    private val isArabic: Boolean,
) {
    private val titlePaint = textPaint(
        size = 22f,
        bold = true,
        color = Color.rgb(30, 44, 48),
    )
    private val subtitlePaint = textPaint(
        size = 11f,
        color = Color.rgb(80, 90, 94),
    )
    private val sectionPaint = textPaint(
        size = 15f,
        bold = true,
        color = Color.rgb(25, 83, 96),
    )
    private val labelPaint = textPaint(
        size = 11f,
        bold = true,
        color = Color.rgb(45, 52, 55),
    )
    private val bodyPaint = textPaint(
        size = 11f,
        color = Color.BLACK,
    )
    private val notePaint = textPaint(
        size = 9.5f,
        color = Color.rgb(80, 90, 94),
    )
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(190, 198, 201)
        strokeWidth = 1f
    }

    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    private var y = TOP_MARGIN

    fun render(document: WillDraftDocument) {
        startPage()
        drawBlock(document.title, titlePaint, spacingAfter = 8)
        drawBlock(document.subtitle, subtitlePaint, spacingAfter = 14)
        drawDivider()

        document.sections
            .filter { it.fields.isNotEmpty() }
            .forEach { section ->
                ensureSpace(MIN_SECTION_SPACE)
                drawBlock(section.title, sectionPaint, spacingAfter = 8)
                section.fields.forEach { field ->
                    drawBlock(field.label, labelPaint, spacingAfter = 3)
                    drawBlock(field.value, bodyPaint, spacingAfter = 10)
                }
                drawDivider()
            }

        ensureSpace(MIN_NOTE_SPACE)
        drawBlock(document.privacyReminder, notePaint, spacingAfter = 6)
        drawBlock(document.sharingReminder, notePaint, spacingAfter = 0)
        finishPage()
    }

    private fun drawDivider() {
        ensurePage()
        val currentPage = requireNotNull(page)
        currentPage.canvas.drawLine(
            LEFT_MARGIN,
            y,
            PAGE_WIDTH - RIGHT_MARGIN,
            y,
            dividerPaint,
        )
        y += 12f
    }

    private fun drawBlock(
        text: String,
        paint: TextPaint,
        spacingAfter: Int,
    ) {
        var remaining = text.trim()
        while (remaining.isNotEmpty()) {
            ensurePage()
            val available = CONTENT_BOTTOM - y
            if (available < paint.textSize * 1.8f) {
                newPage()
                continue
            }

            val layout = layoutFor(remaining, paint)
            if (layout.height <= available) {
                drawLayout(layout)
                y += layout.height + spacingAfter
                return
            }

            val lastLine = findLastFittingLine(layout, available)
            if (lastLine < 0) {
                newPage()
                continue
            }

            val end = layout.getLineEnd(lastLine)
            val chunk = remaining.substring(0, end).trimEnd()
            drawLayout(layoutFor(chunk, paint))
            remaining = remaining.substring(end).trimStart()
            newPage()
        }
    }

    private fun findLastFittingLine(
        layout: StaticLayout,
        availableHeight: Float,
    ): Int {
        var result = -1
        for (line in 0 until layout.lineCount) {
            if (layout.getLineBottom(line) <= availableHeight) {
                result = line
            } else {
                break
            }
        }
        return result
    }

    private fun drawLayout(layout: StaticLayout) {
        val currentPage = requireNotNull(page)
        val canvas = currentPage.canvas
        canvas.save()
        canvas.translate(LEFT_MARGIN, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun layoutFor(
        text: String,
        paint: TextPaint,
    ): StaticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, CONTENT_WIDTH)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setTextDirection(
            if (isArabic) {
                TextDirectionHeuristics.RTL
            } else {
                TextDirectionHeuristics.LTR
            },
        )
        .setLineSpacing(0f, 1.15f)
        .setIncludePad(false)
        .build()

    private fun ensureSpace(minimumHeight: Float) {
        ensurePage()
        if (CONTENT_BOTTOM - y < minimumHeight) {
            newPage()
        }
    }

    private fun ensurePage() {
        if (page == null) {
            startPage()
        }
    }

    private fun newPage() {
        finishPage()
        startPage()
    }

    private fun startPage() {
        pageNumber += 1
        page = pdf.startPage(
            PdfDocument.PageInfo.Builder(
                PAGE_WIDTH.toInt(),
                PAGE_HEIGHT.toInt(),
                pageNumber,
            ).create(),
        )
        y = TOP_MARGIN
    }

    private fun finishPage() {
        val currentPage = page ?: return
        drawFooter(currentPage)
        pdf.finishPage(currentPage)
        page = null
    }

    private fun drawFooter(currentPage: PdfDocument.Page) {
        val footer = if (isArabic) {
            "صفحة $pageNumber"
        } else {
            "Page $pageNumber"
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(105, 112, 115)
            textSize = 9f
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val x = if (isArabic) {
            PAGE_WIDTH - RIGHT_MARGIN
        } else {
            LEFT_MARGIN
        }
        currentPage.canvas.drawText(
            footer,
            x,
            PAGE_HEIGHT - FOOTER_MARGIN,
            footerPaint,
        )
    }

    private fun textPaint(
        size: Float,
        bold: Boolean = false,
        color: Int,
    ): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) {
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        } else {
            Typeface.DEFAULT
        }
    }

    private companion object {
        const val PAGE_WIDTH = 595f
        const val PAGE_HEIGHT = 842f
        const val LEFT_MARGIN = 48f
        const val RIGHT_MARGIN = 48f
        const val TOP_MARGIN = 48f
        const val FOOTER_MARGIN = 28f
        const val CONTENT_BOTTOM = PAGE_HEIGHT - 50f
        const val CONTENT_WIDTH = (PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN).toInt()
        const val MIN_SECTION_SPACE = 95f
        const val MIN_NOTE_SPACE = 70f
    }
}
