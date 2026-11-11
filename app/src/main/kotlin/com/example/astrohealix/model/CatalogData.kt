package com.example.astrohealix.model

object CatalogData {

    val VITAL_DEFINITIONS: Map<VitalId, VitalDefinition> = mapOf(
        VitalId.HEART_RATE to VitalDefinition(
            id = VitalId.HEART_RATE,
            bodyZone = BodyZoneId.CARDIOVASCULAR,
            hazards = listOf(HazardId.GRAVITY_FIELDS, HazardId.ISOLATION, HazardId.CLOSED_ENV),
            baseline = PersonalBaseline(
                nominalMin = 60f,
                nominalMax = 78f,
                monitorLow = 52f,
                monitorHigh = 90f,
                criticalLow = 45f,
                criticalHigh = 100f
            ),
            guide = ExamGuide(
                whyExamMatters = "Microgravity reduces blood volume and causes cephalad fluid shifts, deconditioning the heart muscle and increasing orthostatic risk during re-entry or EVA.",
                howToMeasure = "Moisten chest strap electrodes against skin or fasten optical sensor snugly 1 finger-width above the wrist bone while resting calmly for 2 minutes.",
                spaceHazardEffects = "Altered gravity lowers plasma volume by ~10–15%; high cabin CO2 or exertion during EVA elevates resting and recovery pulse.",
                selfCareSteps = listOf(
                    "Initiate 5-minute deep diaphragmatic respiration exercise (4s inhale / 6s exhale).",
                    "Hydrate with 300–500 mL electrolyte water if post-exercise or EVA.",
                    "Recheck resting pulse after 10 minutes seated or tethered rest."
                ),
                escalateCriteria = "Escalate if resting HR stays >100 BPM or <45 BPM, or Baseline Delta Rule 5.2 triggers with dizziness or chest tightness."
            ),
            precision = 0
        ),
        VitalId.BLOOD_PRESSURE to VitalDefinition(
            id = VitalId.BLOOD_PRESSURE,
            bodyZone = BodyZoneId.CARDIOVASCULAR,
            hazards = listOf(HazardId.GRAVITY_FIELDS, HazardId.CLOSED_ENV),
            baseline = PersonalBaseline(
                nominalMin = 100f,
                nominalMax = 126f,
                monitorLow = 92f,
                monitorHigh = 138f,
                criticalLow = 85f,
                criticalHigh = 150f
            ),
            guide = ExamGuide(
                whyExamMatters = "Tracks vascular tone and arterial stiffness accelerated by microgravity fluid redistribution and radiation-related endothelial stress.",
                howToMeasure = "Position automated arm cuff at heart level while tethered still. Keep legs uncrossed and take two readings 2 minutes apart.",
                spaceHazardEffects = "Weightlessness reduces baroreceptor sensitivity, causing blood pressure swings especially when transitioning to lunar or Martian gravity.",
                selfCareSteps = listOf(
                    "Rest 5 minutes before repeating cuff measurement.",
                    "Verify hydration status and avoid stimulants 30 minutes prior.",
                    "If hypotensive on planetary surface, activate compression garment."
                ),
                escalateCriteria = "Escalate if systolic BP >= 150 mmHg or <= 85 mmHg across two consecutive readings."
            ),
            precision = 0
        ),
        VitalId.SPO2 to VitalDefinition(
            id = VitalId.SPO2,
            bodyZone = BodyZoneId.RESPIRATORY,
            hazards = listOf(HazardId.CLOSED_ENV, HazardId.GRAVITY_FIELDS),
            baseline = PersonalBaseline(
                nominalMin = 95f,
                nominalMax = 100f,
                monitorLow = 93f,
                monitorHigh = null,
                criticalLow = 90f,
                criticalHigh = null
            ),
            guide = ExamGuide(
                whyExamMatters = "Confirms adequate oxygen delivery in sealed spacecraft atmospheres and detects early cabin hypoxia, toxic leak, or suit depressurization.",
                howToMeasure = "Warm fingertip before placing pulse oximeter clip; keep hand still below shoulder level for 30 seconds until waveform stabilizes.",
                spaceHazardEffects = "Closed habitat ventilation pockets or lunar regolith dust inhalation on surface EVAs can impair alveolar gas exchange.",
                selfCareSteps = listOf(
                    "Verify probe placement and finger perfusion.",
                    "Check habitat cabin O2/CO2 scrubber telemetry immediately.",
                    "Switch to portable O2 emergency mask if SpO2 remains <93%."
                ),
                escalateCriteria = "Immediate Flight Surgeon & Commander alert if SpO2 drops <= 90% or accompanies dyspnea."
            ),
            precision = 0
        ),
        VitalId.RESPIRATORY_RATE to VitalDefinition(
            id = VitalId.RESPIRATORY_RATE,
            bodyZone = BodyZoneId.RESPIRATORY,
            hazards = listOf(HazardId.CLOSED_ENV, HazardId.RADIATION),
            baseline = PersonalBaseline(
                nominalMin = 12f,
                nominalMax = 18f,
                monitorLow = 10f,
                monitorHigh = 21f,
                criticalLow = 8f,
                criticalHigh = 26f
            ),
            guide = ExamGuide(
                whyExamMatters = "Early indicator of elevated cabin CO2 (hypercapnia), respiratory infection, or acute metabolic and anxiety response.",
                howToMeasure = "Monitored continuously via chest bio-impedance strap, or count full breath cycles manually for 60 seconds while calm.",
                spaceHazardEffects = "Without buoyancy convection in zero-g, exhaled CO2 pockets accumulate near sleeping quarters, elevating respiration rate.",
                selfCareSteps = listOf(
                    "Verify local ventilation fan airflow in sleep station or workstation.",
                    "Move to a high-airflow module and check cabin ppCO2 gauge."
                ),
                escalateCriteria = "Escalate if resting respiratory rate >= 26 RPM or <= 8 RPM."
            ),
            precision = 0
        ),
        VitalId.BODY_TEMP to VitalDefinition(
            id = VitalId.BODY_TEMP,
            bodyZone = BodyZoneId.IMMUNE_RADIATION,
            hazards = listOf(HazardId.CLOSED_ENV, HazardId.ISOLATION, HazardId.RADIATION),
            baseline = PersonalBaseline(
                nominalMin = 36.2f,
                nominalMax = 37.3f,
                monitorLow = 35.6f,
                monitorHigh = 37.8f,
                criticalLow = 35.0f,
                criticalHigh = 38.5f
            ),
            guide = ExamGuide(
                whyExamMatters = "Microgravity impairs sweat evaporation and alters core thermoregulation ('space fever'), while immune suppression makes infection screening critical.",
                howToMeasure = "Use infrared tympanic or forehead temporal thermometer per standard crew clinical protocol; log before heavy exercise.",
                spaceHazardEffects = "Closed habitats foster opportunistic bacterial growth and latent viral reactivation under chronic mission stress.",
                selfCareSteps = listOf(
                    "Hydrate and rest in cool airflow module.",
                    "Monitor for chills, skin rash, or pharyngitis and repeat measurement in 2 hours."
                ),
                escalateCriteria = "Escalate if temperature >= 38.5 °C or <= 35.0 °C, or fever persists > 24 hours."
            ),
            precision = 1
        ),
        VitalId.BLOOD_GLUCOSE to VitalDefinition(
            id = VitalId.BLOOD_GLUCOSE,
            bodyZone = BodyZoneId.METABOLIC_RENAL,
            hazards = listOf(HazardId.GRAVITY_FIELDS, HazardId.ISOLATION, HazardId.CLOSED_ENV),
            baseline = PersonalBaseline(
                nominalMin = 75f,
                nominalMax = 120f,
                monitorLow = 68f,
                monitorHigh = 145f,
                criticalLow = 58f,
                criticalHigh = 185f
            ),
            guide = ExamGuide(
                whyExamMatters = "Spaceflight induces mild insulin resistance; hypoglycemia severely impairs EVA cognition, motor coordination, and reaction time.",
                howToMeasure = "Clean fingertip with alcohol swab, let dry completely, prick side of finger with sterile lancet, and apply drop to glucometer test strip.",
                spaceHazardEffects = "Circadian misalignment, reduced spontaneous movement, and packaged diets alter postprandial glucose curves.",
                selfCareSteps = listOf(
                    "If <70 mg/dL: consume 15g rapid carbohydrate gel and retest in 15 minutes.",
                    "If elevated: prioritize hydration and review meal macronutrient balance."
                ),
                escalateCriteria = "Immediate action if <= 58 mg/dL (hypoglycemia) or >= 185 mg/dL fasting."
            ),
            precision = 0
        ),
        VitalId.SLEEP_HOURS to VitalDefinition(
            id = VitalId.SLEEP_HOURS,
            bodyZone = BodyZoneId.HEAD_NEURO,
            hazards = listOf(HazardId.ISOLATION, HazardId.CLOSED_ENV, HazardId.DISTANCE_DELAY),
            baseline = PersonalBaseline(
                nominalMin = 6.8f,
                nominalMax = 8.5f,
                monitorLow = 6.0f,
                monitorHigh = null,
                criticalLow = 5.0f,
                criticalHigh = null,
                targetDaily = 7.5f
            ),
            guide = ExamGuide(
                whyExamMatters = "Cumulative sleep deficit degrades situational awareness, robotics precision, fine motor control, and immune resilience.",
                howToMeasure = "Review actigraphy sleep window upon waking and confirm subjective sleep quality in morning check-in.",
                spaceHazardEffects = "16 sunrises/day in LEO, acoustic noise, and long-mission confinement disrupt natural melatonin release.",
                selfCareSteps = listOf(
                    "Dim workstation blue light 60 minutes prior to sleep shift.",
                    "Verify sleep station CO2 ventilation fan and use acoustic earplugs.",
                    "Schedule 20-minute tactical recovery nap if cycle was <6.0h."
                ),
                escalateCriteria = "Triggers critical alert automatically after 3 consecutive cycles < 6.0 hours."
            ),
            precision = 1
        ),
        VitalId.HYDRATION_ML to VitalDefinition(
            id = VitalId.HYDRATION_ML,
            bodyZone = BodyZoneId.METABOLIC_RENAL,
            hazards = listOf(HazardId.GRAVITY_FIELDS, HazardId.CLOSED_ENV),
            baseline = PersonalBaseline(
                nominalMin = 2000f,
                nominalMax = 3200f,
                monitorLow = 1400f,
                monitorHigh = null,
                criticalLow = 900f,
                criticalHigh = null,
                targetDaily = 2500f
            ),
            guide = ExamGuide(
                whyExamMatters = "Bone calcium resorption in microgravity dramatically elevates kidney stone risk; consistent hydration protects renal function and plasma volume.",
                howToMeasure = "Log each water pouch or EVA drink-bag intake in 250–500 mL increments throughout the crew day.",
                spaceHazardEffects = "Cephalad fluid shifts blunt thirst sensation in orbit, causing unintentional dehydration.",
                selfCareSteps = listOf(
                    "Drink 250 mL electrolyte water every 90 minutes during active wake shifts.",
                    "Log potassium-citrate supplement if prescribed for renal stone prophylaxis."
                ),
                escalateCriteria = "Escalate if total intake remains <900 mL past mid-cycle or dark urine or flank pain occurs."
            ),
            precision = 0
        ),
        VitalId.RADIATION_MSV to VitalDefinition(
            id = VitalId.RADIATION_MSV,
            bodyZone = BodyZoneId.IMMUNE_RADIATION,
            hazards = listOf(HazardId.RADIATION, HazardId.DISTANCE_DELAY),
            baseline = PersonalBaseline(
                nominalMin = 0.2f,
                nominalMax = 1.2f,
                monitorLow = null,
                monitorHigh = 2.0f,
                criticalLow = null,
                criticalHigh = 4.5f
            ),
            guide = ExamGuide(
                whyExamMatters = "Tracks galactic cosmic rays (GCR) and solar particle events (SPE) that damage DNA, ocular lenses, and hematopoietic bone marrow.",
                howToMeasure = "Read personal active electronic dosimeter worn continuously on torso; verify wireless telemetry link to habitat area monitor.",
                spaceHazardEffects = "Deep space transit and unshielded lunar/Martian EVAs lose Earth magnetosphere protection.",
                selfCareSteps = listOf(
                    "If monitor threshold exceeded, verify habitat storm shelter readiness.",
                    "If critical SPE alert fires, relocate immediately to hydrogen-rich water-walled storm shelter."
                ),
                escalateCriteria = "Immediate Crew Storm-Shelter Protocol & Flight Surgeon log if dose rate >= 4.5 mSv/day."
            ),
            precision = 2
        ),
        VitalId.MOOD_INDEX to VitalDefinition(
            id = VitalId.MOOD_INDEX,
            bodyZone = BodyZoneId.HEAD_NEURO,
            hazards = listOf(HazardId.ISOLATION, HazardId.DISTANCE_DELAY, HazardId.CLOSED_ENV),
            baseline = PersonalBaseline(
                nominalMin = 6.5f,
                nominalMax = 10.0f,
                monitorLow = 5.0f,
                monitorHigh = null,
                criticalLow = 3.5f,
                criticalHigh = null
            ),
            guide = ExamGuide(
                whyExamMatters = "Confinement, crew monotony, and long Earth communication delays amplify stress, interpersonal friction, and cognitive fatigue.",
                howToMeasure = "Complete 30-second daily check-in rating mood/focus (1–10) and tagging fatigue, stress, or sleep satisfaction.",
                spaceHazardEffects = "Long-duration isolation and Earth-out-of-view phenomenon increase behavioral health vulnerability.",
                selfCareSteps = listOf(
                    "Take scheduled Earth family audio/video playback or Earth-viewing cupola break.",
                    "Perform light aerobic exercise and conduct peer crew check-in."
                ),
                escalateCriteria = "Offer confidential Flight Psychologist consult if mood <= 4/10 for 3 consecutive days."
            ),
            precision = 1
        ),
        VitalId.BONE_EXERCISE_MIN to VitalDefinition(
            id = VitalId.BONE_EXERCISE_MIN,
            bodyZone = BodyZoneId.MUSCULOSKELETAL,
            hazards = listOf(HazardId.GRAVITY_FIELDS),
            baseline = PersonalBaseline(
                nominalMin = 60f,
                nominalMax = 150f,
                monitorLow = 35f,
                monitorHigh = null,
                criticalLow = 15f,
                criticalHigh = null,
                targetDaily = 90f
            ),
            guide = ExamGuide(
                whyExamMatters = "Astronauts lose 1–1.5% bone mineral density per month in microgravity without rigorous daily mechanical loading.",
                howToMeasure = "Log resistive ARED loading (squats, deadlifts, heel raises) and cycle ergometer / T2 treadmill duration after cooldown.",
                spaceHazardEffects = "Unloaded skeleton mobilizes calcium into bloodstream while postural spine and calf muscles atrophy rapidly.",
                selfCareSteps = listOf(
                    "Complete minimum 60 min resistive ARED + 30 min aerobic countermeasure protocol.",
                    "Hydrate and consume post-exercise protein/amino countermeasure packet."
                ),
                escalateCriteria = "Flag Medical Officer if joint strain or lumbar discomfort prevents loading for >48 hours."
            ),
            precision = 0
        )
    )

    val DEFAULT_ROUTINE: List<RoutineTask> = listOf(
        RoutineTask(
            id = "routine-morning-vitals",
            title = "Morning Cuff BP & Core Temp",
            cadence = "07:00 UTC",
            relatedVital = VitalId.BLOOD_PRESSURE,
            targetLabel = "100–126 mmHg / 36.8°C",
            isCompleted = true,
            completedAt = "07:10 UTC",
            xpPoints = 20
        ),
        RoutineTask(
            id = "routine-hydration",
            title = "Hydration Milestone (>= 2,500 mL)",
            cadence = "All Day",
            relatedVital = VitalId.HYDRATION_ML,
            targetLabel = "2,500 mL target",
            isCompleted = false,
            completedAt = null,
            xpPoints = 25
        ),
        RoutineTask(
            id = "routine-ared",
            title = "ARED Resistive & Aerobic Load",
            cadence = "14:00 UTC",
            relatedVital = VitalId.BONE_EXERCISE_MIN,
            targetLabel = "90 min session",
            isCompleted = true,
            completedAt = "15:30 UTC",
            xpPoints = 30
        ),
        RoutineTask(
            id = "routine-glucose",
            title = "Metabolic Blood Sugar Check",
            cadence = "18:00 UTC",
            relatedVital = VitalId.BLOOD_GLUCOSE,
            targetLabel = "75–120 mg/dL",
            isCompleted = false,
            completedAt = null,
            xpPoints = 15
        ),
        RoutineTask(
            id = "routine-mood-sleep",
            title = "Evening Mood & Sleep Window Log",
            cadence = "21:30 UTC",
            relatedVital = VitalId.MOOD_INDEX,
            targetLabel = "Check-in + 7.5h plan",
            isCompleted = false,
            completedAt = null,
            xpPoints = 20
        )
    )
}
