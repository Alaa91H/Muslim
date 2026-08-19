package org.muslim.app.feature.quran.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quranDownloadsDataStore by preferencesDataStore(name = "quran_downloads")

/** What a download request covers. */
@Serializable
enum class DownloadScope { Ayah, Surah, FullQuran }

/** One queued/running/finished download shown in the downloads screen. */
enum class DownloadStatus {
    Queued,
    Downloading,
    /** Held by the user; keeps partial files so it can be resumed later. */
    Paused,
    /** Held until the night-download window opens (التحميل الليلي). */
    WaitingNight,
    Completed,
    Failed,
}

/** A user-initiated recitation download request. */
@Serializable
data class DownloadRequest(
    val id: String,
    val reciterId: String,
    val reciterName: String,
    val scope: DownloadScope,
    val surahNumber: Int?,
    val globalNumber: Int?,
    val label: String,
    val totalBytes: Long,
    /** Defer the actual transfer until the night window (التحميل الليلي). */
    val nightOnly: Boolean = false,
)

/** Persisted form of an active task (survives reboot / process death). */
@Serializable
data class PersistedDownload(
    val request: DownloadRequest,
    val status: String = DownloadStatus.Queued.name,
    val downloadedBytes: Long = 0L,
    val progress: Float = 0f,
)

/** Observable snapshot of one download task. */
data class DownloadTaskUi(
    val id: String,
    val label: String,
    val reciterId: String,
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
 *
 * Active tasks (queued/downloading/paused/waiting-night) are persisted to
 * DataStore and restored after a reboot or process death via [restore], so
 * in-progress downloads survive and resume automatically.
 */
@Singleton
class QuranDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tasks = MutableStateFlow<List<DownloadTaskUi>>(emptyList())
    val tasks: StateFlow<List<DownloadTaskUi>> = _tasks.asStateFlow()

    private val requestsById = mutableMapOf<String, DownloadRequest>()

    fun enqueue(request: DownloadRequest) {
        requestsById[request.id] = request
        upsert(
            DownloadTaskUi(
                id = request.id,
                label = request.label,
                reciterId = request.reciterId,
                reciterName = request.reciterName,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = request.totalBytes,
                status = DownloadStatus.Queued,
            )
        )
        startService(request)
    }

    fun pause(id: String) = sendAction(QuranDownloadService.ACTION_PAUSE, id)

    fun resume(id: String) = sendAction(QuranDownloadService.ACTION_RESUME, id)

    fun cancel(id: String) = sendAction(QuranDownloadService.ACTION_CANCEL, id)

    fun requestById(id: String): DownloadRequest? = requestsById[id]

    /** Called by the download service to report progress/status changes. */
    fun update(id: String, transform: (DownloadTaskUi) -> DownloadTaskUi) {
        var statusChanged = false
        _tasks.update { list ->
            list.map { task ->
                if (task.id == id) {
                    val newTask = transform(task)
                    if (newTask.status != task.status) statusChanged = true
                    newTask
                } else {
                    task
                }
            }
        }
        if (statusChanged) persistCurrent()
    }

    /** Adds or replaces [task] (used when a night-held task is re-delivered). */
    fun upsert(task: DownloadTaskUi) {
        _tasks.update { list -> list.filterNot { it.id == task.id } + task }
        persistCurrent()
    }

    /** Removes a task from the observable list and the persisted queue. */
    fun remove(id: String) {
        _tasks.update { list -> list.filterNot { it.id == id } }
        requestsById.remove(id)
        persistCurrent()
    }

    /**
     * Restores persisted active tasks after a reboot or process death and
     * re-arms them. Non-paused tasks resume through a short exact alarm (the
     * same exempt path used by night downloads), so they survive background
     * foreground-service start restrictions. Idempotent.
     */
    fun restore() {
        scope.launch {
            val persisted = readPersisted()
            for (p in persisted) {
                val status = runCatching { DownloadStatus.valueOf(p.status) }
                    .getOrDefault(DownloadStatus.Queued)
                requestsById[p.request.id] = p.request
                upsert(
                    DownloadTaskUi(
                        id = p.request.id,
                        label = p.request.label,
                        reciterId = p.request.reciterId,
                        reciterName = p.request.reciterName,
                        progress = p.progress,
                        downloadedBytes = p.downloadedBytes,
                        totalBytes = p.request.totalBytes,
                        status = if (status == DownloadStatus.Downloading) DownloadStatus.Queued else status,
                    )
                )
                if (status != DownloadStatus.Paused) scheduleResume(p.request, 1_000L)
            }
        }
    }

    private fun sendAction(action: String, id: String) {
        val intent = Intent(context, QuranDownloadService::class.java).apply {
            this.action = action
            putExtra(QuranDownloadService.EXTRA_ID, id)
        }
        context.startService(intent)
    }

    private fun startService(request: DownloadRequest) {
        ContextCompat.startForegroundService(context, QuranDownloadService.startIntent(context, request))
    }

    /** Arms an exact alarm that re-delivers [request] to the download service. */
    private fun scheduleResume(request: DownloadRequest, delayMs: Long) {
        val pending = PendingIntent.getService(
            context,
            request.id.hashCode(),
            QuranDownloadService.startIntent(context, request),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarm = context.getSystemService(AlarmManager::class.java)
        val at = System.currentTimeMillis() + delayMs
        runCatching {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }.onFailure {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    // --- Persistence ---

    /** Writes the non-terminal subset of the current task list to DataStore. */
    private fun persistCurrent() {
        val snapshot = _tasks.value
            .filter { it.status != DownloadStatus.Completed && it.status != DownloadStatus.Failed }
            .mapNotNull { task ->
                val request = requestsById[task.id] ?: return@mapNotNull null
                PersistedDownload(
                    request = request,
                    status = task.status.name,
                    downloadedBytes = task.downloadedBytes,
                    progress = task.progress,
                )
            }
        scope.launch {
            context.quranDownloadsDataStore.edit { prefs ->
                prefs[Keys.QUEUE] = json.encodeToString(
                    kotlinx.serialization.serializer<List<PersistedDownload>>(),
                    snapshot,
                )
            }
        }
    }

    private suspend fun readPersisted(): List<PersistedDownload> {
        val prefs = context.quranDownloadsDataStore.data.first()
        return runCatching {
            json.decodeFromString<List<PersistedDownload>>(prefs[Keys.QUEUE] ?: "[]")
        }.getOrDefault(emptyList())
    }

    private object Keys {
        val QUEUE = stringPreferencesKey("download_queue")
    }
}
