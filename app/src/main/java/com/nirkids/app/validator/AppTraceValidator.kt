package com.nirkids.app.validator

import com.nirkids.tracevalidator.TraceValidator
import android.content.Context

object AppTraceValidator {

    fun initialize(context: Context) {
        TraceValidator.initialize(context)
    }

    fun logLetterInteraction(letter: Char, action: String) {
        TraceValidator.logEvent(
            "letter_$action",
            mapOf("letter" to letter.toString())
        )
    }

    fun logProgressUpdate(letter: Char, learned: Boolean) {
        TraceValidator.logEvent(
            "progress_updated",
            mapOf("letter" to letter.toString(), "learned" to learned.toString())
        )
    }

    fun logParentGateEvent(status: String, details: Map<String, String> = emptyMap()) {
        TraceValidator.logEvent(
            "parent_gate_$status",
            details.toMutableMap().apply { put("status", status) }
        )
    }

    fun logPronunciationEvent(letter: Char, success: Boolean) {
        TraceValidator.logEvent(
            "pronunciation_${if (success) "correct" else "incorrect"}",
            mapOf("letter" to letter.toString())
        )
    }
}
