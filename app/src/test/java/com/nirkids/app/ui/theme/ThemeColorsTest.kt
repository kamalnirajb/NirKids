package com.nirkids.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ThemeColorsTest {

    @Test
    fun `Letter colors are distinct`() {
        assertNotEquals(LetterRed, LetterOrange)
        assertNotEquals(LetterOrange, LetterYellow)
        assertNotEquals(LetterYellow, LetterGreen)
        assertNotEquals(LetterGreen, LetterCyan)
        assertNotEquals(LetterCyan, LetterBlue)
        assertNotEquals(LetterBlue, LetterPurple)
    }

    @Test
    fun `HighContrast colors are black and white`() {
        assertEquals(HighContrastBlack, Color.Black)
        assertEquals(HighContrastWhite, Color.White)
    }

    @Test
    fun `VowelColor is set to LetterRed`() {
        assertEquals(LetterRed, VowelColor)
    }

    @Test
    fun `ConsonantColor is set to LetterBlue`() {
        assertEquals(LetterBlue, ConsonantColor)
    }

    @Test
    fun `Mastery colors are defined`() {
        assertNotNull(MasteryGreen)
        assertNotNull(MasteryOrange)
        assertNotNull(MasteryGray)
    }

    @Test
    fun `Color values are valid ARGB`() {
        assertTrue(LetterRed.value.toLong() ushr 32 == 0xFF.toLong()) // Alpha
        assertTrue(LetterGreen.value.toLong() ushr 32 == 0xFF.toLong())
        assertTrue(LetterPurple.value.toLong() ushr 32 == 0xFF.toLong())
    }
}
