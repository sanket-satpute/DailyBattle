package com.sanket_satpute_20.dailybattle.core.feedback

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Immutable record of a tone playback request made to [TonePlayer].
 *
 * @param toneType The [android.media.ToneGenerator] tone constant requested.
 * @param durationMs Duration of tone in milliseconds.
 * @param timestampNs System nanotime when the call was intercepted before delegation.
 */
data class RecordedToneCall(
    val toneType: Int,
    val durationMs: Int,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Immutable record of a haptic vibration request made to [HapticPlayer].
 *
 * @param cue The [HapticCue] pattern/timing requested.
 * @param timestampNs System nanotime when the call was intercepted before delegation.
 */
data class RecordedHapticCall(
    val cue: HapticCue,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Thread-safe recording decorator for [TonePlayer].
 *
 * Intercepts every invocation, records invocation arguments and timestamps,
 * and delegates the exact call to the real underlying Android [delegate].
 *
 * Distinguishes:
 * 1. A request dispatched by [AndroidInteractionFeedback].
 * 2. An invocation forwarded to the Android framework implementation.
 * (Note: Does not prove human sensory perception of acoustic output).
 */
class RecordingTonePlayer(
    val delegate: TonePlayer = SystemTonePlayer()
) : TonePlayer {

    private val _recordedCalls = CopyOnWriteArrayList<RecordedToneCall>()
    val recordedCalls: List<RecordedToneCall>
        get() = _recordedCalls.toList()

    @Volatile
    var releaseCount: Int = 0
        private set

    override fun playTone(toneType: Int, durationMs: Int) {
        _recordedCalls.add(RecordedToneCall(toneType, durationMs))
        delegate.playTone(toneType, durationMs)
    }

    override fun release() {
        releaseCount++
        delegate.release()
    }

    fun clear() {
        _recordedCalls.clear()
        releaseCount = 0
    }
}

/**
 * Thread-safe recording decorator for [HapticPlayer].
 *
 * Intercepts every vibration invocation, records arguments and timestamps,
 * and delegates to the real Android [delegate].
 */
class RecordingHapticPlayer(
    val delegate: HapticPlayer
) : HapticPlayer {

    private val _recordedCalls = CopyOnWriteArrayList<RecordedHapticCall>()
    val recordedCalls: List<RecordedHapticCall>
        get() = _recordedCalls.toList()

    override fun vibrate(cue: HapticCue) {
        _recordedCalls.add(RecordedHapticCall(cue))
        delegate.vibrate(cue)
    }

    override fun hasVibrator(): Boolean {
        return delegate.hasVibrator()
    }

    fun clear() {
        _recordedCalls.clear()
    }
}
