package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BattleNameState(
    val name: String = "",
    val isValid: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)

@HiltViewModel
class BattleNameViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BattleNameState())
    val state: StateFlow<BattleNameState> = _state.asStateFlow()

    fun onNameChanged(newName: String) {
        val validation = InputValidator.validateBattleName(newName)
        _state.update {
            it.copy(
                name = newName,
                isValid = validation is ValidationResult.Valid,
                isError = false,
                errorMessage = null
            )
        }
    }

    fun onSubmit() {
        val currentState = _state.value
        val validation = InputValidator.validateBattleName(currentState.name)
        
        if (validation is ValidationResult.Valid) {
            _state.update { it.copy(isSubmitting = true, isError = false, errorMessage = null) }
            viewModelScope.launch {
                userRepository.saveBattleName(currentState.name.trim())
                _state.update { it.copy(isSubmitting = false, isSuccess = true) }
            }
        } else if (validation is ValidationResult.Invalid) {
            _state.update {
                it.copy(
                    isError = true,
                    errorMessage = validation.reason
                )
            }
        }
    }
}
