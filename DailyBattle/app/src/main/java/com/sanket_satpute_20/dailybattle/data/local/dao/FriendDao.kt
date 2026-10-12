package com.sanket_satpute_20.dailybattle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT * FROM friends WHERE userId = :userId OR friendUserId = :userId")
    suspend fun getFriendsForUser(userId: String): List<FriendEntity>

    @Query("SELECT * FROM friends WHERE userId = :userId OR friendUserId = :userId")
    fun observeFriendsForUser(userId: String): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE (userId = :userId AND friendUserId = :friendUserId) OR (userId = :friendUserId AND friendUserId = :userId) LIMIT 1")
    suspend fun getFriend(userId: String, friendUserId: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE friendshipId = :friendshipId LIMIT 1")
    suspend fun getFriendById(friendshipId: String): FriendEntity?

    @Query("DELETE FROM friends WHERE friendshipId = :friendshipId")
    suspend fun deleteFriendById(friendshipId: String): Int

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun clearFriendsForUser(userId: String): Int
}
