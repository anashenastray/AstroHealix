package com.example.astrohealix.model

enum class HazardType(val title: String, val code: String, val summary: String) {
    RADIATION(
        "Space Radiation",
        "RAD",
        "Galactic Cosmic Rays (GCR) and Solar Particle Events (SPE) increase cellular oxidative stress, hematologic suppression, and neurological risk."
    ),
    ISOLATION_CONFINEMENT(
        "Isolation & Confinement",
        "ISO",
        "Enclosed module living disrupts circadian entrainment, reduces sleep efficiency, and elevates cognitive fatigue."
    ),
    DISTANCE_FROM_EARTH(
        "Distance from Earth",
        "DIST",
        "Deep-space communications latency requires autonomous onboard triage, diagnostics, and deterministic treatment."
    ),
    GRAVITY_FIELDS(
        "Altered Gravity",
        "GRAV",
        "Weightlessness causes cephalad fluid shift, neuro-ocular syndrome (SANS), orthostatic intolerance, and bone mineral loss."
    ),
    HOSTILE_CLOSED_ENV(
        "Closed Environment",
        "ENV",
        "Recirculated cabin atmosphere with elevated CO2 pockets (>3 mmHg) impacts respiratory and neurological health."
    )
}

data class Countermeasure(
    val title: String,
    val description: String,
    val dosageOrFrequency: String,
    val hardwareUsed: String
)

data class SymptomPrompt(
    val id: String,
    val symptomText: String,
    val severityWeight: Int, // 1 = mild, 2 = moderate, 3 = critical
    val recommendedAction: String
)

data class AnatomySystem(
    val id: String,
    val name: String,
    val shortLabel: String,
    val anatomicalRegion: String,
    val hotspotXFraction: Float,
    val hotspotYFraction: Float,
    val status: VitalStatus,
    val primaryRisk: String,
    val relatedVitalIds: List<VitalId>,
    val spaceflightEffects: List<String>,
    val physiologicalIndicators: List<String>,
    val hazards: List<HazardType>,
    val countermeasures: List<Countermeasure>,
    val symptomChecklist: List<SymptomPrompt>,
    val clinicalAdvice: String
)

data class ProtocolStep(
    val index: Int,
    val instruction: String,
    val detail: String = "",
    val isCompleted: Boolean = false
)

data class ClinicalProtocol(
    val code: String,
    val title: String,
    val category: String,
    val severity: VitalStatus,
    val triggerCondition: String,
    val timerDurationSeconds: Int? = null,
    val timerLabel: String? = null,
    val steps: List<ProtocolStep>
)

object BodySystemsCatalog {
    fun getDefaultSystems(): List<AnatomySystem> = listOf(
        AnatomySystem(
            id = "sys_neuro_sans",
            name = "Neuro & Ocular System",
            shortLabel = "NEURO/SANS",
            anatomicalRegion = "Cranial & Ocular Globe",
            hotspotXFraction = 0.50f,
            hotspotYFraction = 0.12f,
            status = VitalStatus.MONITOR,
            primaryRisk = "Spaceflight Associated Neuro-Ocular Syndrome (SANS)",
            relatedVitalIds = listOf(VitalId.BLOOD_PRESSURE, VitalId.MOOD_INDEX),
            spaceflightEffects = listOf(
                "Cephalad venous fluid redistribution causes optic disc edema",
                "Choroidal fold development and hyperopic refraction shift",
                "Increased intracranial pressure during microgravity transitions"
            ),
            physiologicalIndicators = listOf(
                "Optic Nerve Sheath Diameter (ONSD) > 5.8 mm",
                "Morning retro-orbital headache score >= 2/5",
                "Amsler grid scotoma distortion"
            ),
            hazards = listOf(HazardType.GRAVITY_FIELDS, HazardType.HOSTILE_CLOSED_ENV),
            countermeasures = listOf(
                Countermeasure(
                    "Lower Body Negative Pressure (LBNP)",
                    "Apply -25 mmHg seal for 2 hours daily during sleep cycle",
                    "Daily 120 min",
                    "Chibis-M LBNP Suit"
                ),
                Countermeasure(
                    "Optic Ultrasound Surveillance",
                    "Perform B-scan ocular ultrasound with 12 MHz linear probe",
                    "Bi-weekly",
                    "Vivid E95 Ultrasound"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_sans_1", "Blurred near vision reading instrument panels", 2, "Re-measure ONSD via ultrasound"),
                SymptomPrompt("sym_sans_2", "Persistent retro-orbital dull ache on waking", 2, "Initiate 30m LBNP cycle"),
                SymptomPrompt("sym_sans_3", "Visual field dark spots or flash artifacts", 3, "Urgent Flight Surgeon consult")
            ),
            clinicalAdvice = "Monitor near-vision chart daily. If globe flattening exceeds 0.3mm or papilledema occurs, activate LBNP countermeasure and notify Flight Surgeon."
        ),
        AnatomySystem(
            id = "sys_cardio",
            name = "Cardiovascular System",
            shortLabel = "CARDIO",
            anatomicalRegion = "Thoracic Cavity & Pericardium",
            hotspotXFraction = 0.52f,
            hotspotYFraction = 0.32f,
            status = VitalStatus.NORMAL,
            primaryRisk = "Orthostatic Intolerance & Left Ventricular Atrophy",
            relatedVitalIds = listOf(VitalId.HEART_RATE, VitalId.BLOOD_PRESSURE),
            spaceflightEffects = listOf(
                "Decreased plasma volume (10–15% decline in 72 hours)",
                "Loss of arterial baroreflex sensitivity",
                "Decreased left ventricular mass without exercise loading"
            ),
            physiologicalIndicators = listOf(
                "Resting tachycardia > 100 BPM",
                "Orthostatic systolic drop > 20 mmHg upon posture shift",
                "Elevated circulating catecholamines"
            ),
            hazards = listOf(HazardType.GRAVITY_FIELDS, HazardType.HOSTILE_CLOSED_ENV),
            countermeasures = listOf(
                Countermeasure(
                    "ARED Heavy Resistance Loading",
                    "Deadlift and squat cycles at 85% Earth 1-RM",
                    "Daily 60 min",
                    "Advanced Resistive Exercise Device (ARED)"
                ),
                Countermeasure(
                    "Salt & Fluid Loading Pre-G",
                    "Consume 8x 1g sodium chloride tablets with 1L water",
                    "T-minus 2 hours before acceleration",
                    "Medical Dispenser Unit"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_card_1", "Resting heart rate exceeding 95 BPM at rest", 2, "Initiate 4-7-8 tactical breathing"),
                SymptomPrompt("sym_card_2", "Lightheadedness or tunnel vision upon rapid movement", 2, "Drink 500 mL electrolyte fluid"),
                SymptomPrompt("sym_card_3", "Palpitations or ectopic cardiac skipped beats", 3, "Apply 12-lead ECG monitor")
            ),
            clinicalAdvice = "Maintain daily aerobic cycling (T2 treadmill/cycle ergometer) to preserve cardiac stroke volume and vascular compliance."
        ),
        AnatomySystem(
            id = "sys_respiratory",
            name = "Respiratory System",
            shortLabel = "RESPIRATORY",
            anatomicalRegion = "Pulmonary Lobes & Trachea",
            hotspotXFraction = 0.45f,
            hotspotYFraction = 0.28f,
            status = VitalStatus.NORMAL,
            primaryRisk = "Hypercapnia Narcosis & Dead-Zone CO2 Trapping",
            relatedVitalIds = listOf(VitalId.RESPIRATORY_RATE, VitalId.SPO2),
            spaceflightEffects = listOf(
                "Absence of convection creates localized CO2 bubbles around sleeping crew",
                "Alveolar ventilation-perfusion ratio changes in microgravity",
                "Higher susceptibility to upper airway congestion"
            ),
            physiologicalIndicators = listOf(
                "Elevated cabin ppCO2 > 3.0 mmHg",
                "Respiratory rate > 20 RPM at rest",
                "SpO2 dropping below 95%"
            ),
            hazards = listOf(HazardType.HOSTILE_CLOSED_ENV),
            countermeasures = listOf(
                Countermeasure(
                    "Amine Scrub Bed Cycling",
                    "Switch Amine carbon dioxide removal system to high-rate desorption",
                    "Continuous / Auto",
                    "Life Support CDRA/ECLSS"
                ),
                Countermeasure(
                    "Cabin Air Circulation Fan Check",
                    "Verify sleep station air velocity > 0.08 m/s",
                    "Every shift",
                    "Anemometer Probe"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_resp_1", "Throbbing frontal headache while working in module", 2, "Measure local CO2 pocket level"),
                SymptomPrompt("sym_resp_2", "Shortness of breath with minimal exertion", 2, "Check SpO2 and cabin ppO2"),
                SymptomPrompt("sym_resp_3", "Hypercapnic air hunger (tachypnea > 22 RPM)", 3, "Don emergency O2 mask immediately")
            ),
            clinicalAdvice = "Cabin CO2 must remain under 3 mmHg. When CO2 > 3 mmHg and HR > 100 BPM occur concurrently, execute Hypercapnia Tachycardia Protocol immediately."
        ),
        AnatomySystem(
            id = "sys_immune_rad",
            name = "Immune & Radiation Dosimetry",
            shortLabel = "RAD/IMMUNE",
            anatomicalRegion = "Spleen, Bone Marrow & Lymphoid",
            hotspotXFraction = 0.55f,
            hotspotYFraction = 0.38f,
            status = VitalStatus.NORMAL,
            primaryRisk = "Solar Particle Event (SPE) & T-Cell Dysregulation",
            relatedVitalIds = listOf(VitalId.RADIATION_MSV),
            spaceflightEffects = listOf(
                "Cumulative ionizing radiation increases DNA double-strand breaks",
                "Suppression of NK-cell cytolytic activity",
                "Reactivation of latent herpesviruses (EBV, VZV, CMV)"
            ),
            physiologicalIndicators = listOf(
                "Daily dosimeter rate > 1.2 mSv/day",
                "Total mission cumulative dose > 150 mSv",
                "White blood cell neutrophil-to-lymphocyte ratio shift"
            ),
            hazards = listOf(HazardType.RADIATION, HazardType.ISOLATION_CONFINEMENT),
            countermeasures = listOf(
                Countermeasure(
                    "Storm Shelter Relocation",
                    "Move crew sleeping quarters inside central water-wall shielding",
                    "During active SPE",
                    "Radiation Storm Shelter Module"
                ),
                Countermeasure(
                    "Antioxidant Radioprotection",
                    "Oral N-acetylcysteine (NAC) and selenium supplementation",
                    "Daily 600 mg",
                    "Medical Kit Tray D"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_rad_1", "Visual light flashes with eyes closed in dark (Phosphenes)", 1, "Log cosmic ray retina strike"),
                SymptomPrompt("sym_rad_2", "Unexplained fatigue or low-grade skin erythema", 2, "Review active dosimeter log"),
                SymptomPrompt("sym_rad_3", "Acute nausea/vomiting following solar flare telemetry", 3, "Execute acute radiation sickness shelter protocol")
            ),
            clinicalAdvice = "Monitor personal dosimeter daily. Cumulative deep-space limits must not exceed 250 mSv per 180-day exploration segment."
        ),
        AnatomySystem(
            id = "sys_renal_gi",
            name = "Renal & Metabolic System",
            shortLabel = "RENAL/METAB",
            anatomicalRegion = "Kidneys, Liver & Gastrointestinal",
            hotspotXFraction = 0.46f,
            hotspotYFraction = 0.42f,
            status = VitalStatus.NORMAL,
            primaryRisk = "Nephrolithiasis (Kidney Stones) & Bone Demineralization",
            relatedVitalIds = listOf(VitalId.HYDRATION_ML, VitalId.BLOOD_GLUCOSE, VitalId.BODY_TEMP),
            spaceflightEffects = listOf(
                "Hypercalciuria from accelerated bone resorption increases stone risk",
                "Blunted thirst mechanism leading to chronic sub-clinical dehydration",
                "Altered gastrointestinal microbiome and drug absorption kinetics"
            ),
            physiologicalIndicators = listOf(
                "Daily urine output < 1,500 mL",
                "Elevated urinary calcium-to-creatinine ratio",
                "Blood glucose variability > 30 mg/dL"
            ),
            hazards = listOf(HazardType.GRAVITY_FIELDS, HazardType.HOSTILE_CLOSED_ENV),
            countermeasures = listOf(
                Countermeasure(
                    "Hydration Goal Enforcement",
                    "Target minimum 2,500 mL water intake logged every 24 hours",
                    "Daily >= 2,500 mL",
                    "Potable Water Dispenser"
                ),
                Countermeasure(
                    "Potassium Citrate Therapy",
                    "Alkalinize urine to inhibit calcium oxalate precipitation",
                    "20 mEq daily with meal",
                    "Oral Tablet Dispenser"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_ren_1", "Dark concentrated urine with specific gravity > 1.025", 1, "Drink 750 mL fluid immediately"),
                SymptomPrompt("sym_ren_2", "Mild intermittent flank or lower abdominal discomfort", 2, "Screen urine with test strip"),
                SymptomPrompt("sym_ren_3", "Severe sharp flank colic with hematuria (blood in urine)", 3, "Initiate nephrolithiasis ultrasound protocol")
            ),
            clinicalAdvice = "Maintain hydration above 2.5L daily. If flank pain occurs, perform bedside renal ultrasound and initiate pain management & IV fluids."
        ),
        AnatomySystem(
            id = "sys_musculo",
            name = "Musculoskeletal System",
            shortLabel = "MUSCULO",
            anatomicalRegion = "Spine, Core & Lower Extremities",
            hotspotXFraction = 0.50f,
            hotspotYFraction = 0.54f,
            status = VitalStatus.NORMAL,
            primaryRisk = "Microgravity Bone Mineral Loss & Postural Muscle Atrophy",
            relatedVitalIds = listOf(VitalId.BONE_EXERCISE_MIN),
            spaceflightEffects = listOf(
                "1.0% to 1.5% bone density loss per month in weight-bearing bones",
                "Spinal lengthening (up to 5–7 cm) causing space back pain",
                "Type I slow-twitch muscle fiber conversion to fast-twitch"
            ),
            physiologicalIndicators = listOf(
                "Daily exercise compliance < 90 min",
                "Spinal height elongation > 4.5 cm",
                "Lower extremity calf muscle circumference reduction"
            ),
            hazards = listOf(HazardType.GRAVITY_FIELDS),
            countermeasures = listOf(
                Countermeasure(
                    "ARED Axial Skeleton Loading",
                    "High-load compressive squats, deadlifts, and heel raises",
                    "Daily 60 min",
                    "ARED System"
                ),
                Countermeasure(
                    "T2 Treadmill Harness Bungee Load",
                    "Run with bungee harness providing 70% body weight equivalent",
                    "Daily 30 min",
                    "T2 Treadmill"
                )
            ),
            symptomChecklist = listOf(
                SymptomPrompt("sym_musc_1", "Lumbar stiffness or dull ache along lengthened spine", 1, "Perform fetal tuck spinal stretch"),
                SymptomPrompt("sym_musc_2", "Calf muscle cramping following ARED session", 1, "Review magnesium & hydration log"),
                SymptomPrompt("sym_musc_3", "Acute sharp radicular spinal pain radiating into leg", 3, "Cease heavy axial loading; ultrasound spine")
            ),
            clinicalAdvice = "Execute 90-minute daily countermeasure exercise split between ARED and T2. Do not skip axial loading cycles."
        )
    )

    fun getDefaultClinicalProtocols(): List<ClinicalProtocol> = listOf(
        // 1. CDME-PRO-MAX HYPERCAPNIA TACHYCARDIA PROTOCOL
        ClinicalProtocol(
            code = "NASA-HYPERCAP-TACHY",
            title = "Hypercapnia Tachycardia Protocol",
            category = "Emergency Life-Support & Cardio",
            severity = VitalStatus.CRITICAL,
            triggerCondition = "Cabin pCO2 > 3.0 mmHg AND Resting HR > 100 BPM",
            timerDurationSeconds = 240,
            timerLabel = "Tactical 4-7-8 Breathing (4m)",
            steps = listOf(
                ProtocolStep(1, "Alert Flight Surgeon queue & flag life-support telemetry", "Automated alert sent to Houston uplink delta queue"),
                ProtocolStep(2, "Switch ECLSS Amine scrub beds to high-throughput purge mode", "Directs air scrubber flow to active desorption"),
                ProtocolStep(3, "Initiate 4-7-8 Tactical Breathing pacing beacon", "Inhale through nose 4s, hold breath 7s, exhale slowly 8s"),
                ProtocolStep(4, "Don emergency O2 mask if cabin pCO2 exceeds 5.0 mmHg", "Provides pure 100% O2 to reverse hypercapnic narcosis"),
                ProtocolStep(5, "Inspect module ventilation ducts for pocket dead-zones", "Verify circulation fan velocity > 0.08 m/s across crew bays"),
                ProtocolStep(6, "Log follow-up resting Heart Rate and SpO2 in 10 minutes", "Confirm HR drops below 90 BPM and SpO2 >= 98%")
            )
        ),
        // 2. CDME-PRO-MAX MICROGRAVITY RESUSCITATION & CPR PROTOCOL
        ClinicalProtocol(
            code = "NASA-ACLS-ZGRAV",
            title = "Microgravity CPR & Resuscitation",
            category = "Emergency Resuscitation ACLS",
            severity = VitalStatus.CRITICAL,
            triggerCondition = "Unresponsive Crew Member • Pulseless Cardiac Arrest",
            timerDurationSeconds = 120,
            timerLabel = "CPR Cycle Assessment (2m)",
            steps = listOf(
                ProtocolStep(1, "Position patient against spacecraft bulkhead & engage restraints", "Standard floor compressions fail in 0g without counter-resistance"),
                ProtocolStep(2, "Adopt Hand-Stand or Reverse-Bearhug resuscitation method", "Anchor feet on seat tracks or bulkhead straps to generate compression force"),
                ProtocolStep(3, "Deliver compressions at 100–120 BPM at 5 cm depth", "Follow audible and visual CPR metronome beacon"),
                ProtocolStep(4, "Attach automated external defibrillator (AED) & analyze rhythm", "Shock if VF/pulseless VT; continue CPR immediately"),
                ProtocolStep(5, "Administer Epinephrine 1 mg IV/IO every 3–5 minutes", "Deterministic Safety Interlock enforces ACLS max drug ceiling"),
                ProtocolStep(6, "Log all administered drugs and timestamps into encrypted delta-log", "Encrypted delta queued for Houston post-resuscitation uplink")
            )
        ),
        // 3. CDME-PRO-MAX AUTONOMOUS EMERGENCY RAG & SAFETY INTERLOCK PROTOCOL
        ClinicalProtocol(
            code = "NASA-SAFE-INTERLOCK",
            title = "Emergency RAG & Safety Interlock",
            category = "Blackout Decision Engine",
            severity = VitalStatus.CRITICAL,
            triggerCondition = "Communication Blackout with Houston • Emergency Care Required",
            timerDurationSeconds = 180,
            timerLabel = "Safety Gate Verification",
            steps = listOf(
                ProtocolStep(1, "Engage offline local edge decision engine", "Isolates external networks; switches to deterministic SQLite/Room vectors"),
                ProtocolStep(2, "Pass clinical emergency query into Safety Interlock Gate", "Hard-coded Boolean rules intercept suggestions before rendering"),
                ProtocolStep(3, "Verify life-support override does not exceed O2 flammability (FiO2 <= 24.1%)", "Interlock halts any override exceeding Apollo/ISS fire ceiling"),
                ProtocolStep(4, "Enforce strict ACLS dosage ceilings before drug delivery", "Interlock blocks non-standard or off-label unverified dosing"),
                ProtocolStep(5, "Append signed action delta-log to Room database for uplink", "Cryptographically signs crew action with Sol and timestamp")
            )
        ),
        // 4. CDME-PRO-MAX ENVIRONMENTAL CARBON DYNAMICS & WANNINKHOF FLUX PROTOCOL
        ClinicalProtocol(
            code = "NASA-ENV-WANNINKHOF",
            title = "Environmental Carbon Dynamics & Wanninkhof Flux",
            category = "Environmental Life Support",
            severity = VitalStatus.MONITOR,
            triggerCondition = "Rolling CO2 drift > 0.5 mmHg/hour • Environmental Gas Excursion",
            timerDurationSeconds = 300,
            timerLabel = "Wanninkhof Flux Sampling (5m)",
            steps = listOf(
                ProtocolStep(1, "Sample 1-second rolling CO2 sensor telemetry stream", "Evaluate Wanninkhof gas-environmental flux parameterization"),
                ProtocolStep(2, "Check atmospheric partial pressure (pCO2, pO2, pN2)", "Assess risk of sub-clinical cognitive and psychomotor impairment"),
                ProtocolStep(3, "Trigger automated amine bed regeneration cycle", "Expels saturated CO2 canister to space vacuum"),
                ProtocolStep(4, "Verify cabin air exchange rate between modules", "Ensure hatch ventilation curtains are active and unblocked")
            )
        ),
        // 5. ELEVATED HEART RATE TACHYCARDIA
        ClinicalProtocol(
            code = "NASA-MED-C04",
            title = "Elevated Heart Rate & Tachycardia",
            category = "Cardio",
            severity = VitalStatus.CRITICAL,
            triggerCondition = "Resting HR > 100 BPM",
            timerDurationSeconds = 300,
            timerLabel = "Paced Breathing (5m)",
            steps = listOf(
                ProtocolStep(1, "Stop exertion & rest in neutral microgravity posture", "Relieves cardiovascular workload immediately"),
                ProtocolStep(2, "5m paced breathing (4s in, 6s out)", "Stimulates vagal tone and parasympathetic deceleration"),
                ProtocolStep(3, "Drink 500 mL electrolyte water", "Restores intravascular volume"),
                ProtocolStep(4, "Attach 12-lead ECG electrodes", "Rule out supraventricular tachycardia or arrhythmias"),
                ProtocolStep(5, "Log follow-up Heart Rate", "Record recovery delta in mission log")
            )
        ),
        // 6. SLEEP DEFICIT RECOVERY
        ClinicalProtocol(
            code = "NASA-HRP-7C",
            title = "Sleep Deficit & Circadian Desynchrony",
            category = "Circadian",
            severity = VitalStatus.MONITOR,
            triggerCondition = "Sleep Duration < 6.5 hrs for 2+ consecutive sols",
            timerDurationSeconds = 600,
            timerLabel = "Pre-Sleep Wind-Down (10m)",
            steps = listOf(
                ProtocolStep(1, "Set cabin lighting to 450nm amber spectrum", "Suppresses blue light melatonin inhibition"),
                ProtocolStep(2, "Verify sleep fan airflow across face", "Prevents carbon dioxide dome accumulation during sleep"),
                ProtocolStep(3, "Fit earplugs & latch acoustic sound-damping door", "Reduces module pump and fan background decibels to < 45 dBA"),
                ProtocolStep(4, "Set sleep bay temperature to 19°C", "Facilitates core body temperature drop required for REM sleep")
            )
        ),
        // 7. DEHYDRATION & RENAL
        ClinicalProtocol(
            code = "NASA-HRP-F08",
            title = "Dehydration & Renal Stone Prevention",
            category = "Renal",
            severity = VitalStatus.MONITOR,
            triggerCondition = "Daily Hydration < 2,000 mL OR Urine SG > 1.025",
            steps = listOf(
                ProtocolStep(1, "Drink 500 mL electrolyte water now", "Immediate rehydration bolus"),
                ProtocolStep(2, "Take 20 mEq potassium citrate tablet", "Alkalinizes urine to prevent calcium crystallization"),
                ProtocolStep(3, "Check urine pH & specific gravity test strip", "Target pH 6.5–7.0 and SG < 1.015")
            )
        ),
        // 8. SOLAR RADIATION STORM SHELTER
        ClinicalProtocol(
            code = "NASA-RAD-SPE",
            title = "Solar Storm Shelter SPE Radiation",
            category = "Radiation",
            severity = VitalStatus.CRITICAL,
            triggerCondition = "Dosimeter Rate > 1.20 mSv/day OR Space Weather SPE Alert",
            steps = listOf(
                ProtocolStep(1, "Abort all extravehicular activities (EVA) & seal hatch", "Immediate ingress into pressurized hull"),
                ProtocolStep(2, "Enter central water-wall storm shelter bay", "Hydrogen-rich water tanks provide optimal proton shielding"),
                ProtocolStep(3, "Stack polyethylene cargo bags against sun-facing bulkhead", "Increases secondary shield thickness by 15 g/cm2"),
                ProtocolStep(4, "Monitor active dosimeter hourly & record cumulative dose", "Report readings to Flight Surgeon when comm link re-opens")
            )
        ),
        // 9. SANS VISUAL DISTURBANCE
        ClinicalProtocol(
            code = "NASA-SANS-OPTIC",
            title = "SANS Visual Disturbance Protocol",
            category = "Neuro & Ocular",
            severity = VitalStatus.MONITOR,
            triggerCondition = "Optic Sheath > 5.8 mm OR Sudden Visual Acuity Drop",
            steps = listOf(
                ProtocolStep(1, "Perform bedside ultrasound of optic nerve sheath (ONSD)", "Acquire 30-degree lateral acoustic window scans"),
                ProtocolStep(2, "Test visual acuity with Rosenbaum pocket card", "Record near and distance vision refractive change"),
                ProtocolStep(3, "Execute 2-hour Lower Body Negative Pressure (LBNP) session", "Pulls 500 mL venous volume into lower limbs"),
                ProtocolStep(4, "Check intraocular pressure with Tono-Pen", "Ensure IOP <= 21 mmHg")
            )
        )
    )
}
