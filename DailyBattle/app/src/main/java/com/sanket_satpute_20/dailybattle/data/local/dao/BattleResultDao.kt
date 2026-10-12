package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: BattleResultEntity)

    @Query("SELECT * FROM battle_results WHERE resultId = :resultId LIMIT 1")
    suspend fun getResultById(resultId: String): BattleResultEntity?

    @Query("SELECT * FROM battle_results WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getResultBySessionId(sessionId: String): BattleResultEntity?

    @Query("SELECT * FROM battle_results WHERE userId = :userId AND battleId = :battleId LIMIT 1")
    suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity?

    @Query("SELECT * FROM battle_results WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAllResultsForUser(userId: String): List<BattleResultEntity>

    @Query("SELECT * FROM battle_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun observeAllResultsForUser(userId: String): Flow<List<BattleResultEntity>>

    @Query("DELETE FROM battle_results WHERE resultId = :resultId")
    suspend fun deleteResultById(resultId: String): Int
}
