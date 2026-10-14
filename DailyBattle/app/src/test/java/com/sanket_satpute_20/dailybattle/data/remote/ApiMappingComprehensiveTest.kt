package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.data.remote.auth.AuthenticationRequiredException
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleResultResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.BattleSessionResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.ChallengeResultDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.battle.TodayBattleResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.FriendDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.RivalScoreComparisonResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.error.HttpErrorMapper
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.battle.BattleMode
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionStatus
import com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeType
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Comprehensive verification of API DTO to Domain mapping boundaries (Sprint 23.2).
 *
 * Verifies:
 * - DTO to Domain transformations across Battle, Profile, Social, and Error payloads.
 * - Missing/nullable field resilience and fallback status mappings.
 * - Server metric fidelity preservation (no unauthorized client recalculations).
 * - Safe API call execution and complete HTTP status code mapping matrix.
 * - Strict architectural decoupling between domain models and transport annotations.
 */
class ApiMappingComprehensiveTest {

    // =========================================================================
    // 1. Battle DTO to Domain Mappings
    // =========================================================================

    @Test
    fun mapsTodayBattleDtoPreservingOfficialSequenceAndMetadata() {
        val dto = TodayBattleResponseDto(
            battleId = "battle-2026-10-13",
            battleDate = 1700000000000L,
            status = "AVAILABLE",
            version = 1,
            challenges = listOf(
                ChallengeDto(challengeId = "ch-snap-1", type = "SNAP", order = 1, version = 1),
                ChallengeDto(challengeId = "ch-shift-1", type = "SHIFT", order = 2, version = 1),
                ChallengeDto(challengeId = "ch-crowd-1", type = "CROWD_CALL", order = 3, version = 1),
            ),
        )

        val domain = dto.toDomain()

        assertEquals("battle-2026-10-13", domain.battleId.value)
        assertEquals(1700000000000L, domain.battleDate)
        assertEquals(BattleStatus.AVAILABLE, domain.status)
        assertEquals(1, domain.version)
        assertEquals(3, domain.challenges.size)

        // Strict 3-challenge order verification (08_DATA_AND_API.md Â§9)
        assertEquals(ChallengeType.SNAP, domain.challenges[0].type)
        assertEquals(1, domain.challenges[0].order)
        assertEquals(ChallengeType.SHIFT, domain.challenges[1].type)
        assertEquals(2, domain.challenges[1].order)
        assertEquals(ChallengeType.CROWD_CALL, domain.challenges[2].type)
        assertEquals(3, domain.challenges[2].order)
    }

    @Test
    fun mapsBattleSessionDtoWithUserContext() {
        val dto = BattleSessionResponseDto(
            sessionId = "sess-alpha",
            battleId = "battle-daily",
            mode = "OFFICIAL",
            status = "IN_PROGRESS",
            currentChallenge = 2,
            startedAt = 1000L,
        )

        val domain = dto.toDomain(UserId("user-owner"))

        assertEquals("sess-alpha", domain.sessionId.value)
        assertEquals("user-owner", domain.userId.value)
        assertEquals("battle-daily", domain.battleId.value)
        assertEquals(BattleMode.OFFICIAL, domain.mode)
        assertEquals(BattleSessionStatus.IN_PROGRESS, domain.status)
        assertEquals(2, domain.currentChallenge)
        assertEquals(1000L, domain.startedAt)
        assertNull(domain.completedAt)
    }

    @Test
    fun mapsChallengeResultAndBattleResultFaithfully() {
        val chResultDto = ChallengeResultDto(
            resultId = "cr-10",
            challengeId = "c-10",
            sessionId = "s-10",
            score = 295,
            maxScore = 300,
            completedAt = 2500L,
        )
        val chDomain = chResultDto.toDomain()
        assertEquals("cr-10", chDomain.resultId.value)
        assertEquals(295, chDomain.score)
        assertEquals(300, chDomain.maxScore)

        val battleResultDto = BattleResultResponseDto(
            resultId = "br-100",
            userId = "user-100",
            battleId = "b-100",
            sessionId = "s-10",
            snapScore = 295,
            shiftScore = 285,
            crowdCallScore = 290,
            consistencyScore = 95,
            totalScore = 965,
            percentile = 98.5,
            completedAt = 5000L,
        )
        val battleDomain = battleResultDto.toDomain()
        assertEquals(965, battleDomain.totalScore)
        assertEquals(98.5, battleDomain.percentile, 0.001)
        assertEquals(95, battleDomain.consistencyScore)
    }

    // =========================================================================
    // 2. Profile DTO to Domain Mappings
    // =========================================================================

    @Test
    fun mapsProfileWithNullDnaRadarGracefully() {
        val dto = ProfileResponseDto(
            userId = "usr-novice",
            battleName = "NoviceOne",
            momentum = 0,
            bestScore = 0,
            averageScore = 0,
            battleDNA = BattleDnaDto(speed = null, memory = null, people = null),
            records = PersonalRecordsDto(bestScore = 0, bestMomentum = 0, battlesPlayed = 0),
        )

        val domain = dto.toDomain()

        assertEquals("usr-novice", domain.userId.value)
        assertEquals("NoviceOne", domain.battleName)
        assertNull(domain.battleDNA.speed)
        assertNull(domain.battleDNA.memory)
        assertNull(domain.battleDNA.people)
        assertEquals(0, domain.records.battlesPlayed)
    }

    @Test
    fun preservesAuthoritativeServerProfileCalculationsWithoutTampering() {
        val dto = ProfileResponseDto(
            userId = "usr-master",
            battleName = "Grandmaster",
            momentum = 99,
            bestScore = 1000,
            averageScore = 975,
            battleDNA = BattleDnaDto(speed = 300, memory = 300, people = 300),
            records = PersonalRecordsDto(bestScore = 1000, bestMomentum = 99, battlesPlayed = 200),
        )

        val domain = dto.toDomain()

        assertEquals(99, domain.momentum)
        assertEquals(1000, domain.bestScore)
        assertEquals(975, domain.averageScore)
        assertEquals(300, domain.battleDNA.speed)
        assertEquals(99, domain.records.bestMomentum)
    }

    // =========================================================================
    // 3. Social DTO to Domain Mappings
    // =========================================================================

    @Test
    fun mapsFriendDtoWithUnknownStatusToFallbackPending() {
        val dto = FriendDto(
            friendshipId = "fs-wild",
            userId = "u-me",
            friendUserId = "u-them",
            friendName = "MysteryUser",
            status = "UNKNOWN_SERVER_ENUM_STATE",
            todayScore = null,
            createdAt = 1000L,
            updatedAt = 1100L,
        )

        val domain = dto.toDomain()

        assertEquals("MysteryUser", domain.friendName)
        assertNull(domain.todayScore)
        // Must fall back safely to PENDING per DB-DATA-API resilience guidelines
        assertEquals(FriendStatus.PENDING, domain.status)
    }

    @Test
    fun mapsRivalResponseDtoWithFallbackActive() {
        val dto = RivalResponseDto(
            userId = "u-me",
            rivalUserId = "u-rival",
            rivalName = "Speedy",
            selectedAt = 2000L,
            status = "NEW_UNEXPECTED_RIVAL_STATE",
        )

        val domain = dto.toDomain()

        assertEquals("Speedy", domain.rivalName)
        assertEquals(RivalStatus.ACTIVE, domain.status)
    }

    @Test
    fun mapsRivalScoreComparisonComputingAbsoluteGapsAndDirections() {
        // Ahead
        val dtoAhead = RivalScoreComparisonResponseDto(
            userScore = 900,
            rivalScore = 800,
            scoreGap = 100,
            battleId = "b-comp-1",
            direction = "AHEAD",
        )
        val domAhead = dtoAhead.toDomain()
        assertEquals(ScoreDirection.AHEAD, domAhead.direction)
        assertEquals(100, domAhead.scoreGap)
        assertEquals("b-comp-1", domAhead.battleId.value)

        // Behind
        val dtoBehind = RivalScoreComparisonResponseDto(
            userScore = 750,
            rivalScore = 850,
            scoreGap = 100,
            battleId = "b-comp-2",
            direction = "BEHIND",
        )
        val domBehind = dtoBehind.toDomain()
        assertEquals(ScoreDirection.BEHIND, domBehind.direction)
        assertEquals(100, domBehind.scoreGap)

        // Tied
        val dtoTied = RivalScoreComparisonResponseDto(
            userScore = 900,
            rivalScore = 900,
            scoreGap = 0,
            battleId = "b-comp-3",
            direction = "TIED",
        )
        val domTied = dtoTied.toDomain()
        assertEquals(ScoreDirection.TIED, domTied.direction)
        assertEquals(0, domTied.scoreGap)
    }

    // =========================================================================
    // 4. HTTP Error Mapping Matrix
    // =========================================================================

    @Test
    fun httpErrorMapperExhaustiveStatusCodeMapping() {
        assertEquals(AppError.Authentication, HttpErrorMapper.mapStatusCode(401))
        assertEquals(AppError.Authorization, HttpErrorMapper.mapStatusCode(403))
        assertEquals(AppError.Validation, HttpErrorMapper.mapStatusCode(400))
        assertEquals(AppError.Validation, HttpErrorMapper.mapStatusCode(422))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(500))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(502))
        assertEquals(AppError.Server, HttpErrorMapper.mapStatusCode(503))
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(404))
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(409))
        assertEquals(AppError.Unknown, HttpErrorMapper.mapStatusCode(429))
    }

    @Test
    fun httpErrorMapperParsesStructuredErrorPayloadSafely() {
        val validJson = """{"code":"BATTLE_ALREADY_COMPLETED","message":"Session finalized","details":null,"requestId":"req-123"}"""
        val parsed = HttpErrorMapper.parseErrorResponse(validJson)
        assertNotNull(parsed)
        assertEquals("BATTLE_ALREADY_COMPLETED", parsed?.code)
        assertEquals("req-123", parsed?.requestId)

        // Null / blank / malformed json handling
        assertNull(HttpErrorMapper.parseErrorResponse(null))
        assertNull(HttpErrorMapper.parseErrorResponse(""))
        assertNull(HttpErrorMapper.parseErrorResponse("Not JSON at all"))
    }

    // =========================================================================
    // 5. SafeApiCall Exception Resilience
    // =========================================================================

    @Test
    fun safeApiCallRethrowsCancellationExceptionPromptly() = runBlocking {
        try {
            SafeApiCall.execute<Unit> {
                throw CancellationException("Job was cancelled")
            }
            fail("CancellationException must be rethrown promptly")
        } catch (e: CancellationException) {
            assertEquals("Job was cancelled", e.message)
        }
    }

    @Test
    fun safeApiCallMapsNetworkTimeoutsAndConnectionFailuresToAppErrors() = runBlocking {
        val timeoutResult = SafeApiCall.execute<Unit> { throw SocketTimeoutException("Read timed out") }
        assertTrue(timeoutResult is DomainResult.Failure)
        assertEquals(AppError.Timeout, (timeoutResult as DomainResult.Failure).error)

        val unreachResult = SafeApiCall.execute<Unit> { throw UnknownHostException("No DNS") }
        assertTrue(unreachResult is DomainResult.Failure)
        assertEquals(AppError.Network, (unreachResult as DomainResult.Failure).error)

        val connectResult = SafeApiCall.execute<Unit> { throw ConnectException("Refused") }
        assertTrue(connectResult is DomainResult.Failure)
        assertEquals(AppError.Network, (connectResult as DomainResult.Failure).error)

        val serialResult = SafeApiCall.execute<Unit> { throw SerializationException("Missing key") }
        assertTrue(serialResult is DomainResult.Failure)
        assertEquals(AppError.Validation, (serialResult as DomainResult.Failure).error)

        val authResult = SafeApiCall.execute<Unit> { throw AuthenticationRequiredException() }
        assertTrue(authResult is DomainResult.Failure)
        assertEquals(AppError.Authentication, (authResult as DomainResult.Failure).error)
    }

    // =========================================================================
    // 6. Architectural Boundary Decoupling Verification
    // =========================================================================

    @Test
    fun domainModelsAreDecoupledFromTransportFrameworks() {
        // Assert domain models have ZERO dependencies on Retrofit, OkHttp, or serialization annotations
        val domainClasses = listOf(
            BattleResult::class.java,
            BattleSession::class.java,
            Profile::class.java,
            Friend::class.java,
            Rival::class.java,
        )

        for (clazz in domainClasses) {
            for (annotation in clazz.annotations) {
                val name = annotation.annotationClass.qualifiedName ?: ""
                assertTrue(
                    "Domain model ${clazz.simpleName} must not have network annotation: $name",
                    !name.startsWith("retrofit2") &&
                        !name.startsWith("okhttp3") &&
                        !name.startsWith("kotlinx.serialization"),
                )
            }
        }
    }
}
