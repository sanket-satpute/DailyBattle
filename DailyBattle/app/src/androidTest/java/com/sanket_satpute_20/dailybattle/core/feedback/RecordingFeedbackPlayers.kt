package com.sanket_satpute_20.dailybattle.core.feedback

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Immutable record of an invocation request dispatched to [TonePlayer].
 *
 * Captures the request parameters and timestamp immediately before forwarding to
 * the underlying [TonePlayer] delegate.
 *
 * Verification Level: Framework invocation recorded.
 * NOTE: This record proves that an invocation request was dispatched to the framework delegate
 * with the recorded parameters; it does NOT prove successful acoustic playback completion
 * in AudioFlinger or physical sensory verification.
 *
 * @param toneType The [android.media.ToneGenerator] tone constant requested.
 * @param durationMs Duration of tone in milliseconds.
 * @param timestampNs System nanotime when the request was intercepted before delegation.
 */
data class RecordedToneCall(
    val toneType: Int,
    val durationMs: Int,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Immutable record of a haptic vibration request dispatched to [HapticPlayer].
 *
 * Implements deep immutability for [HapticCue.Waveform]:
 * 1. Clones input timing and amplitude arrays at recording time so subsequent caller array
 *    mutations cannot alter the stored historical telemetry.
 * 2. Returns defensive copies of timing and amplitude arrays whenever [cue] is accessed so
 *    mutations performed on the exposed cue cannot alter the underlying historical record.
 *
 * Verification Level: Framework invocation recorded.
 * NOTE: This record proves that an invocation request was dispatched to the framework delegate
 * with the recorded cue; it does NOT prove physical motor movement or physical sensory verification.
 *
 * @param timestampNs System nanotime when the request was intercepted before delegation.
 */
class RecordedHapticCall(
    cue: HapticCue,
    val timestampNs: Long = System.nanoTime()
) {
    private val rawCue: HapticCue = when (cue) {
        is HapticCue.OneShot -> cue
        is HapticCue.Waveform -> HapticCue.Waveform(
            timings = cue.timings.clone(),
            amplitudes = cue.amplitudes.clone(),
            repeat = cue.repeat
        )
    }

    val cue: HapticCue
        get() = when (rawCue) {
            is HapticCue.OneShot -> rawCue
            is HapticCue.Waveform -> HapticCue.Waveform(
                timings = rawCue.timings.clone(),
                amplitudes = rawCue.amplitudes.clone(),
                repeat = rawCue.repeat
            )
        }

    fun copy(cue: HapticCue = this.cue, timestampNs: Long = this.timestampNs): RecordedHapticCall =
        RecordedHapticCall(cue, timestampNs)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecordedHapticCall) return false
        return rawCue == other.rawCue && timestampNs == other.timestampNs
    }

    override fun hashCode(): Int {
        var result = rawCue.hashCode()
        result = 31 * result + timestampNs.hashCode()
        return result
    }

    override fun toString(): String = "RecordedHapticCall(cue=$cue, timestampNs=$timestampNs)"
}

/**
 * Thread-safe recording decorator for [TonePlayer].
 *
 * Intercepts every invocation request, records arguments and dispatch timestamps,
 * and delegates the exact call to the underlying Android [delegate].
 *
 * Telemetry boundary:
 * - Verification Level: Framework invocation recorded.
 * - Records: Framework invocation requests dispatched by [AndroidInteractionFeedback].
 * - Delegates: Exact calls forwarded to [SystemTonePlayer] / Android framework.
 * - Does NOT prove: Audible sound wave generation, AudioFlinger mixer completion, or speaker output.
 * - Preserves: Synchronous exception propagation (failures in delegate are not swallowed).
 *
 * Thread-safety guarantees:
 * - Uses [CopyOnWriteArrayList] for recorded requests.
 * - [recordedCalls] returns an immutable snapshot list (`.toList()`).
 * - [releaseCount] tracks calls via an [AtomicInteger] for atomic increments across concurrent threads.
 */
class RecordingTonePlayer(
    val delegate: TonePlayer = SystemTonePlayer()
) : TonePlayer {

    private val _recordedCalls = CopyOnWriteArrayList<RecordedToneCall>()
    val recordedCalls: List<RecordedToneCall>
        get() = _recordedCalls.toList()

    private val _releaseCount = AtomicInteger(0)
    val releaseCount: Int
        get() = _releaseCount.get()

    override fun playTone(toneType: Int, durationMs: Int) {
        _recordedCalls.add(RecordedToneCall(toneType, durationMs))
        delegate.playTone(toneType, durationMs)
    }

    override fun release() {
        _releaseCount.incrementAndGet()
        delegate.release()
    }

    fun clear() {
        _recordedCalls.clear()
        _releaseCount.set(0)
    }
}

/**
 * Thread-safe recording decorator for [HapticPlayer].
 *
 * Intercepts every vibration invocation request, records cue arguments and dispatch timestamps,
 * and delegates to the underlying Android [delegate].
 *
 * Telemetry boundary:
 * - Verification Level: Framework invocation recorded.
 * - Records: Framework vibration requests dispatched by [AndroidInteractionFeedback].
 * - Delegates: Exact calls forwarded to [SystemHapticPlayer] / Android framework.
 * - Does NOT prove: Physical motor rotation, linear resonant actuator response, or skin tactile perception.
 * - Preserves: Synchronous exception propagation (failures in delegate are not swallowed).
 *
 * Immutability guarantees:
 * - Deep immutability for [HapticCue.Waveform]: input arrays are defensively cloned upon recording,
 *   and defensive copies are returned whenever [RecordedHapticCall.cue] is accessed.
 * - [recordedCalls] returns an immutable snapshot list (`.toList()`).
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
