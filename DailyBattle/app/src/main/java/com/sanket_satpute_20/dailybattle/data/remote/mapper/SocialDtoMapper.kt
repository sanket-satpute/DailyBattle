package com.sanket_satpute_20.dailybattle.data.remote.mapper

import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection

/**
 * Maps remote transport DTOs to domain models for Social features.
 *
 * Adheres strictly to the contract:
 * - Does not invent new friend/rival status values or scoring conventions.
 * - Does not perform client-side rival selection.
 * - Preserves domain boundaries without coupling remote schemas to local persistence.
 */
fun FriendDto.toDomain(): Friend = Friend(
    friendshipId = FriendshipId(friendshipId),
    userId = UserId(userId),
    friendUserId = UserId(friendUserId),
    friendName = friendName,
    status = runCatching { FriendStatus.valueOf(status) }.getOrDefault(FriendStatus.PENDING),
    todayScore = todayScore,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun RivalResponseDto.toDomain(): Rival = Rival(
    userId = UserId(userId),
    rivalUserId = UserId(rivalUserId),
    rivalName = rivalName,
    selectedAt = selectedAt,
    status = runCatching { RivalStatus.valueOf(status) }.getOrDefault(RivalStatus.ACTIVE),
)

fun RivalScoreComparisonResponseDto.toDomain(): RivalScoreComparison = RivalScoreComparison(
    userScore = userScore,
    rivalScore = rivalScore,
    scoreGap = scoreGap,
    battleId = BattleId(battleId),
    direction = runCatching { ScoreDirection.valueOf(direction) }.getOrDefault(ScoreDirection.TIED),
)
