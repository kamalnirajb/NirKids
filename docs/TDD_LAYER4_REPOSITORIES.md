# TDD Layer 4 — Testing Repository and Data Layer

**Difficulty:** Intermediate → Advanced | **Time:** ~90 min | **Prerequisites:** Layers 1-3 complete

Repository tests verify that your data mapping, database queries, and transformations work correctly. This layer is where you test real Room operations (via in-memory databases) and entity-to-model conversions.

## The NirKids Repository

The `AlphabetRepositoryImpl` in NirKids:
```
app/src/main/java/com/nirkids/app/data/repository/AlphabetRepository.kt
```

It does several things:
1. Fetches data from Room (via DAOs)
2. Converts `AlphabetEntity` → `Alphabet` (data model → domain model)
3. Converts `ProgressEntity` → `LetterProgress`
4. Handles seeding initial data

## TDD Practice 1: Testing Entity-to-Domain Mapping

This is the easiest starting point. Create: `app/src/test/java/com/nirkids/app/data/model/EntitiesTest.kt`

### Test the `toDomainModel()` conversion

```kotlin
package com.nirkids.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EntitiesTest {

    @Test
    fun `AlphabetEntity converts to Alphabet with correct letter`() {
        val entity = AlphabetEntity(
            letter = "A",
            phonetic = "/æ/",
            exampleWord = "Apple",
            imageUrl = "🍎",
            isVowel = true
        )

        val domain = entity.toDomainModel()

        assertEquals('A', domain.letter)
        assertEquals("/æ/", domain.phonetic)
        assertEquals("Apple", domain.exampleWord)
        assertEquals("🍎", domain.imageUrl)
        assertEquals(true, domain.isVowel)
    }

    @Test
    fun `AlphabetEntity toDomainModel returns space for empty letter`() {
        val entity = AlphabetEntity("", "/x/", "Word", "📦", false)
        val domain = entity.toDomainModel()
        assertEquals(' ', domain.letter)
    }

    @Test
    fun `ProgressEntity converts to LetterProgress correctly`() {
        val entity = ProgressEntity(
            letter = "B",
            learned = false,
            attempts = 5,
            masteryLevel = 3
        )

        val domain = entity.toDomainModel()

        assertEquals('B', domain.letter)
        assertEquals(false, domain.learned)
        assertEquals(5, domain.attempts)
        assertEquals(3, domain.masteryLevel)
    }
}
```

**Note:** These tests test the `toDomainModel()` extension functions. You'll need to add them as extension functions on the entity classes, or call them directly. In the NirKids code, these functions are defined inside `AlphabetRepositoryImpl` as private methods. For testing, expose them as top-level functions or make them `internal`.

## TDD Practice 2: Testing Repository with In-Memory Room Database

For real database testing, create an in-memory Room database. Create: `app/src/test/java/com/nirkids/app/data/repository/AlphabetRepositoryImplTest.kt`

### The Test

```kotlin
package com.nirkids.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.data.model.AlphabetEntity
import com.nirkids.app.data.model.ProgressEntity
import com.nirkids.app.domain.model.Alphabet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AlphabetRepositoryImplTest {

    private lateinit var database: AlphabetsDatabase
    private lateinit var repository: AlphabetRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AlphabetsDatabase::class.java
        ).build()
        repository = AlphabetRepositoryImpl(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `saveAllAlphabets stores data that can be retrieved`() = runTest {
        // Arrange
        val alphabets = listOf(
            AlphabetEntity("A", "/æ/", "Apple", "🍎", true),
            AlphabetEntity("B", "/b/", "Ball", "⚽", false)
        )

        // Act
        repository.saveAllAlphabets(alphabets)
        val result = repository.getAllAlphabets().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals('A', result[0].letter)
        assertEquals('B', result[1].letter)
    }

    @Test
    fun `getAlphabetByLetter returns correct alphabet`() = runTest {
        // Arrange
        val alphabet = AlphabetEntity("C", "/k/", "Cat", "🐱", false)
        repository.saveAllAlphabets(listOf(alphabet))

        // Act
        val result = repository.getAlphabetByLetter('C')

        // Assert
        assertNotNull(result)
        assertEquals("Cat", result!!.exampleWord)
    }

    @Test
    fun `getAlphabetByLetter returns null for non-existent letter`() = runTest {
        // Arrange
        repository.saveAllAlphabets(listOf(
            AlphabetEntity("A", "/æ/", "Apple", "🍎", true)
        ))

        // Act
        val result = repository.getAlphabetByLetter('Z')

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `markLetterLearned updates progress correctly`() = runTest {
        // Arrange
        repository.saveAllAlphabets(listOf(
            AlphabetEntity("D", "/d/", "Dog", "🐶", false)
        ))
        repository.seedInitialData(database)

        // Act
        repository.markLetterLearned('D')
        val progress = repository.getProgressForLetter('D')

        // Assert
        assertNotNull(progress)
        assertEquals(true, progress!!.learned)
    }

    @Test
    fun `incrementAttempts increases mastery level`() = runTest {
        // Arrange
        repository.saveAllAlphabets(listOf(
            AlphabetEntity("E", "/e/", "Egg", "🥚", true)
        ))
        repository.seedInitialData(database)

        // Act
        repository.incrementAttempts('E')
        val progress1 = repository.getProgressForLetter('E')

        repository.incrementAttempts('E')
        val progress2 = repository.getProgressForLetter('E')

        // Assert
        assertEquals(1, progress1!!.masteryLevel)
        assertEquals(2, progress2!!.masteryLevel)
    }
}
```

## TDD Practice 3: Testing `seedInitialData`

```kotlin
    @Test
    fun `seedInitialData creates default data when database is empty`() = runTest {
        // Act
        repository.seedInitialData(database)

        // Assert
        val alphabets = repository.getAllAlphabets().first()
        val progresses = repository.getAllProgress().first()

        assertEquals(26, alphabets.size)
        assertEquals(26, progresses.size)
        assertEquals('A', alphabets[0].letter)
        assertEquals('Z', alphabets[25].letter)
    }

    @Test
    fun `seedInitialData does not duplicate data if already seeded`() = runTest {
        // Arrange
        repository.seedInitialData(database)

        // Act
        repository.seedInitialData(database)

        // Assert
        val alphabets = repository.getAllAlphabets().first()
        assertEquals(26, alphabets.size)  // Still 26, not 52
    }
```

## Room Testing Checklist

- [ ] Use `Room.inMemoryDatabaseBuilder` — no real database needed
- [ ] Always `close()` the database in `@After`
- [ ] Test CRUD operations: create, read, update, delete
- [ ] Test edge cases: empty results, null values, special characters
- [ ] Test data integrity: foreign keys, constraints
- [ ] Test migrations if applicable

## Beginner Tips

- **In-memory Room** is surprisingly fast — it's a real SQLite database in RAM
- **Don't test Room migrations** in unit tests — use `InstrumentationTest` for that
- **Separate mapping tests** from repository tests — they're different concerns
- **Use `runTest`** for all suspend function tests

---

**Next step:** [Layer 5 — Testing UI with Compose](./TDD_LAYER5_UI.md)
