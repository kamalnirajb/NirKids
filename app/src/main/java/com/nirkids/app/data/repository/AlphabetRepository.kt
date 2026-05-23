package com.nirkids.app.data.repository

import com.nirkids.app.data.local.AlphabetDao
import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.data.local.ProgressDao
import com.nirkids.app.data.model.AlphabetEntity
import com.nirkids.app.data.model.ProgressEntity
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AlphabetRepositoryImpl(
    private val database: AlphabetsDatabase
) : IAlphabetRepository {

    private val alphabetDao: AlphabetDao = database.alphabetDao()
    private val progressDao: ProgressDao = database.progressDao()

    override fun getAllAlphabets(): Flow<List<Alphabet>> {
        return alphabetDao.getAllAlphabets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAlphabetByLetter(letter: Char): Alphabet? {
        return alphabetDao.getAlphabetByLetter(letter.toString())?.toDomainModel()
    }

    override fun getAllProgress(): Flow<List<LetterProgress>> {
        return progressDao.getAllProgress().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getProgressForLetter(letter: Char): LetterProgress? {
        return progressDao.getProgressForLetter(letter.toString())?.toDomainModel()
    }

    override suspend fun markLetterLearned(letter: Char) {
        alphabetDao.getAlphabetByLetter(letter.toString())?.let {
            progressDao.updateProgress(letter.toString(), learned = true)
        }
    }

    override suspend fun incrementAttempts(letter: Char) {
        progressDao.getProgressForLetter(letter.toString())?.let {
            progressDao.updateMasteryLevel(letter.toString(), it.masteryLevel + 1)
        }
    }

    override suspend fun saveAllAlphabets(alphabets: List<AlphabetEntity>) {
        alphabetDao.insertAllAlphabets(alphabets)
    }

    override suspend fun seedInitialData(database: AlphabetsDatabase) {
        val existing = alphabetDao.getAllAlphabets().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val allAlphabets = generateDefaultAlphabets()
            alphabetDao.insertAllAlphabets(allAlphabets)

            for (alpha in allAlphabets) {
                progressDao.insertProgress(
                    ProgressEntity(
                        letter = alpha.letter,
                        learned = false,
                        attempts = 0,
                        masteryLevel = 0
                    )
                )
            }
        }
    }

    private fun generateDefaultAlphabets(): List<AlphabetEntity> {
        return listOf(
            AlphabetEntity("A", "/æ/", "Apple", "🍎", true),
            AlphabetEntity("B", "/b/", "Ball", "⚽", false),
            AlphabetEntity("C", "/k/", "Cat", "🐱", false),
            AlphabetEntity("D", "/d/", "Dog", "🐶", false),
            AlphabetEntity("E", "/i/", "Egg", "🥚", true),
            AlphabetEntity("F", "/f/", "Fish", "🐟", false),
            AlphabetEntity("G", "/g/", "Grapes", "🍇", false),
            AlphabetEntity("H", "/h/", "House", "🏠", false),
            AlphabetEntity("I", "/aɪ/", "Ice Cream", "🍦", true),
            AlphabetEntity("J", "/dʒ/", "Juice", "🧃", false),
            AlphabetEntity("K", "/k/", "Kite", "🪁", false),
            AlphabetEntity("L", "/l/", "Lion", "🦁", false),
            AlphabetEntity("M", "/m/", "Monkey", "🐵", false),
            AlphabetEntity("N", "/n/", "Nest", "🪺", false),
            AlphabetEntity("O", "/oʊ/", "Orange", "🍊", true),
            AlphabetEntity("P", "/p/", "Pen", "🖊️", false),
            AlphabetEntity("Q", "/kwi/", "Queen", "👸", false),
            AlphabetEntity("R", "/r/", "Robot", "🤖", false),
            AlphabetEntity("S", "/s/", "Sun", "☀️", false),
            AlphabetEntity("T", "/t/", "Tree", "🌳", false),
            AlphabetEntity("U", "/ju/", "Umbrella", "☂️", true),
            AlphabetEntity("V", "/v/", "Van", "🚐", false),
            AlphabetEntity("W", "/dʌblju/", "Whale", "🐋", false),
            AlphabetEntity("X", "/eks/", "Xylophone", "🎵", false),
            AlphabetEntity("Y", "/waɪ/", "Yarn", "🧶", false),
            AlphabetEntity("Z", "/zi/", "Zebra", "🦓", false)
        )
    }

    private fun AlphabetEntity.toDomainModel(): Alphabet {
        return Alphabet(
            letter = this.letter.firstOrNull() ?: ' ',
            phonetic = this.phonetic,
            exampleWord = this.exampleWord,
            imageUrl = this.imageUrl,
            isVowel = this.isVowel
        )
    }

    private fun ProgressEntity.toDomainModel(): LetterProgress {
        return LetterProgress(
            letter = this.letter.firstOrNull() ?: ' ',
            learned = this.learned,
            attempts = this.attempts,
            masteryLevel = this.masteryLevel
        )
    }
}
