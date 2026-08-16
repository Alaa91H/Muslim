package org.example.islamicapp.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads a remote file into app storage with a progress callback.
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
     * Streams [url] into [destination] (created if missing). Reports progress
     * as a fraction 0..1 via [onProgress] on the calling coroutine's context.
     */
    suspend fun download(
        url: String,
        destination: File,
        onProgress: (Float) -> Unit = {},
    ): Result = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.Failure(
                        IllegalStateException("HTTP ${response.code} for $url"),
                    )
                }
                val body = response.body ?: return@withContext Result.Failure(
                    IllegalStateException("Empty body for $url"),
                )
                val total = body.contentLength()
                destination.parentFile?.mkdirs()
                destination.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var written = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                            }
                        }
                    }
                }
                Result.Success(destination)
            }
        }.getOrElse { Result.Failure(it) }
    }
}
