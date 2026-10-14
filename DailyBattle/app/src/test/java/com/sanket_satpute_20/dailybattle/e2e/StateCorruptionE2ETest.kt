package com.sanket_satpute_20.dailybattle.e2e

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.InMemoryBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.battle.Battle
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEngine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleEvent
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleState
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStateMachine
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.Challenge
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSequence
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSession
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.battle.GetOrStartOfficialBattleUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.history.GetHistoryUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.scoring.BattleScoreAggregator
import com.sanket_satpute_20.dailybattle.domain.scoring.ChallengeScoreValidator
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleHistoryDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleResultDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryBattleSessionDao
import com.sanket_satpute_20.dailybattle.integration.fixtures.InMemoryFriendDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Sprint 23.8 — State Corruption Testing Suite
 *
 * Systematically injects and verifies all 8 state corruption vectors per `docs/ROADMAP.md` (lines 1814-1825),
 * `docs/09_OFFLINE_AND_ERROR_HANDLING.md` §41, §42, §43, and `docs/08_DATA_AND_API.md` §32:
 *
 * 1. Invalid local state
 * 2. Invalid API state
 * 3. Incomplete battle
 * 4. Missing challenge
 * 5. Duplicate result
 * 6. Invalid score
 * 7. Stale result
 * 8. Inconsistent friend state
 */
class StateCorruptionE2ETest {

    // =========================================================================
    // 1. INVALID LOCAL STATE
    // =========================================================================

    @Test
    fun `vector 1 - invalid local state - corrupted entity enums and negative indices sanitize to safe defaults`() {
        val corruptedEntity = BattleSessionEntity(
            sessionId = "sess-corrupted",
            userId = "user-1",
            battleId = "battle-1",
            mode = "NON_EXISTENT_MODE_CORRUPTED",
            status = "UNKNOWN_STATUS_CORRUPTED",
            currentChallenge = -99,
            startedAt = -1000L,
            completedAt = -2000L
        )

        val domain = corruptedEntity.toDomain()

        // Mode falls back safely to OFFICIAL
        assertEquals("Corrupted mode falls back to OFFICIAL", BattleMode.OFFICIAL, domain.mode)
        // Status falls back safely to NOT_STARTED
        assertEquals("Corrupted status falls back to NOT_STARTED", BattleSessionStatus.NOT_STARTED, domain.status)
        // Invalid challenge index (< 1 or > 3) sanitized to null
        assertNull("Negative challenge index is sanitized to null", domain.currentChallenge)
        // Negative timestamps are sanitized to null
        assertNull("Negative startedAt is sanitized to null", domain.startedAt)
        assertNull("Negative completedAt is sanitized to null", domain.completedAt)
    }

    @Test
    fun `vector 1 - invalid local state - illegal state transitions rejected without state drift`() {
        val stateMachine = BattleStateMachine(BattleState.NotStarted)

        // Inject illegal events that cannot be transitioned from NotStarted
        val invalidEvent1 = BattleEvent.FinishChallenge
        val result1 = stateMachine.transition(invalidEvent1)
        assertTrue("Illegal transition must return failure", result1 is DomainResult.Failure)
        assertEquals(AppError.BattleState, (result1 as DomainResult.Failure).error)
        assertEquals("State must remain NotStarted after failure", BattleState.NotStarted, stateMachine.currentState)

        val invalidEvent2 = BattleEvent.ShowResults
        val result2 = stateMachine.transition(invalidEvent2)
        assertTrue("Illegal transition must return failure", result2 is DomainResult.Failure)
        assertEquals("State must still remain NotStarted", BattleState.NotStarted, stateMachine.currentState)

        // Advance to Ready(1)
        val validStart = stateMachine.transition(BattleEvent.StartBattle)
        assertTrue(validStart is DomainResult.Success)
        assertEquals(BattleState.Ready(1), stateMachine.currentState)

        // From Ready(1), attempting Continue is illegal
        val illegalContinue = stateMachine.transition(BattleEvent.Continue)
        assertTrue(illegalContinue is DomainResult.Failure)
        assertEquals("State must remain Ready(1)", BattleState.Ready(1), stateMachine.currentState)
    }

    @Test
    fun `vector 1 - invalid local state - database persistence failure handled safely as AppError`() = runTest {
        val failingDao = object : InMemoryBattleResultDao() {
            override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? {
                throw IllegalStateException("SQLite database disk image is malformed")
            }
        }
        val historyDao = InMemoryBattleHistoryDao()
        val sessionDao = InMemoryBattleSessionDao()

        val repo = RoomBattleResultRepository(
            battleResultDao = failingDao,
            battleHistoryDao = historyDao,
            battleSessionDao = sessionDao
        )

        val result = repo.completeBattle(BattleSessionId("sess-1"), "req-1")
        assertTrue("Database corruption must produce Failure", result is DomainResult.Failure)
        assertEquals(AppError.Persistence, (result as DomainResult.Failure).error)
    }

    // =========================================================================
    // 2. INVALID API STATE
    // =========================================================================

    @Test
    fun `vector 2 - invalid API state - duplicate official attempt returns domain error`() {
        val sessionRepo = InMemoryBattleSessionRepository()
        val userId = UserId("user-test")
        val battleId = BattleId("battle-today")

        // Pre-seed an already COMPLETED official session (simulating remote or local already submitted)
        val completedSession = BattleSession(
            sessionId = BattleSessionId("sess-completed"),
            userId = userId,
            battleId = battleId,
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 2000L,
            challengeSessions = emptyList()
        )
        sessionRepo.saveSession(completedSession)

        val useCase = GetOrStartOfficialBattleUseCase(sessionRepo)
        val result = useCase.execute(userId, battleId)

        // Must reject duplicate attempt with AppError.Domain
        assertTrue("Duplicate attempt on completed official battle must fail", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
    }

    // =========================================================================
    // 3. INCOMPLETE BATTLE
    // =========================================================================

    @Test
    fun `vector 3 - incomplete battle - interrupted battle with missing challenges is not unacknowledged completed`() {
        val sessionRepo = InMemoryBattleSessionRepository()

        // Session where only Snap (challenge 1) is complete, Shift and Crowd Call are missing
        val incompleteSession = BattleSession(
            sessionId = BattleSessionId("sess-partial"),
            userId = UserId("user-1"),
            battleId = BattleId("battle-1"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.IN_PROGRESS,
            currentChallenge = 1,
            startedAt = 1000L,
            completedAt = null,
            challengeSessions = listOf(
                ChallengeSession(
                    challengeId = ChallengeId("snap-1"),
                    sessionId = BattleSessionId("sess-partial"),
                    status = ChallengeSessionStatus.COMPLETE,
                    startedAt = 1000L,
                    completedAt = 1050L
                )
            )
        )
        sessionRepo.saveSession(incompleteSession)

        val unacknowledged = sessionRepo.getUnacknowledgedSessions()
        assertTrue("Incomplete battle must NOT qualify as unacknowledged completed session", unacknowledged.isEmpty())
    }

    @Test
    fun `vector 3 - incomplete battle - score aggregator safely handles missing challenge results`() {
        val aggregator = BattleScoreAggregator()

        val snapResult = ChallengeResult(
            resultId = ChallengeResultId("cr-1"),
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("sess-partial"),
            score = 250,
            maxScore = 300,
            completedAt = 1000L
        )

        // Shift and Crowd Call are null (interrupted/abandoned battle)
        val aggregated = aggregator.aggregate(
            mode = BattleMode.OFFICIAL,
            snapResult = snapResult,
            shiftResult = null,
            crowdCallResult = null
        )

        assertEquals("Snap score present", 250, aggregated.snapScore)
        assertEquals("Missing shift treated as 0", 0, aggregated.shiftScore)
        assertEquals("Missing crowd call treated as 0", 0, aggregated.crowdCallScore)
        assertEquals("Consistency must be 0 when any challenge is missing", 0, aggregated.consistencyScore)
        assertEquals("Total score is sum of available scores", 250, aggregated.totalScore)
    }

    // =========================================================================
    // 4. MISSING CHALLENGE
    // =========================================================================

    @Test
    fun `vector 4 - missing challenge - battle definition missing required challenges rejected by sequence validator`() {
        // Battle with only 2 challenges (missing Crowd Call)
        val incompleteChallenges = listOf(
            Challenge(
                challengeId = ChallengeId("c-1"),
                battleId = BattleId("b-incomplete"),
                type = ChallengeType.SNAP,
                order = 1,
                version = 1
            ),
            Challenge(
                challengeId = ChallengeId("c-2"),
                battleId = BattleId("b-incomplete"),
                type = ChallengeType.SHIFT,
                order = 2,
                version = 1
            )
        )

        assertFalse(
            "Incomplete challenge sequence must be invalid",
            ChallengeSequence.isValidSequence(incompleteChallenges)
        )

        val incompleteBattle = Battle(
            battleId = BattleId("b-incomplete"),
            battleDate = 20261014L,
            status = BattleStatus.ACTIVE,
            version = 1,
            challenges = incompleteChallenges
        )

        val dummySession = BattleSession(
            sessionId = BattleSessionId("s-1"),
            userId = UserId("u-1"),
            battleId = BattleId("b-incomplete"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.NOT_STARTED,
            currentChallenge = null,
            startedAt = null,
            completedAt = null,
            challengeSessions = emptyList()
        )

        try {
            BattleEngine(dummySession, incompleteBattle)
            fail("BattleEngine must reject battle missing challenges with IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("official sequence") == true)
        }
    }

    @Test
    fun `vector 4 - missing challenge - index out of bounds throws IllegalArgumentException`() {
        try {
            ChallengeSequence.typeForIndex(0)
            fail("Index 0 must throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            ChallengeSequence.typeForIndex(4)
            fail("Index 4 must throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    // =========================================================================
    // 5. DUPLICATE RESULT
    // =========================================================================

    @Test
    fun `vector 5 - duplicate result - repository idempotently returns existing result without inserting duplicate`() = runTest {
        val existingEntity = BattleResultEntity(
            resultId = "res-existing-1",
            userId = "user-1",
            battleId = "battle-1",
            sessionId = "sess-1",
            snapScore = 250,
            shiftScore = 260,
            crowdCallScore = 270,
            consistencyScore = 80,
            totalScore = 860,
            percentile = 92.5,
            completedAt = 1000L
        )

        var insertCount = 0
        val seededDao = object : InMemoryBattleResultDao() {
            override suspend fun getResultBySessionId(sessionId: String): BattleResultEntity? {
                return if (sessionId == "sess-1") existingEntity else null
            }

            override suspend fun insertResult(result: BattleResultEntity) {
                insertCount++
                super.insertResult(result)
            }
        }
        val historyDao = InMemoryBattleHistoryDao()
        val sessionDao = InMemoryBattleSessionDao()

        val repo = RoomBattleResultRepository(
            battleResultDao = seededDao,
            battleHistoryDao = historyDao,
            battleSessionDao = sessionDao
        )

        val result = repo.completeBattle(BattleSessionId("sess-1"), "req-new-attempt")
        assertTrue("Must succeed with existing result", result is DomainResult.Success)
        val domain = (result as DomainResult.Success).value
        assertEquals("res-existing-1", domain.resultId.value)
        assertEquals(860, domain.totalScore)

        // Verify no duplicate insert was made
        assertEquals("Insert must not be called when result already exists", 0, insertCount)
    }

    @Test
    fun `vector 5 - duplicate result - multiple results on same day deduplicate in momentum calculation`() = runTest {
        val repo = InMemoryBattleResultRepository()
        val userId = UserId("user-test")
        val fixedClock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("UTC"))

        val todayMillis = Instant.parse("2026-10-14T08:00:00Z").toEpochMilli()
        val todayMillisLater = Instant.parse("2026-10-14T11:00:00Z").toEpochMilli()

        // Inject duplicate results on the same calendar day
        val result1 = BattleResult(
            resultId = BattleResultId("res-1"),
            userId = userId,
            battleId = BattleId("battle-today"),
            sessionId = BattleSessionId("sess-1"),
            snapScore = 200,
            shiftScore = 200,
            crowdCallScore = 200,
            consistencyScore = 50,
            totalScore = 650,
            percentile = 80.0,
            completedAt = todayMillis
        )
        val result2 = BattleResult(
            resultId = BattleResultId("res-2"),
            userId = userId,
            battleId = BattleId("battle-today-duplicate"),
            sessionId = BattleSessionId("sess-2"),
            snapScore = 250,
            shiftScore = 250,
            crowdCallScore = 250,
            consistencyScore = 70,
            totalScore = 820,
            percentile = 95.0,
            completedAt = todayMillisLater
        )

        repo.saveResult(result1)
        repo.saveResult(result2)

        val getHistory = GetHistoryUseCase(repo, fixedClock)
        val historyResult = getHistory(userId)

        assertTrue(historyResult is DomainResult.Success)
        val history = (historyResult as DomainResult.Success).value
        // Both battles occurred on 2026-10-14; momentum must count as 1 day, NOT 2 days
        assertEquals("Duplicate results on same calendar day count as 1 day momentum", 1, history.momentum)
        assertEquals("Best score picks the highest totalScore", 820, history.best)
    }

    // =========================================================================
    // 6. INVALID SCORE
    // =========================================================================

    @Test
    fun `vector 6 - invalid score - negative and excessive scores clamped by validator`() {
        val validator = ChallengeScoreValidator()

        // Negative score clamped to 0
        val negativeResult = ChallengeResult(
            resultId = ChallengeResultId("cr-neg"),
            challengeId = ChallengeId("c-1"),
            sessionId = BattleSessionId("s-1"),
            score = -500,
            maxScore = 300,
            completedAt = 1000L
        )
        val validatedNegative = validator.validate(negativeResult, ChallengeType.SNAP)
        assertEquals("Negative score clamped to 0", 0, validatedNegative.score)
        assertEquals(300, validatedNegative.maxScore)

        // Excessive score clamped to maxScore (300)
        val excessiveResult = ChallengeResult(
            resultId = ChallengeResultId("cr-excessive"),
            challengeId = ChallengeId("c-2"),
            sessionId = BattleSessionId("s-1"),
            score = 99999,
            maxScore = 500,
            completedAt = 1000L
        )
        val validatedExcessive = validator.validate(excessiveResult, ChallengeType.SHIFT)
        assertEquals("Excessive score clamped to 300", 300, validatedExcessive.score)
        assertEquals("Max score corrected to approved 300", 300, validatedExcessive.maxScore)
    }

    @Test
    fun `vector 6 - invalid score - corrupted battle result entity sanitizes negative scores and invalid percentiles`() {
        val corruptedEntity = BattleResultEntity(
            resultId = "res-corrupted",
            userId = "user-1",
            battleId = "battle-1",
            sessionId = "sess-1",
            snapScore = -100,
            shiftScore = -50,
            crowdCallScore = -200,
            consistencyScore = -80,
            totalScore = -430,
            percentile = Double.NaN,
            completedAt = -9999L
        )

        val domain = corruptedEntity.toDomain()

        assertEquals("Negative snapScore sanitized to 0", 0, domain.snapScore)
        assertEquals("Negative shiftScore sanitized to 0", 0, domain.shiftScore)
        assertEquals("Negative crowdCallScore sanitized to 0", 0, domain.crowdCallScore)
        assertEquals("Negative consistencyScore sanitized to 0", 0, domain.consistencyScore)
        assertEquals("Negative totalScore sanitized to 0", 0, domain.totalScore)
        assertEquals("NaN percentile sanitized to 0.0", 0.0, domain.percentile, 0.001)
        assertEquals("Negative completedAt sanitized to 0L", 0L, domain.completedAt)

        // Test infinite and out-of-range percentiles
        val infiniteEntity = corruptedEntity.copy(percentile = Double.POSITIVE_INFINITY)
        assertEquals(0.0, infiniteEntity.toDomain().percentile, 0.001)

        val outOfRangeHigh = corruptedEntity.copy(percentile = 150.0)
        assertEquals(100.0, outOfRangeHigh.toDomain().percentile, 0.001)

        val outOfRangeLow = corruptedEntity.copy(percentile = -25.0)
        assertEquals(0.0, outOfRangeLow.toDomain().percentile, 0.001)
    }

    // =========================================================================
    // 7. STALE RESULT
    // =========================================================================

    @Test
    fun `vector 7 - stale result - previous day result does not satisfy today's battle requirement`() = runTest {
        val sessionRepo = InMemoryBattleSessionRepository()
        val userId = UserId("user-1")

        // Yesterday was 2026-10-13, user completed battle-2026-10-13
        val yesterdaySession = BattleSession(
            sessionId = BattleSessionId("sess-yesterday"),
            userId = userId,
            battleId = BattleId("battle-2026-10-13"),
            mode = BattleMode.OFFICIAL,
            status = BattleSessionStatus.COMPLETED,
            currentChallenge = 3,
            startedAt = 1000L,
            completedAt = 2000L,
            challengeSessions = emptyList()
        )
        sessionRepo.saveSession(yesterdaySession)

        // Today is 2026-10-14, user requests today's battle-2026-10-14
        val useCase = GetOrStartOfficialBattleUseCase(sessionRepo)
        val todayResult = useCase.execute(userId, BattleId("battle-2026-10-14"))

        // Yesterday's completion must not block today's new battle
        assertTrue("Today's battle must be startable despite yesterday's completed battle", todayResult is DomainResult.Success)
        val engine = (todayResult as DomainResult.Success).value
        assertEquals("New session created for today", BattleSessionStatus.NOT_STARTED, engine.session.status)
        assertEquals("battle-2026-10-14", engine.session.battleId.value)
    }

    @Test
    fun `vector 7 - stale result - calendar gap breaks momentum streak`() = runTest {
        val repo = InMemoryBattleResultRepository()
        val userId = UserId("user-streak")
        val fixedClock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("UTC"))

        // Battle completed 2 days ago (2026-10-12), yesterday (2026-10-13) was skipped
        val twoDaysAgoMillis = Instant.parse("2026-10-12T10:00:00Z").toEpochMilli()
        repo.saveResult(
            BattleResult(
                resultId = BattleResultId("res-old"),
                userId = userId,
                battleId = BattleId("b-old"),
                sessionId = BattleSessionId("s-old"),
                snapScore = 200,
                shiftScore = 200,
                crowdCallScore = 200,
                consistencyScore = 50,
                totalScore = 650,
                percentile = 80.0,
                completedAt = twoDaysAgoMillis
            )
        )

        val getHistory = GetHistoryUseCase(repo, fixedClock)
        val history = (getHistory(userId) as DomainResult.Success).value

        // Yesterday was skipped, so momentum from 2 days ago is stale and broken
        assertEquals("Stale momentum broken by calendar gap is 0", 0, history.momentum)
    }

    // =========================================================================
    // 8. INCONSISTENT FRIEND STATE
    // =========================================================================

    @Test
    fun `vector 8 - inconsistent friend state - corrupted status enum and negative scores sanitized safely`() {
        val corruptedFriend = FriendEntity(
            friendshipId = "f-corrupt-1",
            userId = "user-me",
            friendUserId = "user-other",
            friendName = "Corrupt Friend",
            status = "TOTALLY_CORRUPT_STATUS_ENUM",
            todayScore = -999,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val domain = corruptedFriend.toDomain()

        assertEquals("Corrupted friend status defaults safely to PENDING", FriendStatus.PENDING, domain.status)
        assertEquals("Negative friend todayScore sanitized to 0", 0, domain.todayScore)
    }

    @Test
    fun `vector 8 - inconsistent friend state - duplicate friendship request rejected by repository`() = runTest {
        val existingEntity = FriendEntity(
            friendshipId = "f-existing",
            userId = "user-me",
            friendUserId = "user-friend",
            friendName = "Friend",
            status = "ACCEPTED",
            todayScore = 500,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val friendDao = InMemoryFriendDao().apply {
            insertFriend(existingEntity)
        }

        val repo = RoomFriendRepository(friendDao)
        val result = repo.sendFriendRequest(UserId("user-me"), UserId("user-friend"))

        // Rejects duplicate friendship creation to prevent state inconsistency
        assertTrue("Attempting to send request when friend already exists must fail", result is DomainResult.Failure)
        assertEquals(AppError.Domain, (result as DomainResult.Failure).error)
        assertEquals("No new friend row inserted", 1, friendDao.friends.size)
    }

    @Test
    fun `vector 8 - inconsistent friend state - corrupted rival match history outcome and scores sanitized`() {
        val corruptedRivalEntity = RivalMatchHistoryEntity(
            battleId = "b-rival-corrupt",
            userId = "user-me",
            date = -5000L,
            userScore = -300,
            rivalScore = -800,
            outcome = "UNRECOGNIZED_OUTCOME_STRING"
        )

        val domain = corruptedRivalEntity.toDomain()

        assertEquals("Corrupted outcome defaults safely to TIE", MatchOutcome.TIE, domain.outcome)
        assertEquals("Negative userScore sanitized to 0", 0, domain.userScore)
        assertEquals("Negative rivalScore sanitized to 0", 0, domain.rivalScore)
        assertEquals("Negative date sanitized to 0L", 0L, domain.date)
    }
}
