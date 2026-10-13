package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.sync.SyncQueueStore
import com.sanket_satpute_20.dailybattle.data.sync.SyncStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.CompleteBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Report containing details of mutations and sessions reconciled after process death.
 */
data class InterruptedSubmissionRecoveryReport(
    val recoveredSessions: List<BattleSessionId>,
    val reconciledResults: List<BattleResult>,
    val recoveredQueueOperations: List<String>,
)

/**
 * Coordinates recovery of interrupted submissions following process death.
 *
 * Implements Sprint 18.4 (Interrupted Submission):
 * Submit -> Process killed -> Unknown submission status -> without duplicating the mutation.
 *
 * Conforms to:
 * - 08_DATA_AND_API.md §57 (Request Idempotency)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §21 (Challenge Submission Failure)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §22 (Duplicate Challenge Submission Prevention)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §23 (Challenge Result Unknown State)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §24 (Battle Completion Failure & Reconciliation)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §48-50, §55 (Sync Queue and State Reconciliation)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicate requests or attempts)
 * - 15_ANTIGRAVITY_RULES.md Rule 46 (Idempotency Rule)
 */
interface InterruptedSubmissionRecoverer {
    suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<InterruptedSubmissionRecoveryReport>
}

@Singleton
class DefaultInterruptedSubmissionRecoverer @Inject constructor(
    private val sessionRepository: BattleSessionRepository,
    private val resultRepository: BattleResultRepository,
    private val completeBattleUseCase: CompleteBattleUseCase,
    private val queueStore: SyncQueueStore,
) : InterruptedSubmissionRecoverer {

    override suspend fun recoverInterruptedSubmissions(userId: UserId): DomainResult<InterruptedSubmissionRecoveryReport> {
        return try {
            val recoveredSessions = mutableListOf<BattleSessionId>()
            val reconciledResults = mutableListOf<BattleResult>()
            val recoveredQueueOperationIds = mutableListOf<String>()

            // 1. Recover unacknowledged sessions (process killed during completeBattle submission)
            val unacknowledged = sessionRepository.getUnacknowledgedSessions()
            for (session in unacknowledged) {
                // §24: Check Battle Result first
                val existing = resultRepository.getBattleResultBySessionId(session.sessionId).let {
                    if (it is DomainResult.Success) it else resultRepository.getBattleResult(session.userId, session.battleId)
                }

                if (existing is DomainResult.Success) {
                    // Result already confirmed on server/local authority! Reconcile session state without duplicate mutation.
                    val updatedSession = session.copy(
                        status = BattleSessionStatus.COMPLETED,
                        completedAt = existing.value.completedAt,
                        currentChallenge = null,
                    )
                    sessionRepository.saveSession(updatedSession)
                    reconciledResults.add(existing.value)
                    recoveredSessions.add(session.sessionId)
                } else {
                    // Result not found: safe retry with deterministic session idempotencyKey
                    val completionResult = completeBattleUseCase.execute(session)
                    if (completionResult is DomainResult.Success) {
                        reconciledResults.add(completionResult.value)
                        recoveredSessions.add(session.sessionId)
                    }
                }
            }

            // 2. Recover interrupted operations in the sync queue (process killed during sync flight)
            val allOperations = queueStore.getPendingOperations()
            val interrupted = allOperations.filter {
                it.status == SyncStatus.SYNCING || it.status == SyncStatus.UNKNOWN
            }

            for (op in interrupted) {
                // Mark as UNKNOWN status first per §23 / Sprint 18.4
                val unknownOp = op.copy(status = SyncStatus.UNKNOWN)
                queueStore.updateOperation(unknownOp)

                // Safely reset to PENDING for controlled retry using the EXACT SAME payload and clientRequestId
                // to avoid duplicating the mutation (§57, Rule 42, Rule 46)
                val pendingOp = unknownOp.copy(
                    status = SyncStatus.PENDING,
                    retryCount = 0,
                    lastError = "RecoveredFromInterruptedSubmission",
                )
                queueStore.updateOperation(pendingOp)
                recoveredQueueOperationIds.add(pendingOp.operationId)
            }

            DomainResult.Success(
                InterruptedSubmissionRecoveryReport(
                    recoveredSessions = recoveredSessions,
                    reconciledResults = reconciledResults,
                    recoveredQueueOperations = recoveredQueueOperationIds,
                )
            )
        } catch (e: Exception) {
            DomainResult.Failure(AppError.Synchronization)
        }
    }
}
