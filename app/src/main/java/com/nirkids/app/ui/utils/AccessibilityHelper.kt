package com.nirkids.app.ui.utils

import com.nirkids.app.domain.model.Alphabet
import androidx.compose.ui.graphics.Color
import com.nirkids.app.ui.theme.VowelColor
import com.nirkids.app.ui.theme.ConsonantColor

object AccessibilityHelper {

    fun getLetterBackgroundColor(isVowel: Boolean): Color {
        return if (isVowel) VowelColor else ConsonantColor
    }

    fun getPhoneticVisualAid(alphabet: Alphabet): String {
        return buildString {
            append("Letter: ${alphabet.letter}\n")
            append("Sound: ${alphabet.phonetic}\n")
            append("Word: ${alphabet.exampleWord}\n")
            append("Starts with: ${alphabet.letter}\n")
            append("Vowel: ${if (alphabet.isVowel) "Yes" else "No"}")
        }
    }

    fun getSignLanguageHint(letter: Char): String {
        return when (letter) {
            'A' -> "✊ Thumb tucked in"
            'B' -> "✋ Four fingers up, thumb across palm"
            'C' -> "🤏 C-shape hand"
            'D' -> "☝️ Index finger up, others curled"
            'E' -> "🤞 Four fingers down, thumb tucked"
            'F' -> "👌 OK sign"
            'I' -> "🤙 Pinky up"
            'L' -> "🤘 L-shape"
            'O' -> "👌 O-shape"
            'S' -> "✊ Fist"
            'V' -> "✌️ Index + middle up"
            'Y' -> "🤙 Thumb + pinky out"
            'W' -> "🤟 Index + middle + ring up"
            'T' -> "✊ Thumb between index and middle"
            'K' -> "☝️ Index up + middle out"
            'M' -> "🖐️ Three fingers under thumb"
            'N' -> "✌️ Two fingers under thumb"
            'Z' -> "☝️ Draw Z in air"
            else -> "Refer to ASL chart"
        }
    }

    fun getLetterCategory(alphabet: Alphabet): String {
        return if (alphabet.isVowel) "Vowel (a, e, i, o, u)" else "Consonant"
    }
}
