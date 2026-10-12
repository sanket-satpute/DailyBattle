package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleEntity

@Dao
interface BattleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattle(battle: BattleEntity)

    @Query("SELECT * FROM battles WHERE battleId = :battleId LIMIT 1")
    suspend fun getBattleById(battleId: String): BattleEntity?

    @Query("SELECT * FROM battles WHERE battleDate = :date LIMIT 1")
    suspend fun getBattleByDate(date: Long): BattleEntity?

    @Query("DELETE FROM battles WHERE battleId = :battleId")
    suspend fun deleteBattleById(battleId: String): Int

    @Query("DELETE FROM battles")
    suspend fun clearBattles(): Int
}
