package com.sanket_satpute_20.dailybattle.data.local.fake

import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeResultDao
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity

/**
 * In-memory test double for [ChallengeResultDao].
 *
 * Implements deterministic Map-backed storage keying on `resultId`,
 * accurately reflecting Room's `OnConflictStrategy.REPLACE` behavior.
 */
class FakeChallengeResultDao : ChallengeResultDao {
    val results = mutableMapOf<String, ChallengeResultEntity>()
    var shouldFail: Boolean = false

    override suspend fun insertResult(result: ChallengeResultEntity) {
        if (shouldFail) {
            throw RuntimeException("Simulated SQLite write error")
        }
        results[result.resultId] = result
    }

    override suspend fun getResultsForSession(sessionId: String): List<ChallengeResultEntity> {
        if (shouldFail) {
            throw RuntimeException("Simulated SQLite read error")
        }
        return results.values.filter { it.sessionId == sessionId }
    }

    override suspend fun getResultById(resultId: String): ChallengeResultEntity? {
        if (shouldFail) {
            throw RuntimeException("Simulated SQLite read error")
        }
        return results[resultId]
    }

    override suspend fun deleteResultsForSession(sessionId: String): Int {
        if (shouldFail) {
            throw RuntimeException("Simulated SQLite delete error")
        }
        val before = results.size
        results.entries.removeIf { it.value.sessionId == sessionId }
        return before - results.size
    }
}
