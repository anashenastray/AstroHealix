package com.example.astrohealix.evaluator

import com.example.astrohealix.model.HazardId
import com.example.astrohealix.model.HazardScore
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import kotlin.math.roundToInt

object HazardEvaluator {

    fun computeHazards(
        snapshots: List<VitalCardSnapshot>,
        environment: MissionEnvironment,
        isOffline: Boolean
    ): List<HazardScore> {
        val byId = snapshots.associateBy { it.id }

        return HazardId.entries.map { hazardId ->
            var baseRisk = 18
            var summary = "Nominal environmental exposure within mission design limits."

            when (hazardId) {
                HazardId.RADIATION -> {
                    val rad = byId[VitalId.RADIATION_MSV]
                    when (rad?.status) {
                        VitalStatus.CRITICAL -> {
                            baseRisk = 86
                            summary = "Elevated ionizing dose detected; storm-shelter shielding protocol warranted."
                        }
                        VitalStatus.MONITOR -> {
                            baseRisk = 56
                            summary = "Moderate solar flux elevation; keep active dosimeter worn continuously."
                        }
                        VitalStatus.UNAVAILABLE -> {
                            baseRisk = 42
                            summary = "Personal dosimeter telemetry offline; rely on habitat area radiation monitor."
                        }
                        else -> {
                            baseRisk = 24
                            summary = "Dose nominal (${rad?.displayValue ?: "0.64"} mSv/day) under ${environment.shortName} shielding."
                        }
                    }
                }
                HazardId.ISOLATION -> {
                    val mood = byId[VitalId.MOOD_INDEX]
                    val sleep = byId[VitalId.SLEEP_HOURS]
                    val stressPenalty =
                        (if (mood?.status == VitalStatus.CRITICAL) 38 else if (mood?.status == VitalStatus.MONITOR) 20 else 0) +
                        (if (sleep?.status == VitalStatus.CRITICAL) 32 else if (sleep?.status == VitalStatus.MONITOR) 18 else 0)
                    baseRisk = (22 + stressPenalty).coerceAtMost(95)
                    summary = if (stressPenalty > 0) {
                        "Sleep or mood trend indicates confinement fatigue; prioritize recovery window."
                    } else {
                        "Behavioral resilience & sleep cadence steady."
                    }
                }
                HazardId.DISTANCE_DELAY -> {
                    baseRisk = if (isOffline) 74 else if (environment.autonomousCareMode) 52 else 16
                    summary = if (isOffline) {
                        "Satellite link interrupted — local offline cache & autonomous protocols active."
                    } else {
                        "${environment.commDelayLabel}. ${if (environment.autonomousCareMode) "Crew autonomous medical triage enabled." else "Real-time Flight Surgeon telemetry active."}"
                    }
                }
                HazardId.GRAVITY_FIELDS -> {
                    val ex = byId[VitalId.BONE_EXERCISE_MIN]
                    val hyd = byId[VitalId.HYDRATION_ML]
                    val hr = byId[VitalId.HEART_RATE]
                    val penalty =
                        (if (ex?.status == VitalStatus.MONITOR || ex?.status == VitalStatus.CRITICAL) 24 else 0) +
                        (if (hyd?.status == VitalStatus.MONITOR || hyd?.status == VitalStatus.CRITICAL) 20 else 0) +
                        (if (hr?.status == VitalStatus.CRITICAL) 22 else if (hr?.status == VitalStatus.MONITOR) 12 else 0)
                    baseRisk = (26 + penalty).coerceAtMost(95)
                    summary = "${environment.gravityLabel}: fluid shifts & bone remodeling monitored via ARED + hydration."
                }
                HazardId.CLOSED_ENV -> {
                    val spo2 = byId[VitalId.SPO2]
                    val resp = byId[VitalId.RESPIRATORY_RATE]
                    val temp = byId[VitalId.BODY_TEMP]
                    val penalty =
                        (if (spo2?.status == VitalStatus.CRITICAL) 45 else if (spo2?.status == VitalStatus.MONITOR) 22 else 0) +
                        (if (resp?.status == VitalStatus.CRITICAL) 30 else if (resp?.status == VitalStatus.MONITOR) 16 else 0) +
                        (if (temp?.status == VitalStatus.CRITICAL) 25 else if (temp?.status == VitalStatus.MONITOR) 12 else 0)
                    baseRisk = (20 + penalty).coerceAtMost(95)
                    summary = if (penalty > 0) {
                        "Respiratory or thermal drift detected — inspect habitat ventilation & CO2 scrubbers."
                    } else {
                        "Closed-loop O2/CO2 & thermal telemetry nominal."
                    }
                }
            }

            val envMultiplier = when (environment) {
                MissionEnvironment.ORBITAL_STATION -> 1.0f
                MissionEnvironment.DEEP_SPACE_TRANSIT -> if (hazardId == HazardId.RADIATION || hazardId == HazardId.DISTANCE_DELAY) 1.4f else 1.2f
                MissionEnvironment.SURFACE_BASE -> if (hazardId == HazardId.CLOSED_ENV) 1.3f else 1.1f
                MissionEnvironment.ROVER_EVA -> if (hazardId == HazardId.CLOSED_ENV || hazardId == HazardId.GRAVITY_FIELDS) 1.35f else 1.2f
            }

            val score = (baseRisk * envMultiplier).roundToInt().coerceIn(5, 100)
            val status = when {
                score >= 70 -> VitalStatus.CRITICAL
                score >= 45 -> VitalStatus.MONITOR
                else -> VitalStatus.NORMAL
            }

            HazardScore(
                id = hazardId,
                label = hazardId.title,
                score = score,
                status = status,
                summary = summary,
                countermeasure = hazardId.countermeasure
            )
        }
    }

    fun computeReadiness(snapshots: List<VitalCardSnapshot>, routine: List<RoutineTask>): Int {
        var score = 96
        for (s in snapshots) {
            when (s.status) {
                VitalStatus.CRITICAL -> score -= 18
                VitalStatus.MONITOR -> score -= 7
                VitalStatus.UNAVAILABLE -> score -= 3
                VitalStatus.NORMAL -> {}
            }
        }
        val completedCount = routine.count { it.isCompleted }
        val routineBonus = if (routine.isNotEmpty()) {
            ((completedCount.toFloat() / routine.size) * 8f).roundToInt()
        } else 0
        return (score - 4 + routineBonus).coerceIn(15, 100)
    }
}
