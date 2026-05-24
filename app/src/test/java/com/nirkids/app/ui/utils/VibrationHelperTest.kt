package com.nirkids.app.ui.utils

import android.content.Context
import android.os.Vibrator
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class VibrationHelperTest {

    @Test
    fun `vibrate method can be called without crash`() {
        val mockContext = mockk<Context>()
        val mockVibrator = mockk<Vibrator>(relaxed = true)
        every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
        
        val helper = VibrationHelper(mockContext)
        // Should not throw
        helper.vibrate(10)
    }
}
