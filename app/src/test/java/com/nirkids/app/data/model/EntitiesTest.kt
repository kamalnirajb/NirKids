package com.nirkids.app.data.model

import org.junit.Assert.*
import org.junit.Test

class AlphabetEntityTest {

    @Test
    fun `AlphabetEntity stores all fields correctly`() {
        val entity = AlphabetEntity(
            letter = 'A',
            phonetic = "/æ/",
            exampleWord = "Apple",
            imageUrl = "🍎",
            isVowel = true
        )

        assertEquals('A', entity.letter)
        assertEquals("/æ/", entity.phonetic)
        assertEquals("Apple", entity.exampleWord)
        assertEquals("🍎", entity.imageUrl)
        assertTrue(entity.isVowel)
    }
}

class ProgressEntityTest {

    @Test
    fun `ProgressEntity defaults are correct`() {
        val entity = ProgressEntity('A')

        assertTrue(entity.learned)
        assertEquals(0, entity.attempts)
        assertEquals(0, entity.masteryLevel)
    }

    @Test
    fun `ProgressEntity stores custom values`() {
        val entity = ProgressEntity('B', learned = false, attempts = 5, masteryLevel = 3)

        assertFalse(entity.learned)
        assertEquals(5, entity.attempts)
        assertEquals(3, entity.masteryLevel)
    }
}

class ParentGateEntityTest {

    @Test
    fun `ParentGateEntity defaults are correct`() {
        val entity = ParentGateEntity()

        assertEquals(1, entity.id)
        assertEquals(0, entity.operandA)
        assertEquals(0, entity.operandB)
        assertEquals("+", entity.operator)
        assertEquals(0, entity.correctAnswer)
        assertEquals(3, entity.attemptsRemaining)
        assertFalse(entity.isVerified)
        assertFalse(entity.isLocked)
        assertEquals(0L, entity.lockTimeMs)
    }

    @Test
    fun `ParentGateEntity stores custom values`() {
        val entity = ParentGateEntity(
            id = 1,
            operandA = 10,
            operandB = 5,
            operator = "+",
            correctAnswer = 15,
            attemptsRemaining = 2,
            isVerified = true,
            isLocked = false,
            lockTimeMs = 1234567890L
        )

        assertEquals(10, entity.operandA)
        assertEquals(15, entity.correctAnswer)
        assertTrue(entity.isVerified)
    }
}
