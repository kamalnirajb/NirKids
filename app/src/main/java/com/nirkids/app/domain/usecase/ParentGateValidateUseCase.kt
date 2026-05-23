package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.ParentGateState
import kotlin.random.Random

class ParentGateValidateUseCase {
    data class GateConfig(
        val maxOperand: Int = 99,
        val minOperand: Int = 1,
        val attemptsBeforeLock: Int = 3,
        val lockDurationMs: Long = 60_000L
    )

    fun generateQuestion(config: GateConfig = GateConfig()): ParentGateState {
        val operators = listOf("+", "-")
        val operator = operators[Random.nextInt(operators.size)]
        val a = Random.nextInt(config.minOperand, config.maxOperand + 1)
        val b = Random.nextInt(config.minOperand, config.maxOperand + 1)

        val correctAnswer = when (operator) {
            "+" -> a + b
            "-" -> {
                val (larger, smaller) = if (a >= b) Pair(a, b) else Pair(b, a)
                larger - smaller
            }
            else -> 0
        }

        return ParentGateState(
            operandA = a,
            operandB = b,
            operator = operator,
            correctAnswer = correctAnswer,
            attemptsRemaining = config.attemptsBeforeLock,
            isVerified = false,
            isLocked = false
        )
    }

    fun validateAnswer(state: ParentGateState, userAnswer: Int): ParentGateState {
        if (state.isLocked) return state.copy(isLocked = true)

        val newState = if (userAnswer == state.correctAnswer) {
            state.copy(isVerified = true, attemptsRemaining = state.attemptsRemaining)
        } else {
            val newAttempts = state.attemptsRemaining - 1
            if (newAttempts <= 0) {
                state.copy(
                    attemptsRemaining = 0,
                    isLocked = true,
                    lockTimeMs = System.currentTimeMillis()
                )
            } else {
                state.copy(attemptsRemaining = newAttempts)
            }
        }
        return newState
    }

    fun canUnlock(state: ParentGateState): Boolean {
        if (!state.isLocked) return false
        return System.currentTimeMillis() - state.lockTimeMs >= 60_000L
    }

    fun clearLock(state: ParentGateState): ParentGateState {
        return state.copy(isLocked = false, attemptsRemaining = 3)
    }
}
