package com.sanket_satpute_20.dailybattle.domain.crowd

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Defines the contract for processing Crowd Call challenge interactions.
 *
 * This engine holds authoritative domain state for the challenge session.
 */
interface CrowdEngine {
    
    /**
     * The current state of the challenge.
     */
    val currentState: CrowdState

    /**
     * Prepares the challenge for presentation, initializing the question.
     */
    fun prepare(): DomainResult<CrowdState>

    /**
     * Starts the challenge timer and presentation.
     */
    fun start(): DomainResult<CrowdState>

    /**
     * Handles user inputs or environmental timeouts.
     */
    fun handleInput(event: CrowdEvent): DomainResult<CrowdState>

    /**
     * Evaluates the interactions to produce a raw result.
     * Returns null if the challenge is not complete or invalid.
     */
    fun evaluate(): RawCrowdResult?

    /**
     * Finalizes the challenge and exposes the raw result.
     */
    fun complete(): DomainResult<CrowdState>
}
