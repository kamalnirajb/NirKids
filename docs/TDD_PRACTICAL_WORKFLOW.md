# Practical TDD Workflow for NirKids

**Putting it all together — a step-by-step guide for developing new features with TDD.**

## The Complete TDD Cycle (Practical)

Let's walk through TDD-ing a real new feature for NirKids: **"Track which letters the user has attempted but not yet learned."**

### Phase 1: Write Tests First (RED)

#### Step 1: Test the Domain Model

```kotlin
// app/src/test/java/com/nirkids/app/domain/model/AlphabetTest.kt

@Test
fun `Alphabet has an attemptedByUser flag`() {
    val alphabet = Alphabet(
        letter = 'A',
        phonetic = "/æ/",
        exampleWord = "Apple",
        imageUrl = "🍎",
        isVowel = true,
        attemptedByUser = true   // NEW field
    )
    assertTrue(alphabet.attemptedByUser)
}
```

#### Step 2: Test the Use Case

```kotlin
// app/src/test/java/com/nirkids/app/domain/usecase/MarkLetterAttemptedUseCaseTest.kt

@Test
fun `invoke marks letter as attempted in repository`() = runTest {
    // Arrange
    coEvery { repository.incrementAttempts('A') } returns Unit

    // Act
    useCase('A')

    // Assert
    coVerify { repository.incrementAttempts('A') }
}

@Test
fun `invoke returns true when letter was marked`() = runTest {
    // Arrange
    coEvery { repository.incrementAttempts('A') } returns Unit

    // Act
    val result = useCase('A')

    // Assert
    assertTrue(result)
}
```

#### Step 3: Test the ViewModel State Change

```kotlin
// app/src/test/java/com/nirkids/app/ui/main/viewmodel/AlphabetViewModelTest.kt

@Test
fun `onLetterAttempted updates uiState with attempted letter`() = runTest(testDispatcher) {
    // Arrange
    coEvery { markLetterAttemptedUseCase('A') } returns true
    coEvery { getAlphabetUseCase() } returns flowOf(listOf(
        Alphabet('A', "/æ/", "Apple", "🍎", true, true)
    ))
    coEvery { getProgressUseCase() } returns flowOf(listOf(
        LetterProgress('A', false, 1, 1)
    ))

    testDispatcher.scheduler.advanceUntilIdle()

    // Act
    viewModel.onLetterAttempted('A')
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertEquals(1, state.attemptedCount)
}
```

### Phase 2: Make Tests Pass (GREEN)

Now write the *minimum* code to make each test pass.

#### Write the domain model:

```kotlin
// Alphabet.kt — add the new field
data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean,
    val attemptedByUser: Boolean = false  // ← NEW
)
```

#### Write the use case:

```kotlin
// MarkLetterAttemptedUseCase.kt
class MarkLetterAttemptedUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(letter: Char): Boolean {
        return try {
            repository.incrementAttempts(letter)
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

#### Write the ViewModel:

```kotlin
// AlphabetViewModel.kt — add new behavior
fun onLetterAttempted(letter: Char) {
    viewModelScope.launch {
        val success = markLetterAttemptedUseCase(letter)
        if (success) {
            loadProgress()
        }
    }
}
```

### Phase 3: Run All Tests

```bash
# Unit tests
./gradlew :app:testDebugUnitTest

# Make sure nothing broke!
```

### Phase 4: Refactor

- Is there duplicated code?
- Are variable names clear?
- Can you extract a helper function?
- Does the architecture still hold?

### Phase 5: UI Test

```kotlin
// app/src/androidTest/java/com/nirkids/app/ui/screens/AlphabetScreenTest.kt

@Test
fun `attempt button updates the attempted count`() {
    // ... setup Compose UI test ...
    composeTestRule.onNodeWithText("Attempt").performClick()
    composeTestRule.onNodeWithText("Attempted: 1").assertIsDisplayed()
}
```

## Running Tests Workflow

```bash
# Quick check (unit tests only)
./gradlew :app:testDebugUnitTest

# Full check (unit + instrumented)
./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest

# Single test class
./gradlew :app:testDebugUnitTest --tests "com.nirkids.app.domain.usecase.GetRandomLetterUseCaseTest"

# With logging (for debugging failures)
./gradlew :app:testDebugUnitTest --info --tests "*AlphabetTest*"
```

## TDD Anti-Patterns to Avoid

| Anti-Pattern | What to do instead |
|---|---|
| **Writing code first, tests after** | Write the failing test FIRST |
| **Testing 10 things in 1 test** | Split into focused tests |
| **Using real network/database in unit tests** | Mock or use in-memory database |
| **Ignoring red tests** | Never commit red tests |
| **Over-mocking** | Mock only external dependencies |
| **Deleting tests when they "don't help"** | Fix the root cause, not the test |

## Commit Strategy with TDD

```bash
# Each test is a step toward the feature
git add app/src/test/java/
git commit -m "TDD: add test for markLetterAttempted"

# Then the code to pass it
git add app/src/main/java/
git commit -m "TDD: implement markLetterAttempted use case"
```

## Recommended TDD Order for New Features

1. **Domain model tests** (pure Kotlin, fastest)
2. **Use case tests** (business logic, with mocks)
3. **ViewModel tests** (state management, with coroutines)
4. **Repository tests** (in-memory DB)
5. **UI tests** (Composable interactions)

---

**Next step:** [TDD Checklist & Resources](./TDD_CHECKLIST.md)
