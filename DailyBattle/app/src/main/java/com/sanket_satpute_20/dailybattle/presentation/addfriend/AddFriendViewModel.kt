package com.sanket_satpute_20.dailybattle.presentation.addfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AddFriendSubmitState {
    data object Idle : AddFriendSubmitState
    data object Loading : AddFriendSubmitState
    data object Success : AddFriendSubmitState
    data class Error(val message: String) : AddFriendSubmitState
}

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val addFriendByBattleCodeUseCase: AddFriendByBattleCodeUseCase
) : ViewModel() {

    private val _userCode = MutableStateFlow<String?>(null)
    val userCode: StateFlow<String?> = _userCode.asStateFlow()

    private val _enteredCode = MutableStateFlow("")
    val enteredCode: StateFlow<String> = _enteredCode.asStateFlow()

    private val _inputState = MutableStateFlow(DBInputState.Default)
    val inputState: StateFlow<DBInputState> = _inputState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _submitState = MutableStateFlow<AddFriendSubmitState>(AddFriendSubmitState.Idle)
    val submitState: StateFlow<AddFriendSubmitState> = _submitState.asStateFlow()

    init {
        // Placeholder for user code. Will be implemented fully in Sprint 13.6
        _userCode.value = "K4X8M9"
    }

    fun onCodeChanged(code: String) {
        _enteredCode.value = code
        _submitState.value = AddFriendSubmitState.Idle
        
        // Basic length validation. Full logic belongs to Sprint 13.6
        if (code.isEmpty()) {
            _inputState.value = DBInputState.Default
            _feedbackMessage.value = null
        } else if (code.length == 6) {
            _inputState.value = DBInputState.Valid
            _feedbackMessage.value = null
        } else {
            _inputState.value = DBInputState.Default
            _feedbackMessage.value = null
        }
    }

    fun submitCode() {
        val code = _enteredCode.value
        if (code.length != 6) {
            _inputState.value = DBInputState.Invalid
            _feedbackMessage.value = "Code must be exactly 6 characters."
            return
        }

        viewModelScope.launch {
            _submitState.value = AddFriendSubmitState.Loading
            
            // Note: Sprint 13.6 focuses on full submission logic. For Sprint 13.5 UI scaffolding,
            // we will call the usecase we implemented in 13.3.
            val userId = UserId("current-user")
            val result = addFriendByBattleCodeUseCase.execute(userId, code)
            
            when (result) {
                is DomainResult.Success -> {
                    _submitState.value = AddFriendSubmitState.Success
                    _inputState.value = DBInputState.Default
                    _feedbackMessage.value = null
                    _enteredCode.value = "" // clear on success
                }
                is DomainResult.Failure -> {
                    _submitState.value = AddFriendSubmitState.Error("Failed to add friend. Check the code and try again.")
                    _inputState.value = DBInputState.Invalid
                    _feedbackMessage.value = "Invalid or expired Battle Code."
                }
            }
        }
    }

    fun acknowledgeSuccess() {
        _submitState.value = AddFriendSubmitState.Idle
    }
}
