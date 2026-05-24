# TDD Checklist & Resources

## TDD Checklist for Every New Feature

Use this checklist before committing any new feature:

### Before Writing Code
- [ ] I wrote the test **before** the production code
- [ ] The test **fails** (compilation or assertion)
- [ ] The test name clearly describes the expected behavior

### While Writing Code
- [ ] I wrote the minimum code to make the test pass
- [ ] Each test covers one specific behavior
- [ ] I'm not testing implementation details

### After Writing Code
- [ ] All tests pass (unit tests run locally)
- [ ] No existing tests broke
- [ ] Test names are descriptive
- [ ] No `// TODO` comments left in tests

### Architecture Checks
- [ ] Domain models have no Android dependencies
- [ ] Use cases only depend on repository interfaces
- [ ] ViewModels only depend on use cases
- [ ] Data models are isolated from domain models
- [ ] Dependencies are injected (not created with `new`)

### Test Quality
- [ ] Tests are independent (no test depends on another's state)
- [ ] Tests run in any order
- [ ] Tests run in under 10 seconds each
- [ ] Tests don't use `Thread.sleep()` or `wait()`
- [ ] Tests don't rely on system time or randomness

### UI Tests
- [ ] Tests use test tags where text might change
- [ ] Tests verify what the user sees, not internal state
- [ ] Tests don't depend on timing or animations

---

## Quick Reference: Test Commands

```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run all instrumented tests
./gradlew :app:connectedDebugAndroidTest

# Run a single test
./gradlew :app:testDebugUnitTest --tests "com.nirkids.app.domain.usecase.GetRandomLetterUseCaseTest"

# Run tests for a specific class
./gradlew :app:testDebugUnitTest --tests "*AlphabetViewModelTest*"

# Run tests for a specific package
./gradlew :app:testDebugUnitTest --tests "com.nirkids.app.domain.*"

# Run tests and show output
./gradlew :app:testDebugUnitTest --info --tests "*Test*"

# Run specific test method
./gradlew :app:testDebugUnitTest --tests "com.nirkids.app.domain.usecase.GetRandomLetterUseCaseTest.invoke returns null*"
```

---

## Recommended Learning Resources

### Books
- **"Test-Driven Development by Example"** by Kent Beck — The TDD bible
- **"The Art of Unit Testing"** by Roy Osherove — Best practical guide
- **"Growing Object-Oriented Software, Guided by Tests"** — Architecture + TDD

### Online
- [MockK Documentation](https://mockk.io/) — Kotlin mocking library
- [Coroutines Testing Guide](https://kotlinlang.org/docs/coroutines/test.html) — kotlinx.coroutines testing
- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing) — Official Compose test guide
- [Architecture Components Testing](https://developer.android.com/topic/architecture) — Android testing guide

### YouTube
- "Test Driven Development in Android" by Philipp Hauer
- "Unit Testing in Kotlin" by Devoxx
- "Jetpack Compose UI Testing" by Google I/O

---

## NirKids Project-Specific Testing Conventions

### File Naming
- Test files mirror production files: `HomeViewModel.kt` → `HomeViewModelTest.kt`
- Test packages mirror production packages: `com.nirkids.app.domain.usecase`

### Naming Conventions
```kotlin
// Tests: backtick + natural language
@Test
fun `when repository returns empty list, use case returns null`()

// Use cases: verb + object
class GetRandomLetterUseCase
class MarkLetterLearnedUseCase

// ViewModels: camelCase + ViewModel suffix
class HomeViewModel
class AlphabetViewModel

// UI tests: ScreenName + Test suffix
class HomeScreenTest
class AlphabetScreenTest
```

### Assertions
```kotlin
// Standard JUnit
assertEquals(expected, actual)
assertNull(value)
assertTrue(condition)
assertFalse(condition)

// MockK verification
coVerify { repository.method() }
coVerify(exactly = 1) { repository.method() }
coVerify(exactly = 0) { repository.method() }

// Compose testing
composeTestRule.onNodeWithText("text").assertIsDisplayed()
composeTestRule.onNodeWithTag("tag").assertDoesNotExist()
```

---

## TDD Glossary

| Term | Meaning |
|---|---|
| **Red** | Test is failing — feature not implemented |
| **Green** | Test is passing — feature implemented |
| **Refactor** | Improve code without changing behavior |
| **Mock** | Fake object that records interactions |
| **Stub** | Fake object that returns predefined values |
| **Fake** | Fake object with simple implementations (e.g., in-memory DB) |
| **Arrange** | Set up the test conditions |
| **Act** | Execute the code under test |
| **Assert** | Verify the expected result |
| **Fixture** | The fixed state used for tests |
| **Test Pyramid** | More unit tests, fewer UI tests |
| **TDD** | Test-Driven Development |
| **AAA Pattern** | Arrange, Act, Assert — test structure |

---

**🎉 You've completed the TDD learning series!**

Here's the full collection of documents in this project:

| Document | Description |
|---|---|
| [TDD BASICS](./TDD_BASICS.md) | What is TDD, the Red-Green-Refactor cycle |
| [LAYER 1](./TDD_LAYER1_DOMAIN_MODELS.md) | Testing domain models (easiest entry point) |
| [LAYER 2](./TDD_LAYER2_USECASES.md) | Testing use cases with MockK |
| [LAYER 3](./TDD_LAYER3_VIEWMODELS.md) | Testing ViewModels with coroutines |
| [LAYER 4](./TDD_LAYER4_REPOSITORIES.md) | Testing repository and Room database |
| [LAYER 5](./TDD_LAYER5_UI.md) | Testing Compose UI |
| [LAYER 6](./TDD_LAYER6_ARCHITECTURE.md) | Architecture tests and patterns |
| [PRACTICAL WORKFLOW](./TDD_PRACTICAL_WORKFLOW.md) | Complete TDD walkthrough with a real feature |
| [CHECKLIST & RESOURCES](./TDD_CHECKLIST.md) | Quick reference, commands, and learning resources |

---

## How to Use These Documents

**If you're just starting:**
1. Read `TDD_BASICS.md` — understand the concept
2. Follow `TDD_LAYER1_DOMAIN_MODELS.md` — write your first test
3. Progress through Layers 1 → 6 in order
4. Use `TDD_PRACTICAL_WORKFLOW.md` when implementing a real feature
5. Keep `TDD_CHECKLIST.md` handy for every commit

**If you already know TDD and want to learn Android testing:**
1. Skim `TDD_BASICS.md`
2. Jump to `TDD_LAYER2_USECASES.md` (MockK patterns)
3. Read `TDD_LAYER3_VIEWMODELS.md` (coroutine testing)
4. Read `TDD_LAYER6_ARCHITECTURE.md` (testing architecture)
5. Use the checklist before each PR

**If you want to contribute tests to NirKids:**
1. Pick a feature from the codebase
2. Follow the workflow in `TDD_PRACTICAL_WORKFLOW.md`
3. Check `TDD_CHECKLIST.md` before submitting
4. Look at existing tests in `app/src/test/` and `app/src/androidTest/` for patterns
