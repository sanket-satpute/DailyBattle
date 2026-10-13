package com.sanket_satpute_20.dailybattle.design.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Applies a short visual pulse animation to the element.
 *
 * Designed for "Correct" interactions per `10_ANIMATION_HAPTICS.md`.
 * 
 * @param isPulsing Trigger for the animation. When true, animates scale up and then springs back.
 */
fun Modifier.pulse(isPulsing: Boolean): Modifier = composed {
    val isReducedMotion = rememberIsReducedMotion()
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPulsing, isReducedMotion) {
        if (isPulsing) {
            if (isReducedMotion) {
                // Per 11_ACCESSIBILITY.md §49/§50: Skip movement under reduced motion.
                // Visual feedback is maintained through border/color/glyph changes.
                scale.snapTo(1f)
            } else {
                // Quick scale up
                scale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = tween(durationMillis = 100)
                )
                // Spring back
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.5f,
                        stiffness = 500f
                    )
                )
            }
        }
    }

    this.scale(scale.value)
}

/**
 * Applies a short horizontal shake animation to the element.
 *
 * Designed for "Incorrect" interactions per `10_ANIMATION_HAPTICS.md`.
 * The shake is "short, localized, subtle".
 * Respects reduced-motion preference per `11_ACCESSIBILITY.md` §49.
 *
 * @param isShaking Trigger for the animation.
 */
fun Modifier.shake(isShaking: Boolean): Modifier = composed {
    val isReducedMotion = rememberIsReducedMotion()
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isShaking, isReducedMotion) {
        if (isShaking) {
            if (isReducedMotion) {
                // Per 11_ACCESSIBILITY.md §49/§50: Skip horizontal shake displacement.
                // Visual feedback is maintained through border/color/glyph changes.
                offsetX.snapTo(0f)
            } else {
                // Short localized shake sequence (subtle)
                offsetX.animateTo(10f, tween(50))
                offsetX.animateTo(-10f, tween(50))
                offsetX.animateTo(5f, tween(50))
                offsetX.animateTo(-5f, tween(50))
                offsetX.animateTo(0f, tween(50))
            }
        }
    }

    this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}
