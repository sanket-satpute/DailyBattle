package com.sanket_satpute_20.dailybattle.core.feedback

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

/**
 * Centralized, settings-gated interaction feedback.
 *
 * MVP intentionally uses short system-generated tones rather than bundled
 * audio assets. This keeps the first implementation offline and asset-free;
 * final authored SFX can replace the tone mapping without changing callers.
 */
@Singleton
class AndroidInteractionFeedback @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : InteractionFeedback {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var toneGenerator: ToneGenerator? = null

    @Synchronized
    override fun emit(event: InteractionFeedbackEvent) {
        val settings = settingsRepository.settingsFlow.value
        if (settings.soundEnabled) {
            playTone(event)
        }
        if (settings.hapticsEnabled) {
            vibrate(event)
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
        runCatching {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 35)
                .also { toneGenerator = it }
            generator.startTone(tone, durationMs)
        }
    }

    private fun vibrate(event: InteractionFeedbackEvent) {
        val effect = when (event) {
            InteractionFeedbackEvent.UI_TAP ->
                VibrationEffect.createOneShot(12, 35)
            InteractionFeedbackEvent.CORRECT ->
                VibrationEffect.createOneShot(24, 80)
            InteractionFeedbackEvent.INCORRECT ->
                VibrationEffect.createWaveform(longArrayOf(0, 18, 28, 18), intArrayOf(0, 65, 0, 65), -1)
            InteractionFeedbackEvent.SCORE_REVEAL -> null
            InteractionFeedbackEvent.PERSONAL_BEST ->
                VibrationEffect.createWaveform(longArrayOf(0, 28, 35, 45), intArrayOf(0, 75, 0, 110), -1)
            InteractionFeedbackEvent.BATTLE_COMPLETE ->
                VibrationEffect.createOneShot(38, 90)
        }
        if (effect != null) {
            runCatching {
                vibrator?.takeIf { it.hasVibrator() }?.vibrate(effect)
            }
        }
    }

    @Synchronized
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
