package org.example.islamicapp.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.feature.quran.domain.Reciter
import org.junit.Test
import java.io.File

class RecitationAudioTest {

    @Test
    fun `url targets the CDN with the reciter slug and global ayah number`() {
        assertThat(RecitationAudio.url(Reciter.Alafasy, 1))
            .isEqualTo("https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3")
        assertThat(RecitationAudio.url(Reciter.Minshawi, 6236))
            .isEqualTo("https://cdn.islamic.network/quran/audio/128/ar.minshawi/6236.mp3")
    }

    @Test
    fun `local files live under quran_audio per reciter with global-number names`() {
        val filesDir = File("files-root")
        val file = RecitationAudio.fileFor(filesDir, Reciter.Alafasy, 286)
        assertThat(file.name).isEqualTo("286.mp3")
        assertThat(file.parentFile!!.name).isEqualTo("ar.alafasy")
        assertThat(file.parentFile!!.parentFile!!.name).isEqualTo("quran_audio")
        assertThat(file.parentFile!!.parentFile!!.parentFile).isEqualTo(filesDir)
    }

    @Test
    fun `each reciter gets its own directory`() {
        val filesDir = File("/tmp/files")
        assertThat(RecitationAudio.reciterDir(filesDir, Reciter.Alafasy))
            .isEqualTo(File("/tmp/files/quran_audio/ar.alafasy"))
        assertThat(RecitationAudio.reciterDir(filesDir, Reciter.Husary))
            .isEqualTo(File("/tmp/files/quran_audio/ar.husary"))
    }
}
