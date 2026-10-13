package com.sanket_satpute_20.dailybattle.domain.practice

import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Storage interface for Practice Sessions.
 *
 * Requirements:
 * - `08_DATA_AND_API.md` §19: Practice sessions are isolated from official battle sessions.
 * - `DefaultDomainAuthorityRegistry`:
 *   * domain = DomainType.PRACTICE_SESSION
 *   * primaryAuthority = DataAuthority.Local
 *   * storageTier = StorageTier.IN_MEMORY
 *   * conflictResolutionRule = "Local-only; practice plays are never submitted to remote leaderboards or competitive records."
 */
interface PracticeSessionRepository {
    /**
     * Saves or updates a practice session.
     */
    fun savePracticeSession(session: PracticeSession)

    /**
     * Retrieves a practice session by its ID, or null if it does not exist.
     */
    fun getPracticeSession(sessionId: PracticeSessionId): PracticeSession?

    /**
     * Retrieves the currently active (IN_PROGRESS or NOT_STARTED) practice session for a user, if any.
     */
    fun getActivePracticeSession(userId: UserId): PracticeSession?

    /**
     * Retrieves the recent practice sessions for a user, ordered with most recent first.
     */
    fun getRecentPracticeSessions(userId: UserId, limit: Int = 10): List<PracticeSession>

    /**
     * Clears or removes the active practice session for a user.
     */
    fun clearActivePracticeSession(userId: UserId)

    /**
     * Deletes a specific practice session by ID.
     */
    fun deletePracticeSession(sessionId: PracticeSessionId)
}
