package com.sanket_satpute_20.dailybattle.domain.practice

/**
 * Practice difficulty level.
 *
 * Per `02_GAMEPLAY.md` §36 and `ROADMAP.md` Sprint 19.4:
 * Adaptive AI personalization is STATUS: PENDING.
 *
 * [STANDARD] is the only currently approved difficulty level.
 * [ADAPTIVE_PENDING] exists as a domain placeholder for future approved adaptive behavior.
 * It must be rejected by [AdaptivePracticePolicy] until the product decision is made.
 *
 * Per `15_ANTIGRAVITY_RULES.md` Rule 102:
 * "Do not add: arbitrary AI personalization, unless explicitly approved."
 */
enum class PracticeDifficulty {
    /** Standard fixed difficulty — currently the only approved mode. */
    STANDARD,
    /** Placeholder for future adaptive difficulty — NOT approved for use. See [AdaptivePracticePolicy]. */
    ADAPTIVE_PENDING,
}

