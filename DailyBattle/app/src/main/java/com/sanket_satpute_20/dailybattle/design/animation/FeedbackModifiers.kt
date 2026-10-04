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
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPulsing) {
        if (isPulsing) {
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

    this.scale(scale.value)
}

/**
 * Applies a short horizontal shake animation to the element.
 *
 * Designed for "Incorrect" interactions per `10_ANIMATION_HAPTICS.md`.
 * The shake is "short, localized, subtle".
 *
 * @param isShaking Trigger for the animation.
 */
fun Modifier.shake(isShaking: Boolean): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            // Short localized shake sequence (subtle)
            offsetX.animateTo(10f, tween(50))
            offsetX.animateTo(-10f, tween(50))
            offsetX.animateTo(5f, tween(50))
            offsetX.animateTo(-5f, tween(50))
            offsetX.animateTo(0f, tween(50))
        }
    }

    this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}
