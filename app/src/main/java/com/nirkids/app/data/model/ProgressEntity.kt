package com.nirkids.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val letter: String,
    val learned: Boolean = false,
    val attempts: Int = 0,
    val masteryLevel: Int = 0
)
