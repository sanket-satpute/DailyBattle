package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity

@Dao
interface ChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Query("SELECT * FROM challenges WHERE battleId = :battleId ORDER BY challenge_order ASC")
    suspend fun getChallengesForBattle(battleId: String): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getChallengeById(challengeId: String): ChallengeEntity?

    @Query("DELETE FROM challenges WHERE battleId = :battleId")
    suspend fun deleteChallengesForBattle(battleId: String): Int
}
