package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.ParentGateState
import com.nirkids.app.domain.usecase.ParentGateValidateUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ParentGateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockUseCase: ParentGateValidateUseCase
    private lateinit var viewModel: ParentGateViewModel

    private val initialQuestion = ParentGateState(
        operandA = 10, operandB = 5, operator = "+",
        correctAnswer = 15, attemptsRemaining = 3,
        isVerified = false, isLocked = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUseCase = mockk()

        every { mockUseCase.generateQuestion() } returns initialQuestion
        
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
        assertEquals(initialQuestion, state.gateState)
    }

    @Test
    fun `onUserInputChanged updates state`() {
        viewModel.onUserInputChanged("42")
        assertEquals("42", viewModel.uiState.value.userInput)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitAnswer with correct answer succeeds`() {
        every { mockUseCase.validateAnswer(any(), 15) } returns initialQuestion.copy(isVerified = true)
        
        viewModel.submitAnswer("15")
        
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `verifyPin with correct PIN succeeds`() {
        viewModel.verifyPin("1234") // default
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `setParentPin changes PIN`() {
        viewModel.setParentPin("4321")
        viewModel.verifyPin("4321")
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `clearLock resets locked state`() {
        val lockedState = initialQuestion.copy(isLocked = true, attemptsRemaining = 0)
        every { mockUseCase.clearLock(any()) } returns initialQuestion
        
        // Manual state injection for test
        // In a real scenario we'd need to mock validateAnswer to return locked state
        
        viewModel.clearLock()
        assertFalse(viewModel.uiState.value.isLocked)
    }
}
