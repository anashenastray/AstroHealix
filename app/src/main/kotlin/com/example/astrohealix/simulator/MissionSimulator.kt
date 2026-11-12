package com.example.astrohealix.simulator

import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.DataSource
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.SimulationScenario
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalId
import kotlin.math.abs
import kotlin.math.sin

class MissionSimulator {

    private var currentScenario: SimulationScenario = SimulationScenario.NOMINAL_CRUISE
    private var currentEnvironment: MissionEnvironment = MissionEnvironment.ORBITAL_STATION
    private var tickCount = 0
    val historyByVital: MutableMap<VitalId, MutableList<TelemetryPoint>> = mutableMapOf()

    init {
        seedHistory()
    }

    fun getScenario(): SimulationScenario = currentScenario

    fun getEnvironment(): MissionEnvironment = currentEnvironment

    fun setEnvironment(env: MissionEnvironment) {
        currentEnvironment = env
    }

    fun setScenario(scenario: SimulationScenario) {
        currentScenario = scenario
        val nowMs = System.currentTimeMillis()

        if (scenario == SimulationScenario.SLEEP_DEPRIVATION_DRIFT) {
            historyByVital[VitalId.SLEEP_HOURS]?.apply {
                add(TelemetryPoint(nowMs - 48 * 3600_000L, "2d ago", VitalId.SLEEP_HOURS, 5.5f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
                add(TelemetryPoint(nowMs - 24 * 3600_000L, "1d ago", VitalId.SLEEP_HOURS, 5.2f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
                add(TelemetryPoint(nowMs, "Now", VitalId.SLEEP_HOURS, 5.1f, source = DataSource.MANUAL, anomalyRule = "Sleep Drift Rule (3 Consecutive Cycles)", qualityStatus = DataQuality.VALID))
            }
            historyByVital[VitalId.MOOD_INDEX]?.add(
                TelemetryPoint(nowMs, "Now", VitalId.MOOD_INDEX, 4.4f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID)
            )
            historyByVital[VitalId.BLOOD_GLUCOSE]?.add(
                TelemetryPoint(nowMs, "Now", VitalId.BLOOD_GLUCOSE, 152f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID)
            )
        } else if (scenario == SimulationScenario.NOMINAL_CRUISE) {
            historyByVital[VitalId.SLEEP_HOURS]?.add(
                TelemetryPoint(nowMs, "04h ago", VitalId.SLEEP_HOURS, 5.33f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID)
            )
            historyByVital[VitalId.MOOD_INDEX]?.add(
                TelemetryPoint(nowMs, "2h ago", VitalId.MOOD_INDEX, 7.8f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID)
            )
        }
        stepTick()
    }

    private fun pseudoNoise(seed: Int): Float {
        val x = sin(seed * 12.9898f + 78.233f) * 43758.5453f
        return ((x - kotlin.math.floor(x)) * 2f - 1f)
    }

    private fun seedHistory() {
        for (id in VitalId.entries) {
            historyByVital[id] = mutableListOf()
        }

        val nowMs = System.currentTimeMillis()
        // 49 points over 24 hours (30 min increments)
        for (i in 48 downTo 0) {
            val ptMs = nowMs - (i * 30 * 60_000L)
            val hoursAgo = (i * 30) / 60
            val minsAgo = (i * 30) % 60
            val timeLabel = if (i == 0) "12s ago" else if (hoursAgo > 0) "${hoursAgo}h ${minsAgo}m ago" else "${minsAgo}m ago"
            val phase = (48 - i).toFloat() / 48f

            // Gap simulation (indices 14 to 16, ~7h ago) and Rule 5.2 Anomaly spike around index 34
            val isGap = i in 14..16
            val isEstimated = i == 13 || i == 17
            val isSpike = i == 33

            val pointQuality = when {
                isGap -> DataQuality.INCOMPLETE
                isEstimated -> DataQuality.UNVERIFIED
                else -> DataQuality.VALID
            }

            val hrVal = if (isGap) null else if (isSpike) 94f else (71f + sin(phase * Math.PI.toFloat() * 3f) * 5f + pseudoNoise(i) * 3f)

            historyByVital[VitalId.HEART_RATE]?.add(
                TelemetryPoint(
                    timestampMs = ptMs,
                    timeLabel = timeLabel,
                    vitalId = VitalId.HEART_RATE,
                    value = hrVal,
                    source = if (isEstimated) DataSource.ESTIMATED else DataSource.SENSOR,
                    isDisconnected = isGap,
                    isEstimated = isEstimated,
                    anomalyRule = if (isSpike) "Baseline Delta Rule 5.2" else null,
                    note = if (isSpike) "Treadmill calibration excursion" else null,
                    qualityStatus = pointQuality
                )
            )

            historyByVital[VitalId.SPO2]?.add(
                TelemetryPoint(
                    timestampMs = ptMs,
                    timeLabel = timeLabel,
                    vitalId = VitalId.SPO2,
                    value = if (isGap) null else (98f + pseudoNoise(i + 11) * 1.2f).coerceIn(95f, 100f),
                    source = if (isEstimated) DataSource.ESTIMATED else DataSource.SENSOR,
                    isDisconnected = isGap,
                    isEstimated = isEstimated,
                    qualityStatus = pointQuality
                )
            )

            historyByVital[VitalId.RESPIRATORY_RATE]?.add(
                TelemetryPoint(
                    timestampMs = ptMs,
                    timeLabel = timeLabel,
                    vitalId = VitalId.RESPIRATORY_RATE,
                    value = if (isGap) null else (14f + sin(phase * Math.PI.toFloat() * 2f) * 1.5f + pseudoNoise(i + 23)),
                    source = if (isEstimated) DataSource.ESTIMATED else DataSource.SENSOR,
                    isDisconnected = isGap,
                    isEstimated = isEstimated,
                    qualityStatus = pointQuality
                )
            )

            historyByVital[VitalId.RADIATION_MSV]?.add(
                TelemetryPoint(
                    timestampMs = ptMs,
                    timeLabel = timeLabel,
                    vitalId = VitalId.RADIATION_MSV,
                    value = if (isGap) null else (0.64f + abs(sin(phase * Math.PI.toFloat() * 4f)) * 0.25f + pseudoNoise(i + 37) * 0.05f),
                    source = DataSource.SENSOR,
                    isDisconnected = isGap,
                    qualityStatus = if (isGap) DataQuality.INCOMPLETE else DataQuality.VALID
                )
            )

            if (i % 8 == 0) {
                historyByVital[VitalId.BLOOD_PRESSURE]?.add(
                    TelemetryPoint(
                        timestampMs = ptMs,
                        timeLabel = timeLabel,
                        vitalId = VitalId.BLOOD_PRESSURE,
                        value = 116f + pseudoNoise(i + 41) * 5f,
                        secondaryValue = 75f + pseudoNoise(i + 42) * 4f,
                        source = DataSource.MANUAL,
                        qualityStatus = DataQuality.VALID
                    )
                )
                historyByVital[VitalId.BODY_TEMP]?.add(
                    TelemetryPoint(
                        timestampMs = ptMs,
                        timeLabel = timeLabel,
                        vitalId = VitalId.BODY_TEMP,
                        value = 36.8f + pseudoNoise(i + 53) * 0.25f,
                        source = DataSource.MANUAL,
                        qualityStatus = DataQuality.VALID
                    )
                )
                historyByVital[VitalId.BLOOD_GLUCOSE]?.add(
                    TelemetryPoint(
                        timestampMs = ptMs,
                        timeLabel = timeLabel,
                        vitalId = VitalId.BLOOD_GLUCOSE,
                        value = 96f + sin(phase * Math.PI.toFloat() * 4f) * 14f + pseudoNoise(i + 61) * 5f,
                        source = DataSource.MANUAL,
                        qualityStatus = DataQuality.VALID
                    )
                )
            }
        }

        historyByVital[VitalId.SLEEP_HOURS]?.apply {
            add(TelemetryPoint(nowMs - 48 * 3600_000L, "2d ago", VitalId.SLEEP_HOURS, 7.1f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 24 * 3600_000L, "1d ago", VitalId.SLEEP_HOURS, 6.4f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 4 * 3600_000L, "04h ago", VitalId.SLEEP_HOURS, 5.33f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
        }

        historyByVital[VitalId.HYDRATION_ML]?.apply {
            add(TelemetryPoint(nowMs - 6 * 3600_000L, "6h ago", VitalId.HYDRATION_ML, 900f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 3 * 3600_000L, "3h ago", VitalId.HYDRATION_ML, 1450f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 70 * 60_000L, "1h 10m ago", VitalId.HYDRATION_ML, 1800f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
        }

        historyByVital[VitalId.MOOD_INDEX]?.apply {
            add(TelemetryPoint(nowMs - 24 * 3600_000L, "1d ago", VitalId.MOOD_INDEX, 8.2f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 2 * 3600_000L, "2h ago", VitalId.MOOD_INDEX, 7.8f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
        }

        historyByVital[VitalId.BONE_EXERCISE_MIN]?.apply {
            add(TelemetryPoint(nowMs - 24 * 3600_000L, "1d ago", VitalId.BONE_EXERCISE_MIN, 85f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
            add(TelemetryPoint(nowMs - 90 * 60_000L, "1h 30m ago", VitalId.BONE_EXERCISE_MIN, 92f, source = DataSource.MANUAL, qualityStatus = DataQuality.VALID))
        }
    }

    fun stepTick(): Map<VitalId, TelemetryPoint> {
        tickCount++
        val nowMs = System.currentTimeMillis()
        val n = pseudoNoise(tickCount)
        val updates = mutableMapOf<VitalId, TelemetryPoint>()

        val isSensorDrop = currentScenario == SimulationScenario.SENSOR_DISCONNECT
        val isCardioSpike = currentScenario == SimulationScenario.CARDIO_SPIKE_EVA
        val isRadSurge = currentScenario == SimulationScenario.SOLAR_RADIATION_EVENT

        if (isSensorDrop) {
            updates[VitalId.HEART_RATE] = TelemetryPoint(nowMs, "12s ago", VitalId.HEART_RATE, null, isDisconnected = true, qualityStatus = DataQuality.INCOMPLETE)
            updates[VitalId.RADIATION_MSV] = TelemetryPoint(nowMs, "FAILED", VitalId.RADIATION_MSV, null, isDisconnected = true, qualityStatus = DataQuality.INCOMPLETE)
            updates[VitalId.RESPIRATORY_RATE] = TelemetryPoint(nowMs, "12s ago", VitalId.RESPIRATORY_RATE, null, isDisconnected = true, qualityStatus = DataQuality.INCOMPLETE)
        } else {
            val hr = if (isCardioSpike) (104f + n * 4f) else if (isRadSurge) (86f + n * 3f) else (74f + n * 3f)
            val spo2 = if (isCardioSpike) 94f else (98f + n).coerceIn(96f, 100f)
            val resp = if (isRadSurge || isCardioSpike) (22f + n * 1.5f) else (14f + n * 1.2f)
            val rad = if (isRadSurge) (5.35f + abs(n) * 0.4f) else (0.68f + abs(n) * 0.14f)

            updates[VitalId.HEART_RATE] = TelemetryPoint(
                timestampMs = nowMs,
                timeLabel = "12s ago",
                vitalId = VitalId.HEART_RATE,
                value = hr,
                anomalyRule = if (isCardioSpike) "Baseline Delta Rule 5.2" else null,
                qualityStatus = DataQuality.VALID
            )
            updates[VitalId.SPO2] = TelemetryPoint(nowMs, "12s ago", VitalId.SPO2, spo2, qualityStatus = DataQuality.VALID)
            updates[VitalId.RESPIRATORY_RATE] = TelemetryPoint(nowMs, "12s ago", VitalId.RESPIRATORY_RATE, resp, qualityStatus = DataQuality.VALID)
            updates[VitalId.RADIATION_MSV] = TelemetryPoint(nowMs, "12s ago", VitalId.RADIATION_MSV, rad, qualityStatus = DataQuality.VALID)
        }

        if (isCardioSpike) {
            updates[VitalId.BLOOD_PRESSURE] = TelemetryPoint(
                timestampMs = nowMs,
                timeLabel = "12s ago",
                vitalId = VitalId.BLOOD_PRESSURE,
                value = 142f,
                secondaryValue = 88f,
                source = DataSource.ESTIMATED,
                isEstimated = true,
                qualityStatus = DataQuality.UNVERIFIED
            )
        }

        for ((id, pt) in updates) {
            val list = historyByVital[id] ?: mutableListOf()
            list.add(pt)
            if (list.size > 200) list.removeAt(0)
            historyByVital[id] = list
        }

        return updates
    }

    fun recordManualLog(
        vitalId: VitalId,
        value: Float,
        secondaryValue: Float? = null,
        note: String? = null,
        qualityStatus: DataQuality = DataQuality.VALID
    ) {
        val nowMs = System.currentTimeMillis()
        val pt = TelemetryPoint(
            timestampMs = nowMs,
            timeLabel = "Just now",
            vitalId = vitalId,
            value = value,
            secondaryValue = secondaryValue,
            source = DataSource.MANUAL,
            note = note,
            qualityStatus = qualityStatus
        )
        val list = historyByVital[vitalId] ?: mutableListOf()
        list.add(pt)
        if (list.size > 200) list.removeAt(0)
        historyByVital[vitalId] = list
    }

    fun getLatestByVital(): Map<VitalId, TelemetryPoint> {
        return VitalId.entries.mapNotNull { id ->
            val pt = historyByVital[id]?.lastOrNull()
            if (pt != null) id to pt else null
        }.toMap()
    }
}
