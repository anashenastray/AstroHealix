package com.example.astrohealix.data

import com.example.astrohealix.evaluator.HazardEvaluator
import com.example.astrohealix.evaluator.VitalEvaluator
import com.example.astrohealix.model.AppThemeMode
import com.example.astrohealix.model.AstronautProfile
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.ConfidentialSupportRequest
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.HazardScore
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.ManualLogEntry
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.MissionReport
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.SimulationScenario
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.model.WellbeingCheckIn
import com.example.astrohealix.simulator.MissionSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AstroUiState(
    val astronaut: AstronautProfile = AstronautProfile(),
    val environment: MissionEnvironment = MissionEnvironment.ORBITAL_STATION,
    val scenario: SimulationScenario = SimulationScenario.NOMINAL_CRUISE,
    val isOffline: Boolean = false,
    val pendingSyncQueue: List<ManualLogEntry> = emptyList(),
    val lastSyncedAt: String = "Just now",
    val readinessScore: Int = 94,
    val vitals: List<VitalCardSnapshot> = emptyList(),
    val alerts: List<HealthAlert> = emptyList(),
    val hazards: List<HazardScore> = emptyList(),
    val routine: List<RoutineTask> = CatalogData.DEFAULT_ROUTINE,
    val recentLogs: List<ManualLogEntry> = emptyList(),
    val disclaimer: String = NON_DIAGNOSTIC_DISCLAIMER,
    val isDemoData: Boolean = true,
    val hasCompletedOnboarding: Boolean = true,
    val baselines: Map<VitalId, PersonalBaseline> = CatalogData.VITAL_DEFINITIONS.mapValues { it.value.baseline },
    val wellbeingCheckIns: List<WellbeingCheckIn> = emptyList(),
    val supportRequests: List<ConfidentialSupportRequest> = emptyList(),
    val emergencyReportedSymptoms: List<String> = emptyList(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class AstroRepository(
    private val simulator: MissionSimulator = MissionSimulator()
) {
    private data class RecentSubmission(
        val value: Float,
        val secondaryValue: Float?,
        val timestampMs: Long
    )

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val lock = Any()
    private val _uiState = MutableStateFlow(AstroUiState())
    val uiState: StateFlow<AstroUiState> = _uiState.asStateFlow()

    private var astronautProfile = AstronautProfile()
    private var onboardingCompleted = true
    private var activeThemeMode = AppThemeMode.SYSTEM
    private val customBaselines = CatalogData.VITAL_DEFINITIONS.mapValues { it.value.baseline }.toMutableMap()
    private val localLogs = mutableListOf<ManualLogEntry>()
    private val logTimestampsMs = mutableMapOf<String, Long>()
    private val pendingQueue = mutableListOf<ManualLogEntry>()
    private val alertOverrides = mutableMapOf<String, HealthAlert>()
    private val emergencyAlerts = mutableListOf<HealthAlert>()
    private val routineTasks = CatalogData.DEFAULT_ROUTINE.toMutableList()
    private val wellbeingHistory = mutableListOf<WellbeingCheckIn>()
    private val supportRequestsList = mutableListOf<ConfidentialSupportRequest>()
    private val reportedSymptoms = mutableListOf<String>()
    private val lastSubmissions = mutableMapOf<VitalId, RecentSubmission>()

    init {
        val initLogId = "init-hyd-1"
        localLogs.add(
            ManualLogEntry(
                id = initLogId,
                clientMutationId = UUID.randomUUID().toString(),
                vitalId = VitalId.HYDRATION_ML,
                value = 1800f,
                notes = "Morning hydration pouch",
                recordedAt = "1h 10m ago",
                isSynced = true
            )
        )
        logTimestampsMs[initLogId] = System.currentTimeMillis() - 70L * 60_000L
        refreshState()
        startLiveLoop()
    }

    private fun startLiveLoop() {
        scope.launch {
            while (true) {
                delay(3000L)
                synchronized(lock) {
                    simulator.stepTick()
                    refreshStateLocked()
                }
            }
        }
    }

    private fun refreshState() {
        synchronized(lock) {
            refreshStateLocked()
        }
    }

    private fun refreshStateLocked() {
        val env = simulator.getEnvironment()
        val sc = simulator.getScenario()
        val isOffline = sc == SimulationScenario.LINK_BLACKOUT || _uiState.value.isOffline

        val pendingVitals = pendingQueue.map { it.vitalId }.toSet()
        val snapshots = VitalEvaluator.buildSnapshots(
            latestByVital = simulator.getLatestByVital(),
            historyByVital = simulator.historyByVital.mapValues { it.value.toList() },
            environment = env,
            pendingVitalIds = pendingVitals,
            customBaselines = customBaselines
        )

        val rawAlerts = VitalEvaluator.evaluateAlerts(snapshots, env)
        val combinedAlerts = (emergencyAlerts + rawAlerts)
            .distinctBy { it.id }
            .map { a ->
                val override = alertOverrides[a.id]
                if (override != null) {
                    a.copy(
                        isAcknowledged = override.isAcknowledged || a.isAcknowledged,
                        acknowledgedAt = override.acknowledgedAt ?: a.acknowledgedAt,
                        isEscalated = override.isEscalated || a.isEscalated,
                        escalatedAt = override.escalatedAt ?: a.escalatedAt,
                        escalationNote = override.escalationNote ?: a.escalationNote
                    )
                } else {
                    a
                }
            }
            .sortedWith(
                compareBy<HealthAlert> { if (it.severity == VitalStatus.CRITICAL) 0 else 1 }
                    .thenBy { if (it.isAcknowledged) 1 else 0 }
            )

        val hazards = HazardEvaluator.computeHazards(snapshots, env, isOffline)
        val readiness = HazardEvaluator.computeReadiness(snapshots, routineTasks)

        val timeFormatter = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
        val lastSync = if (isOffline) "OFFLINE CACHE" else timeFormatter.format(Date())

        _uiState.update { current ->
            current.copy(
                astronaut = astronautProfile,
                environment = env,
                scenario = sc,
                isOffline = isOffline,
                pendingSyncQueue = pendingQueue.toList(),
                lastSyncedAt = lastSync,
                readinessScore = readiness,
                vitals = snapshots,
                alerts = combinedAlerts,
                hazards = hazards,
                routine = routineTasks.toList(),
                recentLogs = localLogs.toList(),
                isDemoData = true,
                hasCompletedOnboarding = onboardingCompleted,
                baselines = customBaselines.toMap(),
                wellbeingCheckIns = wellbeingHistory.toList(),
                supportRequests = supportRequestsList.toList(),
                emergencyReportedSymptoms = reportedSymptoms.toList(),
                themeMode = activeThemeMode
            )
        }
    }

    fun setScenario(scenario: SimulationScenario) {
        synchronized(lock) {
            simulator.setScenario(scenario)
            if (scenario == SimulationScenario.LINK_BLACKOUT) {
                _uiState.update { it.copy(isOffline = true) }
            } else if (_uiState.value.isOffline) {
                reconcileOfflineQueueLocked()
            }
            refreshStateLocked()
        }
    }

    fun setEnvironment(env: MissionEnvironment) {
        synchronized(lock) {
            simulator.setEnvironment(env)
            refreshStateLocked()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        synchronized(lock) {
            activeThemeMode = mode
            refreshStateLocked()
        }
    }

    fun toggleOfflineLink() {
        synchronized(lock) {
            val nextOffline = !_uiState.value.isOffline
            _uiState.update { it.copy(isOffline = nextOffline) }
            if (!nextOffline) {
                reconcileOfflineQueueLocked()
            }
            refreshStateLocked()
        }
    }

    fun logManualVital(
        vitalId: VitalId,
        value: Float,
        secondaryValue: Float? = null,
        isAdditive: Boolean = false,
        note: String? = null
    ) {
        synchronized(lock) {
            logManualVitalLocked(vitalId, value, secondaryValue, isAdditive, note)
            refreshStateLocked()
        }
    }

    private fun logManualVitalLocked(
        vitalId: VitalId,
        value: Float,
        secondaryValue: Float?,
        isAdditive: Boolean,
        note: String?
    ) {
        val nowMs = System.currentTimeMillis()
        if (!isAdditive) {
            val prevSub = lastSubmissions[vitalId]
            if (prevSub != null &&
                prevSub.value == value &&
                prevSub.secondaryValue == secondaryValue &&
                (nowMs - prevSub.timestampMs) <= 5_000L
            ) {
                return
            }
            lastSubmissions[vitalId] = RecentSubmission(value, secondaryValue, nowMs)
        }

        val isOffline = _uiState.value.isOffline
        val finalValue = if (vitalId == VitalId.HYDRATION_ML && isAdditive) {
            val prev = simulator.getLatestByVital()[VitalId.HYDRATION_ML]?.value ?: 0f
            prev + value
        } else {
            value
        }

        val existingHistory = simulator.historyByVital[vitalId]?.toList() ?: emptyList()
        val quality = VitalEvaluator.validateTelemetry(
            vitalId = vitalId,
            value = finalValue,
            secondaryValue = secondaryValue,
            timestampMs = nowMs,
            nowMs = nowMs,
            isDisconnected = false,
            isEstimated = false,
            existingHistory = existingHistory
        )

        val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date(nowMs))
        val effectiveNote = when (quality) {
            DataQuality.INVALID -> listOfNotNull("[INVALID BOUNDS]", note?.takeIf { it.isNotBlank() }).joinToString(" ")
            DataQuality.UNVERIFIED -> listOfNotNull("[UNVERIFIED]", note?.takeIf { it.isNotBlank() }).joinToString(" ")
            else -> note?.takeIf { it.isNotBlank() }
        }

        val entryId = "log-${UUID.randomUUID()}"
        val entry = ManualLogEntry(
            id = entryId,
            clientMutationId = UUID.randomUUID().toString(),
            vitalId = vitalId,
            value = finalValue,
            secondaryValue = secondaryValue,
            notes = effectiveNote,
            recordedAt = nowStr,
            isSynced = !isOffline
        )

        localLogs.add(0, entry)
        logTimestampsMs[entryId] = nowMs
        if (isOffline) {
            pendingQueue.add(0, entry)
        }

        simulator.recordManualLog(
            vitalId = vitalId,
            value = finalValue,
            secondaryValue = secondaryValue,
            note = effectiveNote,
            qualityStatus = quality
        )

        if (quality != DataQuality.INVALID) {
            if (vitalId == VitalId.HYDRATION_ML && finalValue >= 2500f) {
                completeRoutineTask("routine-hydration")
            } else if (vitalId == VitalId.BLOOD_GLUCOSE) {
                completeRoutineTask("routine-glucose")
            } else if (vitalId == VitalId.MOOD_INDEX) {
                completeRoutineTask("routine-mood-sleep")
            } else if (vitalId == VitalId.BONE_EXERCISE_MIN && finalValue >= 60f) {
                completeRoutineTask("routine-ared")
            }
        }
    }

    fun logWellbeingCheckIn(
        moodScore: Int,
        stressLevel: Int,
        fatigueLevel: Int,
        notes: String? = null
    ) {
        synchronized(lock) {
            val clampedMood = moodScore.coerceIn(1, 10)
            val clampedStress = stressLevel.coerceIn(1, 5)
            val clampedFatigue = fatigueLevel.coerceIn(1, 5)
            val cleanNote = notes?.takeIf { it.isNotBlank() }
            val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date())

            val checkIn = WellbeingCheckIn(
                id = "wb-${UUID.randomUUID()}",
                moodScore = clampedMood,
                stressLevel = clampedStress,
                fatigueLevel = clampedFatigue,
                notes = cleanNote,
                recordedAt = nowStr,
                isPrivate = true,
                isSelfReported = true
            )
            wellbeingHistory.add(0, checkIn)

            val combinedNote = buildString {
                append("[SELF-REPORTED] Stress $clampedStress/5, Fatigue $clampedFatigue/5")
                if (cleanNote != null) {
                    append(" — ")
                    append(cleanNote)
                }
            }
            logManualVitalLocked(
                vitalId = VitalId.MOOD_INDEX,
                value = clampedMood.toFloat(),
                secondaryValue = null,
                isAdditive = false,
                note = combinedNote
            )
            refreshStateLocked()
        }
    }

    fun requestConfidentialSupport(category: String, note: String? = null) {
        synchronized(lock) {
            val isOffline = _uiState.value.isOffline
            val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date())
            val req = ConfidentialSupportRequest(
                id = "sup-${UUID.randomUUID()}",
                category = category.ifBlank { "Flight Psychologist Consult" },
                note = note?.takeIf { it.isNotBlank() },
                requestedAt = nowStr,
                statusLabel = if (isOffline) "PRIVATE QUEUED (OFFLINE)" else "PRIVATE QUEUED"
            )
            supportRequestsList.add(0, req)
            refreshStateLocked()
        }
    }

    fun reportEmergencySymptoms(symptoms: List<String>, note: String? = null) {
        synchronized(lock) {
            val cleanSymptoms = symptoms.map { it.trim() }.filter { it.isNotBlank() }
            val cleanNote = note?.takeIf { it.isNotBlank() }
            if (cleanSymptoms.isEmpty() && cleanNote == null) return

            for (symptom in cleanSymptoms) {
                if (!reportedSymptoms.contains(symptom)) {
                    reportedSymptoms.add(symptom)
                }
            }

            val relatedVital = when {
                cleanSymptoms.any { it.contains("breathing", ignoreCase = true) } -> VitalId.SPO2
                cleanSymptoms.any {
                    it.contains("fainting", ignoreCase = true) ||
                        it.contains("confusion", ignoreCase = true) ||
                        it.contains("vision", ignoreCase = true) ||
                        it.contains("neurological", ignoreCase = true)
                } -> VitalId.BLOOD_PRESSURE
                else -> VitalId.HEART_RATE
            }

            val symptomSummary = if (cleanSymptoms.isNotEmpty()) {
                cleanSymptoms.joinToString(", ")
            } else {
                "Acute crew-reported medical symptom"
            }
            val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date())
            val alert = HealthAlert(
                id = "alert-emergency-${UUID.randomUUID().toString().take(8)}",
                vitalId = relatedVital,
                severity = VitalStatus.CRITICAL,
                title = "EMERGENCY SYMPTOM PROTOCOL: Immediate Crew Evaluation",
                triggerEvent = buildString {
                    append("Crew self-reported acute symptom(s): ")
                    append(symptomSummary)
                    if (cleanNote != null) {
                        append(". Note: ")
                        append(cleanNote)
                    }
                },
                guidance = "IMMEDIATE EMERGENCY ESCALATION: Notify Crew Medical Officer & Flight Surgeon immediately, halt physical exertion/EVA, verify cabin O2/CO2 and vital telemetry, and initiate contingency emergency medical checklist.",
                ruleApplied = "Rule 4.9 Emergency Symptom Protocol",
                triggeredAt = nowStr,
                isAcknowledged = false,
                isEscalated = false
            )
            emergencyAlerts.add(0, alert)
            refreshStateLocked()
        }
    }

    fun updateBaseline(vitalId: VitalId, updated: PersonalBaseline) {
        synchronized(lock) {
            customBaselines[vitalId] = updated
            refreshStateLocked()
        }
    }

    fun updateProfile(name: String, missionName: String, missionDay: Int) {
        synchronized(lock) {
            astronautProfile = astronautProfile.copy(
                name = name.trim().ifBlank { astronautProfile.name },
                missionName = missionName.trim().ifBlank { astronautProfile.missionName },
                missionDay = missionDay.coerceAtLeast(1)
            )
            refreshStateLocked()
        }
    }

    fun completeFirstTimeSetup() {
        synchronized(lock) {
            onboardingCompleted = true
            refreshStateLocked()
        }
    }

    fun reconcileOfflineQueue() {
        synchronized(lock) {
            reconcileOfflineQueueLocked()
            refreshStateLocked()
        }
    }

    private fun reconcileOfflineQueueLocked() {
        for (item in pendingQueue) {
            val idx = localLogs.indexOfFirst { it.clientMutationId == item.clientMutationId }
            if (idx >= 0) {
                localLogs[idx] = localLogs[idx].copy(isSynced = true)
            }
        }
        pendingQueue.clear()
        for (i in supportRequestsList.indices) {
            val req = supportRequestsList[i]
            if (req.statusLabel.contains("OFFLINE", ignoreCase = true)) {
                supportRequestsList[i] = req.copy(statusLabel = "PRIVATE ENCRYPTED UPLINK")
            }
        }
        _uiState.update { it.copy(isOffline = false) }
        if (simulator.getScenario() == SimulationScenario.LINK_BLACKOUT) {
            simulator.setScenario(SimulationScenario.NOMINAL_CRUISE)
        }
    }

    fun acknowledgeAlert(alertId: String) {
        synchronized(lock) {
            val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date())
            val existing = alertOverrides[alertId]
                ?: emergencyAlerts.find { it.id == alertId }
                ?: _uiState.value.alerts.find { it.id == alertId }
            alertOverrides[alertId] = (existing ?: HealthAlert(
                id = alertId,
                vitalId = VitalId.HEART_RATE,
                severity = VitalStatus.MONITOR,
                title = "",
                triggerEvent = "",
                guidance = "",
                ruleApplied = "",
                triggeredAt = nowStr
            )).copy(
                isAcknowledged = true,
                acknowledgedAt = nowStr
            )
            refreshStateLocked()
        }
    }

    fun escalateAlert(alertId: String, note: String = "Dispatched to Flight Surgeon") {
        synchronized(lock) {
            val nowStr = SimpleDateFormat("HH:mm 'UTC'", Locale.US).format(Date())
            val existing = alertOverrides[alertId]
                ?: emergencyAlerts.find { it.id == alertId }
                ?: _uiState.value.alerts.find { it.id == alertId }
            alertOverrides[alertId] = (existing ?: HealthAlert(
                id = alertId,
                vitalId = VitalId.HEART_RATE,
                severity = VitalStatus.CRITICAL,
                title = "",
                triggerEvent = "",
                guidance = "",
                ruleApplied = "",
                triggeredAt = nowStr
            )).copy(
                isAcknowledged = true,
                acknowledgedAt = existing?.acknowledgedAt ?: nowStr,
                isEscalated = true,
                escalatedAt = nowStr,
                escalationNote = note
            )
            refreshStateLocked()
        }
    }

    fun toggleRoutineTask(taskId: String) {
        synchronized(lock) {
            val idx = routineTasks.indexOfFirst { it.id == taskId }
            if (idx >= 0) {
                val task = routineTasks[idx]
                val nextState = !task.isCompleted
                val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
                routineTasks[idx] = task.copy(
                    isCompleted = nextState,
                    completedAt = if (nextState) timeFormatter.format(Date()) else null
                )
                refreshStateLocked()
            }
        }
    }

    private fun completeRoutineTask(taskId: String) {
        val idx = routineTasks.indexOfFirst { it.id == taskId }
        if (idx >= 0 && !routineTasks[idx].isCompleted) {
            val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
            routineTasks[idx] = routineTasks[idx].copy(
                isCompleted = true,
                completedAt = timeFormatter.format(Date())
            )
        }
    }

    fun getTelemetryHistory(vitalId: VitalId): List<TelemetryPoint> {
        synchronized(lock) {
            return simulator.historyByVital[vitalId]?.toList() ?: emptyList()
        }
    }

    fun exportReportCsv(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.append("AstroHealix Mission Medical Telemetry Report\n")
        sb.append("DataMode: ${if (state.isDemoData) "DEMO DATA (SIMULATED)" else "FLIGHT TELEMETRY"}\n")
        sb.append("GeneratedAt: ${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}\n")
        sb.append("Astronaut: ${state.astronaut.name} (${state.astronaut.id}), Mission: ${state.astronaut.missionName}, MissionDay: ${state.astronaut.missionDay}\n")
        sb.append("Environment: ${state.environment.title}, CommDelay: ${state.environment.commDelayLabel}\n")
        sb.append("ReadinessScore: ${state.readinessScore}/100\n")
        sb.append("Disclaimer: ${state.disclaimer}\n\n")

        sb.append("Vital ID,Title,Display Value,Unit,Status,Quality,Source,Baseline Delta,Last Updated\n")
        for (v in state.vitals) {
            val displayVal = if (v.status == VitalStatus.UNAVAILABLE) "DATA UNAVAILABLE" else v.displayValue
            sb.append("${v.id.name},\"${v.title}\",\"$displayVal\",\"${v.unit}\",${v.status.name},${v.qualityStatus.name},${v.source.name},\"${v.deltaFromBaseline}\",\"${v.lastUpdated}\"\n")
        }

        sb.append("\nActive & Acknowledged Alerts\n")
        sb.append("Severity,Title,Rule,Acknowledged,Escalated,TriggerEvent\n")
        for (a in state.alerts) {
            sb.append("${a.severity.name},\"${a.title}\",\"${a.ruleApplied}\",${a.isAcknowledged},${a.isEscalated},\"${a.triggerEvent}\"\n")
        }

        return sb.toString()
    }

    fun exportReportObject(periodHours: Int = 24): MissionReport {
        synchronized(lock) {
            val state = _uiState.value
            val safePeriodHours = periodHours.coerceAtLeast(1)
            val nowMs = System.currentTimeMillis()
            val cutoffMs = nowMs - safePeriodHours.toLong() * 3600_000L

            val filteredHistory = simulator.historyByVital.mapValues { (_, list) ->
                list.filter { it.timestampMs >= cutoffMs }
            }
            val periodLatest = VitalId.entries.mapNotNull { id ->
                val latest = filteredHistory[id]?.lastOrNull()
                if (latest != null) id to latest else null
            }.toMap()

            val pendingVitals = pendingQueue.map { it.vitalId }.toSet()
            val periodVitals = VitalEvaluator.buildSnapshots(
                latestByVital = periodLatest,
                historyByVital = filteredHistory,
                environment = state.environment,
                pendingVitalIds = pendingVitals,
                customBaselines = customBaselines
            )

            val periodLogs = localLogs.filter { entry ->
                val ts = logTimestampsMs[entry.id]
                ts == null || ts >= cutoffMs
            }

            return MissionReport(
                generatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(nowMs)),
                astronaut = state.astronaut,
                environment = state.environment,
                readinessScore = HazardEvaluator.computeReadiness(periodVitals, state.routine),
                hazards = HazardEvaluator.computeHazards(periodVitals, state.environment, state.isOffline),
                vitals = periodVitals,
                alerts = state.alerts,
                recentLogs = periodLogs
            )
        }
    }
}
