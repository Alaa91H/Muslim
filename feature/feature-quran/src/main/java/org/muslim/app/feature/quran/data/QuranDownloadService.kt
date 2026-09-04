package org.muslim.app.feature.quran.data

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.network.FileDownloader
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.NightDownloadWindow
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter
import java.io.File
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

/**
 * Foreground service that performs user-initiated recitation downloads in the
 * background (ayah / surah / full Quran), reporting progress through a
 * notification and through [QuranDownloadManager] for the in-app screen.
 * Interrupted downloads resume via [FileDownloader]'s Range support.
 *
 * Night-only downloads (التحميل الليلي) are parked with a `WaitingNight`
 * status and an exact alarm at the window start, so the transfer begins
 * automatically inside the configured window (default 23:00–05:00). Pausing
 * keeps the partial `.part` files; resuming continues from where it stopped.
 */
@AndroidEntryPoint
class QuranDownloadService : Service() {

    @Inject lateinit var recitationRepository: RecitationRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var manager: QuranDownloadManager
    @Inject lateinit var prefsRepository: QuranPrefsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, Job>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val request = parseRequest(intent) ?: return START_NOT_STICKY
                start(request)
                return START_REDELIVER_INTENT
            }
            ACTION_PAUSE -> {
                val id = intent.getStringExtra(EXTRA_ID) ?: return START_NOT_STICKY
                jobs.remove(id)?.cancel()
                cancelNightAlarm(id)
                manager.update(id) { it.copy(status = DownloadStatus.Paused) }
                maybeStop()
                return START_NOT_STICKY
            }
            ACTION_RESUME -> {
                val id = intent.getStringExtra(EXTRA_ID) ?: return START_NOT_STICKY
                val request = manager.requestById(id) ?: return START_NOT_STICKY
                start(request)
                return START_REDELIVER_INTENT
            }
            ACTION_CANCEL -> {
                val id = intent.getStringExtra(EXTRA_ID) ?: return START_NOT_STICKY
                jobs.remove(id)?.cancel()
                cancelNightAlarm(id)
                val request = manager.requestById(id)
                if (request != null) {
                    scope.launch { deletePartials(request) }
                }
                manager.update(id) { it.copy(status = DownloadStatus.Failed) }
                maybeStop()
                return START_NOT_STICKY
            }
            ACTION_RESTORE -> {
                ensureChannel()
                startForeground(NOTIFICATION_ID, buildRestoreNotification())
                scope.launch {
                    manager.restore()
                    maybeStop()
                }
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun start(request: DownloadRequest) {
        ensureChannel()
        // Night-held tasks re-delivered by the alarm must not re-park forever;
        // upsert so the UI always sees the task, preserving prior progress.
        val current = manager.tasks.value.firstOrNull { it.id == request.id }
        manager.upsert(
            DownloadTaskUi(
                id = request.id,
                label = request.label,
                reciterId = request.reciterId,
                reciterName = request.reciterName,
                progress = current?.progress ?: 0f,
                downloadedBytes = current?.downloadedBytes ?: 0L,
                totalBytes = request.totalBytes,
                status = DownloadStatus.Queued,
            )
        )
        startForeground(
            NOTIFICATION_ID,
            buildNotification(request.label, 0, current?.downloadedBytes ?: 0L, request.totalBytes),
        )
        jobs[request.id]?.cancel()
        jobs[request.id] = scope.launch { run(request) }
    }

    private suspend fun run(request: DownloadRequest) {
        if (request.nightOnly && !insideNightWindow()) {
            holdForNight(request)
            return
        }
        manager.update(request.id) { it.copy(status = DownloadStatus.Downloading) }
        val reciter = Reciter.Bundled.firstOrNull { it.id == request.reciterId } ?: Reciter.Bundled.first()
        val result = when (request.scope) {
            DownloadScope.Ayah -> downloadAyah(reciter, request)
            DownloadScope.Surah -> downloadSurah(reciter, request.surahNumber ?: 1, request)
            DownloadScope.FullQuran -> downloadFullQuran(reciter, request)
        }
        when (result) {
            is FileDownloader.Result.Success -> {
                manager.update(request.id) {
                    it.copy(status = DownloadStatus.Completed, progress = 1f, downloadedBytes = it.totalBytes)
                }
                notifyFinal(request.label, success = true)
            }
            is FileDownloader.Result.Failure -> {
                manager.update(request.id) { it.copy(status = DownloadStatus.Failed) }
                notifyFinal(request.label, success = false)
            }
        }
        jobs.remove(request.id)
        maybeStop()
    }

    /**
     * Night-only download outside the window: mark the task as waiting, post a
     * notification, and arm an exact alarm for the window start so the
     * transfer begins automatically (or wakes the app if it was killed).
     */
    private suspend fun holdForNight(request: DownloadRequest) {
        manager.update(request.id) { it.copy(status = DownloadStatus.WaitingNight) }
        val windowStart = prefsRepository.nightDownloadStart.first()
        val windowEnd = prefsRepository.nightDownloadEnd.first()
        val openAt = NightDownloadWindow.nextOpenMillis(
            System.currentTimeMillis(), ZoneId.systemDefault(), windowStart,
        )
        val pending = PendingIntent.getService(
            this, request.id.hashCode(), startIntent(this, request.copy(nightOnly = true)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarm = getSystemService(AlarmManager::class.java)
        runCatching {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, openAt, pending)
        }.onFailure {
            // Exact alarms can be unavailable (e.g. Doze policies); fall back to
            // an inexact one — the transfer still starts near the window.
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, openAt, pending)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNightNotification(request.label, windowStart, windowEnd))
        // This request is parked; nothing else to run for it right now.
        jobs.remove(request.id)
        maybeStop()
    }

    private suspend fun insideNightWindow(): Boolean {
        val start = prefsRepository.nightDownloadStart.first()
        val end = prefsRepository.nightDownloadEnd.first()
        return NightDownloadWindow.contains(System.currentTimeMillis(), ZoneId.systemDefault(), start, end)
    }

    /** Removes any in-flight `.part` files for the cancelled request's scope. */
    private suspend fun deletePartials(request: DownloadRequest) {
        val surah = if (request.scope == DownloadScope.FullQuran) null else request.surahNumber
        recitationRepository.deletePartials(request.reciterId, surah)
    }

    /** Cancels the exact alarm armed for a night-held or deferred task. */
    private fun cancelNightAlarm(id: String) {
        val alarm = getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getService(
            this,
            id.hashCode(),
            Intent(this, QuranDownloadService::class.java).apply { action = ACTION_START },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarm.cancel(pending)
    }

    private suspend fun downloadAyah(reciter: Reciter, request: DownloadRequest): FileDownloader.Result {
        val global = request.globalNumber ?: return FileDownloader.Result.Failure(
            IllegalStateException("Ayah download without a global number"),
        )
        val ayah = quranRepository.ayahByGlobal(global)
            ?: return FileDownloader.Result.Failure(IllegalStateException("Unknown ayah $global"))
        return recitationRepository.downloadSurah(
            reciter,
            ayah.surahNumber,
            mapOf(ayah.numberInSurah to ayah.globalNumber),
        ) { fraction -> report(request, (request.totalBytes * fraction).toLong()) }
    }

    private suspend fun downloadSurah(reciter: Reciter, surahNumber: Int, request: DownloadRequest): FileDownloader.Result {
        val ayahs = quranRepository.observeSurah(surahNumber).first()
        if (ayahs.isEmpty()) return FileDownloader.Result.Failure(IllegalStateException("Empty surah $surahNumber"))
        val mapping = ayahs.associate { it.numberInSurah to it.globalNumber }
        return recitationRepository.downloadSurah(reciter, surahNumber, mapping) { fraction ->
            report(request, (request.totalBytes * fraction).toLong())
        }
    }

    private suspend fun downloadFullQuran(reciter: Reciter, request: DownloadRequest): FileDownloader.Result {
        var offsetBytes = 0L
        for (surahNumber in 1..114) {
            val ayahs = quranRepository.observeSurah(surahNumber).first()
            if (ayahs.isEmpty()) continue
            val surahBytes = reciter.estimatedBytesPerAyah() * ayahs.size
            val mapping = ayahs.associate { it.numberInSurah to it.globalNumber }
            when (val result = recitationRepository.downloadSurah(reciter, surahNumber, mapping) { fraction ->
                report(request, offsetBytes + (surahBytes * fraction).toLong())
            }) {
                is FileDownloader.Result.Failure -> return result
                is FileDownloader.Result.Success -> {
                    offsetBytes += surahBytes
                }
            }
        }
        report(request, request.totalBytes)
        return FileDownloader.Result.Success(File(recitationRepository.reciterDir(reciter.id).absolutePath))
    }

    private fun report(request: DownloadRequest, bytes: Long) {
        val fraction = if (request.totalBytes > 0) {
            (bytes.toFloat() / request.totalBytes).coerceIn(0f, 1f)
        } else 0f
        manager.update(request.id) {
            it.copy(progress = fraction, downloadedBytes = bytes, status = DownloadStatus.Downloading)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(request.label, (fraction * 100).toInt(), bytes, request.totalBytes))
    }

    private fun notifyFinal(label: String, success: Boolean) {
        val text = getString(if (success) R.string.quran_download_done else R.string.quran_download_failed)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setContentTitle(label)
            .setContentText(text)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, percent: Int, bytes: Long, total: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val percentText = getString(R.string.quran_download_percent, percent)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setContentTitle(title)
            .setContentText(percentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
        if (total > 0) {
            builder.setProgress(100, percent, false)
        }
        return builder.build()
    }

    /** Transient notification shown while a reboot restore re-arms the queue. */
    private fun buildRestoreNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setContentTitle(getString(R.string.quran_download_restoring))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    /** Parked night-only download: shows when the window opens. */
    private fun buildNightNotification(label: String, startMinutes: Int, endMinutes: Int): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setContentTitle(label)
            .setContentText(getString(R.string.quran_download_night_waiting, formatWindow(startMinutes, endMinutes)))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun formatWindow(startMinutes: Int, endMinutes: Int): String {
        fun fmt(min: Int) = String.format(Locale.ROOT, "%02d:%02d", min / 60, min % 60)
        return "${fmt(startMinutes)} – ${fmt(endMinutes)}"
    }

    private fun ensureChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.quran_download_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun maybeStop() {
        if (jobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun parseRequest(intent: Intent): DownloadRequest? {
        val id = intent.getStringExtra(EXTRA_ID) ?: return null
        val reciterId = intent.getStringExtra(EXTRA_RECITER_ID) ?: return null
        return DownloadRequest(
            id = id,
            reciterId = reciterId,
            reciterName = intent.getStringExtra(EXTRA_RECITER_NAME) ?: reciterId,
            scope = runCatching { DownloadScope.valueOf(intent.getStringExtra(EXTRA_SCOPE).orEmpty()) }
                .getOrDefault(DownloadScope.Surah),
            surahNumber = intent.getIntExtra(EXTRA_SURAH, -1).takeIf { it > 0 },
            globalNumber = intent.getIntExtra(EXTRA_GLOBAL, -1).takeIf { it > 0 },
            label = intent.getStringExtra(EXTRA_LABEL).orEmpty(),
            totalBytes = intent.getLongExtra(EXTRA_TOTAL_BYTES, 0L),
            nightOnly = intent.getBooleanExtra(EXTRA_NIGHT_ONLY, false),
        )
    }

    companion object {
        private const val CHANNEL_ID = "quran_downloads"
        private const val NOTIFICATION_ID = 7100
        const val ACTION_START = "org.muslim.app.feature.quran.download.START"
        const val ACTION_PAUSE = "org.muslim.app.feature.quran.download.PAUSE"
        const val ACTION_RESUME = "org.muslim.app.feature.quran.download.RESUME"
        const val ACTION_CANCEL = "org.muslim.app.feature.quran.download.CANCEL"
        const val ACTION_RESTORE = "org.muslim.app.feature.quran.download.RESTORE"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_RECITER_ID = "extra_reciter_id"
        const val EXTRA_RECITER_NAME = "extra_reciter_name"
        const val EXTRA_SCOPE = "extra_scope"
        const val EXTRA_SURAH = "extra_surah"
        const val EXTRA_GLOBAL = "extra_global"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_TOTAL_BYTES = "extra_total_bytes"
        const val EXTRA_NIGHT_ONLY = "extra_night_only"

        /** Builds the ACTION_START intent that (re)delivers [request]. */
        fun startIntent(context: Context, request: DownloadRequest): Intent =
            Intent(context, QuranDownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ID, request.id)
                putExtra(EXTRA_RECITER_ID, request.reciterId)
                putExtra(EXTRA_RECITER_NAME, request.reciterName)
                putExtra(EXTRA_SCOPE, request.scope.name)
                putExtra(EXTRA_SURAH, request.surahNumber ?: -1)
                putExtra(EXTRA_GLOBAL, request.globalNumber ?: -1)
                putExtra(EXTRA_LABEL, request.label)
                putExtra(EXTRA_TOTAL_BYTES, request.totalBytes)
                putExtra(EXTRA_NIGHT_ONLY, request.nightOnly)
            }
    }
}
