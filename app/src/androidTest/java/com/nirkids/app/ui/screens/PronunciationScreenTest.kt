package com.nirkids.app.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.ui.main.screens.PronunciationScreenContent
import com.nirkids.app.ui.main.viewmodel.PronunciationUiState
import com.nirkids.app.ui.main.viewmodel.FeedbackType
import org.junit.Rule
import org.junit.Test

class PronunciationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pronunciationScreen_displaysLetterInfo() {
        val mockLetter = Alphabet('C', "/k/", "Cat", "🐱", false)
        val mockState = PronunciationUiState(
            currentLetter = mockLetter
        )

        composeTestRule.setContent {
            PronunciationScreenContent(
                uiState = mockState,
                onNavigateBack = {},
                onTogglePracticeMode = {},
                onLoadNextLetter = {},
                onCheckPronunciation = {},
                onClearFeedback = {},
                onSpeakLetter = { _, _ -> },
                onSpeakText = {}
            )
        }

        composeTestRule.onNodeWithText("C").assertIsDisplayed()
        composeTestRule.onNodeWithText("/k/").assertIsDisplayed()
        composeTestRule.onNodeWithText("as in Cat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Listen").assertIsDisplayed()
    }

    @Test
    fun pronunciationScreen_showsFeedback() {
        val mockLetter = Alphabet('A', "/æ/", "Apple", "🍎", true)
        val mockState = PronunciationUiState(
            currentLetter = mockLetter,
            feedbackMessage = "Great job!",
            feedbackType = FeedbackType.GREAT
        )

        composeTestRule.setContent {
            PronunciationScreenContent(
                uiState = mockState,
                onNavigateBack = {},
                onTogglePracticeMode = {},
                onLoadNextLetter = {},
                onCheckPronunciation = {},
                onClearFeedback = {},
                onSpeakLetter = { _, _ -> },
                onSpeakText = {}
            )
        }

        composeTestRule.onNodeWithText("Great job!").assertIsDisplayed()
    }
}
