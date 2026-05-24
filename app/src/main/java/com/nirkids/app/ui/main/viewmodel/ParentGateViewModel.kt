package com.nirkids.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirkids.app.domain.model.ParentGateState
import com.nirkids.app.domain.usecase.ParentGateValidateUseCase
import com.nirkids.tracevalidator.TraceValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentGateUiState(
    val gateState: ParentGateState = ParentGateState(),
    val userInput: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isLocked: Boolean = false
)

@HiltViewModel
class ParentGateViewModel @Inject constructor(
    private val parentGateUseCase: ParentGateValidateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentGateUiState(gateState = parentGateUseCase.generateQuestion()))
    val uiState: StateFlow<ParentGateUiState> = _uiState

    val gateState: ParentGateState get() = _uiState.value.gateState

    private var parentPin = "1234"

    fun onUserInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input, errorMessage = null)
    }

    fun generateNewQuestion() {
        val newState = parentGateUseCase.generateQuestion()
        _uiState.value = _uiState.value.copy(
            gateState = newState,
            userInput = "",
            errorMessage = null,
            isSuccess = false
        )
        TraceValidator.logEvent("parent_gate_question_generated", mapOf("question" to newState.displayQuestion))
    }

    fun verifyPin(pin: String) {
        if (pin == parentPin) {
            _uiState.value = _uiState.value.copy(
                isSuccess = true,
                errorMessage = null,
                userInput = ""
            )
            TraceValidator.logEvent("parent_gate_pin_verified", emptyMap())
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Incorrect PIN"
            )
            TraceValidator.logEvent("parent_gate_pin_wrong", emptyMap())
        }
    }

    fun setParentPin(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            parentPin = newPin
            TraceValidator.logEvent("parent_gate_pin_changed", emptyMap())
        }
    }

    fun submitAnswer(answer: String) {
        val trimmed = answer.trim()
        val answerInt = trimmed.toIntOrNull()
        if (answerInt == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a number",
                isSuccess = false
            )
            return
        }

        val newState = parentGateUseCase.validateAnswer(_uiState.value.gateState, answerInt)
        if (newState.isVerified) {
            _uiState.value = _uiState.value.copy(
                gateState = newState,
                isSuccess = true,
                errorMessage = null,
                userInput = ""
            )
            TraceValidator.logEvent("parent_gate_verified", mapOf("question" to _uiState.value.gateState.displayQuestion))
        } else if (newState.isLocked) {
            _uiState.value = _uiState.value.copy(
                gateState = newState,
                isLocked = true,
                errorMessage = "Too many wrong attempts! Wait 60 seconds.",
                isSuccess = false
            )
            TraceValidator.logEvent("parent_gate_locked", mapOf("attempts" to newState.attemptsRemaining.toString()))
        } else {
            _uiState.value = _uiState.value.copy(
                gateState = newState,
                userInput = "",
                errorMessage = "Not quite! You have ${newState.attemptsRemaining} attempts left."
            )
            TraceValidator.logEvent("parent_gate_wrong_attempt", mapOf("remaining" to newState.attemptsRemaining.toString()))
        }
    }

    fun clearLock() {
        val newState = parentGateUseCase.clearLock(_uiState.value.gateState)
        _uiState.value = _uiState.value.copy(
            gateState = newState,
            isLocked = false,
            errorMessage = null,
            userInput = ""
        )
        TraceValidator.logEvent("parent_gate_unlocked", emptyMap())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
