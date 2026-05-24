package com.nirkids.app.data.repository

import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.data.local.AlphabetDao
import com.nirkids.app.data.local.ProgressDao
import com.nirkids.app.data.model.AlphabetEntity
import com.nirkids.app.data.model.ProgressEntity
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AlphabetRepositoryImplTest {

    private lateinit var repository: AlphabetRepositoryImpl
    private lateinit var mockDatabase: AlphabetsDatabase
    private lateinit var mockAlphabetDao: AlphabetDao
    private lateinit var mockProgressDao: ProgressDao

    private val testAlphabets = listOf(
        AlphabetEntity("A", "/æ/", "Apple", "🍎", true),
        AlphabetEntity("B", "/b/", "Ball", "⚽", false),
        AlphabetEntity("C", "/k/", "Cat", "🐱", false)
    )

    private val testProgress = listOf(
        ProgressEntity("A", learned = true, attempts = 3, masteryLevel = 2),
        ProgressEntity("B", learned = false, attempts = 1, masteryLevel = 1),
        ProgressEntity("C", learned = false, attempts = 0, masteryLevel = 0)
    )

    @Before
    fun setup() {
        mockDatabase = mockk()
        mockAlphabetDao = mockk()
        mockProgressDao = mockk()

        every { mockDatabase.alphabetDao() } returns mockAlphabetDao
        every { mockDatabase.progressDao() } returns mockProgressDao

        every { mockAlphabetDao.getAllAlphabets() } returns flowOf(testAlphabets)
        every { mockProgressDao.getAllProgress() } returns flowOf(testProgress)

        repository = AlphabetRepositoryImpl(mockDatabase)
    }

    @Test
    fun `getAllAlphabets returns list of Alphabet domain models`() = runBlocking {
        val alphabets = repository.getAllAlphabets().first()

        assertEquals(3, alphabets.size)
        assertEquals('A', alphabets[0].letter)
        assertEquals('B', alphabets[1].letter)
        assertEquals('C', alphabets[2].letter)
        assertEquals("Apple", alphabets[0].exampleWord)
        assertTrue(alphabets[0].isVowel)
        assertFalse(alphabets[1].isVowel)
    }

    @Test
    fun `getAlphabetByLetter returns correct alphabet`() = runBlocking {
        coEvery { mockAlphabetDao.getAlphabetByLetter("B") } returns testAlphabets[1]

        val result = repository.getAlphabetByLetter('B')

        assertNotNull(result)
        assertEquals('B', result!!.letter)
        assertEquals("/b/", result.phonetic)
        assertEquals("Ball", result.exampleWord)
    }

    @Test
    fun `getAlphabetByLetter returns null for unknown letter`() = runBlocking {
        coEvery { mockAlphabetDao.getAlphabetByLetter("Z") } returns null

        val result = repository.getAlphabetByLetter('Z')

        assertNull(result)
    }

    @Test
    fun `getAllProgress returns list of LetterProgress domain models`() = runBlocking {
        val progresses = repository.getAllProgress().first()

        assertEquals(3, progresses.size)
        assertEquals('A', progresses[0].letter)
        assertTrue(progresses[0].learned)
        assertEquals(3, progresses[0].attempts)
        assertEquals(2, progresses[0].masteryLevel)
    }

    @Test
    fun `getProgressForLetter returns correct progress`() = runBlocking {
        coEvery { mockProgressDao.getProgressForLetter("B") } returns testProgress[1]

        val result = repository.getProgressForLetter('B')

        assertNotNull(result)
        assertEquals('B', result!!.letter)
        assertFalse(result.learned)
        assertEquals(1, result.attempts)
    }

    @Test
    fun `markLetterLearned updates progress`() = runBlocking {
        coEvery { mockAlphabetDao.getAlphabetByLetter("B") } returns testAlphabets[1]
        coEvery { mockProgressDao.updateProgress("B", true) } returns Unit

        repository.markLetterLearned('B')

        coVerify(exactly = 1) { mockProgressDao.updateProgress("B", true) }
    }

    @Test
    fun `incrementAttempts increases mastery level`() = runBlocking {
        coEvery { mockProgressDao.getProgressForLetter("B") } returns testProgress[1]
        coEvery { mockProgressDao.updateMasteryLevel("B", 2) } returns Unit

        repository.incrementAttempts('B')

        coVerify(exactly = 1) { mockProgressDao.updateMasteryLevel("B", 2) }
    }

    @Test
    fun `saveAllAlphabets saves all entities`() = runBlocking {
        coEvery { mockAlphabetDao.insertAllAlphabets(testAlphabets) } returns Unit

        repository.saveAllAlphabets(testAlphabets)

        coVerify(exactly = 1) { mockAlphabetDao.insertAllAlphabets(testAlphabets) }
    }

    @Test
    fun `seedInitialData inserts default alphabets when empty`() = runBlocking {
        every { mockAlphabetDao.getAllAlphabets() } returns flowOf(emptyList())
        coEvery { mockAlphabetDao.insertAllAlphabets(any()) } returns Unit
        coEvery { mockProgressDao.insertProgress(any()) } returns Unit

        repository.seedInitialData(mockDatabase)

        coVerify(exactly = 1) { mockAlphabetDao.insertAllAlphabets(any()) }
        coVerify(exactly = 26) { mockProgressDao.insertProgress(any()) }
    }
}
