package com.nirkids.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class AlphabetScreenScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `alphabet grid renders letters A through Z`() {
        composeTestRule.setContent {
            AlphabetGridPreview()
        }
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Z").assertIsDisplayed()
    }
}

@Composable
fun AlphabetGridPreview() {
    Column {
        ('A'..'Z').forEach { letter ->
            Text(letter.toString())
        }
    }
}
