package org.muslim.app.feature.learn.data

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
                writeWillDraftPdf(
                    output = it,
                    document = draft.toDocument(isArabic),
                    isArabic = isArabic,
                )
            }
        }.isSuccess
    }
}

internal fun writeWillDraftPdf(
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

private class WillDraftPdfRenderer(
    private val pdf: PdfDocument,
    private val isArabic: Boolean,
) {
    private val titlePaint = textPaint(24f, bold = true, color = Ink)
    private val subtitlePaint = textPaint(11f, color = MutedInk)
    private val badgePaint = textPaint(9.5f, bold = true, color = AccentDark)
    private val sectionPaint = textPaint(15f, bold = true, color = AccentDark)
    private val labelPaint = textPaint(10.5f, bold = true, color = Ink)
    private val bodyPaint = textPaint(11f, color = Color.BLACK)
    private val notePaint = textPaint(9.5f, color = MutedInk)
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Divider
        strokeWidth = 1f
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Accent }
    private val softAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = SoftAccent }
    private val noticePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NoticeBackground }

    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    private var y = TopMargin

    fun render(document: WillDraftDocument) {
        startPage()
        drawDocumentHeader(document)

        document.sections
            .filter { it.fields.isNotEmpty() }
            .forEach(::drawSection)

        drawReminder(
            title = if (isArabic) "الخصوصية" else "Privacy",
            text = document.privacyReminder,
        )
        drawReminder(
            title = if (isArabic) "المشاركة" else "Sharing",
            text = document.sharingReminder,
        )
        finishPage()
    }

    private fun drawDocumentHeader(document: WillDraftDocument) {
        val currentPage = requireNotNull(page)
        currentPage.canvas.drawRect(
            0f,
            0f,
            PageWidth,
            AccentBarHeight,
            accentPaint,
        )
        drawBrand(currentPage)

        drawBlock(document.title, titlePaint, spacingAfter = 7)
        drawBlock(document.subtitle, subtitlePaint, spacingAfter = 10)
        drawBadge(
            if (isArabic) {
                "نسخة تنظيمية خاصة — غير موقعة"
            } else {
                "Private organisational copy — unsigned"
            },
        )
        y += 8f
        drawDivider()
    }

    private fun drawBrand(currentPage: PdfDocument.Page) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AccentDark
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val x = if (isArabic) PageWidth - RightMargin else LeftMargin
        currentPage.canvas.drawText("Muslim", x, BrandBaseline, paint)
    }

    private fun drawBadge(text: String) {
        val layout = layoutFor(text, badgePaint, BadgeTextWidth)
        ensureSpace(layout.height + 14f)
        val currentPage = requireNotNull(page)
        val badgeWidth = layout.width + 18f
        val left = if (isArabic) {
            PageWidth - RightMargin - badgeWidth
        } else {
            LeftMargin
        }
        val rect = RectF(
            left,
            y,
            left + badgeWidth,
            y + layout.height + 10f,
        )
        currentPage.canvas.drawRoundRect(rect, 8f, 8f, softAccentPaint)
        drawLayout(
            layout = layout,
            x = left + 9f,
            top = y + 5f,
        )
        y += rect.height()
    }

    private fun drawSection(section: org.muslim.app.feature.learn.domain.WillDraftDocumentSection) {
        ensureSpace(MinSectionSpace)
        drawSectionHeader(section.title)
        section.fields.forEach { field ->
            drawBlock(field.label, labelPaint, spacingAfter = 3)
            drawBlock(field.value, bodyPaint, spacingAfter = 10)
        }
        drawDivider()
    }

    private fun drawSectionHeader(title: String) {
        val layout = layoutFor(title, sectionPaint)
        ensureSpace(layout.height + 16f)
        val currentPage = requireNotNull(page)
        currentPage.canvas.drawRoundRect(
            RectF(
                LeftMargin,
                y - 3f,
                PageWidth - RightMargin,
                y + layout.height + 9f,
            ),
            7f,
            7f,
            softAccentPaint,
        )
        drawLayout(layout, x = LeftMargin + 10f, top = y + 3f)
        y += layout.height + 15f
    }

    private fun drawReminder(
        title: String,
        text: String,
    ) {
        val titleLayout = layoutFor(title, labelPaint, NoticeTextWidth)
        val textLayout = layoutFor(text, notePaint, NoticeTextWidth)
        val height = titleLayout.height + textLayout.height + 24f
        ensureSpace(height + 8f)

        val currentPage = requireNotNull(page)
        currentPage.canvas.drawRoundRect(
            RectF(
                LeftMargin,
                y,
                PageWidth - RightMargin,
                y + height,
            ),
            8f,
            8f,
            noticePaint,
        )
        drawLayout(titleLayout, x = LeftMargin + 12f, top = y + 8f)
        drawLayout(
            textLayout,
            x = LeftMargin + 12f,
            top = y + titleLayout.height + 12f,
        )
        y += height + 8f
    }

    private fun drawDivider() {
        ensurePage()
        val currentPage = requireNotNull(page)
        currentPage.canvas.drawLine(
            LeftMargin,
            y,
            PageWidth - RightMargin,
            y,
            dividerPaint,
        )
        y += 13f
    }

    private fun drawBlock(
        text: String,
        paint: TextPaint,
        spacingAfter: Int,
    ) {
        var remaining = text.trim()
        while (remaining.isNotEmpty()) {
            ensurePage()
            val available = ContentBottom - y
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
            val chunkLayout = layoutFor(chunk, paint)
            drawLayout(chunkLayout)
            y += chunkLayout.height
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

    private fun drawLayout(
        layout: StaticLayout,
        x: Float = LeftMargin,
        top: Float = y,
    ) {
        val canvas = requireNotNull(page).canvas
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun layoutFor(
        text: String,
        paint: TextPaint,
        width: Int = ContentWidth,
    ): StaticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setTextDirection(
            if (isArabic) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR,
        )
        .setLineSpacing(0f, 1.16f)
        .setIncludePad(false)
        .build()

    private fun ensureSpace(minimumHeight: Float) {
        ensurePage()
        if (ContentBottom - y < minimumHeight) {
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
                PageWidth.toInt(),
                PageHeight.toInt(),
                pageNumber,
            ).create(),
        )
        y = TopMargin
        requireNotNull(page).canvas.drawRect(
            0f,
            0f,
            PageWidth,
            AccentBarHeight,
            accentPaint,
        )
    }

    private fun finishPage() {
        val currentPage = page ?: return
        drawFooter(currentPage)
        pdf.finishPage(currentPage)
        page = null
    }

    private fun drawFooter(currentPage: PdfDocument.Page) {
        currentPage.canvas.drawLine(
            LeftMargin,
            PageHeight - 42f,
            PageWidth - RightMargin,
            PageHeight - 42f,
            dividerPaint,
        )
        val pageLabel = if (isArabic) "صفحة $pageNumber" else "Page $pageNumber"
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MutedInk
            textSize = 9f
            textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val x = if (isArabic) PageWidth - RightMargin else LeftMargin
        currentPage.canvas.drawText(
            "$pageLabel  •  Muslim",
            x,
            PageHeight - FooterMargin,
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
        val Ink: Int = Color.rgb(26, 39, 43)
        val MutedInk: Int = Color.rgb(83, 94, 98)
        val Accent: Int = Color.rgb(38, 111, 123)
        val AccentDark: Int = Color.rgb(24, 79, 90)
        val SoftAccent: Int = Color.rgb(231, 242, 244)
        val NoticeBackground: Int = Color.rgb(246, 248, 248)
        val Divider: Int = Color.rgb(194, 203, 206)

        const val PageWidth = 595f
        const val PageHeight = 842f
        const val LeftMargin = 48f
        const val RightMargin = 48f
        const val TopMargin = 48f
        const val FooterMargin = 24f
        const val AccentBarHeight = 10f
        const val BrandBaseline = 31f
        const val ContentBottom = PageHeight - 54f
        const val ContentWidth = (PageWidth - LeftMargin - RightMargin).toInt()
        const val BadgeTextWidth = ContentWidth - 18
        const val NoticeTextWidth = ContentWidth - 24
        const val MinSectionSpace = 100f
    }
}
