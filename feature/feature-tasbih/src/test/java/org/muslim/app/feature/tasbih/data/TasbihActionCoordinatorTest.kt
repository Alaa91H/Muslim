package org.muslim.app.feature.tasbih.data

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionConfig
import org.muslim.app.feature.tasbih.domain.TasbihSessionEndReason
import org.muslim.app.feature.tasbih.domain.TasbihSessionEngine
import org.muslim.app.feature.tasbih.domain.TasbihSessionMode
import org.muslim.app.feature.tasbih.domain.TasbihSessionTransition

class TasbihActionCoordinatorTest {

    private val counterRepository = mockk<TasbihRepository>()
    private val sessionRepository = mockk<TasbihSessionRepository>()
    private val coordinator = TasbihActionCoordinator(counterRepository, sessionRepository)

    @Test
    fun `increment writes visible counter then durable session history`() = runTest {
        val phrase = TasbihPhrase.SubhanAllah
        val transition = TasbihSessionTransition(
            state = TasbihSessionEngine.start(
                TasbihSessionConfig(
                    phraseId = phrase.storageId,
                    mode = TasbihSessionMode.Free,
                    target = 33,
                ),
                nowEpochMillis = 1L,
            ),
        )
        coEvery { counterRepository.increment(phrase) } returns Unit
        coEvery {
            sessionRepository.increment(
                phrase,
                33,
                TasbihSessionMode.Free,
                TasbihRepository.DEFAULT_ROUNDS_GOAL,
                any(),
            )
        } returns transition

        coordinator.increment(phrase, 33)

        coVerifyOrder {
            counterRepository.increment(phrase)
            sessionRepository.increment(
                phrase,
                33,
                TasbihSessionMode.Free,
                TasbihRepository.DEFAULT_ROUNDS_GOAL,
                any(),
            )
        }
    }

    @Test
    fun `session configuration closes active session then persists new config`() = runTest {
        coEvery {
            sessionRepository.endActive(TasbihSessionEndReason.ContextChanged, any())
        } returns Unit
        coEvery {
            counterRepository.setSessionConfig(TasbihSessionMode.Rounds, 33, 3)
        } returns Unit

        coordinator.configureSession(TasbihSessionMode.Rounds, 33, 3)

        coVerifyOrder {
            sessionRepository.endActive(TasbihSessionEndReason.ContextChanged, any())
            counterRepository.setSessionConfig(TasbihSessionMode.Rounds, 33, 3)
        }
    }

    @Test
    fun `changing phrase closes current session before changing selection`() = runTest {
        val phrase = TasbihPhrase.Alhamdulillah
        coEvery {
            sessionRepository.endActive(TasbihSessionEndReason.ContextChanged, any())
        } returns Unit
        coEvery { counterRepository.setPhrase(phrase) } returns Unit

        coordinator.setPhrase(phrase)

        coVerifyOrder {
            sessionRepository.endActive(TasbihSessionEndReason.ContextChanged, any())
            counterRepository.setPhrase(phrase)
        }
    }

    @Test
    fun `reset closes current session before clearing phrase count`() = runTest {
        val phrase = TasbihPhrase.Astaghfirullah
        coEvery {
            sessionRepository.endActive(TasbihSessionEndReason.Reset, any())
        } returns Unit
        coEvery { counterRepository.reset(phrase) } returns Unit

        coordinator.reset(phrase)

        coVerifyOrder {
            sessionRepository.endActive(TasbihSessionEndReason.Reset, any())
            counterRepository.reset(phrase)
        }
    }
}
