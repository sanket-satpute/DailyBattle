package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.core.lifecycle.ProcessDeathRecoverer
import com.sanket_satpute_20.dailybattle.domain.battle.RecoverUnacknowledgedResultUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import kotlinx.coroutines.launch

/**
 * Owns the application's initial presentation state. No asynchronous dependency currently exists
 * to await, so the initial state resolves directly to [ScreenState.Success] once Hilt constructs
 * this view model.
 *
 * Dispatches background tasks such as recovering interrupted results and restoring active
 * battle state following process death on cold start (§16, §17).
 */
@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val recoverUnacknowledgedResultUseCase: RecoverUnacknowledgedResultUseCase,
    private val processDeathRecoverer: ProcessDeathRecoverer,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<Unit>>(ScreenState.Success(Unit))
    val state: StateFlow<ScreenState<Unit>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            recoverUnacknowledgedResultUseCase.execute()
            processDeathRecoverer.recoverBattleAfterProcessDeath(UserId("mock_user_id"))
        }
    }
}
