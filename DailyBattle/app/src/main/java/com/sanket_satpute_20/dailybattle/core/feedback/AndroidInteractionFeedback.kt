package com.sanket_satpute_20.dailybattle.core.feedback

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf
import com.sanket_satpute_20.dailybattle.domain.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class InteractionFeedbackEvent {
    UI_TAP,
    CORRECT,
    INCORRECT,
    SCORE_REVEAL,
    PERSONAL_BEST,
    BATTLE_COMPLETE
}

interface InteractionFeedback {
    fun emit(event: InteractionFeedbackEvent)
}

object NoOpInteractionFeedback : InteractionFeedback {
    override fun emit(event: InteractionFeedbackEvent) = Unit
}

val LocalInteractionFeedback = staticCompositionLocalOf<InteractionFeedback> {
    NoOpInteractionFeedback
}

sealed interface HapticCue {
    data class OneShot(val durationMs: Long, val amplitude: Int) : HapticCue
    data class Waveform(val timings: LongArray, val amplitudes: IntArray, val repeat: Int = -1) : HapticCue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Waveform) return false
            return timings.contentEquals(other.timings) && amplitudes.contentEquals(other.amplitudes) && repeat == other.repeat
        }
        override fun hashCode(): Int = 31 * timings.contentHashCode() + amplitudes.contentHashCode()
    }
}

interface TonePlayer {
    fun playTone(toneType: Int, durationMs: Int)
    fun release()
}

interface HapticPlayer {
    fun vibrate(cue: HapticCue)
    fun hasVibrator(): Boolean
}

internal class SystemTonePlayer : TonePlayer {
    private var toneGenerator: ToneGenerator? = null

    @Synchronized
    override fun playTone(toneType: Int, durationMs: Int) {
        runCatching {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 35)
                .also { toneGenerator = it }
            generator.startTone(toneType, durationMs)
        }
    }

    @Synchronized
    override fun release() {
        runCatching {
            toneGenerator?.release()
        }
        toneGenerator = null
    }
}

internal class SystemHapticPlayer(private val context: Context) : HapticPlayer {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun hasVibrator(): Boolean {
        return runCatching { vibrator?.hasVibrator() == true }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    override fun vibrate(cue: HapticCue) {
        if (!hasVibrator()) return
        runCatching {
            val effect = when (cue) {
                is HapticCue.OneShot -> VibrationEffect.createOneShot(cue.durationMs, cue.amplitude)
                is HapticCue.Waveform -> VibrationEffect.createWaveform(cue.timings, cue.amplitudes, cue.repeat)
            }
            vibrator?.vibrate(effect)
        }
    }
}

/**
 * Centralized, settings-gated interaction feedback.
 *
 * MVP intentionally uses short system-generated tones rather than bundled
 * audio assets. This keeps the first implementation offline and asset-free;
 * final authored SFX can replace the tone mapping without changing callers.
 */
@Singleton
class AndroidInteractionFeedback internal constructor(
    private val settingsRepository: SettingsRepository,
    private val tonePlayer: TonePlayer,
    private val hapticPlayer: HapticPlayer
) : InteractionFeedback {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ) : this(
        settingsRepository = settingsRepository,
        tonePlayer = SystemTonePlayer(),
        hapticPlayer = SystemHapticPlayer(context)
    )

    @Synchronized
    override fun emit(event: InteractionFeedbackEvent) {
        val settings = settingsRepository.settingsFlow.value
        if (settings.soundEnabled) {
            runCatching { playTone(event) }
        }
        if (settings.hapticsEnabled) {
            runCatching { vibrate(event) }
        }
    }

    @Synchronized
    private fun playTone(event: InteractionFeedbackEvent) {
        val tone = when (event) {
            InteractionFeedbackEvent.UI_TAP -> ToneGenerator.TONE_PROP_BEEP
            InteractionFeedbackEvent.CORRECT -> ToneGenerator.TONE_PROP_ACK
            InteractionFeedbackEvent.INCORRECT -> ToneGenerator.TONE_PROP_NACK
            InteractionFeedbackEvent.SCORE_REVEAL -> ToneGenerator.TONE_PROP_PROMPT
            InteractionFeedbackEvent.PERSONAL_BEST -> ToneGenerator.TONE_PROP_ACK
            InteractionFeedbackEvent.BATTLE_COMPLETE -> ToneGenerator.TONE_PROP_PROMPT
        }
        val durationMs = when (event) {
            InteractionFeedbackEvent.UI_TAP -> 35
            InteractionFeedbackEvent.CORRECT -> 75
            InteractionFeedbackEvent.INCORRECT -> 95
            InteractionFeedbackEvent.SCORE_REVEAL -> 110
            InteractionFeedbackEvent.PERSONAL_BEST -> 150
            InteractionFeedbackEvent.BATTLE_COMPLETE -> 130
        }
        tonePlayer.playTone(tone, durationMs)
    }

    private fun vibrate(event: InteractionFeedbackEvent) {
        val cue = when (event) {
            InteractionFeedbackEvent.UI_TAP ->
                HapticCue.OneShot(12, 35)
            InteractionFeedbackEvent.CORRECT ->
                HapticCue.OneShot(24, 80)
            InteractionFeedbackEvent.INCORRECT ->
                HapticCue.Waveform(longArrayOf(0, 18, 28, 18), intArrayOf(0, 65, 0, 65), -1)
            InteractionFeedbackEvent.SCORE_REVEAL -> null
            InteractionFeedbackEvent.PERSONAL_BEST ->
                HapticCue.Waveform(longArrayOf(0, 28, 35, 45), intArrayOf(0, 75, 0, 110), -1)
            InteractionFeedbackEvent.BATTLE_COMPLETE ->
                HapticCue.OneShot(38, 90)
        }
        if (cue != null) {
            hapticPlayer.vibrate(cue)
        }
    }

    @Synchronized
    fun release() {
        tonePlayer.release()
    }
}
