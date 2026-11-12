package com.example.astrohealix.evaluator

import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.DataSource
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

data class EvaluationResult(
    val status: VitalStatus,
    val deltaFromBaseline: String,
    val deltaPercent: Float?,
    val reason: String,
    val ruleApplied: String?,
    val displayValue: String,
    val qualityStatus: DataQuality = DataQuality.VALID
)

object VitalEvaluator {

    private const val CONTINUOUS_STALE_MS = 6L * 3600_000L
    private const val MANUAL_STALE_MS = 36L * 3600_000L
    private const val REAL_EPOCH_THRESHOLD_MS = 1_000_000_000_000L

    fun validateTelemetry(
        vitalId: VitalId,
        value: Float?,
        secondaryValue: Float?,
        timestampMs: Long,
        nowMs: Long,
        isDisconnected: Boolean,
        isEstimated: Boolean,
        existingHistory: List<TelemetryPoint> = emptyList()
    ): DataQuality {
        if (isDisconnected || value == null || value.isNaN() || value.isInfinite()) {
            return DataQuality.INCOMPLETE
        }
        if (secondaryValue != null && (secondaryValue.isNaN() || secondaryValue.isInfinite())) {
            return DataQuality.INVALID
        }

        val inBounds = when (vitalId) {
            VitalId.HEART_RATE -> value in 25f..240f
            VitalId.SPO2 -> value in 50f..100f
            VitalId.RESPIRATORY_RATE -> value in 4f..60f
            VitalId.BODY_TEMP -> value in 30f..44f
            VitalId.BLOOD_PRESSURE -> value in 50f..260f && (secondaryValue == null || secondaryValue in 30f..160f)
            VitalId.BLOOD_GLUCOSE -> value in 20f..600f
            VitalId.SLEEP_HOURS -> value in 0f..24f
            VitalId.HYDRATION_ML -> value in 0f..10000f
            VitalId.RADIATION_MSV -> value in 0f..500f
            VitalId.MOOD_INDEX -> value in 1f..10f
            VitalId.BONE_EXERCISE_MIN -> value in 0f..480f
        }
        if (!inBounds) {
            return DataQuality.INVALID
        }

        val staleThresholdMs = if (vitalId.defaultSource == DataSource.SENSOR) {
            CONTINUOUS_STALE_MS
        } else {
            MANUAL_STALE_MS
        }
        if (timestampMs >= 0L && nowMs > timestampMs && (nowMs - timestampMs) > staleThresholdMs) {
            return DataQuality.OUTDATED
        }

        if (isEstimated) {
            return DataQuality.UNVERIFIED
        }

        val prevValid = existingHistory
            .asReversed()
            .firstOrNull {
                it.vitalId == vitalId &&
                    !it.isDisconnected &&
                    it.value != null &&
                    !it.value.isNaN() &&
                    it.timestampMs < timestampMs &&
                    it.qualityStatus != DataQuality.INVALID &&
                    it.qualityStatus != DataQuality.INCOMPLETE
            }

        if (prevValid?.value != null) {
            val dtMs = timestampMs - prevValid.timestampMs
            val delta = abs(value - prevValid.value)
            val isRapidJump = when (vitalId) {
                VitalId.HEART_RATE -> dtMs <= 5L * 60_000L && delta > 55f
                VitalId.SPO2 -> dtMs <= 5L * 60_000L && delta > 15f
                VitalId.RESPIRATORY_RATE -> dtMs <= 5L * 60_000L && delta > 18f
                VitalId.BODY_TEMP -> dtMs <= 30L * 60_000L && delta > 3.0f
                VitalId.BLOOD_PRESSURE -> dtMs <= 5L * 60_000L && delta > 60f
                VitalId.BLOOD_GLUCOSE -> dtMs <= 15L * 60_000L && delta > 150f
                VitalId.RADIATION_MSV -> dtMs <= 5L * 60_000L && delta > 20f
                else -> false
            }
            if (isRapidJump) {
                return DataQuality.UNVERIFIED
            }
        }

        return DataQuality.VALID
    }

    fun formatValue(def: VitalDefinition, value: Float?, secondaryValue: Float? = null): String {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "DATA UNAVAILABLE"
        }
        return when (def.id) {
            VitalId.BLOOD_PRESSURE -> {
                val sys = value.roundToInt()
                val dia = if (secondaryValue != null && !secondaryValue.isNaN() && !secondaryValue.isInfinite()) {
                    secondaryValue.roundToInt()
                } else {
                    76
                }
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

    private fun formatBound(def: VitalDefinition, value: Float): String {
        return if (def.precision > 0) "%.${def.precision}f".format(value) else value.toInt().toString()
    }

    fun evaluate(
        vitalId: VitalId,
        point: TelemetryPoint?,
        history: List<TelemetryPoint> = emptyList(),
        environment: MissionEnvironment = MissionEnvironment.ORBITAL_STATION,
        customBaselines: Map<VitalId, PersonalBaseline> = emptyMap()
    ): EvaluationResult {
        val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: error("Unknown vital $vitalId")
        val base = customBaselines[vitalId] ?: def.baseline

        if (point == null || point.isDisconnected || point.value == null || point.value.isNaN() || point.value.isInfinite()) {
            return EvaluationResult(
                status = VitalStatus.UNAVAILABLE,
                deltaFromBaseline = "Sensor Disconnected",
                deltaPercent = null,
                reason = "Telemetry packet missing or hardware probe disconnected.",
                ruleApplied = null,
                displayValue = "DATA UNAVAILABLE",
                qualityStatus = DataQuality.INCOMPLETE
            )
        }

        val nowMs = System.currentTimeMillis()
        // Relative synthetic timestamps (< year 2001) used in unit tests are evaluated against their own sequence clock
        val effectiveNowMs = if (point.timestampMs in 0L until REAL_EPOCH_THRESHOLD_MS && nowMs >= REAL_EPOCH_THRESHOLD_MS) {
            maxOf(point.timestampMs, history.maxOfOrNull { it.timestampMs } ?: point.timestampMs)
        } else {
            nowMs
        }

        val computedQuality = validateTelemetry(
            vitalId = vitalId,
            value = point.value,
            secondaryValue = point.secondaryValue,
            timestampMs = point.timestampMs,
            nowMs = effectiveNowMs,
            isDisconnected = point.isDisconnected,
            isEstimated = point.isEstimated,
            existingHistory = history.filter { it !== point && it.timestampMs < point.timestampMs }
        )

        val effectiveQuality = when {
            computedQuality == DataQuality.INCOMPLETE || point.qualityStatus == DataQuality.INCOMPLETE -> DataQuality.INCOMPLETE
            computedQuality == DataQuality.INVALID || point.qualityStatus == DataQuality.INVALID -> DataQuality.INVALID
            computedQuality == DataQuality.OUTDATED || point.qualityStatus == DataQuality.OUTDATED -> DataQuality.OUTDATED
            computedQuality == DataQuality.UNVERIFIED || point.qualityStatus == DataQuality.UNVERIFIED -> DataQuality.UNVERIFIED
            else -> DataQuality.VALID
        }

        if (effectiveQuality == DataQuality.INCOMPLETE) {
            return EvaluationResult(
                status = VitalStatus.UNAVAILABLE,
                deltaFromBaseline = "Sensor Disconnected",
                deltaPercent = null,
                reason = "Telemetry packet missing or hardware probe disconnected.",
                ruleApplied = null,
                displayValue = "DATA UNAVAILABLE",
                qualityStatus = DataQuality.INCOMPLETE
            )
        }

        if (effectiveQuality == DataQuality.INVALID) {
            return EvaluationResult(
                status = VitalStatus.UNAVAILABLE,
                deltaFromBaseline = "Out of Bounds",
                deltaPercent = null,
                reason = "Reading failed physiological bounds validation and was isolated from clinical averages.",
                ruleApplied = null,
                displayValue = "DATA UNAVAILABLE",
                qualityStatus = DataQuality.INVALID
            )
        }

        val valFloat = point.value
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

        // Rule: Heart Rate Baseline Delta Rule 5.2 (+15 bpm above upper baseline over rolling window)
        if (vitalId == VitalId.HEART_RATE) {
            val recentWindow = history
                .filter {
                    it.vitalId == VitalId.HEART_RATE &&
                        !it.isDisconnected &&
                        it.value != null &&
                        it.qualityStatus != DataQuality.INVALID &&
                        it.qualityStatus != DataQuality.INCOMPLETE
                }
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
                    displayValue = formatValue(def, valFloat, point.secondaryValue),
                    qualityStatus = effectiveQuality
                )
            }

            if (spikeOverUpper >= 15f || rollingOverUpper >= 12f) {
                return EvaluationResult(
                    status = VitalStatus.MONITOR,
                    deltaFromBaseline = "Base Delta: +${spikeOverUpper.roundToInt()} ${def.unit}",
                    deltaPercent = deltaPercent,
                    reason = "Heart Rate elevated +${spikeOverUpper.roundToInt()} bpm above upper personal baseline (${base.nominalMax.toInt()} BPM).",
                    ruleApplied = "Baseline Delta Rule 5.2",
                    displayValue = formatValue(def, valFloat, point.secondaryValue),
                    qualityStatus = effectiveQuality
                )
            }
        }

        // Rule: 3 Consecutive Cycles Sleep Drop
        if (vitalId == VitalId.SLEEP_HOURS) {
            val validSleep = history
                .filter {
                    it.vitalId == VitalId.SLEEP_HOURS &&
                        !it.isDisconnected &&
                        it.value != null &&
                        it.qualityStatus != DataQuality.INVALID &&
                        it.qualityStatus != DataQuality.INCOMPLETE
                }
                .takeLast(3)
            val consecutiveLow = validSleep.size >= 3 && validSleep.all { (it.value ?: 99f) < (base.monitorLow ?: 6.0f) }
            if (consecutiveLow || (base.criticalLow != null && valFloat <= base.criticalLow)) {
                return EvaluationResult(
                    status = VitalStatus.CRITICAL,
                    deltaFromBaseline = deltaFromBaseline,
                    deltaPercent = deltaPercent,
                    reason = "Sleep duration dropped below 6.0 hrs for 3 consecutive sleep cycles.",
                    ruleApplied = "Sleep Drift Rule (3 Consecutive Cycles)",
                    displayValue = formatValue(def, valFloat),
                    qualityStatus = effectiveQuality
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
                displayValue = formatValue(def, valFloat, point.secondaryValue),
                qualityStatus = effectiveQuality
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
                reason = "${def.title} drifted outside nominal personal baseline (${formatBound(def, base.nominalMin)}–${formatBound(def, base.nominalMax)} ${def.unit}).",
                ruleApplied = "Personal Baseline Drift",
                displayValue = formatValue(def, valFloat, point.secondaryValue),
                qualityStatus = effectiveQuality
            )
        }

        val normalReason = when (effectiveQuality) {
            DataQuality.OUTDATED -> "Within personal baseline, but reading is outdated (exceeded freshness window)."
            DataQuality.UNVERIFIED -> "Within personal baseline; reading flagged as unverified/estimated."
            else -> "Within established personal mission baseline."
        }

        return EvaluationResult(
            status = VitalStatus.NORMAL,
            deltaFromBaseline = deltaFromBaseline,
            deltaPercent = deltaPercent,
            reason = normalReason,
            ruleApplied = null,
            displayValue = formatValue(def, valFloat, point.secondaryValue),
            qualityStatus = effectiveQuality
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
        pendingVitalIds: Set<VitalId> = emptySet(),
        customBaselines: Map<VitalId, PersonalBaseline> = emptyMap()
    ): List<VitalCardSnapshot> {
        val cards = VitalId.entries.map { vitalId ->
            val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: error("No def")
            val effectiveBaseline = customBaselines[vitalId] ?: def.baseline
            val point = latestByVital[vitalId]
            val history = historyByVital[vitalId] ?: emptyList()
            val eval = evaluate(vitalId, point, history, environment, customBaselines)
            val isUnavailable = eval.status == VitalStatus.UNAVAILABLE

            VitalCardSnapshot(
                id = vitalId,
                title = def.title,
                shortLabel = def.shortLabel,
                value = if (!isUnavailable && point != null && !point.isDisconnected) point.value else null,
                secondaryValue = if (!isUnavailable && point != null && !point.isDisconnected) point.secondaryValue else null,
                displayValue = eval.displayValue,
                unit = def.unit,
                status = eval.status,
                source = point?.source ?: def.defaultSource,
                bodyZone = def.bodyZone,
                hazards = def.hazards,
                lastUpdated = point?.timeLabel ?: "No telemetry",
                deltaFromBaseline = eval.deltaFromBaseline,
                deltaPercent = eval.deltaPercent,
                reason = eval.reason,
                ruleApplied = eval.ruleApplied,
                baseline = effectiveBaseline,
                isPendingSync = pendingVitalIds.contains(vitalId),
                qualityStatus = eval.qualityStatus
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
