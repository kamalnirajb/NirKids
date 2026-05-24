# Testing in NirKids

This document provides an overview of the testing strategy and tools used in the NirKids project.

## Test Types

### 1. Local Unit Tests (`test` source set)
These tests run on your local machine's JVM. They are fast and ideal for testing business logic, ViewModels, and data mapping.
- **Location:** `app/src/test/java/`
- **Frameworks:** JUnit 4, MockK, Robolectric.

### 2. Instrumented Tests (`androidTest` source set)
These tests run on a physical device or emulator. They are used for UI testing and testing components that require a real Android environment.
- **Location:** `app/src/androidTest/java/`
- **Frameworks:** JUnit 4, Espresso, Compose UI Test, Hilt Testing.

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

## Key Libraries
- **JUnit 4:** The standard testing framework.
- **MockK:** A powerful mocking library for Kotlin.
- **Robolectric:** Allows running Android-dependent tests on the JVM without an emulator.
- **Espresso / Compose UI Test:** Tools for interacting with and verifying the UI.
