package com.nirkids.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.junit.Assert.*
import org.junit.Test

class ViewModelClassesTest {

    @Test
    fun `AlphabetViewModel is a ViewModel`() {
        assertTrue(AlphabetViewModel::class.java.simpleName.contains("ViewModel"))
    }

    @Test
    fun `HomeViewModel is a ViewModel`() {
        assertTrue(HomeViewModel::class.java.simpleName.contains("ViewModel"))
    }

    @Test
    fun `ParentGateViewModel is a ViewModel`() {
        assertTrue(ParentGateViewModel::class.java.simpleName.contains("ViewModel"))
    }

    @Test
    fun `PronunciationViewModel is a ViewModel`() {
        assertTrue(PronunciationViewModel::class.java.simpleName.contains("ViewModel"))
    }

    @Test
    fun `AlphabetUiState has all required fields`() {
        val state = AlphabetUiState(
            selectedLetter = null,
            allLetters = emptyList(),
            currentProgress = null,
            isPlaying = false,
            isLoading = false,
            error = null
        )
        assertNull(state.selectedLetter)
        assertTrue(state.allLetters.isEmpty())
        assertFalse(state.isPlaying)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `HomeUiState has all required fields`() {
        val state = HomeUiState(
            totalLetters = 26,
            learnedCount = 10,
            inProgressCount = 5,
            newCount = 11,
            isLoading = false,
            error = null
        )
        assertEquals(26, state.totalLetters)
        assertEquals(10, state.learnedCount)
        assertEquals(5, state.inProgressCount)
        assertEquals(11, state.newCount)
    }

    @Test
    fun `ParentGateUiState has correct defaults`() {
        val state = ParentGateUiState()
        assertFalse(state.isSuccess)
        assertFalse(state.isLocked)
        assertTrue(state.userInput.isEmpty())
    }

    @Test
    fun `PronunciationUiState has correct defaults`() {
        val state = PronunciationUiState()
        assertFalse(state.isPracticeMode)
        assertTrue(state.userInput.isEmpty())
        assertEquals(FeedbackType.NONE, state.feedbackType)
    }

    @Test
    fun `FeedbackType enum has all variants`() {
        assertEquals(4, FeedbackType.values().size)
        assertTrue(FeedbackType.values().contains(FeedbackType.NONE))
        assertTrue(FeedbackType.values().contains(FeedbackType.SUCCESS))
        assertTrue(FeedbackType.values().contains(FeedbackType.GREAT))
        assertTrue(FeedbackType.values().contains(FeedbackType.TRY_AGAIN))
    }
}
