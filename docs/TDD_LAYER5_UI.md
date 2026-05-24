# TDD Layer 5 — Testing UI with Jetpack Compose

**Difficulty:** Advanced | **Time:** ~120 min | **Prerequisites:** Layers 1-4 complete

UI tests verify that your screens render correctly and respond to user interactions. Compose UI testing is different from previous layers — you interact with the UI as a user would (taps, scrolls) and verify what the user sees.

## Two Types of UI Testing

| Type | What it tests | Where it runs |
|---|---|---|
| **Previews** | Static rendering of composables | Android Studio Preview / `test` |
| **Composable tests** | User interactions + state changes | Device or emulator (`androidTest`) |

## TDD Practice 1: Compose Previews (Easy Start)

Composable previews let you render a Composable in Android Studio without running the app. Add to any Composable:

```kotlin
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    NirKidsTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                totalLetters = 26,
                learnedCount = 5,
                inProgressCount = 10,
                newCount = 11
            ),
            onNavigateToAlphabet = {},
            onNavigateToPronunciation = {},
            onNavigateToParentGate = {}
        )
    }
}
```

**Why this is useful:** It's the fastest way to verify UI changes without running a test.

## TDD Practice 2: Compose UI Test (Real Interaction Test)

Create: `app/src/androidTest/java/com/nirkids/app/ui/screens/HomeScreenTest.kt`

### The Test

```kotlin
package com.nirkids.app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.junit.Assert.assertTrue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `home screen displays welcome text`() {
        // Arrange
        composeTestRule.setContent {
            NirKidsTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        totalLetters = 26,
                        learnedCount = 5,
                        inProgressCount = 10,
                        newCount = 11,
                        allLetters = ('A'..'Z').map { letter ->
                            com.nirkids.app.domain.model.Alphabet(
                                letter = letter,
                                phonetic = "/x/",
                                exampleWord = "$letter Word",
                                imageUrl = "📦",
                                isVowel = letter in listOf('A', 'E', 'I', 'O', 'U')
                            )
                        }
                    ),
                    onNavigateToAlphabet = {},
                    onNavigateToPronunciation = {},
                    onNavigateToParentGate = {}
                )
            }
        }

        // Act & Assert
        composeTestRule.onNodeWithText("📚 NirKids").assertIsDisplayed()
        composeTestRule.onNodeWithText("🌈 Welcome to NirKids!").assertIsDisplayed()
    }

    @Test
    fun `learned count displays correct value`() {
        composeTestRule.setContent {
            NirKidsTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        totalLetters = 26,
                        learnedCount = 12,  // <-- change this value
                        inProgressCount = 5,
                        newCount = 9
                    ),
                    onNavigateToAlphabet = {},
                    onNavigateToPronunciation = {},
                    onNavigateToParentGate = {}
                )
            }
        }

        // The stat card showing "12" should be displayed
        // On-screen the value appears as "12"
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun `progress indicator reflects learned percentage`() {
        composeTestRule.setContent {
            NirKidsTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        totalLetters = 10,
                        learnedCount = 10,  // 100% complete
                        inProgressCount = 0,
                        newCount = 0
                    ),
                    onNavigateToAlphabet = {},
                    onNavigateToPronunciation = {},
                    onNavigateToParentGate = {}
                )
            }
        }

        // "100% complete" should be displayed
        composeTestRule.onNodeWithText("100% complete").assertIsDisplayed()
    }

    @Test
    fun `loading state shows progress indicator`() {
        composeTestRule.setContent {
            NirKidsTheme {
                HomeScreenContent(
                    uiState = HomeUiState(isLoading = true),
                    onNavigateToAlphabet = {},
                    onNavigateToPronunciation = {},
                    onNavigateToParentGate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("🌈 Welcome to NirKids!").assertIsDisplayed()
        // ProgressIndicator is displayed (no text to find, so we verify loading)
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}
```

## Compose Test API Reference

| Method | Purpose |
|---|---|
| `onNodeWithText("text")` | Find a node displaying this text |
| `onNodeWithTag("tag")` | Find a node with this test tag |
| `onNode(hasText(...))` | Find with a matcher (more flexible) |
| `assertIsDisplayed()` | Verify the node is visible |
| `assertDoesNotExist()` | Verify the node is NOT visible |
| `performClick()` | Simulate a tap |
| `performScrollTo()` | Scroll to this node |
| `performTextInput("text")` | Type text into a field |
| `assertHasClickAction()` | Verify the node is clickable |
| `assertTextEquals("expected")` | Verify exact text content |

## Finding Nodes in Compose

Sometimes text isn't unique enough. Use test tags instead:

```kotlin
// In your Composable:
Text(
    text = "Learned ✅",
    modifier = Modifier.testTag("learned_count_label")
)

// In your test:
composeTestRule.onNodeWithTag("learned_count_label").assertIsDisplayed()
```

## TDD Practice 3: Testing Interaction Flows

```kotlin
    @Test
    fun `clicking Learn Alphabets button triggers navigation`() {
        var navigationTriggered = false

        composeTestRule.setContent {
            NirKidsTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        totalLetters = 26,
                        learnedCount = 0,
                        inProgressCount = 0,
                        newCount = 26
                    ),
                    onNavigateToAlphabet = { navigationTriggered = true },
                    onNavigateToPronunciation = {},
                    onNavigateToParentGate = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("🔤 Learn Alphabets").performClick()

        // Assert
        assertTrue(navigationTriggered)
    }
```

## UI Testing Best Practices

- **Test what the user sees, not implementation details** — verify "Learned: 5" not "StateFlow emitted X"
- **Use real data in compose tests** — mock the state, not the ViewModel
- **Keep UI tests fast** — avoid real network calls, use fake data
- **One behavior per test** — don't test the whole screen in one test
- **Use test tags for complex screens** — makes tests resilient to text changes

## Running Compose UI Tests

```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest

# Run only this test class
./gradlew :app:connectedDebugAndroidTest --tests "com.nirkids.app.ui.screens.HomeScreenTest"
```

> ⚠️ Compose UI tests require an emulator or physical device. They cannot run on the JVM.

---

**Next step:** [Layer 6 — Testing Architecture & Patterns](./TDD_LAYER6_ARCHITECTURE.md)
