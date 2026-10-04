package com.sanket_satpute_20.dailybattle.domain.shift

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Domain engine interface for Shift.
 *
 * Implements the required lifecycle from `07_TECHNICAL_ARCHITECTURE.md`:
 * - prepare()
 * - start()
 * - handleInput()
 * - evaluate()
 * - complete()
 *
 * The actual implementation of grid generation and scoring is BLOCKED
 * because DEC-GAME-002 (Shift Algorithm) is PENDING.
 *
 * This boundary allows UI to interact with the challenge cleanly without
 * coupling to specific generation formulas.
 */
interface ShiftEngine {
    val currentState: ShiftState

    /**
     * Initializes the engine.
     */
    fun prepare(): DomainResult<ShiftState>

    /**
     * Starts the challenge (enters observation phase).
     */
    fun start(): DomainResult<ShiftState>

    /**
     * Accepts a domain event (e.g. TapCell, BeginShift).
     */
    fun handleInput(event: ShiftEvent): DomainResult<ShiftState>

    /**
     * Returns the evaluated result of the challenge so far.
     */
    fun evaluate(): RawShiftResult?

    /**
     * Finalizes the challenge and prevents further interaction.
     */
    fun complete(): DomainResult<ShiftState>
}
