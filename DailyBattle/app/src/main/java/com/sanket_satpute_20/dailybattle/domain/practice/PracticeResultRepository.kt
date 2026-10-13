package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Storage interface for Practice Results.
 *
 * Requirements:
 * - `ROADMAP.md` Phase 19, Sprint 19.3: Practice results remain isolated.
 * - `02_GAMEPLAY.md` §75: Data separation between Official Battle and Practice Session.
 * - `08_DATA_AND_API.md` §19: Practice results must not be mixed into official history.
 * - `DefaultDomainAuthorityRegistry`:
 *   * domain = DomainType.PRACTICE_SESSION
 *   * primaryAuthority = DataAuthority.Local
 *   * storageTier = StorageTier.IN_MEMORY
 *   * conflictResolutionRule = "Local-only; practice plays are never submitted to remote leaderboards or competitive records."
 */
interface PracticeResultRepository {
    /**
     * Saves or updates an isolated practice result.
     */
    fun savePracticeResult(result: PracticeResult): DomainResult<Unit>

    /**
     * Retrieves a practice result by its unique result ID.
     */
    fun getPracticeResult(resultId: PracticeResultId): DomainResult<PracticeResult>

    /**
     * Retrieves the practice result associated with a specific practice session ID.
     */
    fun getPracticeResultBySessionId(sessionId: PracticeSessionId): DomainResult<PracticeResult>

    /**
     * Retrieves all practice results for a user, ordered from most recent to oldest.
     */
    fun getUserPracticeResults(userId: UserId): DomainResult<List<PracticeResult>>

    /**
     * Retrieves the most recent practice result for a user.
     */
    fun getLatestPracticeResult(userId: UserId): DomainResult<PracticeResult>

    /**
     * Clears all practice results for a user.
     */
    fun clearPracticeResults(userId: UserId): DomainResult<Unit>
}
