package com.nirkids.app

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class NirKidsAppTest {

    @Test
    fun `application class instantiates correctly`() {
        val app = RuntimeEnvironment.getApplication() as NirKidsApp
        assertNotNull(app)
    }

    @Test
    fun `application has valid context`() {
        val app = RuntimeEnvironment.getApplication()
        assertNotNull(app.packageName)
    }
}
