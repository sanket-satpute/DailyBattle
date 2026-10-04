package com.sanket_satpute_20.dailybattle.presentation.snap

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel scaffolding for the Snap Challenge.
 *
 * NOTE: Engine injection and exact UI state management is BLOCKED
 * by DEC-GAME-001. We cannot legitimately start the engine or map
 * its state to the UI without knowing the gameplay rules.
 */
@HiltViewModel
class SnapViewModel @Inject constructor(
    // private val snapEngine: SnapEngine // Blocked until engine is implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow(SnapUiState())
    val uiState: StateFlow<SnapUiState> = _uiState.asStateFlow()

    fun onElementTapped(elementId: String) {
        // Handle tap through engine once implemented
        // snapEngine.handleInput(SnapEvent.Tap(elementId))
    }
}
