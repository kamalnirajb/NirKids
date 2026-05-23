package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var mockGetProgressUseCase: GetProgressUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mock(GetAlphabetUseCase::class.java)
        mockGetProgressUseCase = mock(GetProgressUseCase::class.java)

        `when`(mockGetAlphabetUseCase()).thenReturn(flowOf(
            listOf(
                Alphabet('A', '/æ/', "Apple", "🍎", true),
                Alphabet('B', '/b/', "Ball", "⚽", false),
                Alphabet('C', '/k/', "Cat", "🐱", false),
                Alphabet('D', '/d/', "Dog", "🐶", false),
                Alphabet('E', '/i/', "Egg", "🥚", true)
            )
        ))
        `when`(mockGetProgressUseCase()).thenReturn(flowOf(
            listOf(
                LetterProgress('A', learned = true, 3, 2),
                LetterProgress('B', learned = true, 2, 1),
                LetterProgress('C', learned = false, 1, 1),
                LetterProgress('D', learned = false, 0, 0),
                LetterProgress('E', learned = false, 0, 0)
            )
        ))

        viewModel = HomeViewModel(mockGetAlphabetUseCase, mockGetProgressUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads stats correctly`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertEquals(5, state.totalLetters)
        assertEquals(2, state.learnedCount)
        assertEquals(1, state.inProgressCount)
        assertEquals(2, state.newCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `refreshStats reloads data`() = runTest(testDispatcher) {
        viewModel.refreshStats()
        val state = viewModel.uiState.value
        assertEquals(5, state.totalLetters)
        verify(mockGetAlphabetUseCase, times(2)).invoke()
    }

    @Test
    fun `home stats with all learned`() = runTest(testDispatcher) {
        `when`(mockGetProgressUseCase()).thenReturn(flowOf(
            (65..70).map { LetterProgress(it.toChar(), learned = true, 5, 3) }
        ))

        viewModel.refreshStats()
        val state = viewModel.uiState.value
        assertEquals(state.learnedCount, state.totalLetters)
        assertEquals(0, state.newCount)
    }

    @Test
    fun `home stats with all new`() = runTest(testDispatcher) {
        `when`(mockGetProgressUseCase()).thenReturn(flowOf(
            (65..70).map { LetterProgress(it.toChar(), learned = false, 0, 0) }
        ))

        viewModel.refreshStats()
        val state = viewModel.uiState.value
        assertEquals(0, state.learnedCount)
        assertEquals(state.totalLetters, state.newCount)
    }
}
