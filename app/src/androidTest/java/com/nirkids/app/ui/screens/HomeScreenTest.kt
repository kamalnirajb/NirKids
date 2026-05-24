package com.nirkids.app.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.nirkids.app.ui.main.screens.HomeScreenContent
import com.nirkids.app.ui.main.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysWelcomeMessage() {
        val mockState = HomeUiState(
            isLoading = false,
            totalLetters = 26,
            learnedCount = 5,
            inProgressCount = 3,
            newCount = 18
        )

        composeTestRule.setContent {
            HomeScreenContent(
                uiState = mockState,
                onNavigateToAlphabet = {},
                onNavigateToPronunciation = {},
                onNavigateToParentGate = {}
            )
        }

        composeTestRule.onNodeWithText("🌈 Welcome to NirKids!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learned ✅").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun homeScreen_navigatesToAlphabet() {
        val mockState = HomeUiState()
        var navigated = false

        composeTestRule.setContent {
            HomeScreenContent(
                uiState = mockState,
                onNavigateToAlphabet = { navigated = true },
                onNavigateToPronunciation = {},
                onNavigateToParentGate = {}
            )
        }

        composeTestRule.onNodeWithText("🔤 Learn Alphabets").performClick()
        assert(navigated)
    }

    @Test
    fun homeScreen_displaysLoading() {
        val mockState = HomeUiState(isLoading = true)

        composeTestRule.setContent {
            HomeScreenContent(
                uiState = mockState,
                onNavigateToAlphabet = {},
                onNavigateToPronunciation = {},
                onNavigateToParentGate = {}
            )
        }

        // CircularProgressIndicator doesn't have text, but we can check if it's there
        // or check that other content is NOT there if it was conditional.
        // In our case, stats cards are hidden when loading.
        composeTestRule.onNodeWithText("Learned ✅").assertDoesNotExist()
    }
}
