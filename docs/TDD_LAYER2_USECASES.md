# TDD Layer 2 — Testing Use Cases (with MockK)

**Difficulty:** Intermediate | **Time:** ~45 min | **Prerequisites:** Layer 1 complete

Use cases are where business logic lives. They orchestrate data from repositories and return results. This is where you learn **mocking** — the skill that unlocks testing at every layer.

## What Is Mocking?

Mocking means replacing real dependencies with fake, controlled versions. When your code needs a repository, you give it a mock that returns exactly what the test expects.

**Why mock?** So your test can only fail because of a bug in the code you're testing — not because of a flaky database or network call.

## The NirKids Use Case Example

Here's the real `GetRandomLetterUseCase` from the project:

```kotlin
class GetRandomLetterUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(): Alphabet? {
        val alphabets = repository.getAllAlphabets().first()
        return if (alphabets.isNotEmpty()) {
            alphabets[kotlin.random.Random.nextInt(alphabets.size)]
        } else null
    }
}
```

It depends on `IAlphabetRepository`. In production, this is a real Room-backed repository. In tests, we **mock** it.

## TDD Practice: Testing `GetRandomLetterUseCase`

### Step 1: Write tests (RED)

Create: `app/src/test/java/com/nirkids/app/domain/usecase/GetRandomLetterUseCaseTest.kt`

```kotlin
package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Random

class GetRandomLetterUseCaseTest {

    @MockK
    private lateinit var repository: IAlphabetRepository

    private lateinit var useCase: GetRandomLetterUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }
}
```

Now add the test cases:

```kotlin
class GetRandomLetterUseCaseTest {
    // ... (setup from above)

    @Test
    fun `invoke returns null when repository has no alphabets`() = runTest {
        // Arrange
        coEvery { repository.getAllAlphabets() } returns flowOf(emptyList())

        // Act
        val result = useCase()

        // Assert
        assertNull(result)
    }

    @Test
    fun `invoke returns a letter when repository has alphabets`() = runTest {
        // Arrange
        val mockRepo = mockk<IAlphabetRepository>()
        val alphabet = Alphabet('A', "/æ/", "Apple", "🍎", true)
        coEvery { mockRepo.getAllAlphabets() } returns flowOf(listOf(alphabet))

        val testUseCase = GetRandomLetterUseCase(mockRepo)

        // Act
        val result = testUseCase()

        // Assert
        assertEquals(alphabet, result)
    }

    @Test
    fun `invoke calls repository to get alphabets`() = runTest {
        // Arrange
        coEvery { repository.getAllAlphabets() } returns flowOf(
            listOf(Alphabet('A', "/æ/", "Apple", "🍎", true))
        )

        // Act
        useCase()

        // Assert
        verify { repository.getAllAlphabets() }
    }
}
```

**Expected:** Tests fail — `GetRandomLetterUseCase` doesn't exist yet.

### Step 2: Make it pass (GREEN)

Create: `app/src/main/java/com/nirkids/app/domain/usecase/GetRandomLetterUseCase.kt`

```kotlin
package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GetRandomLetterUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(): Alphabet? {
        val alphabets = repository.getAllAlphabets().first()
        return if (alphabets.isNotEmpty()) {
            alphabets[Random.nextInt(alphabets.size)]
        } else null
    }
}
```

**Expected:** All tests pass! 🟢

### Step 3: Refactor

The code is clean. But we should add one more edge case...

### Step 4: Add an edge case test (RED)

```kotlin
@Test
fun `invoke handles large list of alphabets`() = runTest {
    // Arrange
    val mockRepo = mockk<IAlphabetRepository>()
    val alphabets = ('A'..'Z').map { letter ->
        Alphabet(letter, "/x/", "${letter}pple", "🍎", letter in listOf('A', 'E', 'I', 'O', 'U'))
    }
    coEvery { mockRepo.getAllAlphabets() } returns flowOf(alphabets)

    val testUseCase = GetRandomLetterUseCase(mockRepo)

    // Act
    val result = testUseCase()

    // Assert — result must be one of the 26 letters
    result!!.letter in ('A'..'Z')
}
```

## Understanding the MockK API

| MockK Call | Purpose |
|---|---|
| `mockk<MyType>()` | Creates a mock instance |
| `@MockK` + `MockKAnnotations.init(this)` | Annotation-based mock setup |
| `coEvery { ... } returns value` | Define behavior for suspend/coroutine functions |
| `every { ... } returns value` | Define behavior for regular functions |
| `verify { ... }` | Assert that a method was called |
| `coVerify { ... }` | Assert coroutine function was called |
| `mockkStatic(MyClass::class)` | Mock static/final methods |

## TDD Pattern Summary

```
1. Create mock dependencies
2. Define what mocks return (Arrange)
3. Call the method under test (Act)
4. Assert on results and verify interactions (Assert)
```

## Common Beginner Mistakes

❌ **Mocking everything, even simple utilities** — Use real objects for simple code
❌ **Testing one thing with multiple asserts** — Split into separate tests
❌ **Using `mockkStatic` too early** — It's for final/static methods; prefer dependency injection
❌ **Not cleaning up mocks** — Each test should be independent

---

**Next step:** [Layer 3 — Testing ViewModels](./TDD_LAYER3_VIEWMODELS.md)
