package com.nirkids.app.ui.utils

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.ui.theme.VowelColor
import com.nirkids.app.ui.theme.ConsonantColor
import org.junit.Assert.*
import org.junit.Test

class AccessibilityHelperTest {

    @Test
    fun `getLetterBackgroundColor returns VowelColor for vowels`() {
        val alphabet = Alphabet('A', "/æ/", "Apple", "🍎", true)
        val color = AccessibilityHelper.getLetterBackgroundColor(alphabet.isVowel)
        assertEquals(VowelColor, color)
    }

    @Test
    fun `getLetterBackgroundColor returns ConsonantColor for consonants`() {
        val alphabet = Alphabet('B', "/b/", "Ball", "⚽", false)
        val color = AccessibilityHelper.getLetterBackgroundColor(alphabet.isVowel)
        assertEquals(ConsonantColor, color)
    }

    @Test
    fun `getPhoneticVisualAid returns all fields`() {
        val alphabet = Alphabet('A', "/æ/", "Apple", "🍎", true)
        val aid = AccessibilityHelper.getPhoneticVisualAid(alphabet)

        assertTrue(aid.contains("Letter: A"))
        assertTrue(aid.contains("Sound: /æ/"))
        assertTrue(aid.contains("Word: Apple"))
        assertTrue(aid.contains("Starts with: A"))
        assertTrue(aid.contains("Vowel: Yes"))
    }

    @Test
    fun `getSignLanguageHint returns hint for known letters`() {
        val hints = listOf('A', 'B', 'C', 'D', 'E', 'F', 'I', 'L', 'O', 'S', 'V', 'Y', 'W', 'T', 'K', 'M', 'N', 'Z')
        for (letter in hints) {
            val hint = AccessibilityHelper.getSignLanguageHint(letter)
            assertNotNull(hint)
            assertTrue(hint.isNotEmpty())
        }
    }

    @Test
    fun `getSignLanguageHint returns fallback for unknown letters`() {
        val hint = AccessibilityHelper.getSignLanguageHint('X')
        assertTrue(hint.contains("ASL chart"))
    }

    @Test
    fun `getLetterCategory returns correct category`() {
        val vowel = Alphabet('A', "/æ/", "Apple", "🍎", true)
        assertEquals("Vowel (a, e, i, o, u)", AccessibilityHelper.getLetterCategory(vowel))

        val consonant = Alphabet('B', "/b/", "Ball", "⚽", false)
        assertEquals("Consonant", AccessibilityHelper.getLetterCategory(consonant))
    }
}
