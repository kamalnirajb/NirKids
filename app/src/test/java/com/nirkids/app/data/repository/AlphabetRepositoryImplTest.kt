package com.nirkids.app.data.repository

import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.data.local.AlphabetDao
import com.nirkids.app.data.local.ProgressDao
import com.nirkids.app.data.model.AlphabetEntity
import com.nirkids.app.data.model.ProgressEntity
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.*

@RunWith(RobolectricTestRunner::class)
class AlphabetRepositoryImplTest {

    private lateinit var repository: AlphabetRepositoryImpl
    private lateinit var mockDatabase: AlphabetsDatabase
    private lateinit var mockAlphabetDao: AlphabetDao
    private lateinit var mockProgressDao: ProgressDao

    private val testAlphabets = listOf(
        AlphabetEntity('A', '/æ/', "Apple", "🍎", true),
        AlphabetEntity('B', '/b/', "Ball", "⚽", false),
        AlphabetEntity('C', '/k/', "Cat", "🐱", false)
    )

    private val testProgress = listOf(
        ProgressEntity('A', learned = true, attempts = 3, masteryLevel = 2),
        ProgressEntity('B', learned = false, attempts = 1, masteryLevel = 1),
        ProgressEntity('C', learned = false, attempts = 0, masteryLevel = 0)
    )

    @Before
    fun setup() {
        mockDatabase = mock(AlphabetsDatabase::class.java)
        mockAlphabetDao = mock(AlphabetDao::class.java)
        mockProgressDao = mock(ProgressDao::class.java)

        `when`(mockDatabase.alphabetDao()).thenReturn(mockAlphabetDao)
        `when`(mockDatabase.progressDao()).thenReturn(mockProgressDao)

        `when`(mockAlphabetDao.getAllAlphabets()).thenReturn(flowOf(testAlphabets))
        `when`(mockProgressDao.getAllProgress()).thenReturn(flowOf(testProgress))

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
        `when`(mockAlphabetDao.getAlphabetByLetter('B')).thenReturn(testAlphabets[1])

        val result = repository.getAlphabetByLetter('B')

        assertNotNull(result)
        assertEquals('B', result!!.letter)
        assertEquals("/b/", result.phonetic)
        assertEquals("Ball", result.exampleWord)
    }

    @Test
    fun `getAlphabetByLetter returns null for unknown letter`() = runBlocking {
        `when`(mockAlphabetDao.getAlphabetByLetter('Z')).thenReturn(null)

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
        `when`(mockProgressDao.getProgressForLetter('B')).thenReturn(testProgress[1])

        val result = repository.getProgressForLetter('B')

        assertNotNull(result)
        assertEquals('B', result!!.letter)
        assertFalse(result.learned)
        assertEquals(1, result.attempts)
    }

    @Test
    fun `markLetterLearned updates progress`() = runBlocking {
        `when`(mockAlphabetDao.getAlphabetByLetter('B')).thenReturn(testAlphabets[1])

        repository.markLetterLearned('B')

        verify(mockProgressDao, times(1)).updateProgress('B', true)
    }

    @Test
    fun `markLetterLearned does nothing for unknown letter`() = runBlocking {
        `when`(mockAlphabetDao.getAlphabetByLetter('Z')).thenReturn(null)

        repository.markLetterLearned('Z')

        verify(mockProgressDao, never()).updateProgress(any(), any())
    }

    @Test
    fun `incrementAttempts increases mastery level`() = runBlocking {
        `when`(mockProgressDao.getProgressForLetter('B')).thenReturn(testProgress[1])

        repository.incrementAttempts('B')

        verify(mockProgressDao, times(1)).updateMasteryLevel('B', 2)
    }

    @Test
    fun `incrementAttempts does nothing for unknown letter`() = runBlocking {
        `when`(mockProgressDao.getProgressForLetter('Z')).thenReturn(null)

        repository.incrementAttempts('Z')

        verify(mockProgressDao, never()).updateMasteryLevel(any(), any())
    }

    @Test
    fun `saveAllAlphabets saves all entities`() = runBlocking {
        repository.saveAllAlphabets(testAlphabets)

        verify(mockAlphabetDao, times(1)).insertAllAlphabets(testAlphabets)
    }

    @Test
    fun `seedInitialData inserts default alphabets when empty`() = runBlocking {
        `when`(mockAlphabetDao.getAllAlphabets()).thenReturn(flowOf(emptyList()))

        repository.seedInitialData(mockDatabase)

        verify(mockAlphabetDao, times(1)).insertAllAlphabets(anyList())
        verify(mockProgressDao, times(26)).insertProgress(any())
    }

    @Test
    fun `seedInitialData does not insert when data exists`() = runBlocking {
        `when`(mockAlphabetDao.getAllAlphabets()).thenReturn(flowOf(testAlphabets))

        repository.seedInitialData(mockDatabase)

        verify(mockAlphabetDao, never()).insertAllAlphabets(anyList())
        verify(mockProgressDao, never()).insertProgress(any())
    }

    @Test
    fun `Alphabet displayName contains letter and word`() = runBlocking {
        val alphabet = testAlphabets[0].toDomainModel()

        assertTrue(alphabet.displayName.contains("A"))
        assertTrue(alphabet.displayName.contains("Apple"))
    }

    @Test
    fun `LetterProgress masteryPercent calculated correctly`() {
        val progress = LetterProgress('A', learned = true, attempts = 3, masteryLevel = 2)
        assertEquals(20, progress.masteryPercent)
    }

    @Test
    fun `LetterProgress masteryPercent capped at 100`() {
        val progress = LetterProgress('A', learned = true, attempts = 3, masteryLevel = 10)
        assertEquals(100, progress.masteryPercent)
    }

    @Test
    fun `LetterProgress statusText returns mastered when mastery >= 3`() {
        val progress = LetterProgress('A', learned = true, attempts = 5, masteryLevel = 3)
        assertEquals("✅ Mastered", progress.statusText)
    }

    @Test
    fun `LetterProgress statusText returns new when mastery is 0`() {
        val progress = LetterProgress('A', learned = false, attempts = 0, masteryLevel = 0)
        assertEquals("🆕 New", progress.statusText)
    }
}
