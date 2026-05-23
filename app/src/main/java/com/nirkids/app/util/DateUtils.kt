package com.nirkids.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_FORMAT = "dd MMM yyyy, HH:mm"
    private val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    /**
     * Formats a timestamp into a readable date string.
     */
    fun formatTimestamp(timestamp: Long): String {
        return formatter.format(Date(timestamp))
    }

    /**
     * Gets the current timestamp.
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}
