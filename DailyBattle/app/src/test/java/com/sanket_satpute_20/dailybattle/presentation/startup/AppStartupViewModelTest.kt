package com.sanket_satpute_20.dailybattle.presentation.startup

import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import com.sanket_satpute_20.dailybattle.domain.battle.*
import com.sanket_satpute_20.dailybattle.domain.identifier.*
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.core.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStartupViewModelTest {
    @Test
    fun `initial startup state is success once the view model is constructed`() {
        val fakeSessionRepository = object : BattleSessionRepository {
            override fun getOfficialSession(userId: UserId, battleId: BattleId): BattleSession? = null
            override fun saveSession(session: BattleSession) {}
            override fun getUnacknowledgedSessions(): List<BattleSession> = emptyList()
        }
        val fakeResultRepository = object : BattleResultRepository {
            override suspend fun completeBattle(sessionId: BattleSessionId, clientRequestId: String): DomainResult<BattleResult> {
                return DomainResult.Failure(AppError.Domain)
            }
            override suspend fun getBattleResult(userId: UserId, battleId: BattleId): DomainResult<BattleResult> {
                return DomainResult.Failure(AppError.Domain)
            }
            override suspend fun getUserBattleResults(userId: UserId): DomainResult<List<BattleResult>> {
                return DomainResult.Success(emptyList())
            }
        }
        val completeBattleUseCase = CompleteBattleUseCase(fakeResultRepository, fakeSessionRepository)
        val recoverUseCase = RecoverUnacknowledgedResultUseCase(fakeSessionRepository, completeBattleUseCase)
        val viewModel = AppStartupViewModel(recoverUseCase)

        assertEquals(ScreenState.Success(Unit), viewModel.state.value)
    }
}
