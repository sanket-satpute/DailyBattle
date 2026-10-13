package com.sanket_satpute_20.dailybattle.domain.practice

/**
 * Lifecycle status of a Practice Session.
 *
 * Distinct from official battle sessions:
 * - [NOT_STARTED] — session initialized with a practice configuration but not yet begun
 * - [IN_PROGRESS] — practice gameplay is actively underway
 * - [COMPLETED]   — practice session was successfully finished
 * - [INTERRUPTED] — practice session was interrupted or abandoned before completion
 */
enum class PracticeSessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    INTERRUPTED,
}
