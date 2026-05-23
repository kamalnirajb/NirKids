package com.nirkids.app.domain.model

data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
) {
    val displayName: String get() = "$letter — $exampleWord"
    val phoneticBreakdown: List<String> get() = phonetic.split("/")
        .filter { it.isNotBlank() }
        .map { it.trim() }
}
