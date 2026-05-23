package com.nirkids.app.ui.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context, this)
        tts?.setLanguage(Locale.US)
        tts?.setPitch(1.0f)
        tts?.setSpeechRate(0.8f) // Slower for kids
    }

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nirkids_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }
}
