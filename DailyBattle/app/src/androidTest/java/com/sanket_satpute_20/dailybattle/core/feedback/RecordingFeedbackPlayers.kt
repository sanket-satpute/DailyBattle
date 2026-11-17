package com.sanket_satpute_20.dailybattle.core.feedback

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Immutable record of an invocation request dispatched to [TonePlayer].
 *
 * Captures the request parameters and timestamp immediately before forwarding to
 * the underlying [TonePlayer] delegate.
 *
 * NOTE: This record proves that an invocation request was dispatched to the framework delegate
 * with the recorded parameters; it does NOT prove successful acoustic playback completion
 * in AudioFlinger or human audible perception.
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
 * Captures the requested [HapticCue] immediately before forwarding to the underlying
 * [HapticPlayer] delegate.
 *
 * NOTE: This record proves that an invocation request was dispatched to the framework delegate
 * with the recorded cue; it does NOT prove successful physical motor vibration or human tactile
 * perception.
 *
 * @param cue The [HapticCue] pattern/timing requested.
 * @param timestampNs System nanotime when the request was intercepted before delegation.
 */
data class RecordedHapticCall(
    val cue: HapticCue,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Thread-safe recording decorator for [TonePlayer].
 *
 * Intercepts every invocation request, records arguments and dispatch timestamps,
 * and delegates the exact call to the underlying Android [delegate].
 *
 * Telemetry boundary:
 * - Records: Framework invocation requests dispatched by [AndroidInteractionFeedback].
 * - Delegates: Exact calls forwarded to [SystemTonePlayer] / Android framework.
 * - Does NOT verify: Audible sound wave generation, AudioFlinger mixer completion, or speaker output.
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
 * - Records: Framework vibration requests dispatched by [AndroidInteractionFeedback].
 * - Delegates: Exact calls forwarded to [SystemHapticPlayer] / Android framework.
 * - Does NOT verify: Physical motor rotation, linear resonant actuator response, or skin tactile perception.
 * - Preserves: Synchronous exception propagation (failures in delegate are not swallowed).
 *
 * Immutability guarantees:
 * - Defensively clones mutable arrays within [HapticCue.Waveform] (`timings` and `amplitudes`)
 *   upon recording to ensure historical records cannot be mutated by caller arrays.
 * - [recordedCalls] returns an immutable snapshot list (`.toList()`).
 */
class RecordingHapticPlayer(
    val delegate: HapticPlayer
) : HapticPlayer {

    private val _recordedCalls = CopyOnWriteArrayList<RecordedHapticCall>()
    val recordedCalls: List<RecordedHapticCall>
        get() = _recordedCalls.toList()

    override fun vibrate(cue: HapticCue) {
        val isolatedCue = when (cue) {
            is HapticCue.OneShot -> cue
            is HapticCue.Waveform -> HapticCue.Waveform(
                timings = cue.timings.clone(),
                amplitudes = cue.amplitudes.clone(),
                repeat = cue.repeat
            )
        }
        _recordedCalls.add(RecordedHapticCall(isolatedCue))
        delegate.vibrate(cue)
    }

    override fun hasVibrator(): Boolean {
        return delegate.hasVibrator()
    }

    fun clear() {
        _recordedCalls.clear()
    }
}
