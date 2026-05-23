package com.nirkids.app.util

import com.nirkids.app.domain.model.Alphabet

object AlphabetUtils {

    private val VOWELS = setOf('A', 'E', 'I', 'O', 'U')

    /**
     * Determines if a letter is a vowel.
     */
    fun isVowel(letter: Char): Boolean {
        return VOWELS.contains(letter.uppercaseChar())
    }

    /**
     * Returns the phonetic representation of a letter (Basic implementation).
     */
    fun getBasicPhonetic(letter: Char): String {
        return when (letter.uppercaseChar()) {
            'A' -> "æ"
            'B' -> "be"
            'C' -> "si"
            'D' -> "di"
            'E' -> "i"
            'F' -> "ef"
            'G' -> "dʒi"
            'H' -> "eɪtʃ"
            'I' -> "aɪ"
            'J' -> "dʒeɪ"
            'K' -> "keɪ"
            'L' -> "el"
            'M' -> "em"
            'N' -> "en"
            'O' -> "oʊ"
            'P' -> "pi"
            'Q' -> "kju"
            'R' -> "ɑr"
            'S' -> "es"
            'T' -> "ti"
            'U' -> "ju"
            'V' -> "vi"
            'W' -> "ˈdʌbəl.ju"
            'X' -> "eks"
            'Y' -> "waɪ"
            'Z' -> "zi"
            else -> letter.toString()
        }
    }

    /**
     * Formats progress as a percentage string.
     */
    fun formatProgress(learned: Int, total: Int): String {
        if (total == 0) return "0%"
        val percentage = (learned.toFloat() / total * 100).toInt()
        return "$percentage%"
    }
}
