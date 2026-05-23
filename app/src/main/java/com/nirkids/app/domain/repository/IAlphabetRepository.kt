package com.nirkids.app.domain.repository

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import kotlinx.coroutines.flow.Flow

interface IAlphabetRepository {
    fun getAllAlphabets(): Flow<List<Alphabet>>
    suspend fun getAlphabetByLetter(letter: Char): Alphabet?
    fun getAllProgress(): Flow<List<LetterProgress>>
    suspend fun getProgressForLetter(letter: Char): LetterProgress?
    suspend fun markLetterLearned(letter: Char)
    suspend fun incrementAttempts(letter: Char)
    suspend fun saveAllAlphabets(alphabets: List<com.nirkids.app.data.model.AlphabetEntity>)
    suspend fun seedInitialData(database: com.nirkids.app.data.local.AlphabetsDatabase)
}
