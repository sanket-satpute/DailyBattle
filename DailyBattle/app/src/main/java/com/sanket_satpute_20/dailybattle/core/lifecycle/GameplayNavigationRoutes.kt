package com.sanket_satpute_20.dailybattle.core.lifecycle

/**
 * Standard route identifiers used by lifecycle restoration and recovery coordinators.
 *
 * Defined in core to preserve layer isolation without depending upward on UI navigation components.
 */
object GameplayNavigationRoutes {
    const val BATTLE_INTRO = "battle_intro"
    const val SNAP = "snap"
    const val SHIFT = "shift"
    const val CROWD_CALL = "crowd_call"
    const val RESULTS = "results"
}
