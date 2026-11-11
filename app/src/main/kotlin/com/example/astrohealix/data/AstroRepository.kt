package com.example.astrohealix.data

import com.example.astrohealix.evaluator.HazardEvaluator
import com.example.astrohealix.evaluator.VitalEvaluator
import com.example.astrohealix.model.AstronautProfile
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.HazardScore
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.ManualLogEntry
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.MissionReport
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.SimulationScenario
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
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
    val disclaimer: String = NON_DIAGNOSTIC_DISCLAIMER
)

class AstroRepository(
    private val simulator: MissionSimulator = MissionSimulator()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _uiState = MutableStateFlow(AstroUiState())
    val uiState: StateFlow<AstroUiState> = _uiState.asStateFlow()

    private val localLogs = mutableListOf<ManualLogEntry>()
    private val pendingQueue = mutableListOf<ManualLogEntry>()
    private var alertOverrides = mutableMapOf<String, HealthAlert>()
    private val routineTasks = CatalogData.DEFAULT_ROUTINE.toMutableList()

    init {
        // Seed an initial manual log
        localLogs.add(
            ManualLogEntry(
                id = "init-hyd-1",
                clientMutationId = UUID.randomUUID().toString(),
                vitalId = VitalId.HYDRATION_ML,
                value = 1800f,
                notes = "Morning hydration pouch",
                recordedAt = "1h 10m ago",
                isSynced = true
            )
        )
        refreshState()
        startLiveLoop()
    }

    private fun startLiveLoop() {
        scope.launch {
            while (true) {
                delay(3000L)
                simulator.stepTick()
                refreshState()
            }
        }
    }

    private fun refreshState() {
        val env = simulator.getEnvironment()
        val sc = simulator.getScenario()
        val isOffline = sc == SimulationScenario.LINK_BLACKOUT || _uiState.value.isOffline

        val pendingVitals = pendingQueue.map { it.vitalId }.toSet()
        val snapshots = VitalEvaluator.buildSnapshots(
            latestByVital = simulator.getLatestByVital(),
            historyByVital = simulator.historyByVital,
            environment = env,
            pendingVitalIds = pendingVitals
        )

        val rawAlerts = VitalEvaluator.evaluateAlerts(snapshots, env)
        val mergedAlerts = rawAlerts.map { a ->
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

        val hazards = HazardEvaluator.computeHazards(snapshots, env, isOffline)
        val readiness = HazardEvaluator.computeReadiness(snapshots, routineTasks)

        val timeFormatter = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
        val lastSync = if (isOffline) "OFFLINE CACHE" else timeFormatter.format(Date())

        _uiState.update { current ->
            current.copy(
                environment = env,
                scenario = sc,
                isOffline = isOffline,
                pendingSyncQueue = pendingQueue.toList(),
                lastSyncedAt = lastSync,
                readinessScore = readiness,
                vitals = snapshots,
                alerts = mergedAlerts,
                hazards = hazards,
                routine = routineTasks.toList(),
                recentLogs = localLogs.toList()
            )
        }
    }

    fun setScenario(scenario: SimulationScenario) {
        simulator.setScenario(scenario)
        if (scenario == SimulationScenario.LINK_BLACKOUT) {
            _uiState.update { it.copy(isOffline = true) }
        } else if (_uiState.value.isOffline && scenario != SimulationScenario.LINK_BLACKOUT) {
            // restoration
            reconcileOfflineQueue()
        }
        refreshState()
    }

    fun setEnvironment(env: MissionEnvironment) {
        simulator.setEnvironment(env)
        refreshState()
    }

    fun toggleOfflineLink() {
        val nextOffline = !_uiState.value.isOffline
        _uiState.update { it.copy(isOffline = nextOffline) }
        if (!nextOffline) {
            reconcileOfflineQueue()
        }
        refreshState()
    }

    fun logManualVital(vitalId: VitalId, value: Float, secondaryValue: Float? = null, isAdditive: Boolean = false, note: String? = null) {
        val isOffline = _uiState.value.isOffline

        val finalValue = if (vitalId == VitalId.HYDRATION_ML && isAdditive) {
            val prev = simulator.getLatestByVital()[VitalId.HYDRATION_ML]?.value ?: 0f
            prev + value
        } else {
            value
        }

        val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
        val nowStr = timeFormatter.format(Date())

        val entry = ManualLogEntry(
            id = "log-${UUID.randomUUID()}",
            clientMutationId = UUID.randomUUID().toString(),
            vitalId = vitalId,
            value = finalValue,
            secondaryValue = secondaryValue,
            notes = note,
            recordedAt = nowStr,
            isSynced = !isOffline
        )

        localLogs.add(0, entry)
        if (isOffline) {
            pendingQueue.add(0, entry)
        }

        simulator.recordManualLog(vitalId, finalValue, secondaryValue, note)

        if (vitalId == VitalId.HYDRATION_ML && finalValue >= 2500f) {
            completeRoutineTask("routine-hydration")
        } else if (vitalId == VitalId.BLOOD_GLUCOSE) {
            completeRoutineTask("routine-glucose")
        } else if (vitalId == VitalId.MOOD_INDEX) {
            completeRoutineTask("routine-mood-sleep")
        } else if (vitalId == VitalId.BONE_EXERCISE_MIN && finalValue >= 60f) {
            completeRoutineTask("routine-ared")
        }

        refreshState()
    }

    fun reconcileOfflineQueue() {
        for (item in pendingQueue) {
            val idx = localLogs.indexOfFirst { it.clientMutationId == item.clientMutationId }
            if (idx >= 0) {
                localLogs[idx] = localLogs[idx].copy(isSynced = true)
            }
        }
        pendingQueue.clear()
        _uiState.update { it.copy(isOffline = false) }
        if (simulator.getScenario() == SimulationScenario.LINK_BLACKOUT) {
            simulator.setScenario(SimulationScenario.NOMINAL_CRUISE)
        }
        refreshState()
    }

    fun acknowledgeAlert(alertId: String) {
        val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
        val nowStr = timeFormatter.format(Date())
        val existing = alertOverrides[alertId]
        alertOverrides[alertId] = (existing ?: HealthAlert(
            id = alertId,
            vitalId = VitalId.HEART_RATE,
            severity = com.example.astrohealix.model.VitalStatus.MONITOR,
            title = "",
            triggerEvent = "",
            guidance = "",
            ruleApplied = "",
            triggeredAt = nowStr
        )).copy(
            isAcknowledged = true,
            acknowledgedAt = nowStr
        )
        refreshState()
    }

    fun escalateAlert(alertId: String, note: String = "Dispatched to Flight Surgeon") {
        val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
        val nowStr = timeFormatter.format(Date())
        val existing = alertOverrides[alertId]
        alertOverrides[alertId] = (existing ?: HealthAlert(
            id = alertId,
            vitalId = VitalId.HEART_RATE,
            severity = com.example.astrohealix.model.VitalStatus.CRITICAL,
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
        refreshState()
    }

    fun toggleRoutineTask(taskId: String) {
        val idx = routineTasks.indexOfFirst { it.id == taskId }
        if (idx >= 0) {
            val task = routineTasks[idx]
            val nextState = !task.isCompleted
            val timeFormatter = SimpleDateFormat("HH:mm 'UTC'", Locale.US)
            routineTasks[idx] = task.copy(
                isCompleted = nextState,
                completedAt = if (nextState) timeFormatter.format(Date()) else null
            )
            refreshState()
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
        return simulator.historyByVital[vitalId] ?: emptyList()
    }

    fun exportReportCsv(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.append("AstroHealix Mission Medical Telemetry Report\n")
        sb.append("GeneratedAt: ${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}\n")
        sb.append("Astronaut: ${state.astronaut.name} (${state.astronaut.id}), MissionDay: ${state.astronaut.missionDay}\n")
        sb.append("Environment: ${state.environment.title}, CommDelay: ${state.environment.commDelayLabel}\n")
        sb.append("ReadinessScore: ${state.readinessScore}/100\n")
        sb.append("Disclaimer: ${state.disclaimer}\n\n")

        sb.append("Vital ID,Title,Display Value,Unit,Status,Source,Baseline Delta,Last Updated\n")
        for (v in state.vitals) {
            sb.append("${v.id.name},\"${v.title}\",\"${v.displayValue}\",\"${v.unit}\",${v.status.name},${v.source.name},\"${v.deltaFromBaseline}\",\"${v.lastUpdated}\"\n")
        }

        sb.append("\nActive & Acknowledged Alerts\n")
        sb.append("Severity,Title,Rule,Acknowledged,Escalated,TriggerEvent\n")
        for (a in state.alerts) {
            sb.append("${a.severity.name},\"${a.title}\",\"${a.ruleApplied}\",${a.isAcknowledged},${a.isEscalated},\"${a.triggerEvent}\"\n")
        }

        return sb.toString()
    }

    fun exportReportObject(): MissionReport {
        val state = _uiState.value
        return MissionReport(
            generatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            astronaut = state.astronaut,
            environment = state.environment,
            readinessScore = state.readinessScore,
            hazards = state.hazards,
            vitals = state.vitals,
            alerts = state.alerts,
            recentLogs = state.recentLogs
        )
    }
}
