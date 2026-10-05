package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import javax.inject.Inject

import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val battleResultRepository: BattleResultRepository
) {
    suspend fun execute(userId: UserId): DomainResult<Profile> {
        val profileResult = profileRepository.getProfile(userId)
        if (profileResult !is DomainResult.Success) {
            return profileResult
        }
        
        val profile = profileResult.value
        val resultsResponse = battleResultRepository.getUserBattleResults(userId)
        
        val dna = if (resultsResponse is DomainResult.Success && resultsResponse.value.isNotEmpty()) {
            val battles = resultsResponse.value
            val speed = battles.sumOf { it.snapScore } / battles.size
            val memory = battles.sumOf { it.shiftScore } / battles.size
            val people = battles.sumOf { it.crowdCallScore } / battles.size
            BattleDNA(speed = speed, memory = memory, people = people)
        } else {
            BattleDNA(speed = null, memory = null, people = null)
        }
        
        return DomainResult.Success(profile.copy(battleDNA = dna))
    }
}
