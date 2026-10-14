package com.sanket_satpute_20.dailybattle.core.performance

/**
 * Classifies the type of application startup per Android architecture standards:
 * - [COLD]: Fresh process startup; Application and Activity created from scratch.
 * - [WARM]: Process is resident or recreated from saved state; activity restored.
 * - [HOT]: Activity brought to foreground without activity recreation.
 *
 * Conforms to `docs/ROADMAP.md` Sprint 24.1 and `docs/16_DEFINITION_OF_DONE.md` §54.
 */
enum class StartupType {
    COLD,
    WARM,
    HOT
}
