package com.sanket_satpute_20.dailybattle.data.remote.datasource

import com.sanket_satpute_20.dailybattle.data.remote.api.ProfileApiService
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResult
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.Profile
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

/**
 * Contract for remote Profile and History network operations (§55, §56).
 */
interface RemoteProfileDataSource {
    suspend fun getProfile(userId: UserId): DomainResult<Profile>
    suspend fun getBattleHistory(userId: UserId): DomainResult<List<BattleResult>>
    suspend fun getBattleHistoryItem(userId: UserId, battleId: BattleId): DomainResult<BattleResult>
}

/**
 * Implementation of [RemoteProfileDataSource] backed by [ProfileApiService].
 */
class RetrofitProfileDataSource @Inject constructor(
    private val apiService: ProfileApiService,
) : RemoteProfileDataSource {

    override suspend fun getProfile(userId: UserId): DomainResult<Profile> =
        SafeApiCall.execute {
            apiService.getProfile(userId.value).toDomain()
        }

    override suspend fun getBattleHistory(userId: UserId): DomainResult<List<BattleResult>> =
        SafeApiCall.execute {
            apiService.getBattleHistory(userId.value).history.map { it.toDomain() }
        }

    override suspend fun getBattleHistoryItem(
        userId: UserId,
        battleId: BattleId,
    ): DomainResult<BattleResult> =
        SafeApiCall.execute {
            apiService.getBattleHistoryItem(
                userId = userId.value,
                battleId = battleId.value,
            ).toDomain()
        }
}
