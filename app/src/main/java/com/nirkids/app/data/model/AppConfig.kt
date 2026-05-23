package com.nirkids.app.data.model

data class AppConfig(
    val apiBaseUrl: String = "https://api.nirkids.com/",
    val timeoutSeconds: Long = 30,
    val retryCount: Int = 3
)
