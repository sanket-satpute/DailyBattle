package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity

@Dao
interface RivalMatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchHistory(items: List<RivalMatchHistoryEntity>)

    @Query("SELECT * FROM rival_match_history WHERE userId = :userId ORDER BY date DESC")
    suspend fun getMatchHistoryForUser(userId: String): List<RivalMatchHistoryEntity>

    @Query("DELETE FROM rival_match_history WHERE userId = :userId")
    suspend fun clearMatchHistoryForUser(userId: String): Int
}
