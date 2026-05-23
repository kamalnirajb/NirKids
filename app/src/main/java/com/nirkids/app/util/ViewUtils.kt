package com.nirkids.app.util

import androidx.compose.ui.graphics.Color
import com.nirkids.app.ui.theme.*

object ViewUtils {

    /**
     * Returns a color based on the letter's progress state.
     */
    fun getProgressColor(masteryLevel: Int): Color {
        return when {
            masteryLevel >= 100 -> MasteryGreen
            masteryLevel >= 50 -> MasteryOrange
            masteryLevel > 0 -> MasteryGray
            else -> MasteryGray.copy(alpha = 0.5f)
        }
    }

    /**
     * Returns a random soft color for UI backgrounds.
     */
    fun getRandomSoftColor(): Color {
        val colors = listOf(
            LetterRed, LetterOrange, LetterYellow,
            LetterGreen, LetterCyan, LetterBlue
        )
        return colors.random().copy(alpha = 0.1f)
    }
}
