package org.muslim.app.feature.quran.data

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
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter
import java.io.File
import javax.inject.Inject

/**
 * Foreground service that performs user-initiated recitation downloads in the
 * background (ayah / surah / full Quran), reporting progress through a
 * notification and through [QuranDownloadManager] for the in-app screen.
 * Interrupted downloads resume via [FileDownloader]'s Range support.
 */
@AndroidEntryPoint
class QuranDownloadService : Service() {

    @Inject lateinit var recitationRepository: RecitationRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var manager: QuranDownloadManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, Job>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val request = parseRequest(intent) ?: return START_NOT_STICKY
                ensureChannel()
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(request.label, 0, 0L, request.totalBytes),
                )
                jobs[request.id]?.cancel()
                jobs[request.id] = scope.launch { run(request) }
                return START_REDELIVER_INTENT
            }
            ACTION_CANCEL -> {
                val id = intent.getStringExtra(EXTRA_ID) ?: return START_NOT_STICKY
                jobs.remove(id)?.cancel()
                manager.update(id) { it.copy(status = DownloadStatus.Failed) }
                maybeStop()
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun run(request: DownloadRequest) {
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
        var remainingBytes = request.totalBytes
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
                    remainingBytes = (request.totalBytes - offsetBytes).coerceAtLeast(0L)
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
            .setSmallIcon(R.drawable.ic_download_notification)
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
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download_notification)
            .setContentTitle(title)
            .setContentText(getString(R.string.quran_download_in_progress))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
        if (total > 0) {
            builder.setProgress(100, percent, false)
        }
        return builder.build()
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
        )
    }

    companion object {
        private const val CHANNEL_ID = "quran_downloads"
        private const val NOTIFICATION_ID = 7100
        const val ACTION_START = "org.muslim.app.feature.quran.download.START"
        const val ACTION_CANCEL = "org.muslim.app.feature.quran.download.CANCEL"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_RECITER_ID = "extra_reciter_id"
        const val EXTRA_RECITER_NAME = "extra_reciter_name"
        const val EXTRA_SCOPE = "extra_scope"
        const val EXTRA_SURAH = "extra_surah"
        const val EXTRA_GLOBAL = "extra_global"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_TOTAL_BYTES = "extra_total_bytes"
    }
}
