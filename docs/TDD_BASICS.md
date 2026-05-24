# TDD Basics for NirKids — A Beginner's Guide

## What is TDD?

TDD (Test-Driven Development) is a development approach where you **write the test before the code**. It follows a simple, repeatable cycle:

```
🔴 RED   → Write a failing test (the feature doesn't exist yet)
🟢 GREEN → Write the minimum code to make the test pass
🔵 REFACTOR → Clean up your code while keeping tests green
```

This cycle is called **"Red-Green-Refactor"**.

## Why TDD Matters for Android

| Problem without TDD | How TDD helps |
|---|---|
| Bugs appear late in development | Catches issues the moment code is written |
| Refactoring is scary | Tests give you confidence to change code |
| Hard to add features to old code | Tests document expected behavior |
| No safety net for changes | Failures are immediate and specific |

## The TDD Cycle in Practice

### Step 1: Write the Test (RED)

You start with a failing test. At this point the code might not even exist.

```kotlin
@Test
fun `when markLetterLearned is called, progress is updated`() {
    // Test will fail because markLetterLearned doesn't exist yet
}
```

### Step 2: Write Code to Pass (GREEN)

Write the *minimum* code needed. Don't over-engineer.

```kotlin
suspend fun markLetterLearned(letter: Char) {
    // Just enough to make the test pass
}
```

### Step 3: Refactor (BLUE)

Clean up. Rename. Extract. The tests are your safety net.

## The Three Laws of TDD (Robert C. Martin)

1. **You may not write production code** until you have a failing test.
2. **You may not write more of a test** than is sufficient to fail — and compilation failures count as failures.
3. **You may not write more production code** than is sufficient to pass the currently-failing test.

> These laws keep you focused. TDD isn't about testing the finished product. It's about **driving** the design of the product.

## TDD Mindset for Beginners

### Think in small steps

Don't try to test "the entire HomeScreen". Test:
- "When the user taps a letter, the count increments"
- "When data loads, the progress bar shows 0%"

### Embrace the red phase

A failing test is **progress**, not failure. It's telling you: "I'm ready for your code."

### Write tests that express intent

```kotlin
// ❌ Bad — describes implementation
@Test
fun test1() {
    val vm = HomeViewModel(mockUseCase())
    vm.uiState.value.learnedCount shouldBe 3
}

// ✅ Good — describes behavior
@Test
fun `when there are 3 learned letters, uiState reflects it`() {
    // Self-documenting: anyone can read this and understand what's expected
}
```

## Quick Reference: TDD Toolchain in NirKids

| Layer | Framework | Location | Runs On |
|---|---|---|---|
| **Unit Tests** | JUnit 4 + MockK | `app/src/test/java/` | JVM (your computer) |
| **Android Unit** | JUnit 4 + MockK + Robolectric | `app/src/test/java/` | JVM (emulates Android) |
| **UI/Instrumented** | Espresso + Compose Test | `app/src/androidTest/java/` | Device or emulator |

## Your First TDD Exercise

Let's practice with a simple concept: checking if a letter is a vowel.

**1. Write the test (RED):**

```kotlin
class VowelCheckTest {
    @Test
    fun `A is a vowel`() {
        Assert.assertTrue(isVowel('A'))
    }

    @Test
    fun `B is not a vowel`() {
        Assert.assertFalse(isVowel('B'))
    }
}
```

**2. Make it pass (GREEN):**

```kotlin
fun isVowel(letter: Char): Boolean {
    return letter in setOf('A', 'E', 'I', 'O', 'U')
}
```

**3. Refactor:**

The code is already clean. But what about lowercase? Add more tests and iterate.

---

**Next step:** [Layer 1 — Testing Pure Domain Models](./TDD_LAYER1_DOMAIN_MODELS.md)
