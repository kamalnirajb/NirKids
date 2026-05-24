# TDD Layer 1 — Testing Domain Models

**Difficulty:** Beginner | **Time:** ~30 min | **Prerequisites:** Basic Kotlin knowledge

This is the easiest and best place to start with TDD. Domain models are simple data classes with no Android dependencies. Perfect for learning the test-first habit.

## What Are Domain Models?

Domain models are plain Kotlin objects that represent your app's core concepts. They know nothing about Android, databases, or networking.

In NirKids, your domain models live in:
```
app/src/main/java/com/nirkids/app/domain/model/
```

### Example: `Alphabet` Model

```kotlin
data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
) {
    val displayName: String get() = "$letter — $exampleWord"
    val phoneticBreakdown: List<String> get() = phonetic.split("/")
        .filter { it.isNotBlank() }
        .map { it.trim() }
}
```

## TDD Practice: Testing `Alphabet.displayName`

### Step 1: Write the test (RED)

Create the file: `app/src/test/java/com/nirkids/app/domain/model/AlphabetTest.kt`

```kotlin
package com.nirkids.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AlphabetTest {

    @Test
    fun `displayName combines letter and example word`() {
        val alphabet = Alphabet(
            letter = 'A',
            phonetic = "/æ/",
            exampleWord = "Apple",
            imageUrl = "🍎",
            isVowel = true
        )

        assertEquals("A — Apple", alphabet.displayName)
    }

    @Test
    fun `displayName works for any letter`() {
        val alphabet = Alphabet(
            letter = 'Z',
            phonetic = "/zi/",
            exampleWord = "Zebra",
            imageUrl = "🦓",
            isVowel = false
        )

        assertEquals("Z — Zebra", alphabet.displayName)
    }
}
```

**Expected:** Tests fail with `Unresolved reference: Alphabet` — compilation failure.

### Step 2: Make it pass (GREEN)

Create the `Alphabet` class: `app/src/main/java/com/nirkids/app/domain/model/Alphabet.kt`

```kotlin
package com.nirkids.app.domain.model

data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
) {
    val displayName: String
        get() = "$letter — $exampleWord"
}
```

**Expected:** Both tests pass! 🟢

### Step 3: Refactor

Is the code clean? Yes. But let's add more cases...

### Step 4: Add more tests (RED → GREEN → REFACTOR)

Add these tests to the same file and follow the cycle:

```kotlin
@Test
fun `phoneticBreakdown splits phonetic string by forward slash`() {
    val alphabet = Alphabet('A', "/æ/", "Apple", "🍎", true)
    assertEquals(listOf("æ"), alphabet.phoneticBreakdown)
}

@Test
fun `phoneticBreakdown filters empty segments`() {
    val alphabet = Alphabet('B', "/b/", "Ball", "⚽", false)
    // "/b/" has empty segments around the slash
    assertEquals(listOf("b"), alphabet.phoneticBreakdown)
}
```

## TDD Practice: Testing `LetterProgress`

Another domain model in your project:

```kotlin
data class LetterProgress(
    val letter: Char,
    val learned: Boolean,
    val attempts: Int,
    val masteryLevel: Int
) {
    val isMastered: Boolean get() = masteryLevel >= 5
    val progressPercent: Int get() = if (learned) 100 else (attempts * 20).coerceAtMost(100)
}
```

**TDD exercise — write these tests first:**

```kotlin
@Test
fun `isMastered is true when masteryLevel is 5 or more`() { ... }
@Test
fun `isMastered is false when masteryLevel is less than 5`() { ... }
@Test
fun `progressPercent is 100 when learned is true`() { ... }
@Test
fun `progressPercent calculates correctly when not learned`() { ... }
@Test
fun `progressPercent caps at 100 even if attempts would exceed it`() { ... }
```

Follow Red → Green → Refactor for each test.

## Key Takeaways

- **Domain models are the best starting point** for TDD — no mocking, no Android setup
- Write tests that express **what** the code should do, not **how** it does it
- One behavior per test method
- Use meaningful test names with backticks for readable failure messages

---

**Next step:** [Layer 2 — Testing Use Cases](./TDD_LAYER2_USECASES.md)
