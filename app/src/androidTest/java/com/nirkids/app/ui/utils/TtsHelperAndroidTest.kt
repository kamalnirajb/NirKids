package com.nirkids.app.ui.utils

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TtsHelperAndroidTest {

    private lateinit var context: Context
    private lateinit var ttsHelper: TtsHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        ttsHelper = TtsHelper(context)
    }

    @Test
    fun TtsHelper_initializes_without_crash() {
        assertTrue(ttsHelper::class.java.simpleName == "TtsHelper")
    }

    @Test
    fun speak_does_not_crash_on_init() {
        // May not speak if TTS engine is not available
        ttsHelper.speak("test")
        // Just ensure it doesn't crash
    }

    @Test
    fun stop_is_callable() {
        ttsHelper.stop()
        // Ensure no crash
    }

    @Test
    fun setPitch_does_not_crash() {
        ttsHelper.setPitch(1.5f)
    }

    @Test
    fun setSpeechRate_does_not_crash() {
        ttsHelper.setSpeechRate(0.5f)
    }
}
