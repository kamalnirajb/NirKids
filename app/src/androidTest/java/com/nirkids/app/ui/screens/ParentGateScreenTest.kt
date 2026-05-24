package com.nirkids.app.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.nirkids.app.domain.model.ParentGateState
import com.nirkids.app.ui.main.screens.ParentGateScreenContent
import com.nirkids.app.ui.main.viewmodel.ParentGateUiState
import org.junit.Rule
import org.junit.Test

class ParentGateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun parentGate_displaysMathPuzzle() {
        val mockGateState = ParentGateState(
            operandA = 10,
            operandB = 5,
            operator = "+",
            correctAnswer = 15
        )
        val mockState = ParentGateUiState(
            gateState = mockGateState
        )

        composeTestRule.setContent {
            ParentGateScreenContent(
                uiState = mockState,
                onDismiss = {},
                onGateSuccess = {},
                onUserInputChanged = {},
                onSubmitAnswer = {},
                onVerifyPin = {},
                onSetParentPin = {},
                onClearLock = {}
            )
        }

        composeTestRule.onNodeWithText("10 + 5 = ?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solve this to continue").assertIsDisplayed()
    }

    @Test
    fun parentGate_displaysSuccess() {
        val mockState = ParentGateUiState(
            isSuccess = true
        )

        composeTestRule.setContent {
            ParentGateScreenContent(
                uiState = mockState,
                onDismiss = {},
                onGateSuccess = {},
                onUserInputChanged = {},
                onSubmitAnswer = {},
                onVerifyPin = {},
                onSetParentPin = {},
                onClearLock = {}
            )
        }

        composeTestRule.onNodeWithText("Verified!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter Settings").assertIsDisplayed()
    }
}
