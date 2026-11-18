package com.sanket_satpute_20.dailybattle.presentation.snap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.dailybattle.domain.battle.ChallengeResult
import com.sanket_satpute_20.dailybattle.domain.battle.RecordChallengeResultUseCase
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel scaffolding for the Snap Challenge.
 *
 * NOTE: Full engine injection and gameplay loop is pending DEC-GAME-001 implementation.
 *
 * SPRINT-CORE-PERSISTENCE Integration Boundary:
 * When the Snap gameplay engine concludes its rounds:
 * 1. [com.sanket_satpute_20.dailybattle.domain.snap.SnapEvaluator] produces the evaluated [ChallengeResult].
 * 2. This ViewModel invokes [RecordChallengeResultUseCase] to safely write the result to Room
 *    under the deterministic key `"${sessionId}_${challengeId}"`.
 * 3. Lifecycle state progression is then signaled via `ChallengeEvent.Finish` in BattleEngine.
 *    (Note: `ChallengeEvent.Finish` only manages lifecycle progression and does not transport scores).
 * 4. The screen then automatically navigates to Shift after the 700 ms hold.
 */
@HiltViewModel
class SnapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SnapUiState())
    val uiState: StateFlow<SnapUiState> = _uiState.asStateFlow()

    fun onElementTapped(elementId: String) {
        // Handle tap through engine once implemented in gameplay sprint
        // snapEngine.handleInput(SnapEvent.Tap(elementId))
    }

    /**
     * Persists the evaluated [ChallengeResult] produced by SnapEvaluator before navigation.
     *
     * @param result The evaluated Snap result.
     * @param useCase The persistence use case.
     * @return [DomainResult.Success] on successful database write, or [DomainResult.Failure].
     */
    suspend fun recordSnapResult(
        result: ChallengeResult,
        useCase: RecordChallengeResultUseCase,
    ): DomainResult<ChallengeResult> {
        return useCase.execute(result)
    }
}
