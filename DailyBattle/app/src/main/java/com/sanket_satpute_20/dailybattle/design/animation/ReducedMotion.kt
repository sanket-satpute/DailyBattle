package com.sanket_satpute_20.dailybattle.design.animation

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * CompositionLocal providing user/system reduced-motion preference.
 *
 * Defaults to false, but can be provided at the theme or container level,
 * or queried from the Android system settings.
 *
 * Per `docs/10_ANIMATION_HAPTICS.md` §60 and `docs/11_ACCESSIBILITY.md` §49, §50, §51.
 */
val LocalReducedMotion = compositionLocalOf { false }

/**
 * Checks whether system-wide reduced motion / animation removal is enabled on Android.
 *
 * Queries ANIMATOR_DURATION_SCALE and TRANSITION_ANIMATION_SCALE from [Settings.Global].
 * Safe against environment exceptions (e.g. robolectric or restricted permissions).
 */
fun isSystemReducedMotionEnabled(context: Context): Boolean {
    return try {
        val resolver = context.contentResolver
        val durationScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        val transitionScale = Settings.Global.getFloat(
            resolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1.0f
        )
        durationScale == 0f || transitionScale == 0f
    } catch (_: Throwable) {
        false
    }
}

/**
 * Remembers whether reduced motion is currently active.
 *
 * Evaluates both the explicit [LocalReducedMotion] CompositionLocal
 * and the Android platform accessibility animation settings.
 */
@Composable
fun rememberIsReducedMotion(): Boolean {
    val local = LocalReducedMotion.current
    if (local) return true
    return runCatching {
        val context = LocalContext.current
        remember(context) { isSystemReducedMotionEnabled(context) }
    }.getOrDefault(false)
}

/**
 * Returns an animation spec adapted for reduced motion.
 * When reduced motion is active, returns [snap] (instant 0ms transition).
 * Otherwise returns the original [standardSpec].
 */
fun <T> reducedMotionSpec(
    isReducedMotion: Boolean,
    standardSpec: AnimationSpec<T>
): AnimationSpec<T> {
    return if (isReducedMotion) {
        snap()
    } else {
        standardSpec
    }
}

/**
 * Returns an [EnterTransition] adapted for reduced motion.
 * When reduced motion is active, returns [EnterTransition.None].
 * Otherwise returns the provided [defaultEnter].
 */
@Composable
fun reducedMotionEnterTransition(
    defaultEnter: EnterTransition = fadeIn()
): EnterTransition {
    val isReduced = rememberIsReducedMotion()
    return if (isReduced) EnterTransition.None else defaultEnter
}

/**
 * Returns an [ExitTransition] adapted for reduced motion.
 * When reduced motion is active, returns [ExitTransition.None].
 * Otherwise returns the provided [defaultExit].
 */
@Composable
fun reducedMotionExitTransition(
    defaultExit: ExitTransition = fadeOut()
): ExitTransition {
    val isReduced = rememberIsReducedMotion()
    return if (isReduced) ExitTransition.None else defaultExit
}
