package com.sanket_satpute_20.dailybattle.domain.snap

/**
 * Marker interface for elements presented to the user during Snap.
 *
 * Can be a Target or a Distractor. Exact types (colors, shapes)
 * are PENDING under DEC-GAME-001.
 */
interface SnapElement {
    val id: String
}

/**
 * A correct element the user is supposed to tap.
 */
interface SnapTarget : SnapElement

/**
 * An incorrect element the user must avoid tapping.
 */
interface SnapDistractor : SnapElement
