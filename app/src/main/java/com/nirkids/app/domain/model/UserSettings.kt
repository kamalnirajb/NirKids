package com.nirkids.app.domain.model

data class UserSettings(
    val isMusicEnabled: Boolean = true,
    val isSoundEffectsEnabled: Boolean = true,
    val isTtsEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val preferredVoicePitch: Float = 1.0f,
    val preferredVoiceRate: Float = 1.0f
)
