# UI and Integration Testing

This document covers testing that requires an Android environment, focusing on the real-world examples in `HomeScreenTest`, `AlphabetScreenTest`, `ParentGateScreenTest`, and `PronunciationScreenTest`.

## Stateless Composable Pattern
We test the `*Content` composables. This allows us to pass a pre-defined `UiState` and verify the UI reacts correctly.

### Example: Testing List Rendering and Interaction
From `AlphabetScreenTest.kt`:
```kotlin
@Test
fun alphabetScreen_displaysLetters() {
    val mockLetters = listOf(Alphabet('A', "/æ/", "Apple", "🍎", true))
    val mockState = AlphabetUiState(allLetters = mockLetters)

    composeTestRule.setContent {
        AlphabetScreenContent(
            uiState = mockState,
            onNavigateBack = {},
            // ... other lambdas
        )
    }

    // Verify 'A' is shown
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
}
```

### Example: Testing Navigation and Callbacks
From `HomeScreenTest.kt`:
```kotlin
@Test
fun homeScreen_navigatesToAlphabet() {
    var navigated = false
    composeTestRule.setContent {
        HomeScreenContent(
            uiState = HomeUiState(),
            onNavigateToAlphabet = { navigated = true },
            onNavigateToPronunciation = {},
            onNavigateToParentGate = {}
        )
    }

    composeTestRule.onNodeWithText("🔤 Learn Alphabets").performClick()
    assert(navigated)
}
```

### Example: Testing Conditional Visibility
From `ParentGateScreenTest.kt`:
```kotlin
@Test
fun parentGate_displaysSuccess() {
    val mockState = ParentGateUiState(isSuccess = true)

    composeTestRule.setContent {
        ParentGateScreenContent(
            uiState = mockState,
            // ... callbacks
        )
    }

    // Nodes that only appear on success
    composeTestRule.onNodeWithText("Verified!").assertIsDisplayed()
}
```

## Important constraints for `androidTest`
1.  **Method Names**: Avoid spaces in backticks. Use underscores (e.g., `fun test_my_feature()`).
2.  **MockK**: In `androidTest`, use `mockk<T>()` with caution. It's often better to use real state objects (like `HomeUiState`) rather than mocking the State class itself, as the state objects are simple data classes.
3.  **Synchronization**: The `createComposeRule()` (v2) uses `StandardTestDispatcher`. If your tests rely on immediate execution, use `composeTestRule.waitForIdle()` or `runOnIdle { }`.
