package com.nirkids.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nirkids.app.ui.main.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_screen_displays_welcome_text() {
        composeTestRule.onNodeWithText("📚 NirKids").assertIsDisplayed()
    }

    @Test
    fun home_screen_displays_Learn_Alphabets_button() {
        composeTestRule.onNodeWithText("🔤 Learn Alphabets").assertIsDisplayed()
    }

    @Test
    fun home_screen_displays_Pronunciation_button() {
        composeTestRule.onNodeWithText("🗣️ Pronunciation").assertIsDisplayed()
    }
}
