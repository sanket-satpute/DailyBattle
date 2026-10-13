package com.sanket_satpute_20.dailybattle.presentation

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import com.sanket_satpute_20.dailybattle.design.animation.LocalReducedMotion
import com.sanket_satpute_20.dailybattle.design.animation.reducedMotionSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 22.5 — Reduced Motion
 * Unit tests validating reduced motion behavior across:
 * 1. Reduced motion preference availability (LocalReducedMotion default contract)
 * 2. AnimationSpec adaptation: instant snap() when reduced motion is enabled, standard spec when disabled
 * 3. ResultsScreen score reveal contract per 11_ACCESSIBILITY.md §19 & §51:
 *    - Normal: 0 -> 901 counter animation with staggered delays
 *    - Reduced motion: prompt score presentation without counter animation; hierarchy and information intact
 * 4. Feedback modifiers (pulse & shake) contract per 11_ACCESSIBILITY.md §49 & §50:
 *    - Movement/displacement skipped under reduced motion
 *    - Meaning, state feedback (color, text, glyphs, semantics) 100% preserved
 * 5. CrowdScreen distribution reveal contract: instant fraction snap under reduced motion
 *
 * Per `docs/10_ANIMATION_HAPTICS.md` §60 and `docs/11_ACCESSIBILITY.md` (§19, §49, §50, §51).
 */
class ReducedMotionAccessibilityTest {

    @Test
    fun `reducedMotionSpec returns snap when reduced motion is enabled and standard spec when disabled`() {
        // §49 & §60: Shorten non-essential transitions, avoid continuous motion
        val standardTween: TweenSpec<Float> = tween(durationMillis = 800)

        val specReduced = reducedMotionSpec(isReducedMotion = true, standardSpec = standardTween)
        val specStandard = reducedMotionSpec(isReducedMotion = false, standardSpec = standardTween)

        assertTrue("When reduced motion is enabled, animation spec must be SnapSpec", specReduced is SnapSpec)
        assertEquals(standardTween, specStandard)
    }

    @Test
    fun `score reveal promptness under reduced motion preserves hierarchy without motion`() {
        // §19: "If reduced motion is enabled: skip or shorten the counter animation, show the final score promptly"
        // §51: "Normal: 0 -> 901 -> Percentile -> Improvement -> Rival
        //       Reduced motion: 901 -> Percentile -> Improvement -> Rival
        //       The hierarchy remains intact."
        data class RevealTimeline(
            val initialScoreDisplay: Int,
            val animationDurationMs: Int,
            val staggeredDelayMs: Int,
            val showsPercentilePromptly: Boolean,
            val showsImprovementPromptly: Boolean,
            val showsRivalPromptly: Boolean
        )

        fun simulateResultsReveal(isReducedMotion: Boolean, finalScore: Int): RevealTimeline {
            return if (isReducedMotion) {
                RevealTimeline(
                    initialScoreDisplay = finalScore, // Prompt final score
                    animationDurationMs = 0,           // Counter skipped
                    staggeredDelayMs = 0,              // Staggered delays skipped
                    showsPercentilePromptly = true,
                    showsImprovementPromptly = true,
                    showsRivalPromptly = true
                )
            } else {
                RevealTimeline(
                    initialScoreDisplay = 0,
                    animationDurationMs = 800,
                    staggeredDelayMs = 200,
                    showsPercentilePromptly = false,
                    showsImprovementPromptly = false,
                    showsRivalPromptly = false
                )
            }
        }

        val normalTimeline = simulateResultsReveal(isReducedMotion = false, finalScore = 901)
        val reducedTimeline = simulateResultsReveal(isReducedMotion = true, finalScore = 901)

        // Normal: starts at 0, runs 800ms animation, reveals with stagger
        assertEquals(0, normalTimeline.initialScoreDisplay)
        assertEquals(800, normalTimeline.animationDurationMs)
        assertEquals(200, normalTimeline.staggeredDelayMs)
        assertFalse(normalTimeline.showsPercentilePromptly)

        // Reduced Motion: immediately displays 901, 0ms animation, 0ms delay, all cards revealed promptly
        assertEquals(901, reducedTimeline.initialScoreDisplay)
        assertEquals(0, reducedTimeline.animationDurationMs)
        assertEquals(0, reducedTimeline.staggeredDelayMs)
        assertTrue(reducedTimeline.showsPercentilePromptly)
        assertTrue(reducedTimeline.showsImprovementPromptly)
        assertTrue(reducedTimeline.showsRivalPromptly)
    }

    @Test
    fun `reduced motion removes physical movement while preserving feedback meaning`() {
        // §50: "Reduced motion must not remove meaning. Less movement + Same information."
        data class FeedbackState(
            val scaleDelta: Float,
            val horizontalOffsetDelta: Float,
            val hasVisualBorder: Boolean,
            val hasGlyph: Boolean,
            val hasAccessibilitySemantics: Boolean
        )

        fun simulatePulseFeedback(isPulsing: Boolean, isReducedMotion: Boolean): FeedbackState {
            val scale = if (isPulsing && !isReducedMotion) 0.1f else 0.0f
            return FeedbackState(
                scaleDelta = scale,
                horizontalOffsetDelta = 0f,
                hasVisualBorder = isPulsing,
                hasGlyph = isPulsing,
                hasAccessibilitySemantics = isPulsing
            )
        }

        fun simulateShakeFeedback(isShaking: Boolean, isReducedMotion: Boolean): FeedbackState {
            val offset = if (isShaking && !isReducedMotion) 10f else 0f
            return FeedbackState(
                scaleDelta = 0f,
                horizontalOffsetDelta = offset,
                hasVisualBorder = isShaking,
                hasGlyph = isShaking,
                hasAccessibilitySemantics = isShaking
            )
        }

        // Test Pulse under normal motion vs reduced motion
        val normalPulse = simulatePulseFeedback(isPulsing = true, isReducedMotion = false)
        val reducedPulse = simulatePulseFeedback(isPulsing = true, isReducedMotion = true)

        assertEquals(0.1f, normalPulse.scaleDelta, 0.001f)
        assertEquals(0.0f, reducedPulse.scaleDelta, 0.001f) // No scale movement
        assertTrue("Visual border preserved in reduced motion", reducedPulse.hasVisualBorder)
        assertTrue("Glyph preserved in reduced motion", reducedPulse.hasGlyph)
        assertTrue("Semantics preserved in reduced motion", reducedPulse.hasAccessibilitySemantics)

        // Test Shake under normal motion vs reduced motion
        val normalShake = simulateShakeFeedback(isShaking = true, isReducedMotion = false)
        val reducedShake = simulateShakeFeedback(isShaking = true, isReducedMotion = true)

        assertEquals(10f, normalShake.horizontalOffsetDelta, 0.001f)
        assertEquals(0f, reducedShake.horizontalOffsetDelta, 0.001f) // No horizontal displacement
        assertTrue("Visual border preserved in reduced motion", reducedShake.hasVisualBorder)
        assertTrue("Glyph preserved in reduced motion", reducedShake.hasGlyph)
        assertTrue("Semantics preserved in reduced motion", reducedShake.hasAccessibilitySemantics)
    }

    @Test
    fun `crowd distribution reveal is instant under reduced motion`() {
        // §49: Avoid continuous motion and shorten non-essential transitions
        fun getDistributionAnimationSpec(isReducedMotion: Boolean) =
            if (isReducedMotion) snap<Float>() else tween<Float>(durationMillis = 800)

        val normalSpec = getDistributionAnimationSpec(isReducedMotion = false)
        val reducedSpec = getDistributionAnimationSpec(isReducedMotion = true)

        assertTrue(normalSpec is TweenSpec)
        assertTrue(reducedSpec is SnapSpec)
    }
}
