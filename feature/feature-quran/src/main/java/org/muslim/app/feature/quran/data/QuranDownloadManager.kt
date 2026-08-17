package org.muslim.app.feature.quran.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** What a download request covers. */
enum class DownloadScope { Ayah, Surah, FullQuran }

/** One queued/running/finished download shown in the downloads screen. */
enum class DownloadStatus { Queued, Downloading, Completed, Failed }

/** A user-initiated recitation download request. */
data class DownloadRequest(
    val id: String,
    val reciterId: String,
    val reciterName: String,
    val scope: DownloadScope,
    val surahNumber: Int?,
    val globalNumber: Int?,
    val label: String,
    val totalBytes: Long,
)

/** Observable snapshot of one download task. */
data class DownloadTaskUi(
    val id: String,
    val label: String,
    val reciterName: String,
    val progress: Float,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val status: DownloadStatus,
)

/**
 * Single source of truth for recitation downloads. The UI observes [tasks];
 * enqueuing starts [QuranDownloadService] (a foreground service that keeps the
 * download running in the background and posts a progress notification).
 */
@Singleton
class QuranDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val _tasks = MutableStateFlow<List<DownloadTaskUi>>(emptyList())
    val tasks: StateFlow<List<DownloadTaskUi>> = _tasks.asStateFlow()

    fun enqueue(request: DownloadRequest) {
        upsert(
            DownloadTaskUi(
                id = request.id,
                label = request.label,
                reciterName = request.reciterName,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = request.totalBytes,
                status = DownloadStatus.Queued,
            )
        )
        val intent = Intent(context, QuranDownloadService::class.java).apply {
            action = QuranDownloadService.ACTION_START
            putExtra(QuranDownloadService.EXTRA_ID, request.id)
            putExtra(QuranDownloadService.EXTRA_RECITER_ID, request.reciterId)
            putExtra(QuranDownloadService.EXTRA_RECITER_NAME, request.reciterName)
            putExtra(QuranDownloadService.EXTRA_SCOPE, request.scope.name)
            putExtra(QuranDownloadService.EXTRA_SURAH, request.surahNumber ?: -1)
            putExtra(QuranDownloadService.EXTRA_GLOBAL, request.globalNumber ?: -1)
            putExtra(QuranDownloadService.EXTRA_LABEL, request.label)
            putExtra(QuranDownloadService.EXTRA_TOTAL_BYTES, request.totalBytes)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun cancel(id: String) {
        val intent = Intent(context, QuranDownloadService::class.java).apply {
            action = QuranDownloadService.ACTION_CANCEL
            putExtra(QuranDownloadService.EXTRA_ID, id)
        }
        context.startService(intent)
    }

    /** Called by the download service to report progress/status changes. */
    fun update(id: String, transform: (DownloadTaskUi) -> DownloadTaskUi) {
        _tasks.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    private fun upsert(task: DownloadTaskUi) {
        _tasks.update { list -> list.filterNot { it.id == task.id } + task }
    }
}
