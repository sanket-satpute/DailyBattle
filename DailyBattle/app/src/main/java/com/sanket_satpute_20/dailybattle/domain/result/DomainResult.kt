package com.sanket_satpute_20.dailybattle.domain.result

import com.sanket_satpute_20.dailybattle.core.error.AppError

/**
 * Boundary for values produced by domain use cases. The concrete operation contract remains
 * feature-specific, while failures retain a structured category.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>

    data class Failure(val error: AppError) : DomainResult<Nothing>
}
