package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity

@Dao
interface ChallengeSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeSession(session: ChallengeSessionEntity)

    @Query("SELECT * FROM challenge_sessions WHERE sessionId = :sessionId")
    suspend fun getChallengeSessionsForSession(sessionId: String): List<ChallengeSessionEntity>

    @Query("SELECT * FROM challenge_sessions WHERE challengeId = :challengeId AND sessionId = :sessionId LIMIT 1")
    suspend fun getChallengeSession(challengeId: String, sessionId: String): ChallengeSessionEntity?

    @Query("DELETE FROM challenge_sessions WHERE sessionId = :sessionId")
    suspend fun deleteChallengeSessionsForSession(sessionId: String): Int
}
