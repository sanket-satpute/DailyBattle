package com.sanket_satpute_20.dailybattle.core.error

/**
 * Structured technical failure categories. They intentionally contain no raw exception, user-facing
 * copy, retry policy, recovery action, request data, or account data.
 */
sealed interface AppError {
    data object Network : AppError
    data object Offline : AppError
    data object Timeout : AppError
    data object Server : AppError
    data object Authentication : AppError
    data object Authorization : AppError
    data object Validation : AppError
    data object Persistence : AppError
    data object Synchronization : AppError
    data object Domain : AppError
    data object BattleState : AppError
    data object Unknown : AppError
}
