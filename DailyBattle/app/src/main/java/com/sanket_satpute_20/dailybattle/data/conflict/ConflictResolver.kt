package com.sanket_satpute_20.dailybattle.data.conflict

import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import com.sanket_satpute_20.dailybattle.data.sync.AddFriendPayload
import com.sanket_satpute_20.dailybattle.data.sync.CompleteBattlePayload
import com.sanket_satpute_20.dailybattle.data.sync.PendingOperation
import com.sanket_satpute_20.dailybattle.data.sync.StartBattlePayload
import com.sanket_satpute_20.dailybattle.data.sync.SubmitChallengeResultPayload
import com.sanket_satpute_20.dailybattle.data.sync.SyncOperationType
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contract for identifying, resolving, and reconciling data, session, and queue conflicts.
 *
 * Conforms to:
 * - 08_DATA_AND_API.md §74 (Backend is authoritative for conflict resolution)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §24 (Reconciliation of server-completed battles)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicate requests, sessions, submissions, or friend actions)
 */
interface ConflictResolver {

    /**
     * Evaluates data freshness and determines whether incoming server data is stale
     * compared to local cached timestamp.
     */
    fun isServerDataStale(cachedTimestamp: Long, remoteTimestamp: Long): Boolean

    /**
     * Reconciles an active local battle session with authoritative remote progress.
     */
    fun resolveSessionConflict(
        localSession: BattleSession,
        remoteSessionStatus: BattleSessionStatus,
        remoteCompletedAt: Long?,
    ): ConflictResolutionAction<BattleSession>

    /**
     * Inspects candidate pending mutation against existing queued operations
     * to prevent duplicate or conflicting operations per Rule 42.
     * Returns a [ConflictResolutionAction.DiscardMutation] if conflict or duplicate detected, or null if valid.
     */
    fun resolveDuplicateMutation(
        existingOps: List<PendingOperation>,
        candidate: PendingOperation,
    ): ConflictResolutionAction<PendingOperation>?
}

@Singleton
class DefaultConflictResolver @Inject constructor(
    private val json: Json = DailyBattleJson,
) : ConflictResolver {

    override fun isServerDataStale(cachedTimestamp: Long, remoteTimestamp: Long): Boolean {
        // Server response with older timestamp than local record indicates stale replication (§74)
        return remoteTimestamp < cachedTimestamp
    }

    override fun resolveSessionConflict(
        localSession: BattleSession,
        remoteSessionStatus: BattleSessionStatus,
        remoteCompletedAt: Long?,
    ): ConflictResolutionAction<BattleSession> {
        // Backend is authoritative for final session completion (§24, §74)
        if (remoteSessionStatus == BattleSessionStatus.COMPLETED &&
            localSession.status != BattleSessionStatus.COMPLETED
        ) {
            val reconciled = localSession.copy(
                status = BattleSessionStatus.COMPLETED,
                completedAt = remoteCompletedAt ?: localSession.completedAt,
                currentChallenge = null,
            )
            return ConflictResolutionAction.ReconcileSession(
                session = reconciled,
                reason = "Remote session is authoritative and already marked COMPLETED (§24)",
            )
        }

        if (localSession.status == BattleSessionStatus.COMPLETED &&
            remoteSessionStatus != BattleSessionStatus.COMPLETED
        ) {
            // Local state preserved per §50 until pending completion operation synchronizes
            return ConflictResolutionAction.RetainLocal(
                localData = localSession,
                reason = "Local session completed; awaiting synchronization acknowledgment",
            )
        }

        return ConflictResolutionAction.RetainLocal(
            localData = localSession,
            reason = "Session states compatible",
        )
    }

    override fun resolveDuplicateMutation(
        existingOps: List<PendingOperation>,
        candidate: PendingOperation,
    ): ConflictResolutionAction<PendingOperation>? {
        // 1. Direct ID collision
        if (existingOps.any { it.operationId == candidate.operationId }) {
            return ConflictResolutionAction.DiscardMutation(
                operationId = candidate.operationId,
                reason = "Operation with identical operationId already queued",
            )
        }

        // 2. Domain-level duplicate protection per Rule 42
        for (existing in existingOps) {
            if (existing.operationType != candidate.operationType) {
                // Check conflicting lifecycle sequence (e.g. START_BATTLE after COMPLETE_BATTLE)
                if (existing.operationType == SyncOperationType.COMPLETE_BATTLE &&
                    candidate.operationType == SyncOperationType.START_BATTLE
                ) {
                    val existingPayload = runCatching {
                        json.decodeFromString<CompleteBattlePayload>(existing.payload)
                    }.getOrNull()
                    val candidatePayload = runCatching {
                        json.decodeFromString<StartBattlePayload>(candidate.payload)
                    }.getOrNull()

                    if (existingPayload != null && candidatePayload != null &&
                        existingPayload.clientRequestId == candidatePayload.clientRequestId
                    ) {
                        return ConflictResolutionAction.DiscardMutation(
                            operationId = candidate.operationId,
                            reason = "Cannot start battle when completion mutation already queued",
                        )
                    }
                }
                continue
            }

            when (candidate.operationType) {
                SyncOperationType.START_BATTLE -> {
                    val ex = runCatching { json.decodeFromString<StartBattlePayload>(existing.payload) }.getOrNull()
                    val cand = runCatching { json.decodeFromString<StartBattlePayload>(candidate.payload) }.getOrNull()
                    if (ex != null && cand != null) {
                        if (ex.clientRequestId == cand.clientRequestId ||
                            (ex.battleId == cand.battleId && ex.userId == cand.userId)
                        ) {
                            return ConflictResolutionAction.DiscardMutation(
                                operationId = candidate.operationId,
                                reason = "Duplicate START_BATTLE for battleId=${cand.battleId} (Rule 42)",
                            )
                        }
                    }
                }

                SyncOperationType.SUBMIT_CHALLENGE_RESULT -> {
                    val ex = runCatching {
                        json.decodeFromString<SubmitChallengeResultPayload>(existing.payload)
                    }.getOrNull()
                    val cand = runCatching {
                        json.decodeFromString<SubmitChallengeResultPayload>(candidate.payload)
                    }.getOrNull()
                    if (ex != null && cand != null) {
                        if (ex.clientRequestId == cand.clientRequestId ||
                            (ex.sessionId == cand.sessionId && ex.challengeId == cand.challengeId)
                        ) {
                            return ConflictResolutionAction.DiscardMutation(
                                operationId = candidate.operationId,
                                reason = "Duplicate SUBMIT_CHALLENGE_RESULT for challengeId=${cand.challengeId} (Rule 42)",
                            )
                        }
                    }
                }

                SyncOperationType.COMPLETE_BATTLE -> {
                    val ex = runCatching { json.decodeFromString<CompleteBattlePayload>(existing.payload) }.getOrNull()
                    val cand = runCatching { json.decodeFromString<CompleteBattlePayload>(candidate.payload) }.getOrNull()
                    if (ex != null && cand != null) {
                        if (ex.clientRequestId == cand.clientRequestId || ex.sessionId == cand.sessionId) {
                            return ConflictResolutionAction.DiscardMutation(
                                operationId = candidate.operationId,
                                reason = "Duplicate COMPLETE_BATTLE for sessionId=${cand.sessionId} (Rule 42)",
                            )
                        }
                    }
                }

                SyncOperationType.ADD_FRIEND -> {
                    val ex = runCatching { json.decodeFromString<AddFriendPayload>(existing.payload) }.getOrNull()
                    val cand = runCatching { json.decodeFromString<AddFriendPayload>(candidate.payload) }.getOrNull()
                    if (ex != null && cand != null) {
                        if (ex.clientRequestId == cand.clientRequestId ||
                            (ex.userId == cand.userId && ex.battleCode == cand.battleCode)
                        ) {
                            return ConflictResolutionAction.DiscardMutation(
                                operationId = candidate.operationId,
                                reason = "Duplicate ADD_FRIEND for battleCode=${cand.battleCode} (Rule 42)",
                            )
                        }
                    }
                }

                SyncOperationType.REFRESH_PROFILE -> {
                    // Duplicate profile refresh within queue can be safely collapsed
                    val ex = runCatching { json.decodeFromString<com.sanket_satpute_20.dailybattle.data.sync.RefreshProfilePayload>(existing.payload) }.getOrNull()
                    val cand = runCatching { json.decodeFromString<com.sanket_satpute_20.dailybattle.data.sync.RefreshProfilePayload>(candidate.payload) }.getOrNull()
                    if (ex != null && cand != null && ex.userId == cand.userId) {
                        return ConflictResolutionAction.DiscardMutation(
                            operationId = candidate.operationId,
                            reason = "Redundant REFRESH_PROFILE already queued",
                        )
                    }
                }
            }
        }

        return null
    }
}
