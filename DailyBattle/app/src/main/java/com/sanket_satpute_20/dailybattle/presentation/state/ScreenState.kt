package com.sanket_satpute_20.dailybattle.presentation.state

/**
 * Mutually exclusive presentation states for a screen with asynchronously supplied content.
 * Error details are intentionally deferred to Sprint 1.4's structured error model.
 */
sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>

    data class Success<T>(val value: T) : ScreenState<T>

    data object Empty : ScreenState<Nothing>

    data object Error : ScreenState<Nothing>
}
