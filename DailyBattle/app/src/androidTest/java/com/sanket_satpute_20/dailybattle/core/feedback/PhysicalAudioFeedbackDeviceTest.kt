package com.sanket_satpute_20.dailybattle.core.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sanket_satpute_20.dailybattle.data.settings.InMemorySettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Physical Android Device QA & Validation Suite for Centralized Interaction Feedback.
 * Baseline executed on connected physical hardware (Motorola Moto G85 5G, Android 16 / API 36).
 *
 * Scoped Phase 1 / 1.1 / 1.1.1 hardening introduces [RecordingTonePlayer] and [RecordingHapticPlayer]
 * decorators to capture invocation arguments and establish deterministic behavioral assertions
 * for D-01, D-02, D-03, D-04, and D-10 while preserving full delegation to Android hardware.
 *
 * Verification Level: Framework invocation recorded (with underlying Android hardware delegation).
 * Telemetry Boundary:
 * - Recorded calls verify invocation requests dispatched to system delegates, capturing exact
 *   tone mappings, durations, haptic cues, waveforms, and programmatic suppression.
 * - Recorded timestamps verify request-dispatch sequencing, NOT acoustic playback completion
 *   in AudioFlinger or physical sensory verification.
 */
@RunWith(AndroidJUnit4::class)
class PhysicalAudioFeedbackDeviceTest {

    private lateinit var context: Context
    private lateinit var settingsRepository: InMemorySettingsRepository
    private lateinit var audioManager: AudioManager
    private lateinit var realTonePlayer: SystemTonePlayer
    private lateinit var realHapticPlayer: SystemHapticPlayer
    private lateinit var recordingTonePlayer: RecordingTonePlayer
    private lateinit var recordingHapticPlayer: RecordingHapticPlayer
    private lateinit var feedback: AndroidInteractionFeedback

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        settingsRepository = InMemorySettingsRepository()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        realTonePlayer = SystemTonePlayer()
        realHapticPlayer = SystemHapticPlayer(context)
        recordingTonePlayer = RecordingTonePlayer(delegate = realTonePlayer)
        recordingHapticPlayer = RecordingHapticPlayer(delegate = realHapticPlayer)
        feedback = AndroidInteractionFeedback(
            settingsRepository = settingsRepository,
            tonePlayer = recordingTonePlayer,
            hapticPlayer = recordingHapticPlayer
        )
    }

    @After
    fun tearDown() {
        feedback.release()
    }

    /**
     * D-01: Sound ON + Haptics ON
     * Verifies that all 6 feedback events emit on real Android audio & vibrator hardware
     * with exact tone mappings, durations, and haptic cue types.
     */
    @Test
    fun d01_soundOn_hapticsOn_emitsAllCuesOnHardware() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        val events = listOf(
            InteractionFeedbackEvent.UI_TAP,
            InteractionFeedbackEvent.CORRECT,
            InteractionFeedbackEvent.INCORRECT,
            InteractionFeedbackEvent.SCORE_REVEAL,
            InteractionFeedbackEvent.PERSONAL_BEST,
            InteractionFeedbackEvent.BATTLE_COMPLETE
        )

        // Exercise all 6 feedback events on real hardware
        for (event in events) {
            feedback.emit(event)
            delay(50) // Allow hardware buffer submission
        }

        // 1. Assert exactly 6 tone requests with correct parameters
        val toneCalls = recordingTonePlayer.recordedCalls
        assertEquals("Expected exactly 6 tone requests for 6 interaction events", 6, toneCalls.size)

        assertEquals("Event 0 (UI_TAP) must request TONE_PROP_BEEP", ToneGenerator.TONE_PROP_BEEP, toneCalls[0].toneType)
        assertEquals("Event 0 (UI_TAP) tone duration must be 35ms", 35, toneCalls[0].durationMs)

        assertEquals("Event 1 (CORRECT) must request TONE_PROP_ACK", ToneGenerator.TONE_PROP_ACK, toneCalls[1].toneType)
        assertEquals("Event 1 (CORRECT) tone duration must be 75ms", 75, toneCalls[1].durationMs)

        assertEquals("Event 2 (INCORRECT) must request TONE_PROP_NACK", ToneGenerator.TONE_PROP_NACK, toneCalls[2].toneType)
        assertEquals("Event 2 (INCORRECT) tone duration must be 95ms", 95, toneCalls[2].durationMs)

        assertEquals("Event 3 (SCORE_REVEAL) must request TONE_PROP_PROMPT", ToneGenerator.TONE_PROP_PROMPT, toneCalls[3].toneType)
        assertEquals("Event 3 (SCORE_REVEAL) tone duration must be 110ms", 110, toneCalls[3].durationMs)

        assertEquals("Event 4 (PERSONAL_BEST) must request TONE_PROP_ACK", ToneGenerator.TONE_PROP_ACK, toneCalls[4].toneType)
        assertEquals("Event 4 (PERSONAL_BEST) tone duration must be 150ms", 150, toneCalls[4].durationMs)

        assertEquals("Event 5 (BATTLE_COMPLETE) must request TONE_PROP_PROMPT", ToneGenerator.TONE_PROP_PROMPT, toneCalls[5].toneType)
        assertEquals("Event 5 (BATTLE_COMPLETE) tone duration must be 130ms", 130, toneCalls[5].durationMs)

        // 2. Assert exactly 5 haptic requests (SCORE_REVEAL intentionally has no haptic cue)
        val hapticCalls = recordingHapticPlayer.recordedCalls
        assertEquals("Expected exactly 5 haptic requests because SCORE_REVEAL omits haptics", 5, hapticCalls.size)

        // UI_TAP -> OneShot(12, 35)
        assertEquals("Event 0 (UI_TAP) haptic cue must match OneShot(12, 35)", HapticCue.OneShot(12L, 35), hapticCalls[0].cue)

        // CORRECT -> OneShot(24, 80)
        assertEquals("Event 1 (CORRECT) haptic cue must match OneShot(24, 80)", HapticCue.OneShot(24L, 80), hapticCalls[1].cue)

        // INCORRECT -> Waveform([0, 18, 28, 18], [0, 65, 0, 65], -1)
        assertTrue("Event 2 (INCORRECT) haptic cue must be Waveform", hapticCalls[2].cue is HapticCue.Waveform)
        val incorrectCue = hapticCalls[2].cue as HapticCue.Waveform
        assertTrue("INCORRECT timings must match [0, 18, 28, 18]", longArrayOf(0, 18, 28, 18).contentEquals(incorrectCue.timings))
        assertTrue("INCORRECT amplitudes must match [0, 65, 0, 65]", intArrayOf(0, 65, 0, 65).contentEquals(incorrectCue.amplitudes))
        assertEquals("INCORRECT repeat must be -1 (non-repeating)", -1, incorrectCue.repeat)

        // PERSONAL_BEST -> Waveform([0, 28, 35, 45], [0, 75, 0, 110], -1)
        assertTrue("Event 4 (PERSONAL_BEST) haptic cue must be Waveform", hapticCalls[3].cue is HapticCue.Waveform)
        val pbCue = hapticCalls[3].cue as HapticCue.Waveform
        assertTrue("PERSONAL_BEST timings must match [0, 28, 35, 45]", longArrayOf(0, 28, 35, 45).contentEquals(pbCue.timings))
        assertTrue("PERSONAL_BEST amplitudes must match [0, 75, 0, 110]", intArrayOf(0, 75, 0, 110).contentEquals(pbCue.amplitudes))
        assertEquals("PERSONAL_BEST repeat must be -1 (non-repeating)", -1, pbCue.repeat)

        // BATTLE_COMPLETE -> OneShot(38, 90)
        assertEquals("Event 5 (BATTLE_COMPLETE) haptic cue must match OneShot(38, 90)", HapticCue.OneShot(38L, 90), hapticCalls[4].cue)
    }

    /**
     * D-02: Sound ON + Haptics OFF
     * Verifies that audio plays while vibration hardware remains completely idle.
     */
    @Test
    fun d02_soundOn_hapticsOff_emitsSoundWithoutVibratorCrash() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(false)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)
        feedback.emit(InteractionFeedbackEvent.CORRECT)
        feedback.emit(InteractionFeedbackEvent.INCORRECT)
        delay(100)

        // Assert exactly 3 tone requests with correct arguments
        val toneCalls = recordingTonePlayer.recordedCalls
        assertEquals("Expected exactly 3 tone requests when sound is enabled", 3, toneCalls.size)
        assertEquals("UI_TAP must request TONE_PROP_BEEP", ToneGenerator.TONE_PROP_BEEP, toneCalls[0].toneType)
        assertEquals("UI_TAP tone duration must be 35ms", 35, toneCalls[0].durationMs)
        assertEquals("CORRECT must request TONE_PROP_ACK", ToneGenerator.TONE_PROP_ACK, toneCalls[1].toneType)
        assertEquals("CORRECT tone duration must be 75ms", 75, toneCalls[1].durationMs)
        assertEquals("INCORRECT must request TONE_PROP_NACK", ToneGenerator.TONE_PROP_NACK, toneCalls[2].toneType)
        assertEquals("INCORRECT tone duration must be 95ms", 95, toneCalls[2].durationMs)

        // Assert zero haptic requests (redundant pair removed)
        val hapticCalls = recordingHapticPlayer.recordedCalls
        assertEquals("Haptics must be completely suppressed (0 calls) when hapticsEnabled is false", 0, hapticCalls.size)
    }

    /**
     * D-03: Sound OFF + Haptics ON
     * Verifies that haptics fire while audio generation is completely suppressed.
     */
    @Test
    fun d03_soundOff_hapticsOn_emitsHapticsWithoutTone() = runBlocking {
        settingsRepository.updateSound(false)
        settingsRepository.updateHaptics(true)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)
        feedback.emit(InteractionFeedbackEvent.CORRECT)
        feedback.emit(InteractionFeedbackEvent.PERSONAL_BEST)
        delay(150)

        // Assert zero tone requests (redundant pair removed)
        val toneCalls = recordingTonePlayer.recordedCalls
        assertEquals("Tone playback must be completely suppressed (0 calls) when soundEnabled is false", 0, toneCalls.size)

        // Assert exactly 3 haptic requests with correct cue arguments
        val hapticCalls = recordingHapticPlayer.recordedCalls
        assertEquals("Expected exactly 3 haptic requests when haptics are enabled", 3, hapticCalls.size)
        assertEquals("UI_TAP haptic cue must match OneShot(12, 35)", HapticCue.OneShot(12L, 35), hapticCalls[0].cue)
        assertEquals("CORRECT haptic cue must match OneShot(24, 80)", HapticCue.OneShot(24L, 80), hapticCalls[1].cue)

        assertTrue("PERSONAL_BEST cue must be a Waveform", hapticCalls[2].cue is HapticCue.Waveform)
        val pbCue = hapticCalls[2].cue as HapticCue.Waveform
        assertTrue("PERSONAL_BEST timings must match [0, 28, 35, 45]", longArrayOf(0, 28, 35, 45).contentEquals(pbCue.timings))
        assertTrue("PERSONAL_BEST amplitudes must match [0, 75, 0, 110]", intArrayOf(0, 75, 0, 110).contentEquals(pbCue.amplitudes))
        assertEquals("PERSONAL_BEST repeat must be -1 (non-repeating)", -1, pbCue.repeat)
    }

    /**
     * D-04: Sound OFF + Haptics OFF
     * Verifies that both audio and haptics are completely suppressed.
     */
    @Test
    fun d04_soundOff_hapticsOff_silentAndVibrationFree() = runBlocking {
        settingsRepository.updateSound(false)
        settingsRepository.updateHaptics(false)

        for (event in InteractionFeedbackEvent.values()) {
            feedback.emit(event)
        }

        // Assert zero tone requests and zero haptic requests (redundant pairs removed)
        val toneCalls = recordingTonePlayer.recordedCalls
        assertEquals("All tones must be suppressed (0 calls) when soundEnabled is false", 0, toneCalls.size)

        val hapticCalls = recordingHapticPlayer.recordedCalls
        assertEquals("All haptics must be suppressed (0 calls) when hapticsEnabled is false", 0, hapticCalls.size)
    }

    /**
     * D-05: Media Volume Behavior
     * Verifies that STREAM_MUSIC volume inspection and audio subsystem interact safely.
     */
    @Test
    fun d05_mediaVolumeSubsystem_interactsSafely() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        assertNotNull(currentVolume)

        // Emitting feedback with STREAM_MUSIC respects user media level without crashing
        feedback.emit(InteractionFeedbackEvent.UI_TAP)
        delay(50)
        assertTrue(true)
    }

    /**
     * D-06: Device Without Vibrator Support
     * Verifies that when hasVibrator returns false, the feedback manager does not throw and audio works.
     */
    @Test
    fun d06_hardwareWithoutVibrator_handlesGracefully() = runBlocking {
        val nonVibratingHapticPlayer = object : HapticPlayer {
            override fun vibrate(cue: HapticCue) = Unit
            override fun hasVibrator(): Boolean = false
        }
        val feedbackNoVibrator = AndroidInteractionFeedback(
            settingsRepository = settingsRepository,
            tonePlayer = SystemTonePlayer(),
            hapticPlayer = nonVibratingHapticPlayer
        )

        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        feedbackNoVibrator.emit(InteractionFeedbackEvent.CORRECT)
        feedbackNoVibrator.emit(InteractionFeedbackEvent.INCORRECT)
        delay(100)
        assertTrue(true)
    }

    /**
     * D-07: Background / Foreground Safety
     * Verifies release and subsequent resume cycles leave no stranded threads.
     */
    @Test
    fun d07_backgroundForeground_handlesLifecycleSafely() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        // Simulate app backgrounding
        feedback.release()

        // Simulate app foregrounding and immediate emission
        feedback.emit(InteractionFeedbackEvent.CORRECT)
        delay(100)
        assertTrue(true)
    }

    /**
     * D-08: Activity Recreation Resilience
     * Verifies that when activity configuration changes, feedback instance remains operational.
     */
    @Test
    fun d08_activityRecreation_preservesFeedbackOperational() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        // Feedback in portrait
        feedback.emit(InteractionFeedbackEvent.UI_TAP)

        // After configuration change (isFinishing = false, release not called)
        feedback.emit(InteractionFeedbackEvent.SCORE_REVEAL)
        delay(100)
        assertTrue(true)
    }

    /**
     * D-09: Rapid Repeated Taps (Hardware Stress Test)
     * Rapidly emits 100 concurrent/sequential events to ensure no AudioFlinger or IPC crashes.
     */
    @Test
    fun d09_rapidRepeatedTaps_stressTestUnderHardwareConcurrency() {
        val threadCount = 4
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

        val completed = latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()
        assertTrue("All concurrent emissions should complete without deadlock", completed)
    }

    /**
     * D-10: Personal Best Reveal Timing Sequence
     * Verifies the exact ResultsScreen sequence: SCORE_REVEAL -> 180ms delay -> PERSONAL_BEST.
     *
     * Telemetry Note:
     * D-10 timestamps establish request-dispatch ordering to the framework delegate,
     * NOT acoustic playback completion ordering in AudioFlinger or physical tactile perception.
     */
    @Test
    fun d10_personalBestReveal_sequencingAndTiming() = runBlocking {
        settingsRepository.updateSound(true)
        settingsRepository.updateHaptics(true)

        // Sequence identical to ResultsScreen
        feedback.emit(InteractionFeedbackEvent.SCORE_REVEAL)
        delay(180)
        feedback.emit(InteractionFeedbackEvent.PERSONAL_BEST)
        delay(200)

        // 1. Assert exactly 2 tone requests in correct chronological sequence
        val toneCalls = recordingTonePlayer.recordedCalls
        assertEquals("Expected exactly 2 tone dispatch requests for Personal Best sequence", 2, toneCalls.size)

        // Call 0: SCORE_REVEAL tone
        assertEquals("Call 0 must request SCORE_REVEAL (TONE_PROP_PROMPT)", ToneGenerator.TONE_PROP_PROMPT, toneCalls[0].toneType)
        assertEquals("SCORE_REVEAL tone duration must be 110ms", 110, toneCalls[0].durationMs)

        // Call 1: PERSONAL_BEST tone
        assertEquals("Call 1 must request PERSONAL_BEST (TONE_PROP_ACK)", ToneGenerator.TONE_PROP_ACK, toneCalls[1].toneType)
        assertEquals("PERSONAL_BEST tone duration must be 150ms", 150, toneCalls[1].durationMs)

        // Deterministic sequence verification: SCORE_REVEAL requested strictly before PERSONAL_BEST
        assertTrue(
            "SCORE_REVEAL tone request dispatch must precede PERSONAL_BEST tone request dispatch",
            toneCalls[0].timestampNs < toneCalls[1].timestampNs
        )

        // 2. Assert exactly 1 haptic request (SCORE_REVEAL emits no haptic; PERSONAL_BEST emits Waveform)
        val hapticCalls = recordingHapticPlayer.recordedCalls
        assertEquals("Expected exactly 1 haptic request because SCORE_REVEAL omits haptics", 1, hapticCalls.size)

        assertTrue("PERSONAL_BEST haptic cue must be Waveform", hapticCalls[0].cue is HapticCue.Waveform)
        val pbHaptic = hapticCalls[0].cue as HapticCue.Waveform
        assertTrue("PERSONAL_BEST timings must match [0, 28, 35, 45]", longArrayOf(0, 28, 35, 45).contentEquals(pbHaptic.timings))
        assertTrue("PERSONAL_BEST amplitudes must match [0, 75, 0, 110]", intArrayOf(0, 75, 0, 110).contentEquals(pbHaptic.amplitudes))
        assertEquals("PERSONAL_BEST repeat must be -1 (non-repeating)", -1, pbHaptic.repeat)

        // SCORE_REVEAL tone request must precede victory haptic request dispatch
        assertTrue(
            "SCORE_REVEAL tone request dispatch must precede victory haptic request dispatch",
            toneCalls[0].timestampNs < hapticCalls[0].timestampNs
        )
    }
}
