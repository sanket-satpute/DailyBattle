package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.user.AccountStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MalformedRecordSanitizationTest {

    @Test
    fun battleResultEntitySanitizesNegativeScoresAndInvalidPercentile() {
        val corrupted = BattleResultEntity(
            resultId = "res-corrupt",
            userId = "user-1",
            battleId = "b-1",
            sessionId = "s-1",
            snapScore = -100,
            shiftScore = -50,
            crowdCallScore = -25,
            consistencyScore = -10,
            totalScore = -185,
            percentile = Double.NaN,
            completedAt = -9999L,
        )

        val domain = corrupted.toDomain()
        assertEquals(0, domain.snapScore)
        assertEquals(0, domain.shiftScore)
        assertEquals(0, domain.crowdCallScore)
        assertEquals(0, domain.consistencyScore)
        assertEquals(0, domain.totalScore)
        assertEquals(0.0, domain.percentile, 0.001)
        assertEquals(0L, domain.completedAt)

        // Out-of-bounds percentile
        val highPercentile = corrupted.copy(percentile = 150.0).toDomain()
        assertEquals(100.0, highPercentile.percentile, 0.001)

        val lowPercentile = corrupted.copy(percentile = -20.0).toDomain()
        assertEquals(0.0, lowPercentile.percentile, 0.001)
    }

    @Test
    fun profileEntitySanitizesNegativeScoresAndOutOfBoundsDna() {
        val corrupted = ProfileEntity(
            userId = "u-corrupt",
            battleName = "CorruptUser",
            momentum = -5,
            bestScore = -200,
            averageScore = -30,
            speedDna = 180,
            memoryDna = -30,
            peopleDna = 270,
            bestMomentumRecord = -10,
            battlesPlayedRecord = -1,
            updatedAt = 0L,
        )

        val domain = corrupted.toDomain()
        assertEquals(0, domain.momentum)
        assertEquals(0, domain.bestScore)
        assertEquals(0, domain.averageScore)
        assertEquals(180, domain.battleDNA.speed)
        assertEquals(0, domain.battleDNA.memory)
        assertEquals(270, domain.battleDNA.people)
        assertEquals(0, domain.records.bestScore)
        assertEquals(0, domain.records.bestMomentum)
        assertEquals(0, domain.records.battlesPlayed)
    }

    @Test
    fun battleSessionEntitySanitizesInvalidChallengeIndexAndCorruptedEnums() {
        val corrupted = BattleSessionEntity(
            sessionId = "s-corrupt",
            userId = "u-1",
            battleId = "b-1",
            mode = "NON_EXISTENT_MODE",
            status = "UNKNOWN_STATUS",
            currentChallenge = 99, // invalid index
            startedAt = -1L,
            completedAt = -1L,
        )

        val domain = corrupted.toDomain()
        assertEquals(BattleMode.OFFICIAL, domain.mode)
        assertEquals(BattleSessionStatus.NOT_STARTED, domain.status)
        assertNull("Invalid currentChallenge must be sanitized to null", domain.currentChallenge)
        assertNull("Negative startedAt must be sanitized to null", domain.startedAt)
        assertNull("Negative completedAt must be sanitized to null", domain.completedAt)
    }

    @Test
    fun friendEntitySanitizesNegativeScoreAndInvalidStatus() {
        val corrupted = FriendEntity(
            friendshipId = "f-1",
            userId = "u-1",
            friendUserId = "u-2",
            friendName = "Bob",
            status = "CORRUPTED_STATUS",
            todayScore = -50,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val domain = corrupted.toDomain()
        assertEquals(FriendStatus.PENDING, domain.status)
        assertEquals(0, domain.todayScore)
    }

    @Test
    fun rivalMatchHistorySanitizesNegativeScoresAndCorruptOutcome() {
        val corrupted = RivalMatchHistoryEntity(
            battleId = "b-1",
            userId = "u-1",
            date = -10L,
            userScore = -50,
            rivalScore = -100,
            outcome = "CORRUPT_OUTCOME",
        )

        val domain = corrupted.toDomain()
        assertEquals(MatchOutcome.TIE, domain.outcome)
        assertEquals(0, domain.userScore)
        assertEquals(0, domain.rivalScore)
        assertEquals(0L, domain.date)
    }

    @Test
    fun battleHistoryItemSanitizesNegativeScoresAndInvalidPercentile() {
        val corrupted = BattleHistoryItemEntity(
            battleId = "b-1",
            userId = "u-1",
            date = -5L,
            score = -300,
            percentile = Double.NaN,
            personalBest = false,
            status = "COMPLETED",
        )

        val domain = corrupted.toBattleResult(snapScore = -10, shiftScore = -20)
        assertEquals(0, domain.totalScore)
        assertEquals(0, domain.snapScore)
        assertEquals(0, domain.shiftScore)
        assertEquals(0.0, domain.percentile, 0.001)
        assertEquals(0L, domain.completedAt)
    }

    @Test
    fun challengeEntitiesSanitizeNegativeScoresAndCorruptTypes() {
        val corruptedResult = ChallengeResultEntity(
            resultId = "cr-1",
            challengeId = "c-1",
            sessionId = "s-1",
            score = -10,
            maxScore = -100,
            completedAt = -1L,
            metadataJson = null,
        ).toDomain()

        assertEquals(0, corruptedResult.score)
        assertEquals(0, corruptedResult.maxScore)
        assertEquals(0L, corruptedResult.completedAt)

        val corruptedChallenge = ChallengeEntity(
            challengeId = "c-1",
            battleId = "b-1",
            type = "CORRUPTED_TYPE",
            order = -5,
            version = 1,
        ).toDomain()

        assertEquals(ChallengeType.SNAP, corruptedChallenge.type)
        assertEquals(1, corruptedChallenge.order)
    }
}
