package com.sanket_satpute_20.dailybattle.domain.snap

import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Domain engine interface for Snap.
 *
 * Implements the required lifecycle from `07_TECHNICAL_ARCHITECTURE.md`:
 * - prepare()
 * - start()
 * - handleInput()
 * - evaluate()
 * - complete()
 *
 * The actual implementation of target generation and scoring is BLOCKED
 * because DEC-GAME-001 (Snap Algorithm) is PENDING.
 *
 * This boundary allows UI to interact with the challenge cleanly without
 * coupling to specific generation formulas.
 */
interface SnapEngine {
    val currentState: SnapState

    /**
     * Initializes the engine.
     */
    fun prepare(): DomainResult<SnapState>

    /**
     * Starts the challenge/round.
     */
    fun start(): DomainResult<SnapState>

    /**
     * Accepts a domain event (e.g. Tap).
     */
    fun handleInput(event: SnapEvent): DomainResult<SnapState>

    /**
     * Returns the evaluated result of the challenge so far.
     */
    fun evaluate(): RawSnapResult?

    /**
     * Finalizes the challenge and prevents further interaction.
     */
    fun complete(): DomainResult<SnapState>
}
