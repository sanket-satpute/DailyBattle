package com.sanket_satpute_20.dailybattle.integration.fixtures

import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

open class InMemoryUserDao : UserDao {
    val users = mutableMapOf<String, UserEntity>()

    override suspend fun insertUser(user: UserEntity) {
        users[user.userId] = user
    }

    override suspend fun getUserById(userId: String): UserEntity? = users[userId]

    override suspend fun deleteUserById(userId: String): Int {
        return if (users.remove(userId) != null) 1 else 0
    }

    override suspend fun clearUsers(): Int {
        val count = users.size
        users.clear()
        return count
    }
}

open class InMemoryBattleSessionDao : BattleSessionDao {
    val sessions = mutableMapOf<String, BattleSessionEntity>()

    override suspend fun insertSession(session: BattleSessionEntity) {
        sessions[session.sessionId] = session
    }

    override suspend fun getSessionById(sessionId: String): BattleSessionEntity? = sessions[sessionId]

    override suspend fun getSession(userId: String, battleId: String, mode: String): BattleSessionEntity? =
        sessions.values.find { it.userId == userId && it.battleId == battleId && it.mode == mode }

    override suspend fun getActiveSession(userId: String, mode: String): BattleSessionEntity? =
        sessions.values.find { it.userId == userId && it.mode == mode && it.status == "IN_PROGRESS" }

    override suspend fun getSessionsByModeAndStatus(mode: String, status: String): List<BattleSessionEntity> =
        sessions.values.filter { it.mode == mode && it.status == status }

    override suspend fun deleteSessionById(sessionId: String): Int =
        if (sessions.remove(sessionId) != null) 1 else 0
}

open class InMemoryChallengeSessionDao : ChallengeSessionDao {
    val challengeSessions = mutableListOf<ChallengeSessionEntity>()

    override suspend fun insertChallengeSession(session: ChallengeSessionEntity) {
        challengeSessions.removeIf { it.sessionId == session.sessionId && it.challengeId == session.challengeId }
        challengeSessions.add(session)
    }

    override suspend fun insertChallengeSessions(sessions: List<ChallengeSessionEntity>) {
        sessions.forEach { insertChallengeSession(it) }
    }

    override suspend fun getChallengeSessionsForSession(sessionId: String): List<ChallengeSessionEntity> =
        challengeSessions.filter { it.sessionId == sessionId }

    override suspend fun getChallengeSession(challengeId: String, sessionId: String): ChallengeSessionEntity? =
        challengeSessions.find { it.challengeId == challengeId && it.sessionId == sessionId }

    override suspend fun deleteChallengeSessionsForSession(sessionId: String): Int {
        val count = challengeSessions.count { it.sessionId == sessionId }
        challengeSessions.removeAll { it.sessionId == sessionId }
        return count
    }
}

open class InMemoryBattleResultDao : BattleResultDao {
    val results = mutableMapOf<String, BattleResultEntity>()

    override suspend fun insertResult(result: BattleResultEntity) {
        results[result.resultId] = result
    }

    override suspend fun getResultById(resultId: String): BattleResultEntity? = results[resultId]

    override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? =
        results.values.find { it.sessionId == sessionId }

    override suspend fun getResultForBattle(userId: String, battleId: String): BattleResultEntity? =
        results.values.find { it.userId == userId && it.battleId == battleId }

    override suspend fun getAllResultsForUser(userId: String): List<BattleResultEntity> =
        results.values.filter { it.userId == userId }

    override fun observeAllResultsForUser(userId: String): Flow<List<BattleResultEntity>> =
        flowOf(results.values.filter { it.userId == userId })

    override suspend fun deleteResultById(resultId: String): Int =
        if (results.remove(resultId) != null) 1 else 0
}

open class InMemoryBattleHistoryDao : BattleHistoryDao {
    val historyItems = mutableListOf<BattleHistoryItemEntity>()

    override suspend fun insertHistoryItems(items: List<BattleHistoryItemEntity>) {
        historyItems.addAll(items)
    }

    override suspend fun getHistoryForUser(userId: String): List<BattleHistoryItemEntity> =
        historyItems.filter { it.userId == userId }

    override suspend fun clearHistoryForUser(userId: String): Int {
        val count = historyItems.count { it.userId == userId }
        historyItems.removeAll { it.userId == userId }
        return count
    }
}

open class InMemoryFriendDao : FriendDao {
    val friends = mutableMapOf<String, FriendEntity>()

    override suspend fun insertFriend(friend: FriendEntity) {
        friends[friend.friendshipId] = friend
    }

    override suspend fun insertFriends(friendsList: List<FriendEntity>) {
        friendsList.forEach { friends[it.friendshipId] = it }
    }

    override suspend fun getFriend(userId: String, friendUserId: String): FriendEntity? =
        friends.values.find {
            (it.userId == userId && it.friendUserId == friendUserId) ||
            (it.userId == friendUserId && it.friendUserId == userId)
        }

    override suspend fun getFriendById(friendshipId: String): FriendEntity? = friends[friendshipId]

    override suspend fun getFriendsForUser(userId: String): List<FriendEntity> =
        friends.values.filter { it.userId == userId || it.friendUserId == userId }

    override fun observeFriendsForUser(userId: String): Flow<List<FriendEntity>> =
        flowOf(friends.values.filter { it.userId == userId || it.friendUserId == userId })

    override suspend fun deleteFriendById(friendshipId: String): Int =
        if (friends.remove(friendshipId) != null) 1 else 0

    override suspend fun clearFriendsForUser(userId: String): Int {
        val count = friends.values.count { it.userId == userId }
        val toRemove = friends.filter { it.value.userId == userId }.keys
        toRemove.forEach { friends.remove(it) }
        return count
    }
}

open class InMemoryRivalDao : RivalDao {
    val rivals = mutableMapOf<String, RivalEntity>()

    override suspend fun insertRival(rival: RivalEntity) {
        rivals[rival.userId] = rival
    }

    override suspend fun getRivalForUser(userId: String): RivalEntity? = rivals[userId]

    override fun observeRivalForUser(userId: String): Flow<RivalEntity?> =
        flowOf(rivals[userId])

    override suspend fun deleteRivalForUser(userId: String): Int =
        if (rivals.remove(userId) != null) 1 else 0
}

open class InMemoryRivalMatchHistoryDao : RivalMatchHistoryDao {
    val history = mutableListOf<RivalMatchHistoryEntity>()

    override suspend fun insertMatchHistory(items: List<RivalMatchHistoryEntity>) {
        history.addAll(items)
    }

    override suspend fun getMatchHistoryForUser(userId: String): List<RivalMatchHistoryEntity> =
        history.filter { it.userId == userId }

    override suspend fun clearMatchHistoryForUser(userId: String): Int {
        val count = history.count { it.userId == userId }
        history.removeAll { it.userId == userId }
        return count
    }
}

open class InMemoryProfileDao : ProfileDao {
    val profiles = mutableMapOf<String, ProfileEntity>()

    override suspend fun insertProfile(profile: ProfileEntity) {
        profiles[profile.userId] = profile
    }

    override suspend fun getProfileForUser(userId: String): ProfileEntity? = profiles[userId]

    override fun observeProfileForUser(userId: String): Flow<ProfileEntity?> =
        flowOf(profiles[userId])

    override suspend fun deleteProfileForUser(userId: String): Int =
        if (profiles.remove(userId) != null) 1 else 0
}
