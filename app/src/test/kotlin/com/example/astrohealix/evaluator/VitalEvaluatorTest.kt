package com.example.astrohealix.evaluator

import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.DataSource
import com.example.astrohealix.model.HazardId
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VitalEvaluatorTest {

    @Test
    fun missingOrDisconnectedTelemetry_resolvesToUnavailable_andNeverCoercedToZeroOrCritical() {
        val disconnected = TelemetryPoint(
            timestampMs = 1000L,
            timeLabel = "Now",
            vitalId = VitalId.RADIATION_MSV,
            value = null,
            source = DataSource.SENSOR,
            isDisconnected = true
        )

        val result = VitalEvaluator.evaluate(VitalId.RADIATION_MSV, disconnected)

        assertEquals(VitalStatus.UNAVAILABLE, result.status)
        assertEquals("DATA UNAVAILABLE", result.displayValue)
        assertEquals("Sensor Disconnected", result.deltaFromBaseline)
        assertNull(result.deltaPercent)
    }

    @Test
    fun baselineDeltaRule52_triggersMonitorAndCritical_onHeartRateSpikes() {
        val history = listOf(
            TelemetryPoint(100L, "15m ago", VitalId.HEART_RATE, 72f, source = DataSource.SENSOR),
            TelemetryPoint(200L, "10m ago", VitalId.HEART_RATE, 75f, source = DataSource.SENSOR),
            TelemetryPoint(300L, "5m ago", VitalId.HEART_RATE, 88f, source = DataSource.SENSOR)
        )

        // Monitor Spike (16 bpm above upper baseline 78 -> 94 bpm)
        val monitorSpike = VitalEvaluator.evaluate(
            VitalId.HEART_RATE,
            TelemetryPoint(400L, "Now", VitalId.HEART_RATE, 94f, source = DataSource.SENSOR),
            history
        )
        assertEquals(VitalStatus.MONITOR, monitorSpike.status)
        assertEquals("Baseline Delta Rule 5.2", monitorSpike.ruleApplied)

        // Critical Spike (>= 100 bpm)
        val criticalSpike = VitalEvaluator.evaluate(
            VitalId.HEART_RATE,
            TelemetryPoint(500L, "Now", VitalId.HEART_RATE, 102f, source = DataSource.SENSOR),
            history
        )
        assertEquals(VitalStatus.CRITICAL, criticalSpike.status)
        assertEquals("Baseline Delta Rule 5.2", criticalSpike.ruleApplied)
    }

    @Test
    fun sleepDriftRule_escalatesToCritical_after3ConsecutiveCyclesBelow6Hours() {
        val history = listOf(
            TelemetryPoint(100L, "2d ago", VitalId.SLEEP_HOURS, 5.5f, source = DataSource.MANUAL),
            TelemetryPoint(200L, "1d ago", VitalId.SLEEP_HOURS, 5.2f, source = DataSource.MANUAL),
            TelemetryPoint(300L, "Now", VitalId.SLEEP_HOURS, 5.3f, source = DataSource.MANUAL)
        )

        val evalSleep = VitalEvaluator.evaluate(VitalId.SLEEP_HOURS, history[2], history)

        assertEquals(VitalStatus.CRITICAL, evalSleep.status)
        assertTrue(evalSleep.ruleApplied?.contains("3 Consecutive Cycles") == true)
    }

    @Test
    fun vitalSnapshots_autoSortsCritical_thenMonitor_thenNormal_thenUnavailable() {
        val snapshots = VitalEvaluator.buildSnapshots(
            latestByVital = mapOf(
                VitalId.HEART_RATE to TelemetryPoint(100L, "Now", VitalId.HEART_RATE, 72f),
                VitalId.SPO2 to TelemetryPoint(100L, "Now", VitalId.SPO2, 89f), // Critical
                VitalId.SLEEP_HOURS to TelemetryPoint(100L, "Now", VitalId.SLEEP_HOURS, 5.4f), // Monitor
                VitalId.RADIATION_MSV to TelemetryPoint(100L, "Now", VitalId.RADIATION_MSV, null, isDisconnected = true) // Unavailable
            ),
            historyByVital = emptyMap(),
            environment = MissionEnvironment.ORBITAL_STATION
        )

        assertEquals(VitalId.SPO2, snapshots[0].id)
        assertEquals(VitalStatus.CRITICAL, snapshots[0].status)

        assertEquals(VitalId.SLEEP_HOURS, snapshots[1].id)
        assertEquals(VitalStatus.MONITOR, snapshots[1].status)

        assertEquals(VitalStatus.UNAVAILABLE, snapshots.last().status)
    }

    @Test
    fun environmentShift_increasesDeepSpaceHazardScores() {
        val snapshots = VitalEvaluator.buildSnapshots(
            latestByVital = mapOf(
                VitalId.HEART_RATE to TelemetryPoint(100L, "Now", VitalId.HEART_RATE, 72f),
                VitalId.RADIATION_MSV to TelemetryPoint(100L, "Now", VitalId.RADIATION_MSV, 0.7f)
            ),
            historyByVital = emptyMap(),
            environment = MissionEnvironment.ORBITAL_STATION
        )

        val leoHazards = HazardEvaluator.computeHazards(snapshots, MissionEnvironment.ORBITAL_STATION, false)
        val transitHazards = HazardEvaluator.computeHazards(snapshots, MissionEnvironment.DEEP_SPACE_TRANSIT, false)

        val leoDist = leoHazards.find { it.id == HazardId.DISTANCE_DELAY }!!.score
        val transitDist = transitHazards.find { it.id == HazardId.DISTANCE_DELAY }!!.score

        assertTrue("Transit distance hazard ($transitDist) should exceed LEO ($leoDist)", transitDist > leoDist)
        assertTrue(NON_DIAGNOSTIC_DISCLAIMER.contains("does not replace"))
    }
}
