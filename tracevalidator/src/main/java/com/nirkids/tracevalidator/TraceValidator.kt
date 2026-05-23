package com.nirkids.tracevalidator

import java.util.concurrent.atomic.AtomicInteger

/**
 * TraceValidator — A custom audit/validation framework that tracks data changes
 * and provides a comprehensive audit trail.
 *
 * Used across the NirKids app to log:
 * - Letter interactions (tapped, learned, pronounced)
 * - Parent gate attempts (success/failure/lock)
 * - Progress milestones
 * - App lifecycle events
 */
public class AuditEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val event: String,
    val details: Map<String, String> = emptyMap(),
    val severity: Severity = Severity.INFO,
    val category: String = "general",
    val deviceInfo: DeviceInfo? = null
) {
    enum class Severity { DEBUG, INFO, WARNING, ERROR, CRITICAL }

    data class DeviceInfo(
        val appId: String,
        val versionName: String,
        val deviceModel: String,
        val androidVersion: Int
    )
}

/**
 * TraceEntry represents a single traced event with timing.
 */
public data class TraceEntry(
    val traceId: String,
    val eventType: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val durationMs: Long = 0L,
    val data: Map<String, String> = emptyMap(),
    val status: Status = Status.PENDING
) {
    enum class Status { PENDING, SUCCESS, FAILED, TIMEOUT }
}

/**
 * ValidatorRule — Defines a validation rule that can be applied to data.
 */
public class ValidatorRule(
    val name: String,
    val description: String,
    val severity: AuditEntry.Severity = AuditEntry.Severity.INFO,
    val category: String = "validation",
    private val predicate: (Map<String, Any>) -> Boolean
) {
    fun validate(data: Map<String, Any>): ValidationResult {
        return ValidationResult(
            ruleName = name,
            isPassing = predicate(data),
            severity = severity,
            timestamp = System.currentTimeMillis()
        )
    }
}

/**
 * ValidationResult holds the outcome of a validation check.
 */
public data class ValidationResult(
    val ruleName: String,
    val isPassing: Boolean,
    val severity: AuditEntry.Severity = AuditEntry.Severity.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * TraceResult aggregates validation outcomes for a specific event.
 */
public data class TraceResult(
    val traceId: String,
    val eventType: String,
    val validationResults: List<ValidationResult>,
    val allPassing: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * TraceManager — Central manager for all traces and audits.
 */
public class TraceManager internal constructor(
    context: android.content.Context
) {
    private val auditLog = mutableListOf<AuditEntry>()
    private val traceStore = mutableListOf<TraceEntry>()
    private val validationRules = mutableMapOf<String, List<ValidatorRule>>()

    init {
        loadDefaultRules()
    }

    companion object {
        private var INSTANCE: TraceManager? = null
        private val traceSequence = AtomicInteger(0)

        @JvmStatic
        fun getInstance(): TraceManager? = INSTANCE

        @JvmStatic
        fun initialize(context: android.content.Context) {
            if (INSTANCE == null) {
                INSTANCE = TraceManager(context.applicationContext)
            }
        }

        @JvmStatic
        fun getTraceId(): String = "TRACE_${System.currentTimeMillis()}_${traceSequence.incrementAndGet()}"
    }

    /**
     * Log a simple audit event.
     */
    fun logEvent(event: String, details: Map<String, String> = emptyMap()) {
        val entry = AuditEntry(
            timestamp = System.currentTimeMillis(),
            event = event,
            details = details,
            severity = AuditEntry.Severity.INFO,
            category = determineCategory(event)
        )
        synchronized(auditLog) {
            auditLog.add(entry)
        }
    }

    /**
     * Log an audit event with custom severity.
     */
    fun logEventWithSeverity(
        event: String,
        severity: AuditEntry.Severity,
        details: Map<String, String> = emptyMap()
    ) {
        val entry = AuditEntry(
            event = event,
            severity = severity,
            details = details,
            category = determineCategory(event)
        )
        synchronized(auditLog) {
            auditLog.add(entry)
        }
    }

    /**
     * Start tracing an operation.
     */
    fun startTrace(eventType: String, data: Map<String, String> = emptyMap()): TraceEntry {
        val trace = TraceEntry(
            traceId = getTraceId(),
            eventType = eventType,
            startTime = System.currentTimeMillis(),
            data = data,
            status = TraceEntry.Status.PENDING
        )
        synchronized(traceStore) {
            traceStore.add(trace)
        }
        logEvent("trace_started", mapOf("traceId" to trace.traceId, "type" to eventType))
        return trace
    }

    /**
     * Complete a trace successfully.
     */
    fun completeTrace(trace: TraceEntry) {
        val endTime = System.currentTimeMillis()
        val completed = trace.copy(
            endTime = endTime,
            durationMs = endTime - trace.startTime,
            status = TraceEntry.Status.SUCCESS
        )
        synchronized(traceStore) {
            val idx = traceStore.indexOfFirst { it.traceId == trace.traceId }
            if (idx >= 0) traceStore[idx] = completed
        }
    }

    /**
     * Mark a trace as failed.
     */
    fun failTrace(trace: TraceEntry, reason: String) {
        val failed = trace.copy(
            endTime = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - trace.startTime,
            status = TraceEntry.Status.FAILED
        )
        synchronized(traceStore) {
            val idx = traceStore.indexOfFirst { it.traceId == trace.traceId }
            if (idx >= 0) traceStore[idx] = failed
        }
        logEvent("trace_failed", mapOf("traceId" to trace.traceId, "reason" to reason))
    }

    /**
     * Register validation rules for a specific event type.
     */
    fun registerRules(eventType: String, rules: List<ValidatorRule>) {
        synchronized(validationRules) {
            validationRules[eventType] = rules
        }
        logEvent("rules_registered", mapOf("eventType" to eventType, "count" to rules.size.toString()))
    }

    /**
     * Validate data against registered rules for an event type.
     */
    fun validate(eventType: String, data: Map<String, Any>): TraceResult {
        val results = mutableListOf<ValidationResult>()
        synchronized(validationRules) {
            validationRules[eventType]?.forEach { rule ->
                results.add(rule.validate(data))
            }
        }
        val traceId = getTraceId()
        return TraceResult(
            traceId = traceId,
            eventType = eventType,
            validationResults = results,
            allPassing = results.all { it.isPassing }
        )
    }

    /**
     * Get all audit entries (for export or display).
     */
    fun getAuditLog(): List<AuditEntry> {
        synchronized(auditLog) {
            return auditLog.toList()
        }
    }

    /**
     * Get all traces.
     */
    fun getTraces(): List<TraceEntry> {
        synchronized(traceStore) {
            return traceStore.toList()
        }
    }

    /**
     * Get audit entries filtered by event type.
     */
    fun getAuditByEvent(event: String): List<AuditEntry> {
        synchronized(auditLog) {
            return auditLog.filter { it.event == event }
        }
    }

    /**
     * Get audit entries filtered by severity.
     */
    fun getAuditBySeverity(severity: AuditEntry.Severity): List<AuditEntry> {
        synchronized(auditLog) {
            return auditLog.filter { it.severity == severity }
        }
    }

    /**
     * Clear all audit data.
     */
    fun clearAll() {
        synchronized(auditLog) { auditLog.clear() }
        synchronized(traceStore) { traceStore.clear() }
    }

    /**
     * Export audit log as JSON string.
     */
    fun exportAuditLog(): String {
        val entries = synchronized(auditLog) { auditLog.toList() }
        val sb = StringBuilder()
        sb.appendLine("[")
        entries.forEachIndexed { i, entry ->
            sb.append("  {")
            sb.append("\"timestamp\":${entry.timestamp},")
            sb.append("\"event\":\"${entry.event}\",")
            sb.append("\"severity\":\"${entry.severity}\",")
            sb.append("\"category\":\"${entry.category}\",")
            sb.append("\"details\":{")
            val detailList = entry.details.entries.joinToString(",") { (k, v) -> "\"$k\":\"$v\"" }
            sb.append(detailList)
            sb.append("}}")
            if (i < entries.size - 1) sb.append(",")
            sb.appendLine()
        }
        sb.appendLine("]")
        return sb.toString()
    }

    /**
     * Export traces as JSON string.
     */
    fun exportTraces(): String {
        val entries = synchronized(traceStore) { traceStore.toList() }
        val sb = StringBuilder()
        sb.appendLine("[")
        entries.forEachIndexed { i, entry ->
            sb.append("  {")
            sb.append("\"traceId\":\"${entry.traceId}\",")
            sb.append("\"eventType\":\"${entry.eventType}\",")
            sb.append("\"startTime\":${entry.startTime},")
            sb.append("\"endTime\":${entry.endTime},")
            sb.append("\"durationMs\":${entry.durationMs},")
            sb.append("\"status\":\"${entry.status}\"")
            sb.append("}")
            if (i < entries.size - 1) sb.append(",")
            sb.appendLine()
        }
        sb.appendLine("]")
        return sb.toString()
    }

    /**
     * Determine category from event name.
     */
    private fun determineCategory(event: String): String {
        return when {
            event.startsWith("letter") -> "alphabet"
            event.startsWith("parent_gate") -> "parent_gate"
            event.startsWith("pronunciation") -> "pronunciation"
            event.startsWith("progress") -> "progress"
            event.startsWith("trace_") -> "trace"
            event.startsWith("app_") -> "app_lifecycle"
            event.startsWith("rules_") -> "validation"
            else -> "general"
        }
    }

    /**
     * Load default validation rules for all event types.
     */
    private fun loadDefaultRules() {
        val letterRules = listOf(
            ValidatorRule(
                name = "letter_range",
                description = "Letter must be A-Z",
                severity = AuditEntry.Severity.ERROR,
                category = "alphabet"
            ) { data ->
                val letter = data["letter"]?.toString()
                letter != null && letter.length == 1 && letter.uppercase() in "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            },
            ValidatorRule(
                name = "phonetic_present",
                description = "Phonetic must not be empty",
                severity = AuditEntry.Severity.WARNING,
                category = "alphabet"
            ) { data ->
                val phonetic = data["phonetic"]?.toString()
                !phonetic.isNullOrBlank()
            }
        )

        val progressRules = listOf(
            ValidatorRule(
                name = "attempts_non_negative",
                description = "Attempts must be >= 0",
                severity = AuditEntry.Severity.ERROR,
                category = "progress"
            ) { data ->
                val attempts = data["attempts"] as? Int
                attempts == null || attempts >= 0
            },
            ValidatorRule(
                name = "mastery_level_range",
                description = "Mastery level must be 0-5",
                severity = AuditEntry.Severity.WARNING,
                category = "progress"
            ) { data ->
                val mastery = data["mastery_level"] as? Int
                mastery == null || mastery in 0..5
            }
        )

        val parentGateRules = listOf(
            ValidatorRule(
                name = "answer_is_number",
                description = "Answer must be a valid number",
                severity = AuditEntry.Severity.ERROR,
                category = "parent_gate"
            ) { data ->
                val answer = data["answer"]?.toString()
                answer != null && answer.toIntOrNull() != null
            }
        )

        validationRules["alphabet"] = letterRules
        validationRules["progress"] = progressRules
        validationRules["parent_gate"] = parentGateRules
    }
}

/**
 * Convenience object for easy access to TraceValidator functionality.
 */
object TraceValidator {

    @JvmStatic
    fun initialize(context: android.content.Context) {
        TraceManager.initialize(context)
    }

    @JvmStatic
    fun logEvent(event: String, details: Map<String, String> = emptyMap()) {
        TraceManager.getInstance()?.logEvent(event, details)
    }

    @JvmStatic
    fun logEventWithSeverity(event: String, severity: AuditEntry.Severity, details: Map<String, String> = emptyMap()) {
        TraceManager.getInstance()?.logEventWithSeverity(event, severity, details)
    }

    @JvmStatic
    fun startTrace(eventType: String, data: Map<String, String> = emptyMap()): TraceEntry {
        return TraceManager.getInstance()?.startTrace(eventType, data)
            ?: throw IllegalStateException("TraceValidator not initialized")
    }

    @JvmStatic
    fun completeTrace(trace: TraceEntry) {
        TraceManager.getInstance()?.completeTrace(trace)
    }

    @JvmStatic
    fun failTrace(trace: TraceEntry, reason: String) {
        TraceManager.getInstance()?.failTrace(trace, reason)
    }

    @JvmStatic
    fun registerRules(eventType: String, rules: List<ValidatorRule>) {
        TraceManager.getInstance()?.registerRules(eventType, rules)
    }

    @JvmStatic
    fun validate(eventType: String, data: Map<String, Any>): TraceResult {
        return TraceManager.getInstance()?.validate(eventType, data)
            ?: throw IllegalStateException("TraceValidator not initialized")
    }

    @JvmStatic
    fun getAuditLog(): List<AuditEntry> {
        return TraceManager.getInstance()?.getAuditLog() ?: emptyList()
    }

    @JvmStatic
    fun getTraces(): List<TraceEntry> {
        return TraceManager.getInstance()?.getTraces() ?: emptyList()
    }

    @JvmStatic
    fun clearAll() {
        TraceManager.getInstance()?.clearAll()
    }

    @JvmStatic
    fun exportAuditLog(): String {
        return TraceManager.getInstance()?.exportAuditLog() ?: "[]"
    }

    @JvmStatic
    fun exportTraces(): String {
        return TraceManager.getInstance()?.exportTraces() ?: "[]"
    }
}
