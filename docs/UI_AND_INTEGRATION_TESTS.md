# UI and Integration Testing

This document covers testing that requires an Android environment (emulator or device).

## Jetpack Compose UI Testing
Compose uses a specific testing library to find elements and perform actions.

### Example: Verifying a Button
```kotlin
class MyComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myTest() {
        // Start the app with a specific Composable
        composeTestRule.setContent {
            MyScreen()
        }

        // Find a node by text and click it
        composeTestRule.onNodeWithText("Submit").performClick()

        // Verify something happened
        composeTestRule.onNodeWithText("Success").assertIsDisplayed()
    }
}
```

## Testing with Hilt
When using Hilt for Dependency Injection, your instrumented tests need a special setup to inject dependencies or provide test doubles (fakes/mocks).

### Hilt Test Setup
1. Use the `@HiltAndroidTest` annotation.
2. Add the `HiltAndroidRule`.

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MyHiltTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testWithDI() {
        // Your test here...
    }
}
```

## Room Database Testing
Instrumented tests are ideal for testing Room migrations or complex queries using an in-memory database.

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: NirKidsDatabase
    private lateinit var dao: ProgressDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NirKidsDatabase::class.java).build()
        dao = db.progressDao()
    }

    @After
    fun closeDb() {
        db.close()
    }
}
```
