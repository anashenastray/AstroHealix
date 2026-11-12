package com.example.astrohealix.model

const val NON_DIAGNOSTIC_DISCLAIMER =
    "MISSION DECISION SUPPORT ONLY: AstroHealix assists crew self-assessment and trend monitoring during spaceflight. It does not replace clinical diagnosis by a Flight Surgeon or Crew Medical Officer."

enum class VitalId(val title: String, val shortLabel: String, val unit: String, val defaultSource: DataSource) {
    HEART_RATE("Heart Rate", "HEART RATE", "BPM", DataSource.SENSOR),
    BLOOD_PRESSURE("Blood Pressure", "BLOOD PRESS.", "mmHg", DataSource.MANUAL),
    SPO2("Oxygen Saturation", "OXYGEN SAT.", "%", DataSource.SENSOR),
    RESPIRATORY_RATE("Respiratory Rate", "RESPIRATORY", "RPM", DataSource.SENSOR),
    BODY_TEMP("Body Temperature", "BODY TEMP", "°C", DataSource.MANUAL),
    BLOOD_GLUCOSE("Blood Sugar", "BLOOD SUGAR", "mg/dL", DataSource.MANUAL),
    SLEEP_HOURS("Sleep Duration", "SLEEP QUAL.", "hrs", DataSource.MANUAL),
    HYDRATION_ML("Daily Hydration", "HYDRATION", "mL", DataSource.MANUAL),
    RADIATION_MSV("Radiation Dose", "RADIATION", "mSv/d", DataSource.SENSOR),
    MOOD_INDEX("Behavioral Mood", "MOOD INDEX", "/10", DataSource.MANUAL),
    BONE_EXERCISE_MIN("Bone & ARED Load", "BONE EXER.", "min", DataSource.MANUAL)
}

enum class VitalStatus {
    NORMAL,
    MONITOR,
    CRITICAL,
    UNAVAILABLE
}

enum class DataSource {
    SENSOR,
    MANUAL,
    ESTIMATED
}

enum class DataQuality {
    VALID,
    INCOMPLETE,
    UNVERIFIED,
    OUTDATED,
    INVALID
}

enum class VitalCategory(val title: String, val vitalIds: List<VitalId>) {
    CARDIOVASCULAR("Cardiovascular", listOf(VitalId.HEART_RATE, VitalId.BLOOD_PRESSURE)),
    RESPIRATORY("Respiratory", listOf(VitalId.SPO2, VitalId.RESPIRATORY_RATE)),
    SLEEP_FATIGUE("Sleep & Fatigue", listOf(VitalId.SLEEP_HOURS)),
    WELLBEING("Psychological Wellbeing", listOf(VitalId.MOOD_INDEX)),
    EXERCISE("Exercise & Musculoskeletal", listOf(VitalId.BONE_EXERCISE_MIN)),
    METABOLIC("Metabolic", listOf(VitalId.BLOOD_GLUCOSE, VitalId.HYDRATION_ML, VitalId.BODY_TEMP)),
    RADIATION("Radiation", listOf(VitalId.RADIATION_MSV))
}

enum class MissionEnvironment(
    val title: String,
    val shortName: String,
    val gravityLabel: String,
    val commDelayLabel: String,
    val commDelaySeconds: Long,
    val autonomousCareMode: Boolean,
    val description: String
) {
    ORBITAL_STATION(
        "ISS / Orbital Space Station",
        "ORBITAL STATION",
        "0.00 g (LEO)",
        "250 ms Live Link",
        0,
        false,
        "Real-time Flight Surgeon voice/telemetry link with 16 orbital day-night cycles."
    ),
    DEEP_SPACE_TRANSIT(
        "Deep Space Transit Spacecraft",
        "TRANSIT CRAFT",
        "0.00 g (Deep Space)",
        "14m 20s One-Way Delay",
        860,
        true,
        "Outside Earth magnetosphere with communication delay; crew autonomous triage enabled."
    ),
    SURFACE_BASE(
        "Lunar / Martian Surface Base",
        "SURFACE BASE",
        "0.16–0.38 g",
        "4m 10s Base Relay",
        250,
        true,
        "Partial gravity planetary surface habitat with regolith dust and thermal mitigation."
    ),
    ROVER_EVA(
        "Pressurized Rover / Surface EVA",
        "ROVER / EVA",
        "0.16–0.38 g (Suit)",
        "Relay / Intermittent",
        180,
        true,
        "High metabolic excursion mode prioritizing hydration, heart load, O2/CO2, and radiation."
    )
}

enum class HazardId(val title: String, val countermeasure: String) {
    RADIATION(
        "Space Radiation",
        "Wear active dosimeter continuously; move to hydrogen-shielded storm shelter on SPE warnings."
    ),
    ISOLATION(
        "Isolation & Confinement",
        "Maintain daily mood check-ins, crew social meals, and protected 7.5h sleep windows."
    ),
    DISTANCE_DELAY(
        "Distance from Earth",
        "Use onboard autonomous self-care protocols and local cache queue when comms lag exceeds 1 minute."
    ),
    GRAVITY_FIELDS(
        "Altered Gravity Fields",
        "Execute 90 min daily ARED/aerobic loading and maintain >= 2,500 mL hydration."
    ),
    CLOSED_ENV(
        "Hostile Closed Environment",
        "Verify cabin airflow, ppCO2 scrubbers, and rapid temp/SpO2 screening after suit operations."
    )
}

enum class BodyZoneId(val title: String, val subtitle: String, val recommendedUltrasound: String) {
    HEAD_NEURO(
        "Neuro, Ocular & Behavioral",
        "Circadian Rhythm, SANS & Cognitive Health",
        "Ocular Optic Nerve Sheath Diameter (ONSD) Ultrasound"
    ),
    CARDIOVASCULAR(
        "Cardiovascular System",
        "Heart Rate, Vascular Tone & Orthostasis",
        "Subxiphoid / Parasternal Cardiac Ultrasound"
    ),
    RESPIRATORY(
        "Pulmonary & Air Exchange",
        "SpO2, Respiratory Rate & Cabin Gas",
        "Thoracic / Pleural Sliding Lung Ultrasound"
    ),
    METABOLIC_RENAL(
        "Metabolic & Renal Systems",
        "Hydration, Blood Sugar & Stone Prevention",
        "Renal & Bladder Volume Ultrasound"
    ),
    MUSCULOSKELETAL(
        "Musculoskeletal & Spine",
        "Bone Density & Resistive Countermeasures",
        "Musculoskeletal Tendon / Calf Cross-Section Scan"
    ),
    IMMUNE_RADIATION(
        "Immune & Radiation Shielding",
        "Dosimetry, Temperature & Cellular Defense",
        "Dermatology Camera / Lymph Node Survey"
    )
}

enum class SimulationScenario(val title: String, val badge: String, val description: String) {
    NOMINAL_CRUISE(
        "Nominal Mission Day",
        "DEFAULT",
        "Normal orbital telemetry with mild sleep duration monitor alert (5h 20m) and historical sensor gap."
    ),
    SENSOR_DISCONNECT(
        "Chest Strap & Dosimeter Disconnect",
        "STEP 1 DEMO",
        "Wireless chest strap & radiation dosimeter interrupted -> cards transition to grey UNAVAILABLE."
    ),
    LINK_BLACKOUT(
        "Satellite Link Blackout",
        "STEP 2 DEMO",
        "Drops Earth telemetry uplink -> switches to [OFFLINE CACHE ACTIVE] while local queue logs actions."
    ),
    CARDIO_SPIKE_EVA(
        "EVA Cardiovascular Spike (Rule 5.2)",
        "ANOMALY",
        "Triggers Baseline Delta Rule 5.2 (+22 bpm HR spike), elevated BP, and exertion pressure."
    ),
    SOLAR_RADIATION_EVENT(
        "Solar Particle Radiation Surge",
        "CRITICAL",
        "Simulates acute dosimetry surge (5.4 mSv/d) requiring storm shelter protocol."
    ),
    SLEEP_DEPRIVATION_DRIFT(
        "3-Cycle Circadian Sleep Drift",
        "BEHAVIORAL",
        "3 consecutive sleep cycles < 6.0h, declining mood score, and elevated glucose variability."
    )
}

data class PersonalBaseline(
    val nominalMin: Float,
    val nominalMax: Float,
    val monitorLow: Float? = null,
    val monitorHigh: Float? = null,
    val criticalLow: Float? = null,
    val criticalHigh: Float? = null,
    val targetDaily: Float? = null
)

data class ExamGuide(
    val whyExamMatters: String,
    val howToMeasure: String,
    val spaceHazardEffects: String,
    val selfCareSteps: List<String>,
    val escalateCriteria: String
)

data class VitalDefinition(
    val id: VitalId,
    val bodyZone: BodyZoneId,
    val hazards: List<HazardId>,
    val baseline: PersonalBaseline,
    val guide: ExamGuide,
    val precision: Int = 0
) {
    val title: String get() = id.title
    val shortLabel: String get() = id.shortLabel
    val unit: String get() = id.unit
    val defaultSource: DataSource get() = id.defaultSource
}

data class TelemetryPoint(
    val timestampMs: Long,
    val timeLabel: String,
    val vitalId: VitalId,
    val value: Float?,
    val secondaryValue: Float? = null,
    val source: DataSource = DataSource.SENSOR,
    val isDisconnected: Boolean = false,
    val isEstimated: Boolean = false,
    val anomalyRule: String? = null,
    val note: String? = null,
    val qualityStatus: DataQuality = DataQuality.VALID
)

data class VitalCardSnapshot(
    val id: VitalId,
    val title: String,
    val shortLabel: String,
    val value: Float?,
    val secondaryValue: Float? = null,
    val displayValue: String,
    val unit: String,
    val status: VitalStatus,
    val source: DataSource,
    val bodyZone: BodyZoneId,
    val hazards: List<HazardId>,
    val lastUpdated: String,
    val deltaFromBaseline: String,
    val deltaPercent: Float?,
    val reason: String,
    val ruleApplied: String?,
    val baseline: PersonalBaseline,
    val isPendingSync: Boolean = false,
    val qualityStatus: DataQuality = DataQuality.VALID
)

data class HealthAlert(
    val id: String,
    val vitalId: VitalId,
    val severity: VitalStatus, // MONITOR or CRITICAL
    val title: String,
    val triggerEvent: String,
    val guidance: String,
    val ruleApplied: String,
    val triggeredAt: String,
    val isAcknowledged: Boolean = false,
    val acknowledgedAt: String? = null,
    val isEscalated: Boolean = false,
    val escalatedAt: String? = null,
    val escalationNote: String? = null
)

data class HazardScore(
    val id: HazardId,
    val label: String,
    val score: Int, // 0-100
    val status: VitalStatus,
    val summary: String,
    val countermeasure: String
)

data class RoutineTask(
    val id: String,
    val title: String,
    val cadence: String,
    val relatedVital: VitalId,
    val targetLabel: String,
    val isCompleted: Boolean,
    val completedAt: String? = null,
    val xpPoints: Int = 20
)

data class ManualLogEntry(
    val id: String,
    val clientMutationId: String,
    val vitalId: VitalId,
    val value: Float,
    val secondaryValue: Float? = null,
    val notes: String? = null,
    val recordedAt: String,
    val isSynced: Boolean = true
)

data class WellbeingCheckIn(
    val id: String,
    val moodScore: Int,
    val stressLevel: Int,
    val fatigueLevel: Int,
    val notes: String? = null,
    val recordedAt: String,
    val isPrivate: Boolean = true,
    val isSelfReported: Boolean = true
)

data class ConfidentialSupportRequest(
    val id: String,
    val category: String,
    val note: String? = null,
    val requestedAt: String,
    val statusLabel: String = "PRIVATE QUEUED"
)

data class AstronautProfile(
    val id: String = "#A-8842",
    val name: String = "Cmdr. J. Vance",
    val role: String = "Flight Engineer / Medical Officer",
    val missionName: String = "ARTEMIS-MARS TRANSIT",
    val missionDay: Int = 47,
    val batteryPercent: Int = 94,
    val habitatO2Percent: Float = 20.9f,
    val cabinCo2MmHg: Float = 2.3f,
    val streakDays: Int = 14,
    val missionDurationDays: Int = 180,
    val createdAtLabel: String = "Mission Day 1",
    val isAuthenticated: Boolean = true
)

data class MissionReport(
    val generatedAt: String,
    val disclaimer: String = NON_DIAGNOSTIC_DISCLAIMER,
    val astronaut: AstronautProfile,
    val environment: MissionEnvironment,
    val readinessScore: Int,
    val hazards: List<HazardScore>,
    val vitals: List<VitalCardSnapshot>,
    val alerts: List<HealthAlert>,
    val recentLogs: List<ManualLogEntry>
)
