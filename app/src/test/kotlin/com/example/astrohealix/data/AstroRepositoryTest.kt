package com.example.astrohealix.data

import com.example.astrohealix.model.AppThemeMode
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.SimulationScenario
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AstroRepositoryTest {

    @Test
    fun initialState_exposesDemoDataAstronautIdAndDisclaimer() {
        val repo = AstroRepository()
        val state = repo.uiState.value

        assertTrue(state.isDemoData)
        assertEquals("#A-8842", state.astronaut.id)
        assertEquals(180, state.astronaut.missionDurationDays)
        assertEquals(NON_DIAGNOSTIC_DISCLAIMER, state.disclaimer)
        assertEquals(VitalId.entries.size, state.vitals.size)
    }

    @Test
    fun logManualVital_suppressesImmediateDuplicate_unlessAdditiveHydration() {
        val repo = AstroRepository()
        val initialCount = repo.uiState.value.recentLogs.size

        // Non-additive identical submission within 5s is suppressed
        repo.logManualVital(
            vitalId = VitalId.BODY_TEMP,
            value = 36.9f,
            isAdditive = false,
            note = "First temp check"
        )
        assertEquals(initialCount + 1, repo.uiState.value.recentLogs.size)

        repo.logManualVital(
            vitalId = VitalId.BODY_TEMP,
            value = 36.9f,
            isAdditive = false,
            note = "Duplicate temp check"
        )
        assertEquals(initialCount + 1, repo.uiState.value.recentLogs.size)
        assertFalse(repo.uiState.value.recentLogs.any { it.notes == "Duplicate temp check" })

        // Distinct value is accepted
        repo.logManualVital(
            vitalId = VitalId.BODY_TEMP,
            value = 37.1f,
            isAdditive = false,
            note = "Updated temp check"
        )
        assertEquals(initialCount + 2, repo.uiState.value.recentLogs.size)

        // Additive hydration entries accumulate and are NOT suppressed
        val beforeHydrationCount = repo.uiState.value.recentLogs.size
        val startHydration = repo.uiState.value.vitals
            .first { it.id == VitalId.HYDRATION_ML }
            .value ?: 0f

        repo.logManualVital(
            vitalId = VitalId.HYDRATION_ML,
            value = 250f,
            isAdditive = true,
            note = "Pouch A"
        )
        repo.logManualVital(
            vitalId = VitalId.HYDRATION_ML,
            value = 250f,
            isAdditive = true,
            note = "Pouch B"
        )

        val afterState = repo.uiState.value
        assertEquals(beforeHydrationCount + 2, afterState.recentLogs.size)
        val finalHydration = afterState.vitals.first { it.id == VitalId.HYDRATION_ML }.value
        assertEquals(startHydration + 500f, finalHydration)
    }

    @Test
    fun logManualVital_invalidReadingBecomesUnavailable_neverZeroOrCritical() {
        val repo = AstroRepository()

        repo.logManualVital(
            vitalId = VitalId.SPO2,
            value = 12f,
            isAdditive = false,
            note = "Faulty oximeter"
        )

        val spo2Snapshot = repo.uiState.value.vitals.first { it.id == VitalId.SPO2 }
        assertEquals(VitalStatus.UNAVAILABLE, spo2Snapshot.status)
        assertEquals("DATA UNAVAILABLE", spo2Snapshot.displayValue)
        assertEquals(DataQuality.INVALID, spo2Snapshot.qualityStatus)
    }

    @Test
    fun reportEmergencySymptoms_triggersImmediateRule49CriticalAlert() {
        val repo = AstroRepository()

        // Blank report is ignored
        val initialAlerts = repo.uiState.value.alerts.size
        repo.reportEmergencySymptoms(emptyList(), "   ")
        assertEquals(initialAlerts, repo.uiState.value.alerts.size)

        repo.reportEmergencySymptoms(
            symptoms = listOf("Chest pain or pressure", "Severe difficulty breathing"),
            note = "Sudden onset post-ARED"
        )

        val state = repo.uiState.value
        assertTrue(state.emergencyReportedSymptoms.contains("Chest pain or pressure"))
        assertTrue(state.emergencyReportedSymptoms.contains("Severe difficulty breathing"))

        val emergencyAlert = state.alerts.firstOrNull {
            it.ruleApplied == "Rule 4.9 Emergency Symptom Protocol"
        }
        assertNotNull(emergencyAlert)
        assertEquals(VitalStatus.CRITICAL, emergencyAlert!!.severity)
        assertFalse(emergencyAlert.isAcknowledged)
        assertTrue(emergencyAlert.triggerEvent.contains("Chest pain or pressure"))
        assertTrue(emergencyAlert.triggerEvent.contains("Sudden onset post-ARED"))
        assertEquals(emergencyAlert.id, state.alerts.first().id)
    }

    @Test
    fun logWellbeingCheckIn_and_requestConfidentialSupport_updatePrivateState() {
        val repo = AstroRepository()

        repo.logWellbeingCheckIn(
            moodScore = 4,
            stressLevel = 4,
            fatigueLevel = 5,
            notes = "Short sleep cycle"
        )

        val afterCheckIn = repo.uiState.value
        assertEquals(1, afterCheckIn.wellbeingCheckIns.size)
        val checkIn = afterCheckIn.wellbeingCheckIns.first()
        assertEquals(4, checkIn.moodScore)
        assertEquals(4, checkIn.stressLevel)
        assertEquals(5, checkIn.fatigueLevel)
        assertTrue(checkIn.isPrivate)
        assertTrue(checkIn.isSelfReported)

        val moodSnap = afterCheckIn.vitals.first { it.id == VitalId.MOOD_INDEX }
        assertEquals(4f, moodSnap.value)
        assertEquals(VitalStatus.MONITOR, moodSnap.status)
        assertTrue(afterCheckIn.recentLogs.any {
            it.vitalId == VitalId.MOOD_INDEX && it.notes?.contains("[SELF-REPORTED]") == true
        })

        // Confidential support request while offline transitions on reconnect
        repo.toggleOfflineLink()
        repo.requestConfidentialSupport("Flight Psychologist Consult", "Private call requested")
        val offlineReq = repo.uiState.value.supportRequests.first()
        assertEquals("Flight Psychologist Consult", offlineReq.category)
        assertEquals("PRIVATE QUEUED (OFFLINE)", offlineReq.statusLabel)

        repo.reconcileOfflineQueue()
        val syncedReq = repo.uiState.value.supportRequests.first()
        assertEquals("PRIVATE ENCRYPTED UPLINK", syncedReq.statusLabel)
    }

    @Test
    fun updateBaseline_updatesSnapshotStatusImmediately() {
        val repo = AstroRepository()

        val initialHr = repo.uiState.value.vitals.first { it.id == VitalId.HEART_RATE }
        assertEquals(VitalStatus.NORMAL, initialHr.status)

        // Lower critical threshold below nominal HR (~71-74 BPM)
        val strictBaseline = PersonalBaseline(
            nominalMin = 48f,
            nominalMax = 58f,
            monitorLow = 42f,
            monitorHigh = 62f,
            criticalLow = 38f,
            criticalHigh = 65f
        )
        repo.updateBaseline(VitalId.HEART_RATE, strictBaseline)

        val criticalState = repo.uiState.value
        assertEquals(strictBaseline, criticalState.baselines[VitalId.HEART_RATE])
        val updatedHr = criticalState.vitals.first { it.id == VitalId.HEART_RATE }
        assertEquals(VitalStatus.CRITICAL, updatedHr.status)
        assertTrue(criticalState.alerts.any {
            it.vitalId == VitalId.HEART_RATE && it.severity == VitalStatus.CRITICAL
        })

        // Widening baseline back immediately clears critical status
        val relaxedBaseline = PersonalBaseline(
            nominalMin = 55f,
            nominalMax = 88f,
            monitorLow = 48f,
            monitorHigh = 96f,
            criticalLow = 40f,
            criticalHigh = 110f
        )
        repo.updateBaseline(VitalId.HEART_RATE, relaxedBaseline)
        val restoredHr = repo.uiState.value.vitals.first { it.id == VitalId.HEART_RATE }
        assertEquals(VitalStatus.NORMAL, restoredHr.status)
    }

    @Test
    fun exportReportObject_filtersByPeriod_andPreservesUnavailableIndicators() {
        val repo = AstroRepository()

        val report24h = repo.exportReportObject(24)
        assertEquals(NON_DIAGNOSTIC_DISCLAIMER, report24h.disclaimer)
        assertEquals(VitalId.entries.size, report24h.vitals.size)

        // In a 1h window on initial seeded history, manual vitals logged >1h ago (Sleep 4h, Mood 2h, Hydration 70m, Exercise 90m) are missing
        val report1h = repo.exportReportObject(1)
        val sleep1h = report1h.vitals.first { it.id == VitalId.SLEEP_HOURS }
        assertEquals(VitalStatus.UNAVAILABLE, sleep1h.status)
        assertEquals("DATA UNAVAILABLE", sleep1h.displayValue)

        // Sensor disconnect scenario preserves DATA UNAVAILABLE in both report object and CSV
        repo.setScenario(SimulationScenario.SENSOR_DISCONNECT)
        val disconnectReport = repo.exportReportObject(168)
        val radSnap = disconnectReport.vitals.first { it.id == VitalId.RADIATION_MSV }
        assertEquals(VitalStatus.UNAVAILABLE, radSnap.status)
        assertEquals("DATA UNAVAILABLE", radSnap.displayValue)

        val csv = repo.exportReportCsv()
        assertTrue(csv.contains("DEMO DATA (SIMULATED)"))
        assertTrue(csv.contains("DATA UNAVAILABLE"))
        assertTrue(csv.contains(NON_DIAGNOSTIC_DISCLAIMER))
    }

    @Test
    fun setThemeMode_updatesThemeModeStateBetweenSystemDarkAndLight() {
        val repo = AstroRepository()
        assertEquals(AppThemeMode.SYSTEM, repo.uiState.value.themeMode)

        repo.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, repo.uiState.value.themeMode)

        repo.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repo.uiState.value.themeMode)

        repo.setThemeMode(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, repo.uiState.value.themeMode)
    }
}
