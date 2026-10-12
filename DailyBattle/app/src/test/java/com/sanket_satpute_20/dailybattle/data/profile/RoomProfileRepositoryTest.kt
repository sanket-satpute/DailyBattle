package com.sanket_satpute_20.dailybattle.data.profile

import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomProfileRepositoryTest {

    private lateinit var fakeProfileDao: FakeProfileDao
    private lateinit var repository: RoomProfileRepository

    @Before
    fun setup() {
        fakeProfileDao = FakeProfileDao()
        repository = RoomProfileRepository(fakeProfileDao)
    }

    @Test
    fun getProfileReturnsFailureWhenNotSaved() = runTest {
        val result = repository.getProfile(UserId("unknown"))
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun saveProfileAndGetProfileRetrievesSavedEntity() = runTest {
        val profile = Profile(
            userId = UserId("user_1"),
            battleName = "Gladiator",
            momentum = 5,
            bestScore = 950,
            averageScore = 820,
            battleDNA = BattleDNA(speed = 90, memory = 85, people = 95),
            records = PersonalRecords(bestScore = 950, bestMomentum = 10, battlesPlayed = 30),
        )

        repository.saveProfile(profile)

        val result = repository.getProfile(UserId("user_1"))
        assertTrue(result is DomainResult.Success)
        val retrieved = (result as DomainResult.Success).value
        assertEquals("user_1", retrieved.userId.value)
        assertEquals("Gladiator", retrieved.battleName)
        assertEquals(5, retrieved.momentum)
        assertEquals(950, retrieved.bestScore)
        assertEquals(90, retrieved.battleDNA.speed)
        assertEquals(10, retrieved.records.bestMomentum)
    }

    private class FakeProfileDao : ProfileDao {
        val profiles = mutableMapOf<String, ProfileEntity>()

        override suspend fun insertProfile(profile: ProfileEntity) {
            profiles[profile.userId] = profile
        }

        override suspend fun getProfileForUser(userId: String): ProfileEntity? = profiles[userId]

        override fun observeProfileForUser(userId: String): Flow<ProfileEntity?> = flowOf(profiles[userId])

        override suspend fun deleteProfileForUser(userId: String): Int {
            return if (profiles.remove(userId) != null) 1 else 0
        }
    }
}
