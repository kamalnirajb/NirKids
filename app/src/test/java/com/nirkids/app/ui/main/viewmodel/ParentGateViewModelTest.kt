package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.ParentGateState
import com.nirkids.app.domain.usecase.ParentGateValidateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ParentGateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockUseCase: ParentGateValidateUseCase
    private lateinit var viewModel: ParentGateViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUseCase = mock(ParentGateValidateUseCase::class.java)

        val mockQuestion = ParentGateState(
            operandA = 10, operandB = 5, operator = "+",
            correctAnswer = 15, attemptsRemaining = 3,
            isVerified = false, isLocked = false
        )
        `when`(mockUseCase.generateQuestion()).thenReturn(mockQuestion)
        `when`(mockUseCase.validateAnswer(any(), any())).thenAnswer {
            val state = it.arguments[0] as ParentGateState
            val answer = it.arguments[1] as Int
            if (answer == state.correctAnswer) {
                state.copy(isVerified = true)
            } else {
                state.copy(attemptsRemaining = state.attemptsRemaining - 1)
            }
        }
        `when`(mockUseCase.clearLock(any())).thenAnswer {
            val state = it.arguments[0] as ParentGateState
            state.copy(isLocked = false, attemptsRemaining = 3)
        }

        viewModel = ParentGateViewModel(mockUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init generates initial question`() {
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertFalse(state.isLocked)
        assertFalse(state.userInput.isBlank())
    }

    @Test
    fun `generateNewQuestion resets state`() = runTest(testDispatcher) {
        viewModel.generateNewQuestion()
        val state = viewModel.uiState.value
        assertEquals("", state.userInput)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
        verify(mockUseCase, atLeastOnce()).generateQuestion()
    }

    @Test
    fun `submitAnswer with correct answer succeeds`() = runTest(testDispatcher) {
        viewModel.submitAnswer("15")
        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `submitAnswer with wrong answer shows error`() = runTest(testDispatcher) {
        viewModel.submitAnswer("99")
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("left"))
    }

    @Test
    fun `submitAnswer with non-numeric shows error`() = runTest(testDispatcher) {
        viewModel.submitAnswer("abc")
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage!!.contains("number"))
    }

    @Test
    fun `clearLock resets locked state`() = runTest(testDispatcher) {
        val lockedState = ParentGateState(
            isLocked = true, attemptsRemaining = 0,
            lockTimeMs = System.currentTimeMillis()
        )
        `when`(mockUseCase.clearLock(lockedState)).thenReturn(lockedState.copy(isLocked = false, attemptsRemaining = 3))

        viewModel.uiState.value = viewModel.uiState.value.copy(
            gateState = lockedState,
            isLocked = true
        )
        viewModel.clearLock()

        val state = viewModel.uiState.value
        assertFalse(state.isLocked)
        assertNull(state.errorMessage)
    }

    @Test
    fun `clearError clears error message`() = runTest(testDispatcher) {
        viewModel.uiState.value = viewModel.uiState.value.copy(errorMessage = "test error")
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
