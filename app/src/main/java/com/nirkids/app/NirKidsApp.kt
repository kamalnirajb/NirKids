package com.nirkids.app

import android.app.Application
import com.nirkids.app.validator.AppTraceValidator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NirKidsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize TraceValidator
        AppTraceValidator.initialize(this)
    }
}
