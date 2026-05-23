package com.nirkids.tracevalidator

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class TraceValidatorTest {

    @Before
    fun setup() {
        TraceValidator.initialize(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `initialize sets up TraceValidator instance`() {
        val instance = TraceManager.getInstance()
        assertNotNull(instance)
    }

    @Test
    fun `logEvent adds entry to audit log`() {
        TraceValidator.clearAll()
        TraceValidator.logEvent("test_event", mapOf("key" to "value"))

        val log = TraceValidator.getAuditLog()
        assertEquals(1, log.size)
        assertEquals("test_event", log[0].event)
        assertEquals("value", log[0].details["key"])
    }

    @Test
    fun `logEventWithSeverity stores correct severity`() {
        TraceValidator.clearAll()
        TraceValidator.logEventWithSeverity(
            "error_event",
            AuditEntry.Severity.ERROR,
            mapOf("detail" to "something failed")
        )

        val log = TraceValidator.getAuditLog()
        assertEquals(1, log.size)
        assertEquals(AuditEntry.Severity.ERROR, log[0].severity)
    }

    @Test
    fun `startTrace creates a new trace`() {
        val trace = TraceValidator.startTrace("test_trace", mapOf("type" to "alphabet"))

        assertNotNull(trace)
        assertEquals("test_trace", trace.eventType)
        assertTrue(trace.startTime > 0)
        assertEquals(TraceEntry.Status.PENDING, trace.status)
        assertEquals("alphabet", trace.data["type"])
    }

    @Test
    fun `completeTrace marks trace as success`() {
        val trace = TraceValidator.startTrace("test_trace")
        TraceValidator.completeTrace(trace)

        val traces = TraceValidator.getTraces()
        assertEquals(1, traces.size)
        assertEquals(TraceEntry.Status.SUCCESS, traces[0].status)
        assertTrue(traces[0].durationMs > 0)
    }

    @Test
    fun `failTrace marks trace as failed`() {
        val trace = TraceValidator.startTrace("test_trace")
        TraceValidator.failTrace(trace, "timeout")

        val traces = TraceValidator.getTraces()
        assertEquals(1, traces.size)
        assertEquals(TraceEntry.Status.FAILED, traces[0].status)

        val auditLog = TraceValidator.getAuditLog()
        val failedEvent = auditLog.find { it.event == "trace_failed" }
        assertNotNull(failedEvent)
        assertEquals("timeout", failedEvent!!.details["reason"])
    }

    @Test
    fun `registerRules and validate works correctly`() {
        val rules = listOf(
            ValidatorRule(
                name = "test_rule",
                description = "Test validation rule",
                severity = AuditEntry.Severity.INFO,
                category = "test"
            ) { data ->
                (data["value"] as? Int) == 42
            }
        )

        TraceValidator.registerRules("test_event", rules)

        val passing = TraceValidator.validate("test_event", mapOf("value" to 42))
        assertTrue(passing.allPassing)
        assertTrue(passing.validationResults.all { it.isPassing })

        val failing = TraceValidator.validate("test_event", mapOf("value" to 99))
        assertFalse(failing.allPassing)
        assertFalse(failing.validationResults.any { it.isPassing })
    }

    @Test
    fun `getAuditByEvent filters correctly`() {
        TraceValidator.clearAll()
        TraceValidator.logEvent("letter_tapped", mapOf("letter" to "A"))
        TraceValidator.logEvent("parent_gate_verified", emptyMap())
        TraceValidator.logEvent("letter_learned", mapOf("letter" to "B"))

        val letterEvents = TraceValidator.getAuditByEvent("letter_tapped")
        assertEquals(1, letterEvents.size)

        val allEvents = TraceValidator.getAuditLog()
        assertEquals(3, allEvents.size)
    }

    @Test
    fun `getAuditBySeverity filters correctly`() {
        TraceValidator.clearAll()
        TraceValidator.logEvent("info_event")
        TraceValidator.logEventWithSeverity("error_event", AuditEntry.Severity.ERROR)

        val errors = TraceValidator.getAuditBySeverity(AuditEntry.Severity.ERROR)
        assertEquals(1, errors.size)
        assertEquals("error_event", errors[0].event)
    }

    @Test
    fun `clearAll clears audit log and traces`() {
        TraceValidator.clearAll()
        TraceValidator.logEvent("test_event")
        TraceValidator.startTrace("test_trace")

        TraceValidator.clearAll()

        assertTrue(TraceValidator.getAuditLog().isEmpty())
        assertTrue(TraceValidator.getTraces().isEmpty())
    }

    @Test
    fun `exportAuditLog returns valid JSON format`() {
        TraceValidator.clearAll()
        TraceValidator.logEvent("test", mapOf("key" to "val"))

        val json = TraceValidator.exportAuditLog()
        assertTrue(json.contains("[") || json.contains("]"))
        assertTrue(json.contains("test"))
        assertTrue(json.contains("key"))
    }

    @Test
    fun `exportTraces returns valid JSON format`() {
        val trace = TraceValidator.startTrace("test_trace")
        val json = TraceValidator.exportTraces()

        assertTrue(json.contains("[") || json.contains("]"))
        assertTrue(json.contains(trace.traceId))
    }

    @Test
    fun `audit entry has non-zero timestamp`() {
        TraceValidator.clearAll()
        val before = System.currentTimeMillis()
        TraceValidator.logEvent("timestamp_test")
        val after = System.currentTimeMillis()

        val entry = TraceValidator.getAuditLog().first()
        assertTrue(entry.timestamp in before..after)
    }

    @Test
    fun `multiple events create separate entries`() {
        TraceValidator.clearAll()
        for (i in 1..10) {
            TraceValidator.logEvent("event_$i")
        }

        val log = TraceValidator.getAuditLog()
        assertEquals(10, log.size)
    }

    @Test
    fun `validate with no rules returns all passing`() {
        TraceValidator.clearAll()
        val result = TraceValidator.validate("unknown_event", mapOf())

        assertTrue(result.allPassing)
        assertTrue(result.validationResults.isEmpty())
    }

    @Test
    fun `TraceResult has correct traceId`() {
        TraceValidator.registerRules("test", emptyList())
        val result = TraceValidator.validate("test", mapOf())

        assertNotNull(result.traceId)
        assertTrue(result.traceId.startsWith("TRACE_"))
    }
}
