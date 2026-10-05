package com.sanket_satpute_20.dailybattle.presentation.addfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import com.sanket_satpute_20.dailybattle.domain.friend.AddFriendByBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.friend.GetUserBattleCodeUseCase
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult
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
    private val addFriendByBattleCodeUseCase: AddFriendByBattleCodeUseCase,
    private val getUserBattleCodeUseCase: GetUserBattleCodeUseCase
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
        loadUserCode()
    }

    private fun loadUserCode() {
        viewModelScope.launch {
            val result = getUserBattleCodeUseCase.execute(UserId("current-user"))
            if (result is DomainResult.Success) {
                _userCode.value = result.value
            } else {
                _userCode.value = "ERROR"
            }
        }
    }

    fun onCodeChanged(code: String) {
        _enteredCode.value = code
        _submitState.value = AddFriendSubmitState.Idle
        
        if (code.isEmpty()) {
            _inputState.value = DBInputState.Default
            _feedbackMessage.value = null
        } else {
            val normalized = InputValidator.normalizeBattleCode(code)
            when (InputValidator.validateBattleCode(normalized)) {
                is ValidationResult.Valid -> {
                    _inputState.value = DBInputState.Valid
                    _feedbackMessage.value = null
                }
                is ValidationResult.Invalid -> {
                    // While typing, we just keep it default unless they attempt to submit
                    if (code.length == 6) {
                        _inputState.value = DBInputState.Invalid
                        _feedbackMessage.value = "Invalid characters in code."
                    } else {
                        _inputState.value = DBInputState.Default
                        _feedbackMessage.value = null
                    }
                }
            }
        }
    }

    fun submitCode() {
        val normalized = InputValidator.normalizeBattleCode(_enteredCode.value)
        val validation = InputValidator.validateBattleCode(normalized)
        
        if (validation is ValidationResult.Invalid) {
            _inputState.value = DBInputState.Invalid
            _feedbackMessage.value = validation.reason
            return
        }

        viewModelScope.launch {
            _submitState.value = AddFriendSubmitState.Loading
            
            // Sprint 13.6: full submission logic
            val userId = UserId("current-user")
            val result = addFriendByBattleCodeUseCase.execute(userId, normalized)
            
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
