package org.muslim.app.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads a remote file into app storage with a progress callback and
 * automatic resume: a partial download is kept as `<name>.part` and continued
 * with an HTTP Range header on the next attempt, so interrupted large files
 * (full-Quran recitations) resume instead of restarting.
 *
 * Used exclusively for *optional* content (recitation audio, extra adhan
 * sounds, translation/tafsir packs). All downloads are user-initiated, and
 * nothing is fetched without an explicit action.
 */
@Singleton
class FileDownloader @Inject constructor(
    private val client: OkHttpClient,
) {

    sealed interface Result {
        data class Success(val file: File) : Result
        data class Failure(val cause: Throwable) : Result
    }

    /**
     * Streams [url] into [destination] (resumable). Reports progress as a
     * fraction 0..1 via [onProgress] on the calling coroutine's context.
     */
    suspend fun download(
        url: String,
        destination: File,
        onProgress: (Float) -> Unit = {},
    ): Result = withContext(Dispatchers.IO) {
        runCatching {
            destination.parentFile?.mkdirs()
            val part = File(destination.parentFile, "${destination.name}.part")
            val already = if (part.exists()) part.length() else 0L

            val request = Request.Builder().url(url).get().apply {
                if (already > 0) header("Range", "bytes=$already-")
            }.build()

            client.newCall(request).execute().use { response ->
                val resume = response.code == 206
                if (!response.isSuccessful && !resume) {
                    return@withContext Result.Failure(
                        IllegalStateException("HTTP ${response.code} for $url"),
                    )
                }
                val body = response.body ?: return@withContext Result.Failure(
                    IllegalStateException("Empty body for $url"),
                )
                // Total length for progress: full size when known, else the
                // bytes remaining in this (possibly resumed) response.
                val total = body.contentLength() + if (resume) already else 0L

                RandomAccessFile(part, "rw").use { raf ->
                    raf.seek(if (resume) already else 0L)
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var written = already
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            raf.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                            }
                        }
                    }
                }

                if (!part.renameTo(destination)) {
                    part.copyTo(destination, overwrite = true)
                    part.delete()
                }
                Result.Success(destination)
            }
        }.getOrElse { Result.Failure(it) }
    }
}
