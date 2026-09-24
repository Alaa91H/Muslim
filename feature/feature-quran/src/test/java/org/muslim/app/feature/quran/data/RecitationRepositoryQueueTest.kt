package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Files

class RecitationRepositoryQueueTest {

    @Test
    fun `local queue is ready only when every audio file is non-empty`() {
        val dir = Files.createTempDirectory("recitation-local").toFile()
        try {
            File(dir, "1.mp3").writeBytes(byteArrayOf(1, 2, 3))
            File(dir, "2.mp3").writeBytes(byteArrayOf(4))

            val queue = buildLocalRecitationQueue(listOf(1, 2)) { global ->
                File(dir, "$global.mp3")
            }

            assertThat(queue?.map { it.globalNumber }).containsExactly(1, 2).inOrder()
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `empty audio file forces download path`() {
        val dir = Files.createTempDirectory("recitation-empty").toFile()
        try {
            File(dir, "1.mp3").writeBytes(byteArrayOf(1))
            File(dir, "2.mp3").createNewFile()

            val queue = buildLocalRecitationQueue(listOf(1, 2)) { global ->
                File(dir, "$global.mp3")
            }

            assertThat(queue).isNull()
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `missing audio file forces download path`() {
        val dir = Files.createTempDirectory("recitation-missing").toFile()
        try {
            File(dir, "1.mp3").writeBytes(byteArrayOf(1))

            val queue = buildLocalRecitationQueue(listOf(1, 2)) { global ->
                File(dir, "$global.mp3")
            }

            assertThat(queue).isNull()
        } finally {
            dir.deleteRecursively()
        }
    }
}
