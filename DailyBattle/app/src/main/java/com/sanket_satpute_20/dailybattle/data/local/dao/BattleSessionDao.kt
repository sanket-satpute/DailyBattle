package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity

@Dao
interface BattleSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BattleSessionEntity)

    @Query("SELECT * FROM battle_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): BattleSessionEntity?

    @Query("SELECT * FROM battle_sessions WHERE userId = :userId AND battleId = :battleId AND mode = :mode LIMIT 1")
    suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity?

    @Query("SELECT * FROM battle_sessions WHERE userId = :userId AND mode = :mode AND status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveSession(userId: String, mode: String): BattleSessionEntity?

    @Query("DELETE FROM battle_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String): Int
}
