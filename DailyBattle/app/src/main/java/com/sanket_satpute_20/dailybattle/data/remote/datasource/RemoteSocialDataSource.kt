package com.sanket_satpute_20.dailybattle.data.remote.datasource

import com.sanket_satpute_20.dailybattle.data.remote.api.SocialApiService
import com.sanket_satpute_20.dailybattle.data.remote.dto.social.AddFriendRequestDto
import com.sanket_satpute_20.dailybattle.data.remote.error.HttpErrorMapper
import com.sanket_satpute_20.dailybattle.data.remote.error.SafeApiCall
import com.sanket_satpute_20.dailybattle.data.remote.mapper.toDomain
import com.sanket_satpute_20.dailybattle.domain.friend.Friend
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.rival.Rival
import com.sanket_satpute_20.dailybattle.domain.rival.RivalScoreComparison
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Contract for remote Social network operations (§52–§54).
 */
interface RemoteSocialDataSource {
    suspend fun getFriends(userId: UserId): DomainResult<List<Friend>>
    suspend fun addFriendByBattleCode(
        userId: UserId,
        battleCode: String,
        clientRequestId: String,
    ): DomainResult<Friend>
    suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?>
    suspend fun getRivalComparison(
        userId: UserId,
        battleId: BattleId,
    ): DomainResult<RivalScoreComparison>
}

/**
 * Implementation of [RemoteSocialDataSource] backed by [SocialApiService].
 */
class RetrofitSocialDataSource @Inject constructor(
    private val apiService: SocialApiService,
) : RemoteSocialDataSource {

    override suspend fun getFriends(userId: UserId): DomainResult<List<Friend>> =
        SafeApiCall.execute {
            apiService.getFriends(userId.value).friends.map { it.toDomain() }
        }

    override suspend fun addFriendByBattleCode(
        userId: UserId,
        battleCode: String,
        clientRequestId: String,
    ): DomainResult<Friend> =
        SafeApiCall.execute {
            apiService.addFriend(
                AddFriendRequestDto(
                    userId = userId.value,
                    battleCode = battleCode,
                    clientRequestId = clientRequestId,
                )
            ).friendship.toDomain()
        }

    override suspend fun getCurrentRival(userId: UserId): DomainResult<Rival?> =
        try {
            val dto = apiService.getCurrentRival(userId.value)
            DomainResult.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 404) {
                DomainResult.Success(null)
            } else {
                DomainResult.Failure(HttpErrorMapper.mapHttpException(e))
            }
        } catch (e: Exception) {
            SafeApiCall.execute { throw e }
        }

    override suspend fun getRivalComparison(
        userId: UserId,
        battleId: BattleId,
    ): DomainResult<RivalScoreComparison> =
        SafeApiCall.execute {
            apiService.getRivalComparison(
                userId = userId.value,
                battleId = battleId.value,
            ).toDomain()
        }
}
