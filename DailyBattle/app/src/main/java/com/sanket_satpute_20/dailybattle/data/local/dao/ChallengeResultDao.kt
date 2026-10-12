package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity

@Dao
interface ChallengeResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ChallengeResultEntity)

    @Query("SELECT * FROM challenge_results WHERE sessionId = :sessionId")
    suspend fun getResultsForSession(sessionId: String): List<ChallengeResultEntity>

    @Query("SELECT * FROM challenge_results WHERE resultId = :resultId LIMIT 1")
    suspend fun getResultById(resultId: String): ChallengeResultEntity?

    @Query("DELETE FROM challenge_results WHERE sessionId = :sessionId")
    suspend fun deleteResultsForSession(sessionId: String): Int
}
