package com.nirkids.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_gate")
data class ParentGateEntity(
    @PrimaryKey val id: Int = 1,
    val operandA: Int = 0,
    val operandB: Int = 0,
    val operator: String = "+",
    val correctAnswer: Int = 0,
    val attemptsRemaining: Int = 3,
    val isVerified: Boolean = false,
    val isLocked: Boolean = false,
    val lockTimeMs: Long = 0
)
