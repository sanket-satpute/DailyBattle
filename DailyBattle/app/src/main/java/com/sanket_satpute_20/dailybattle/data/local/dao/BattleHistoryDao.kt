package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity

@Dao
interface BattleHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<BattleHistoryItemEntity>)

    @Query("SELECT * FROM battle_history_items WHERE userId = :userId ORDER BY date DESC")
    suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity>

    @Query("DELETE FROM battle_history_items WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String): Int
}
