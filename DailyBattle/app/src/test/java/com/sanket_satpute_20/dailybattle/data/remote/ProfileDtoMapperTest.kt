package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileDtoMapperTest {

    @Test
    fun mapsProfileResponseDtoToDomainCorrectly() {
        val dto = ProfileResponseDto(
            userId = "usr-1",
            battleName = "TopScorer",
            momentum = 5,
            bestScore = 930,
            averageScore = 875,
            battleDNA = BattleDnaDto(
                speed = 295,
                memory = 280,
                people = 290,
            ),
            records = PersonalRecordsDto(
                bestScore = 930,
                bestMomentum = 10,
                battlesPlayed = 35,
            ),
        )

        val domain = dto.toDomain()

        assertEquals(UserId("usr-1"), domain.userId)
        assertEquals("TopScorer", domain.battleName)
        assertEquals(5, domain.momentum)
        assertEquals(930, domain.bestScore)
        assertEquals(875, domain.averageScore)
        assertEquals(295, domain.battleDNA.speed)
        assertEquals(280, domain.battleDNA.memory)
        assertEquals(290, domain.battleDNA.people)
        assertEquals(930, domain.records.bestScore)
        assertEquals(10, domain.records.bestMomentum)
        assertEquals(35, domain.records.battlesPlayed)
    }

    @Test
    fun mapsProfileResponseDtoWithNullDnaDimensionsSafely() {
        val dto = ProfileResponseDto(
            userId = "usr-2",
            battleName = "Beginner",
            momentum = 1,
            bestScore = 500,
            averageScore = 500,
            battleDNA = BattleDnaDto(
                speed = null,
                memory = null,
                people = null,
            ),
            records = PersonalRecordsDto(
                bestScore = 500,
                bestMomentum = 1,
                battlesPlayed = 1,
            ),
        )

        val domain = dto.toDomain()

        assertNull(domain.battleDNA.speed)
        assertNull(domain.battleDNA.memory)
        assertNull(domain.battleDNA.people)
        assertEquals(1, domain.records.battlesPlayed)
    }

    @Test
    fun preservesAuthoritativeServerMetricsWithoutClientRecalculation() {
        // Verifies the mapper faithfully mirrors the authoritative server values
        // rather than recalculating or clamping momentum, bestScore, or averages.
        val dto = ProfileResponseDto(
            userId = "usr-auth",
            battleName = "ServerTruth",
            momentum = 42,
            bestScore = 999,
            averageScore = 950,
            battleDNA = BattleDnaDto(speed = 300, memory = 300, people = 300),
            records = PersonalRecordsDto(bestScore = 999, bestMomentum = 42, battlesPlayed = 100),
        )

        val domain = dto.toDomain()

        assertEquals(42, domain.momentum)
        assertEquals(999, domain.bestScore)
        assertEquals(950, domain.averageScore)
        assertEquals(300, domain.battleDNA.speed)
    }
}
