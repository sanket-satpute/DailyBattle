package com.sanket_satpute_20.dailybattle.data.practice

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.PracticeSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeResult
import com.sanket_satpute_20.dailybattle.domain.practice.PracticeResultRepository
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe in-memory implementation of [PracticeResultRepository].
 *
 * Implements the architecture established in `DefaultDomainAuthorityRegistry`:
 * - domain = DomainType.PRACTICE_SESSION
 * - primaryAuthority = DataAuthority.Local
 * - storageTier = StorageTier.IN_MEMORY
 * - isLocallyWritable = true
 * - isRemoteAuthoritative = false
 * - requiresSynchronization = false
 * - allowsOfflineRead = true
 * - allowsOfflineMutation = true
 * - conflictResolutionRule = "Local-only; practice plays are never submitted to remote leaderboards or competitive records."
 */
@Singleton
class InMemoryPracticeResultRepository @Inject constructor() : PracticeResultRepository {

    private val resultsById = ConcurrentHashMap<String, PracticeResult>()
    private val resultsBySessionId = ConcurrentHashMap<String, String>() // sessionId -> resultId
    private val userResultsHistory = ConcurrentHashMap<String, MutableList<String>>() // userId -> list of resultIds (newest first)

    override fun savePracticeResult(result: PracticeResult): DomainResult<Unit> {
        val rId = result.resultId.value
        val sId = result.sessionId.value
        val uId = result.userId.value

        resultsById[rId] = result
        resultsBySessionId[sId] = rId

        val history = userResultsHistory.computeIfAbsent(uId) { mutableListOf() }
        synchronized(history) {
            history.remove(rId)
            history.add(0, rId)
        }

        return DomainResult.Success(Unit)
    }

    override fun getPracticeResult(resultId: PracticeResultId): DomainResult<PracticeResult> {
        val res = resultsById[resultId.value]
            ?: return DomainResult.Failure(AppError.Domain)
        return DomainResult.Success(res)
    }

    override fun getPracticeResultBySessionId(sessionId: PracticeSessionId): DomainResult<PracticeResult> {
        val rId = resultsBySessionId[sessionId.value]
            ?: return DomainResult.Failure(AppError.Domain)
        val res = resultsById[rId]
            ?: return DomainResult.Failure(AppError.Domain)
        return DomainResult.Success(res)
    }

    override fun getUserPracticeResults(userId: UserId): DomainResult<List<PracticeResult>> {
        val history = userResultsHistory[userId.value] ?: return DomainResult.Success(emptyList())
        val resultIds = synchronized(history) { history.toList() }
        val results = resultIds.mapNotNull { resultsById[it] }
        return DomainResult.Success(results)
    }

    override fun getLatestPracticeResult(userId: UserId): DomainResult<PracticeResult> {
        val history = userResultsHistory[userId.value]
            ?: return DomainResult.Failure(AppError.Domain)
        val latestId = synchronized(history) { history.firstOrNull() }
            ?: return DomainResult.Failure(AppError.Domain)
        val res = resultsById[latestId]
            ?: return DomainResult.Failure(AppError.Domain)
        return DomainResult.Success(res)
    }

    override fun clearPracticeResults(userId: UserId): DomainResult<Unit> {
        val history = userResultsHistory.remove(userId.value)
        if (history != null) {
            synchronized(history) {
                for (rId in history) {
                    val removed = resultsById.remove(rId)
                    if (removed != null) {
                        resultsBySessionId.remove(removed.sessionId.value)
                    }
                }
            }
        }
        return DomainResult.Success(Unit)
    }
}
