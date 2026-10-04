package com.sanket_satpute_20.dailybattle.domain.shift

/**
 * Represents the conceptual grid in the Shift challenge.
 * 
 * Grid size and cell content logic is blocked by DEC-GAME-002, 
 * so this provides a flexible data structure for domain boundaries.
 */
data class ShiftGrid(
    val cells: List<ShiftCell>
)

data class ShiftCell(
    val id: String,
    val isOccupied: Boolean,
    val elementId: String? = null
)
