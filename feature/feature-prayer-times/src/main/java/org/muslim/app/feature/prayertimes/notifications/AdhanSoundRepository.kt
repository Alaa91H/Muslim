package org.muslim.app.feature.prayertimes.notifications

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.core.network.FileDownloader
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-prayer adhan sound library (PROJECT_PROMPT.md §6 Phase 1: مكتبة أصوات
 * الأذان + تنزيل اختياري). Sounds are stored in the app's private storage —
 * nothing is ever uploaded or shared.
 *
 * Resolution order when a prayer fires:
 * 1. a user-picked or downloaded file ([customSoundFile]) — played as-is;
 * 2. otherwise the bundled synthesised tone ([AdhanSynthesizer]).
 */
@Singleton
class AdhanSoundRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: PrayerSettingsRepository,
    private val fileDownloader: FileDownloader,
) {

    /** Directory holding custom adhan files, inside app-private storage. */
    private val soundsDir: File
        get() = File(context.filesDir, "adhan_sounds").apply { mkdirs() }

    /** The custom/downloaded sound file for [prayer], or null (use bundled tone). */
    suspend fun customSoundFile(prayer: Prayer): File? =
        settingsRepository.settings.first()
            .adhanSoundFiles[prayer]
            ?.let { path -> File(path).takeIf { f -> f.exists() } }

    /** True when this prayer has a custom sound configured. */
    suspend fun hasCustomSound(prayer: Prayer): Boolean = customSoundFile(prayer) != null

    /**
     * Copies a user-picked audio file ([uri], e.g. from the system picker)
     * into private storage and binds it to [prayer]. Returns the target file.
     */
    suspend fun setCustomSound(prayer: Prayer, uri: Uri): File? = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(soundsDir, "${prayer.name.lowercase()}.audio")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            settingsRepository.update { it.copy(adhanSoundFiles = it.adhanSoundFiles + (prayer to target.absolutePath)) }
            target
        }.getOrNull()
    }

    /**
     * Downloads an adhan audio file from [url] into private storage and binds
     * it to [prayer]. Reports download progress via [onProgress] (0..1).
     */
    suspend fun downloadSound(
        prayer: Prayer,
        url: String,
        onProgress: (Float) -> Unit = {},
    ): FileDownloader.Result {
        val target = File(soundsDir, "${prayer.name.lowercase()}.downloaded.audio")
        return when (val result = fileDownloader.download(url, target, onProgress)) {
            is FileDownloader.Result.Success -> {
                settingsRepository.update {
                    it.copy(adhanSoundFiles = it.adhanSoundFiles + (prayer to result.file.absolutePath))
                }
                result
            }
            is FileDownloader.Result.Failure -> result
        }
    }

    /** Removes the custom sound for [prayer] and falls back to the bundled tone. */
    suspend fun clearCustomSound(prayer: Prayer) {
        val settings = settingsRepository.settings.first()
        settings.adhanSoundFiles[prayer]?.let { File(it).delete() }
        settingsRepository.update {
            it.copy(adhanSoundFiles = it.adhanSoundFiles - prayer)
        }
    }
}
