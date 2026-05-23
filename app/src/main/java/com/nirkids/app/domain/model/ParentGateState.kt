package com.nirkids.app.domain.model

data class ParentGateState(
    val operandA: Int = 0,
    val operandB: Int = 0,
    val operator: String = "+",
    val correctAnswer: Int = 0,
    val attemptsRemaining: Int = 3,
    val isVerified: Boolean = false,
    val isLocked: Boolean = false,
    val lockTimeMs: Long = 0
) {
    val displayQuestion: String
        get() = "$operandA $operator $operandB = ?"
}
