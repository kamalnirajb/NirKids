package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.ParentGateState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParentGateValidateUseCaseTest {

    private lateinit var useCase: ParentGateValidateUseCase

    @Before
    fun setup() {
        useCase = ParentGateValidateUseCase()
    }

    @Test
    fun `generateQuestion creates valid question`() {
        val question = useCase.generateQuestion()

        assertNotNull(question)
        assertTrue(question.operandA in 1..99)
        assertTrue(question.operandB in 1..99)
        assertTrue(question.operator in listOf("+", "-"))
        assertFalse(question.isVerified)
        assertFalse(question.isLocked)
        assertEquals(3, question.attemptsRemaining)
    }

    @Test
    fun `validateAnswer correct answer returns verified`() {
        val state = useCase.generateQuestion()
        val result = useCase.validateAnswer(state, state.correctAnswer)

        assertTrue(result.isVerified)
        assertEquals(3, result.attemptsRemaining)
    }

    @Test
    fun `validateAnswer last wrong attempt locks the gate`() {
        val initial = useCase.generateQuestion()
        val after1st = useCase.validateAnswer(initial, initial.correctAnswer + 100)
        val after2nd = useCase.validateAnswer(after1st, initial.correctAnswer + 200)

        assertEquals(1, after2nd.attemptsRemaining)
        assertFalse(after2nd.isLocked)

        val after3rd = useCase.validateAnswer(after2nd, initial.correctAnswer + 300)
        assertTrue(after3rd.isLocked)
        assertEquals(0, after3rd.attemptsRemaining)
    }

    @Test
    fun `canUnlock returns true after lock duration`() {
        val locked = ParentGateState(
            isLocked = true,
            attemptsRemaining = 0,
            lockTimeMs = System.currentTimeMillis() - 61_000
        )
        assertTrue(useCase.canUnlock(locked))
    }

    @Test
    fun `clearLock resets locked state`() {
        val locked = ParentGateState(
            isLocked = true,
            attemptsRemaining = 0,
            lockTimeMs = System.currentTimeMillis()
        )
        val unlocked = useCase.clearLock(locked)

        assertFalse(unlocked.isLocked)
        assertEquals(3, unlocked.attemptsRemaining)
    }
}
