package com.nirkids.app.ui.utils

import org.junit.Assert.*
import org.junit.Test

class VibrationHelperTest {

    @Test
    fun `vibrate method can be called without crash`() {
        // VibrationHelper may return null vibrator on device without vibration hardware
        // This test verifies the API doesn't throw
        val helper = VibrationHelper(android.content.ContextWrapper(android.content.Context.emptyContext))
        // Should not throw
        helper.vibrate(10)
    }
}
