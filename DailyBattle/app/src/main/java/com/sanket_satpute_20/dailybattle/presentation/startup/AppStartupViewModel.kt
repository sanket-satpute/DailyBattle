package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.lifecycle.ViewModel
import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Owns the application's initial presentation state. No asynchronous dependency currently exists
 * to await, so the initial state resolves directly to [ScreenState.Success] once Hilt constructs
 * this view model.
 */
@HiltViewModel
class AppStartupViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<Unit>>(ScreenState.Success(Unit))
    val state: StateFlow<ScreenState<Unit>> = _state.asStateFlow()
}
