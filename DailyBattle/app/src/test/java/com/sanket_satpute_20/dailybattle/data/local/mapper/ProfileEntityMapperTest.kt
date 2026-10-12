package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileEntityMapperTest {

    @Test
    fun `toDomain maps all ProfileEntity fields accurately with non-null Battle DNA`() {
        val entity = ProfileEntity(
            userId = "user_me",
            battleName = "ShadowStriker",
            momentum = 5,
            bestScore = 945,
            averageScore = 872,
            speedDna = 280,
            memoryDna = 265,
            peopleDna = 270,
            bestMomentumRecord = 12,
            battlesPlayedRecord = 30,
            updatedAt = 1700000000000L,
        )

        val domain = entity.toDomain()

        assertEquals(UserId("user_me"), domain.userId)
        assertEquals("ShadowStriker", domain.battleName)
        assertEquals(5, domain.momentum)
        assertEquals(945, domain.bestScore)
        assertEquals(872, domain.averageScore)
        assertEquals(280, domain.battleDNA.speed)
        assertEquals(265, domain.battleDNA.memory)
        assertEquals(270, domain.battleDNA.people)
        assertEquals(945, domain.records.bestScore)
        assertEquals(12, domain.records.bestMomentum)
        assertEquals(30, domain.records.battlesPlayed)
    }

    @Test
    fun `toDomain handles null Battle DNA dimensions accurately when unplayed`() {
        val entity = ProfileEntity(
            userId = "user_new",
            battleName = "NewPlayer",
            momentum = 0,
            bestScore = 0,
            averageScore = 0,
            speedDna = null,
            memoryDna = null,
            peopleDna = null,
            bestMomentumRecord = 0,
            battlesPlayedRecord = 0,
            updatedAt = 1700000000000L,
        )

        val domain = entity.toDomain()

        assertNull(domain.battleDNA.speed)
        assertNull(domain.battleDNA.memory)
        assertNull(domain.battleDNA.people)
        assertEquals(0, domain.records.battlesPlayed)
    }

    @Test
    fun `toEntity maps all Profile domain fields accurately`() {
        val domain = Profile(
            userId = UserId("user_pro"),
            battleName = "ProPlayer",
            momentum = 7,
            bestScore = 980,
            averageScore = 910,
            battleDNA = BattleDNA(speed = 295, memory = 285, people = 290),
            records = PersonalRecords(bestScore = 980, bestMomentum = 14, battlesPlayed = 42),
        )

        val entity = domain.toEntity(updatedAt = 1700000500000L)

        assertEquals("user_pro", entity.userId)
        assertEquals("ProPlayer", entity.battleName)
        assertEquals(7, entity.momentum)
        assertEquals(980, entity.bestScore)
        assertEquals(910, entity.averageScore)
        assertEquals(295, entity.speedDna)
        assertEquals(285, entity.memoryDna)
        assertEquals(290, entity.peopleDna)
        assertEquals(14, entity.bestMomentumRecord)
        assertEquals(42, entity.battlesPlayedRecord)
        assertEquals(1700000500000L, entity.updatedAt)
    }

    @Test
    fun `roundtrip preserves Profile data`() {
        val original = Profile(
            userId = UserId("user_rt"),
            battleName = "RoundtripUser",
            momentum = 3,
            bestScore = 890,
            averageScore = 820,
            battleDNA = BattleDNA(speed = 270, memory = 260, people = 250),
            records = PersonalRecords(bestScore = 890, bestMomentum = 5, battlesPlayed = 10),
        )

        val roundtrip = original.toEntity(12345L).toDomain()

        assertEquals(original, roundtrip)
    }
}
