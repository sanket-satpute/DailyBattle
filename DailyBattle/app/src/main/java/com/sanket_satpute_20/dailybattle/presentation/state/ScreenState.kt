package com.sanket_satpute_20.dailybattle.presentation.state

import com.sanket_satpute_20.dailybattle.core.error.AppError

/**
 * Mutually exclusive presentation states for a screen with asynchronously supplied content.
 * The error category is retained without introducing user-facing copy or recovery behavior.
 */
sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>

    data class Success<T>(val value: T) : ScreenState<T>

    data object Empty : ScreenState<Nothing>

    data class Error(val error: AppError) : ScreenState<Nothing>
}
