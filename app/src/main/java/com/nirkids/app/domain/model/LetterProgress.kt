package com.nirkids.app.domain.model

data class LetterProgress(
    val letter: Char,
    val learned: Boolean = false,
    val attempts: Int = 0,
    val masteryLevel: Int = 0
) {
    val masteryPercent: Int
        get() = if (attempts > 0) (masteryLevel * 100 / 10).coerceIn(0, 100) else 0

    val statusText: String
        get() = when {
            learned && masteryLevel >= 3 -> "✅ Mastered"
            learned -> "🌟 Learned"
            masteryLevel >= 2 -> "📚 Almost there"
            masteryLevel >= 1 -> "🔤 Practicing"
            else -> "🆕 New"
        }
}
