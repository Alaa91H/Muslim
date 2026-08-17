package org.muslim.app.feature.hadith.data

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.muslim.app.feature.hadith.domain.Hadith
import org.muslim.app.feature.hadith.domain.HadithCollection

/**
 * Unit tests for [HadithOfTheDayWorker]. The Worker environment (Context,
 * WorkerParameters) and the Hilt entry point are replaced by fakes, so the
 * decision logic runs on the JVM without Robolectric or a real application.
 */
class HadithOfTheDayWorkerTest {

    private fun worker(
        source: HadithOfTheDaySource,
        notifier: HadithOfTheDayNotifier = mockk(relaxed = true),
        categoryEnabled: Boolean = true,
    ): HadithOfTheDayWorker =
        object : HadithOfTheDayWorker(
            context = mockk(relaxed = true),
            params = mockk(relaxed = true),
        ) {
            override fun repository(): HadithOfTheDaySource = source
            override fun notifier(): HadithOfTheDayNotifier = notifier
            override suspend fun categoryEnabled(): Boolean = categoryEnabled
        }

    @Test
    fun `returns retry when there is no hadith of the day`() = runTest {
        val source = mockk<HadithOfTheDaySource> {
            coEvery { isDailyNotificationEnabled() } returns true
            coEvery { hadithOfTheDay() } returns null
        }
        val notifier = mockk<HadithOfTheDayNotifier>(relaxed = true)

        val result = worker(source, notifier).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
        verify(exactly = 0) { notifier.show(any()) }
    }

    @Test
    fun `returns success without posting when the toggle is disabled`() = runTest {
        val source = mockk<HadithOfTheDaySource> {
            coEvery { isDailyNotificationEnabled() } returns false
        }
        val notifier = mockk<HadithOfTheDayNotifier>(relaxed = true)

        val result = worker(source, notifier).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        verify(exactly = 0) { notifier.show(any()) }
        coVerify(exactly = 0) { source.hadithOfTheDay() }
    }

    @Test
    fun `returns success without posting when the unified manager disables the category`() = runTest {
        val source = mockk<HadithOfTheDaySource> {
            coEvery { isDailyNotificationEnabled() } returns true
        }
        val notifier = mockk<HadithOfTheDayNotifier>(relaxed = true)

        val result = worker(source, notifier, categoryEnabled = false).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        verify(exactly = 0) { notifier.show(any()) }
        coVerify(exactly = 0) { source.hadithOfTheDay() }
    }

    @Test
    fun `posts the notification and returns success when a hadith exists`() = runTest {
        val hadith = Hadith(
            id = 1L,
            collection = HadithCollection.Bukhari,
            chapter = null,
            numberInBook = 1,
            arabicText = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
            translation = "Actions are but by intentions",
            grade = "Sahih",
            source = "صحيح البخاري",
        )
        val source = mockk<HadithOfTheDaySource> {
            coEvery { isDailyNotificationEnabled() } returns true
            coEvery { hadithOfTheDay() } returns hadith
        }
        val notifier = mockk<HadithOfTheDayNotifier>(relaxed = true)

        val result = worker(source, notifier).doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        verify(exactly = 1) { notifier.show(hadith) }
    }
}
