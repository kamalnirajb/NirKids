package com.nirkids.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alphabet")
data class AlphabetEntity(
    @PrimaryKey val letter: String,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
)
