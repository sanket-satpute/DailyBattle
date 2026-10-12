package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RivalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRival(rival: RivalEntity)

    @Query("SELECT * FROM rivals WHERE userId = :userId LIMIT 1")
    suspend fun getRivalForUser(userId: String): RivalEntity?

    @Query("SELECT * FROM rivals WHERE userId = :userId LIMIT 1")
    fun observeRivalForUser(userId: String): Flow<RivalEntity?>

    @Query("DELETE FROM rivals WHERE userId = :userId")
    suspend fun deleteRivalForUser(userId: String): Int
}
