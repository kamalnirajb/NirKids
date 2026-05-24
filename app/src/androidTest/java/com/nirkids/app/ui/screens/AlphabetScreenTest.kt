package com.nirkids.app.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.ui.main.screens.AlphabetScreenContent
import com.nirkids.app.ui.main.viewmodel.AlphabetUiState
import org.junit.Rule
import org.junit.Test

class AlphabetScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alphabetScreen_displaysLetters() {
        val mockLetters = listOf(
            Alphabet('A', "/æ/", "Apple", "🍎", true),
            Alphabet('B', "/b/", "Ball", "⚽", false)
        )
        val mockState = AlphabetUiState(
            isLoading = false,
            allLetters = mockLetters
        )

        composeTestRule.setContent {
            AlphabetScreenContent(
                uiState = mockState,
                onNavigateBack = {},
                onPlayPronunciation = {},
                onMarkLearned = {},
                onLetterTapped = {}
            )
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertIsDisplayed()
    }

    @Test
    fun alphabetScreen_showsDetailOnTap() {
        val mockLetters = listOf(
            Alphabet('A', "/æ/", "Apple", "🍎", true)
        )
        val mockState = AlphabetUiState(
            allLetters = mockLetters
        )

        composeTestRule.setContent {
            AlphabetScreenContent(
                uiState = mockState,
                onNavigateBack = {},
                onPlayPronunciation = {},
                onMarkLearned = {},
                onLetterTapped = {}
            )
        }

        composeTestRule.onNodeWithText("A").performClick()
        
        // After clicking A, the detail card should show "as in Apple"
        composeTestRule.onNodeWithText("as in Apple").assertIsDisplayed()
    }
}
