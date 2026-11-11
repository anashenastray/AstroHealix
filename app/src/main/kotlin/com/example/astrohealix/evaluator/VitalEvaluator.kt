package com.example.astrohealix.evaluator

import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.DataSource
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import kotlin.math.floor
import kotlin.math.roundToInt

data class EvaluationResult(
    val status: VitalStatus,
    val deltaFromBaseline: String,
    val deltaPercent: Float?,
    val reason: String,
    val ruleApplied: String?,
    val displayValue: String
)

object VitalEvaluator {

    fun formatValue(def: VitalDefinition, value: Float?, secondaryValue: Float? = null): String {
        if (value == null || value.isNaN()) {
            return "DATA UNAVAILABLE"
        }
        return when (def.id) {
            VitalId.BLOOD_PRESSURE -> {
                val sys = value.roundToInt()
                val dia = secondaryValue?.roundToInt() ?: 76
                "$sys/$dia"
            }
            VitalId.SLEEP_HOURS -> {
                val wholeHours = floor(value).toInt()
                val mins = ((value - wholeHours) * 60).roundToInt()
                "${wholeHours}h ${mins.toString().padStart(2, '0')}m"
            }
            VitalId.HYDRATION_ML -> {
                "%,d".format(value.roundToInt())
            }
            else -> {
                if (def.precision > 0) {
                    "%.${def.precision}f".format(value)
                } else {
                    value.roundToInt().toString()
                }
            }
        }
    }

    fun evaluate(
        vitalId: VitalId,
        point: TelemetryPoint?,
        history: List<TelemetryPoint> = emptyList(),
        environment: MissionEnvironment = MissionEnvironment.ORBITAL_STATION
    ): EvaluationResult {
        val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: error("Unknown vital $vitalId")

        if (point == null || point.isDisconnected || point.value == null || point.value.isNaN()) {
            return EvaluationResult(
                status = VitalStatus.UNAVAILABLE,
                deltaFromBaseline = "Sensor Disconnected",
                deltaPercent = null,
                reason = "Telemetry packet missing or hardware probe disconnected.",
                ruleApplied = null,
                displayValue = "DATA UNAVAILABLE"
            )
        }

        val valFloat = point.value
        val base = def.baseline
        val midpoint = (base.nominalMin + base.nominalMax) / 2f
        val deltaPercent = if (midpoint > 0f) ((valFloat - midpoint) / midpoint) * 100f else null

        var deltaFromBaseline = "Base Delta: Nominal"
        if (def.id == VitalId.HYDRATION_ML && base.targetDaily != null) {
            deltaFromBaseline = "Target: %,d mL/day".format(base.targetDaily.toInt())
        } else if (def.id == VitalId.BONE_EXERCISE_MIN && base.targetDaily != null) {
            deltaFromBaseline = "Target: ${base.targetDaily.toInt()} min/day"
        } else if (valFloat > base.nominalMax) {
            val diff = valFloat - base.nominalMax
            val diffStr = if (def.precision > 0) "%.${def.precision}f".format(diff) else diff.roundToInt().toString()
            deltaFromBaseline = "Base Delta: +$diffStr ${def.unit}"
        } else if (valFloat < base.nominalMin) {
            val diff = base.nominalMin - valFloat
            val diffStr = if (def.precision > 0) "%.${def.precision}f".format(diff) else diff.roundToInt().toString()
            deltaFromBaseline = "Base Delta: -$diffStr ${def.unit}"
        }

        // Rule: Heart Rate Baseline Delta Rule 5.2 (+15 bpm above upper baseline 78 over rolling window)
        if (vitalId == VitalId.HEART_RATE) {
            val recentWindow = history
                .filter { it.vitalId == VitalId.HEART_RATE && it.value != null }
                .takeLast(5)
            val rollingAvg = if (recentWindow.isNotEmpty()) {
                recentWindow.mapNotNull { it.value }.average().toFloat()
            } else {
                valFloat
            }
            val spikeOverUpper = valFloat - base.nominalMax
            val rollingOverUpper = rollingAvg - base.nominalMax

            if ((base.criticalHigh != null && valFloat >= base.criticalHigh) ||
                (base.criticalLow != null && valFloat <= base.criticalLow) ||
                rollingOverUpper >= 20f
            ) {
                return EvaluationResult(
                    status = VitalStatus.CRITICAL,
                    deltaFromBaseline = "Base Delta: +${spikeOverUpper.roundToInt().coerceAtLeast(0)} ${def.unit}",
                    deltaPercent = deltaPercent,
                    reason = "Heart Rate baseline deviation (+${spikeOverUpper.roundToInt()} bpm over 15-min rolling window).",
                    ruleApplied = "Baseline Delta Rule 5.2",
                    displayValue = formatValue(def, valFloat, point.secondaryValue)
                )
            }

            if (spikeOverUpper >= 15f || rollingOverUpper >= 12f) {
                return EvaluationResult(
                    status = VitalStatus.MONITOR,
                    deltaFromBaseline = "Base Delta: +${spikeOverUpper.roundToInt()} ${def.unit}",
                    deltaPercent = deltaPercent,
                    reason = "Heart Rate elevated +${spikeOverUpper.roundToInt()} bpm above upper personal baseline (${base.nominalMax.toInt()} BPM).",
                    ruleApplied = "Baseline Delta Rule 5.2",
                    displayValue = formatValue(def, valFloat, point.secondaryValue)
                )
            }
        }

        // Rule: 3 Consecutive Cycles Sleep Drop
        if (vitalId == VitalId.SLEEP_HOURS) {
            val validSleep = history
                .filter { it.vitalId == VitalId.SLEEP_HOURS && it.value != null }
                .takeLast(3)
            val consecutiveLow = validSleep.size >= 3 && validSleep.all { (it.value ?: 99f) < (base.monitorLow ?: 6.0f) }
            if (consecutiveLow || (base.criticalLow != null && valFloat <= base.criticalLow)) {
                return EvaluationResult(
                    status = VitalStatus.CRITICAL,
                    deltaFromBaseline = deltaFromBaseline,
                    deltaPercent = deltaPercent,
                    reason = "Sleep duration dropped below 6.0 hrs for 3 consecutive sleep cycles.",
                    ruleApplied = "Sleep Drift Rule (3 Consecutive Cycles)",
                    displayValue = formatValue(def, valFloat)
                )
            }
        }

        // Critical thresholds check
        if ((base.criticalLow != null && valFloat <= base.criticalLow) ||
            (base.criticalHigh != null && valFloat >= base.criticalHigh)
        ) {
            return EvaluationResult(
                status = VitalStatus.CRITICAL,
                deltaFromBaseline = deltaFromBaseline,
                deltaPercent = deltaPercent,
                reason = "${def.title} crossed critical safety bound (${formatValue(def, valFloat, point.secondaryValue)} ${def.unit}).",
                ruleApplied = "Critical Threshold Bound",
                displayValue = formatValue(def, valFloat, point.secondaryValue)
            )
        }

        // Monitor thresholds check
        if ((base.monitorLow != null && valFloat < base.monitorLow) ||
            (base.monitorHigh != null && valFloat > base.monitorHigh) ||
            valFloat < base.nominalMin ||
            valFloat > base.nominalMax
        ) {
            return EvaluationResult(
                status = VitalStatus.MONITOR,
                deltaFromBaseline = deltaFromBaseline,
                deltaPercent = deltaPercent,
                reason = "${def.title} drifted outside nominal personal baseline (${base.nominalMin.toInt()}–${base.nominalMax.toInt()} ${def.unit}).",
                ruleApplied = "Personal Baseline Drift",
                displayValue = formatValue(def, valFloat, point.secondaryValue)
            )
        }

        return EvaluationResult(
            status = VitalStatus.NORMAL,
            deltaFromBaseline = deltaFromBaseline,
            deltaPercent = deltaPercent,
            reason = "Within established personal mission baseline.",
            ruleApplied = null,
            displayValue = formatValue(def, valFloat, point.secondaryValue)
        )
    }

    fun sortSnapshots(cards: List<VitalCardSnapshot>): List<VitalCardSnapshot> {
        val priorityMap = mapOf(
            VitalStatus.CRITICAL to 0,
            VitalStatus.MONITOR to 1,
            VitalStatus.NORMAL to 2,
            VitalStatus.UNAVAILABLE to 3
        )
        return cards.sortedWith(
            compareBy<VitalCardSnapshot> { priorityMap[it.status] ?: 4 }
                .thenBy { it.id.ordinal }
        )
    }

    fun buildSnapshots(
        latestByVital: Map<VitalId, TelemetryPoint>,
        historyByVital: Map<VitalId, List<TelemetryPoint>>,
        environment: MissionEnvironment,
        pendingVitalIds: Set<VitalId> = emptySet()
    ): List<VitalCardSnapshot> {
        val cards = VitalId.entries.map { vitalId ->
            val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: error("No def")
            val point = latestByVital[vitalId]
            val history = historyByVital[vitalId] ?: emptyList()
            val eval = evaluate(vitalId, point, history, environment)

            VitalCardSnapshot(
                id = vitalId,
                title = def.title,
                shortLabel = def.shortLabel,
                value = if (point != null && !point.isDisconnected) point.value else null,
                secondaryValue = if (point != null && !point.isDisconnected) point.secondaryValue else null,
                displayValue = eval.displayValue,
                unit = def.unit,
                status = eval.status,
                source = point?.source ?: def.defaultSource,
                bodyZone = def.bodyZone,
                hazards = def.hazards,
                lastUpdated = point?.timeLabel ?: "12s ago",
                deltaFromBaseline = eval.deltaFromBaseline,
                deltaPercent = eval.deltaPercent,
                reason = eval.reason,
                ruleApplied = eval.ruleApplied,
                baseline = def.baseline,
                isPendingSync = pendingVitalIds.contains(vitalId)
            )
        }
        return sortSnapshots(cards)
    }

    fun evaluateAlerts(
        snapshots: List<VitalCardSnapshot>,
        environment: MissionEnvironment,
        existingAlerts: List<HealthAlert> = emptyList()
    ): List<HealthAlert> {
        val existingMap = existingAlerts.associateBy { it.id }
        val alerts = mutableListOf<HealthAlert>()

        for (card in snapshots) {
            if (card.status != VitalStatus.CRITICAL && card.status != VitalStatus.MONITOR) continue
            val def = CatalogData.VITAL_DEFINITIONS[card.id] ?: continue
            val alertId = "alert-${card.id.name.lowercase()}-${card.status.name.lowercase()}"
            val prev = existingMap[alertId]

            val protocol = def.guide.selfCareSteps.firstOrNull() ?: "Review vitals and repeat measurement."
            val guidance = if (environment.autonomousCareMode) {
                "$protocol (Autonomous Crew Care active: ${environment.commDelayLabel} lag to Flight Surgeon)."
            } else {
                "$protocol Escalate if persistent on repeat check."
            }

            alerts.add(
                HealthAlert(
                    id = alertId,
                    vitalId = card.id,
                    severity = card.status,
                    title = if (card.status == VitalStatus.CRITICAL) {
                        "ATTENTION REQUIRED: ${def.title} Anomaly"
                    } else {
                        "MONITOR: ${def.title} Deviation"
                    },
                    triggerEvent = "${card.reason} Current reading: ${card.displayValue} ${card.unit}.",
                    guidance = guidance,
                    ruleApplied = card.ruleApplied ?: "Baseline Threshold Guard",
                    triggeredAt = prev?.triggeredAt ?: card.lastUpdated,
                    isAcknowledged = prev?.isAcknowledged ?: false,
                    acknowledgedAt = prev?.acknowledgedAt,
                    isEscalated = prev?.isEscalated ?: false,
                    escalatedAt = prev?.escalatedAt,
                    escalationNote = prev?.escalationNote
                )
            )
        }

        return alerts.sortedWith(
            compareBy<HealthAlert> { if (it.severity == VitalStatus.CRITICAL) 0 else 1 }
                .thenBy { if (it.isAcknowledged) 1 else 0 }
        )
    }
}
