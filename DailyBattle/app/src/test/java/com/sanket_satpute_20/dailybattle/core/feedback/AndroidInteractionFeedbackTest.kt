package com.sanket_satpute_20.dailybattle.core.feedback

import android.media.ToneGenerator
import com.sanket_satpute_20.dailybattle.data.settings.InMemorySettingsRepository
import com.sanket_satpute_20.dailybattle.domain.user.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AndroidInteractionFeedbackTest {

    private data class ToneCall(val toneType: Int, val durationMs: Int)

    private class FakeTonePlayer : TonePlayer {
        val playedTones = CopyOnWriteArrayList<ToneCall>()
        var releaseCount = 0
        var shouldThrow = false

        override fun playTone(toneType: Int, durationMs: Int) {
            if (shouldThrow) {
                throw RuntimeException("Simulated audio engine failure")
            }
            playedTones.add(ToneCall(toneType, durationMs))
        }

        override fun release() {
            releaseCount++
        }
    }

    private class FakeHapticPlayer : HapticPlayer {
        val vibrations = CopyOnWriteArrayList<HapticCue>()
        var hasVibratorHardware = true
        var shouldThrow = false

        override fun vibrate(cue: HapticCue) {
            if (!hasVibrator()) return
            if (shouldThrow) {
                throw RuntimeException("Simulated vibrator hardware failure")
            }
            vibrations.add(cue)
        }

        override fun hasVibrator(): Boolean = hasVibratorHardware
    }

    private lateinit var settingsRepository: InMemorySettingsRepository
    private lateinit var fakeTonePlayer: FakeTonePlayer
    private lateinit var fakeHapticPlayer: FakeHapticPlayer
    private lateinit var feedback: AndroidInteractionFeedback

    @Before
    fun setUp() {
        settingsRepository = InMemorySettingsRepository()
        fakeTonePlayer = FakeTonePlayer()
        fakeHapticPlayer = FakeHapticPlayer()
        feedback = AndroidInteractionFeedback(
            settingsRepository = settingsRepository,
            tonePlayer = fakeTonePlayer,
            hapticPlayer = fakeHapticPlayer
        )
    }

    // -------------------------------------------------------------------------
    // 1. Settings Matrix Verification
    // -------------------------------------------------------------------------

    @Test
    fun `settings matrix - sound ON and haptics ON emits both tone and haptic`() = runTest {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(1, fakeHapticPlayer.vibrations.size)
    }

    @Test
    fun `settings matrix - sound ON and haptics OFF emits tone only`() = runTest {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(false)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertTrue(fakeHapticPlayer.vibrations.isEmpty())
    }

    @Test
    fun `settings matrix - sound OFF and haptics ON emits haptic only`() = runTest {
        settingsRepository.updateSound(false)
        settingsRepository.updateHaptics(true)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        assertTrue(fakeTonePlayer.playedTones.isEmpty())
        assertEquals(1, fakeHapticPlayer.vibrations.size)
    }

    @Test
    fun `settings matrix - sound OFF and haptics OFF emits neither`() = runTest {
        settingsRepository.updateSound(false)
        settingsRepository.updateHaptics(false)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        assertTrue(fakeTonePlayer.playedTones.isEmpty())
        assertTrue(fakeHapticPlayer.vibrations.isEmpty())
    }

    // -------------------------------------------------------------------------
    // 2. Event Mapping Verification (All 6 Events)
    // -------------------------------------------------------------------------

    @Test
    fun `event mapping - UI_TAP maps to TONE_PROP_BEEP 35ms and light OneShot haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_BEEP, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(35, fakeTonePlayer.playedTones[0].durationMs)

        assertEquals(1, fakeHapticPlayer.vibrations.size)
        val cue = fakeHapticPlayer.vibrations[0] as HapticCue.OneShot
        assertEquals(12L, cue.durationMs)
        assertEquals(35, cue.amplitude)
    }

    @Test
    fun `event mapping - CORRECT maps to TONE_PROP_ACK 75ms and success OneShot haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.CORRECT)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_ACK, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(75, fakeTonePlayer.playedTones[0].durationMs)

        assertEquals(1, fakeHapticPlayer.vibrations.size)
        val cue = fakeHapticPlayer.vibrations[0] as HapticCue.OneShot
        assertEquals(24L, cue.durationMs)
        assertEquals(80, cue.amplitude)
    }

    @Test
    fun `event mapping - INCORRECT maps to TONE_PROP_NACK 95ms and error Waveform haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.INCORRECT)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_NACK, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(95, fakeTonePlayer.playedTones[0].durationMs)

        assertEquals(1, fakeHapticPlayer.vibrations.size)
        val cue = fakeHapticPlayer.vibrations[0] as HapticCue.Waveform
        assertTrue(longArrayOf(0, 18, 28, 18).contentEquals(cue.timings))
        assertTrue(intArrayOf(0, 65, 0, 65).contentEquals(cue.amplitudes))
        assertEquals(-1, cue.repeat)
    }

    @Test
    fun `event mapping - SCORE_REVEAL maps to TONE_PROP_PROMPT 110ms and no haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.SCORE_REVEAL)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_PROMPT, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(110, fakeTonePlayer.playedTones[0].durationMs)

        // Spec: score reveal does not vibrate
        assertTrue(fakeHapticPlayer.vibrations.isEmpty())
    }

    @Test
    fun `event mapping - PERSONAL_BEST maps to TONE_PROP_ACK 150ms and strong Waveform haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.PERSONAL_BEST)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_ACK, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(150, fakeTonePlayer.playedTones[0].durationMs)

        assertEquals(1, fakeHapticPlayer.vibrations.size)
        val cue = fakeHapticPlayer.vibrations[0] as HapticCue.Waveform
        assertTrue(longArrayOf(0, 28, 35, 45).contentEquals(cue.timings))
        assertTrue(intArrayOf(0, 75, 0, 110).contentEquals(cue.amplitudes))
        assertEquals(-1, cue.repeat)
    }

    @Test
    fun `event mapping - BATTLE_COMPLETE maps to TONE_PROP_PROMPT 130ms and medium OneShot haptic`() = runTest {
        feedback.emit(InteractionFeedbackEvent.BATTLE_COMPLETE)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(ToneGenerator.TONE_PROP_PROMPT, fakeTonePlayer.playedTones[0].toneType)
        assertEquals(130, fakeTonePlayer.playedTones[0].durationMs)

        assertEquals(1, fakeHapticPlayer.vibrations.size)
        val cue = fakeHapticPlayer.vibrations[0] as HapticCue.OneShot
        assertEquals(38L, cue.durationMs)
        assertEquals(90, cue.amplitude)
    }

    // -------------------------------------------------------------------------
    // 3. Edge Cases & Robustness
    // -------------------------------------------------------------------------

    @Test
    fun `edge case - no vibrator hardware does not throw and skips vibration`() = runTest {
        fakeHapticPlayer.hasVibratorHardware = false

        feedback.emit(InteractionFeedbackEvent.CORRECT)

        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertTrue(fakeHapticPlayer.vibrations.isEmpty())
    }

    @Test
    fun `edge case - tone player exception is caught and does not block haptics`() = runTest {
        fakeTonePlayer.shouldThrow = true

        // Should not crash the caller
        runCatching {
            feedback.emit(InteractionFeedbackEvent.CORRECT)
        }

        // Even though audio threw an error, haptics still fired
        assertEquals(1, fakeHapticPlayer.vibrations.size)
    }

    @Test
    fun `edge case - haptic player exception is caught and does not block audio`() = runTest {
        fakeHapticPlayer.shouldThrow = true

        // Should not crash the caller
        runCatching {
            feedback.emit(InteractionFeedbackEvent.CORRECT)
        }

        assertEquals(1, fakeTonePlayer.playedTones.size)
    }

    @Test
    fun `edge case - rapid concurrent events execute without thread safety exceptions`() {
        val threadCount = 8
        val eventsPerThread = 25
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        val events = InteractionFeedbackEvent.values()
        for (i in 0 until threadCount) {
            executor.execute {
                try {
                    for (j in 0 until eventsPerThread) {
                        feedback.emit(events[(i + j) % events.size])
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        val completed = latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()
        assertTrue("All concurrent emissions should complete within timeout", completed)
        assertEquals(threadCount * eventsPerThread, fakeTonePlayer.playedTones.size)
    }

    @Test
    fun `lifecycle - release delegates to tone player and allows subsequent emissions`() = runTest {
        assertEquals(0, fakeTonePlayer.releaseCount)

        feedback.release()
        assertEquals(1, fakeTonePlayer.releaseCount)

        // Subsequent emit does not crash and works
        feedback.emit(InteractionFeedbackEvent.UI_TAP)
        assertEquals(1, fakeTonePlayer.playedTones.size)
    }

    @Test
    fun `dynamic settings change - toggles sound and haptics in real time`() = runTest {
        // Initial state: both enabled
        feedback.emit(InteractionFeedbackEvent.UI_TAP)
        assertEquals(1, fakeTonePlayer.playedTones.size)
        assertEquals(1, fakeHapticPlayer.vibrations.size)

        // Turn off sound dynamically
        settingsRepository.updateSound(false)
        feedback.emit(InteractionFeedbackEvent.CORRECT)
        assertEquals(1, fakeTonePlayer.playedTones.size) // No new tone
        assertEquals(2, fakeHapticPlayer.vibrations.size) // Haptic fired

        // Turn off haptics dynamically, turn on sound
        settingsRepository.updateHaptics(false)
        settingsRepository.updateSound(true)
        feedback.emit(InteractionFeedbackEvent.INCORRECT)
        assertEquals(2, fakeTonePlayer.playedTones.size) // Tone fired
        assertEquals(2, fakeHapticPlayer.vibrations.size) // No new haptic
    }

    @Test
    fun `composition local - default NoOpInteractionFeedback executes without throwing`() {
        val noOp = NoOpInteractionFeedback
        for (event in InteractionFeedbackEvent.values()) {
            noOp.emit(event)
        }
    }
}

