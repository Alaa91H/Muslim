package org.muslim.app.feature.learn.data

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import org.muslim.app.feature.learn.domain.WillDraft
import java.io.FileOutputStream

fun printWillDraft(
    context: Context,
    draft: WillDraft,
    isArabic: Boolean,
    jobName: String,
): Boolean = runCatching {
    val printManager = context.getSystemService(PrintManager::class.java)
    val adapter = WillDraftPrintAdapter(
        draft = draft,
        isArabic = isArabic,
        jobName = jobName,
    )
    val attributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
        .build()
    printManager.print(jobName, adapter, attributes)
}.isSuccess

private class WillDraftPrintAdapter(
    private val draft: WillDraft,
    private val isArabic: Boolean,
    private val jobName: String,
) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }

        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build(),
            oldAttributes != newAttributes,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onWriteCancelled()
            return
        }

        runCatching {
            FileOutputStream(destination.fileDescriptor).use { output ->
                writeWillDraftPdf(
                    output = output,
                    document = draft.toDocument(isArabic),
                    isArabic = isArabic,
                )
            }
        }.onSuccess {
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }.onFailure { error ->
            callback.onWriteFailed(error.message)
        }
    }
}
