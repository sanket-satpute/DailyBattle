package com.sanket_satpute_20.dailybattle.core.feedback

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Focused verification test suite for recording decorators ([RecordingTonePlayer] and [RecordingHapticPlayer]).
 *
 * Verification Level: Fake delegate verified.
 *
 * Verifies:
 * 1. Invocation argument recording and framework delegation.
 * 2. Atomic concurrent release counter increments and safe reset.
 * 3. Defensive cloning of [HapticCue.Waveform] arrays against post-recording caller mutations.
 * 4. Deep immutability of exposed [RecordedHapticCall.cue] snapshots against consumer array mutations.
 * 5. Preservation of original cue instance and arguments forwarded to delegate.
 * 6. Snapshot immutability of returned recorded call lists.
 * 7. Transparent exception propagation from underlying delegates.
 */
@RunWith(AndroidJUnit4::class)
class RecordingFeedbackPlayersTest {

    private class FakeTonePlayer : TonePlayer {
        val playedTones = mutableListOf<Pair<Int, Int>>()
        var released = false

        override fun playTone(toneType: Int, durationMs: Int) {
            playedTones.add(toneType to durationMs)
        }

        override fun release() {
            released = true
        }
    }

    private class FakeHapticPlayer(private val hasVibratorVal: Boolean = true) : HapticPlayer {
        val vibratedCues = mutableListOf<HapticCue>()

        override fun vibrate(cue: HapticCue) {
            vibratedCues.add(cue)
        }

        override fun hasVibrator(): Boolean = hasVibratorVal
    }

    @Test
    fun recordingTonePlayer_recordsInvocationArguments_andDelegates() {
        val fakeDelegate = FakeTonePlayer()
        val recordingPlayer = RecordingTonePlayer(delegate = fakeDelegate)

        recordingPlayer.playTone(toneType = 24, durationMs = 80)

        // Verifies decorator recorded invocation arguments
        val calls = recordingPlayer.recordedCalls
        assertEquals("Should record exactly 1 tone call", 1, calls.size)
        assertEquals("Tone type must match", 24, calls[0].toneType)
        assertEquals("Duration must match", 80, calls[0].durationMs)
        assertTrue("Timestamp should be recorded", calls[0].timestampNs > 0)

        // Verifies call was forwarded to delegate
        assertEquals("Delegate should have received exactly 1 call", 1, fakeDelegate.playedTones.size)
        assertEquals(24 to 80, fakeDelegate.playedTones[0])
    }

    @Test
    fun recordingHapticPlayer_recordsInvocationArguments_andDelegates() {
        val fakeDelegate = FakeHapticPlayer()
        val recordingPlayer = RecordingHapticPlayer(delegate = fakeDelegate)

        val cue = HapticCue.OneShot(durationMs = 25L, amplitude = 90)
        recordingPlayer.vibrate(cue)

        // Verifies decorator recorded invocation arguments
        val calls = recordingPlayer.recordedCalls
        assertEquals("Should record exactly 1 haptic call", 1, calls.size)
        assertEquals("Cue must match", cue, calls[0].cue)
        assertTrue("Timestamp should be recorded", calls[0].timestampNs > 0)

        // Verifies call was forwarded to delegate
        assertEquals("Delegate should have received exactly 1 call", 1, fakeDelegate.vibratedCues.size)
        assertEquals(cue, fakeDelegate.vibratedCues[0])

        // Verifies hasVibrator() delegation
        assertTrue("hasVibrator should delegate", recordingPlayer.hasVibrator())
    }

    @Test
    fun recordingTonePlayer_concurrentRelease_threadSafeCounter() {
        val fakeDelegate = FakeTonePlayer()
        val recordingPlayer = RecordingTonePlayer(delegate = fakeDelegate)

        val threadCount = 10
        val incrementsPerThread = 50
        val totalExpected = threadCount * incrementsPerThread
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        for (t in 0 until threadCount) {
            executor.execute {
                try {
                    for (i in 0 until incrementsPerThread) {
                        recordingPlayer.release()
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        val completed = latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue("All concurrent release calls must complete without deadlock", completed)
        assertEquals(
            "releaseCount must equal exactly total concurrent calls",
            totalExpected,
            recordingPlayer.releaseCount
        )

        // Verify clear() resets counter safely
        recordingPlayer.clear()
        assertEquals("clear() must reset releaseCount to 0", 0, recordingPlayer.releaseCount)
    }

    @Test
    fun recordingHapticPlayer_waveformArrays_defensivelyCopied() {
        val fakeDelegate = FakeHapticPlayer()
        val recordingPlayer = RecordingHapticPlayer(delegate = fakeDelegate)

        val mutableTimings = longArrayOf(0, 50, 100)
        val mutableAmplitudes = intArrayOf(0, 128, 255)
        val waveform = HapticCue.Waveform(mutableTimings, mutableAmplitudes, repeat = -1)

        recordingPlayer.vibrate(waveform)

        // Mutate caller arrays after recording
        mutableTimings[1] = 9999L
        mutableAmplitudes[1] = 999

        val recordedCue = recordingPlayer.recordedCalls[0].cue as HapticCue.Waveform

        // Assert defensive copy in recordedCalls preserved original values
        assertEquals("Defensive copy must preserve original timing value", 50L, recordedCue.timings[1])
        assertEquals("Defensive copy must preserve original amplitude value", 128, recordedCue.amplitudes[1])
    }

    @Test
    fun recordingHapticPlayer_retrievedWaveformCue_mutationsDoNotCorruptHistoricalTelemetry() {
        val fakeDelegate = FakeHapticPlayer()
        val recordingPlayer = RecordingHapticPlayer(delegate = fakeDelegate)

        val timings = longArrayOf(0, 50, 100)
        val amplitudes = intArrayOf(0, 128, 255)
        val waveform = HapticCue.Waveform(timings, amplitudes, repeat = -1)

        recordingPlayer.vibrate(waveform)

        // Retrieve recorded call and cue
        val firstRetrieval = recordingPlayer.recordedCalls[0].cue as HapticCue.Waveform

        // Mutate arrays on the retrieved cue object
        firstRetrieval.timings[1] = 7777L
        firstRetrieval.amplitudes[1] = 777

        // Re-access the cue from the player to verify historical record was immune
        val secondRetrieval = recordingPlayer.recordedCalls[0].cue as HapticCue.Waveform
        assertEquals(
            "Historical timings must not be altered by mutating a previously retrieved cue",
            50L,
            secondRetrieval.timings[1]
        )
        assertEquals(
            "Historical amplitudes must not be altered by mutating a previously retrieved cue",
            128,
            secondRetrieval.amplitudes[1]
        )
    }

    @Test
    fun recordingHapticPlayer_delegateReceivesOriginalCueInstanceAndValues() {
        val fakeDelegate = FakeHapticPlayer()
        val recordingPlayer = RecordingHapticPlayer(delegate = fakeDelegate)

        val timings = longArrayOf(0, 50, 100)
        val amplitudes = intArrayOf(0, 128, 255)
        val waveform = HapticCue.Waveform(timings, amplitudes, repeat = -1)

        recordingPlayer.vibrate(waveform)

        assertEquals("Delegate must receive exactly 1 cue", 1, fakeDelegate.vibratedCues.size)
        val delegateCue = fakeDelegate.vibratedCues[0] as HapticCue.Waveform

        // Delegate receives original cue instance and exact arrays
        assertTrue("Delegate must receive the original timings array", timings === delegateCue.timings)
        assertTrue("Delegate must receive the original amplitudes array", amplitudes === delegateCue.amplitudes)
        assertEquals(waveform, delegateCue)
    }

    @Test
    fun snapshotIsolation_clearDoesNotAffectPreviouslyReturnedList() {
        val tonePlayer = RecordingTonePlayer(delegate = FakeTonePlayer())
        val hapticPlayer = RecordingHapticPlayer(delegate = FakeHapticPlayer())

        tonePlayer.playTone(1, 10)
        hapticPlayer.vibrate(HapticCue.OneShot(10, 50))

        val toneSnapshot = tonePlayer.recordedCalls
        val hapticSnapshot = hapticPlayer.recordedCalls

        assertEquals(1, toneSnapshot.size)
        assertEquals(1, hapticSnapshot.size)

        // Clear both players
        tonePlayer.clear()
        hapticPlayer.clear()

        // Returned snapshot lists must remain populated and unchanged
        assertEquals("Snapshot must remain intact after clear()", 1, toneSnapshot.size)
        assertEquals("Snapshot must remain intact after clear()", 1, hapticSnapshot.size)

        // Players' current state is empty
        assertEquals(0, tonePlayer.recordedCalls.size)
        assertEquals(0, hapticPlayer.recordedCalls.size)
    }

    @Test
    fun exceptionPropagation_delegateFailurePropagatesDirectly() {
        val throwingTonePlayer = object : TonePlayer {
            override fun playTone(toneType: Int, durationMs: Int) {
                throw IllegalStateException("ToneGenerator failure")
            }
            override fun release() = Unit
        }
        val recordingTonePlayer = RecordingTonePlayer(delegate = throwingTonePlayer)

        try {
            recordingTonePlayer.playTone(1, 10)
            fail("Exception should have propagated from delegate")
        } catch (e: IllegalStateException) {
            assertEquals("ToneGenerator failure", e.message)
        }

        // Decorator should still have recorded the invocation request before delegate failed
        assertEquals("Call should be recorded even if delegate throws", 1, recordingTonePlayer.recordedCalls.size)
    }
}
