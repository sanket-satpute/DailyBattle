package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileForUser(userId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    fun observeProfileForUser(userId: String): Flow<ProfileEntity?>

    @Query("DELETE FROM profiles WHERE userId = :userId")
    suspend fun deleteProfileForUser(userId: String): Int
}
