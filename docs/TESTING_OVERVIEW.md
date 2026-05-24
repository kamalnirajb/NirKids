# Testing in NirKids

This document provides an overview of the testing strategy, tools, and learning path for the NirKids project.

## 📚 Learning Path (Beginner-Friendly)

Want to learn TDD step by step? Start here:

1. **[TDD Basics](./TDD_BASICS.md)** — What is TDD and the Red-Green-Refactor cycle
2. **[Layer 1: Domain Models](./TDD_LAYER1_DOMAIN_MODELS.md)** — Your first tests (easiest)
3. **[Layer 2: Use Cases](./TDD_LAYER2_USECASES.md)** — Mocking with MockK
4. **[Layer 3: ViewModels](./TDD_LAYER3_VIEWMODELS.md)** — State testing with coroutines
5. **[Layer 4: Repositories](./TDD_LAYER4_REPOSITORIES.md)** — Room database testing
6. **[Layer 5: UI Tests](./TDD_LAYER5_UI.md)** — Compose UI testing
7. **[Layer 6: Architecture](./TDD_LAYER6_ARCHITECTURE.md)** — Architecture patterns
8. **[Practical Workflow](./TDD_PRACTICAL_WORKFLOW.md)** — Real feature walkthrough
9. **[Checklist & Resources](./TDD_CHECKLIST.md)** — Quick reference

## Test Types

### 1. Local Unit Tests (`test` source set)
These tests run on your local machine's JVM. They are fast and ideal for testing business logic, ViewModels, and data mapping.
- **Location:** `app/src/test/java/`
- **Frameworks:** JUnit 4, MockK, Robolectric, kotlinx-coroutines-test, Arch Core Testing.

### 2. Instrumented Tests (`androidTest` source set)
These tests run on a physical device or emulator. They are used for UI testing and testing components that require a real Android environment.
- **Location:** `app/src/androidTest/java/`
- **Frameworks:** JUnit 4, Espresso, Compose UI Test, Hilt Testing, MockK (Android).

## Running Tests

### Using Android Studio
- **Run a single test:** Click the green play icon next to a test class or method.
- **Run all tests in a folder:** Right-click the folder in the Project view and select **Run 'Tests in...'**.

### Using Command Line (Gradle)
- **Run all unit tests:**
  ```bash
  ./gradlew test
  ```
- **Run unit tests for a specific module:**
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
- **Run all instrumented tests:**
  ```bash
  ./gradlew connectedAndroidTest
  ```
- **Build instrumented tests without running (useful for checking compilation):**
  ```bash
  ./gradlew :app:assembleDebugAndroidTest
  ```

## Key Libraries
- **JUnit 4:** The standard testing framework.
- **MockK / MockK-Android:** A powerful mocking library for Kotlin, used in both unit and instrumented tests.
- **Robolectric:** Allows running Android-dependent tests on the JVM without an emulator.
- **Espresso / Compose UI Test:** Tools for interacting with and verifying the UI.
- **Arch Core Testing:** Provides `InstantTaskExecutorRule` for testing LiveData/Architecture components.
- **kotlinx-coroutines-test:** Provides `runTest` and `UnconfinedTestDispatcher` for testing coroutines.

## Best Practices & Troubleshooting
- **Method Naming:** For `androidTest` (Instrumented Tests), avoid using spaces in backticks for method names (e.g., use `fun test_feature()` instead of ``fun `test feature`() ``). Some versions of DEX do not support spaces in method names, leading to build failures.
- **Stateless Composables:** Screens are refactored into `*Content` (stateless) and `*Screen` (stateful) to allow for easier UI testing by injecting mock states.
