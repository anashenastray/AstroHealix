# AstroHealix — System Contract & Specification (`CONTRACT.md`)

## NASA Space Apps Challenge 2026: Health Monitoring Software for Astronauts on Space Missions

AstroHealix is a space-grade, offline-first Android application built in Kotlin & Jetpack Compose implementing the **NASA VITA-SYS Design System** for autonomous astronaut health assessment on long-duration missions (Artemis, Lunar Base, Mars Transit).

---

## 1. Core Data Models & Enumerations

### `VitalId` (11 Spaceflight Health Indicators)
- `HEART_RATE` (BPM) — Cardiovascular monitoring & fluid redistribution deconditioning
- `BLOOD_PRESSURE` (mmHg) — Dual systolic / diastolic arterial vascular tone
- `SPO2` (%) — Sealed spacecraft / suit alveolar gas exchange
- `RESPIRATORY_RATE` (RPM) — Hypercapnia / cabin ventilation pockets detection
- `BODY_TEMP` (°C) — Microgravity thermoregulation & immune defense screening
- `BLOOD_GLUCOSE` (mg/dL) — Spaceflight insulin resistance & EVA cognition
- `SLEEP_HOURS` (hrs) — Circadian misalignment & fatigue tracking
- `HYDRATION_ML` (mL) — Renal calculus / kidney stone prophylaxis (2,500 mL target)
- `RADIATION_MSV` (mSv/d) — Torso active dosimeter GCR & Solar Particle Event (SPE) monitoring
- `MOOD_INDEX` (/10) — Confinement stress & behavioral health screening
- `BONE_EXERCISE_MIN` (min) — ARED resistive mechanical loading & bone demineralization countermeasure

### `VitalStatus` (Triple-Encoded Accessibility Standard)
- `NORMAL` — Medical Emerald Green (`#00E676`) + Circle Icon
- `MONITOR` — Caution Amber (`#FFB300`) + Triangle Warning Icon
- `CRITICAL` / `ATTENTION REQUIRED` — Alert Red (`#FF3D00`) + Octagon Alert Icon
- `UNAVAILABLE` — Data Grey (`#4A5568`) + Slash/Dash Offline Icon
  - **Critical Invariant:** Missing, disconnected, or un-synced data MUST NEVER be coerced to `0` or `CRITICAL`. It renders as `DATA UNAVAILABLE` in neutral `#4A5568` grey.

### `DataSource`
- `SENSOR` — Biometric or radiation hardware telemetry stream
- `MANUAL` — Crew manual log or guided self-exam check-in
- `ESTIMATED` — Interpolated or estimated telemetry

### `MissionEnvironment`
- `ORBITAL_STATION` (ISS, `0.00 g`, ~250 ms live link)
- `DEEP_SPACE_TRANSIT` (Transit spacecraft, `0.00 g`, ~14m 20s one-way Earth delay, autonomous crew triage mode)
- `SURFACE_BASE` (Lunar/Martian surface, `0.16–0.38 g`, regolith dust & closed-env mitigation)
- `ROVER_EVA` (Pressurized rover / EVA suit, metabolic exertion & radiation monitoring priority)

### `HazardId` (NASA Human Research Program 5 Hazards)
1. `RADIATION` — Space Radiation (Cosmic rays & SPE)
2. `ISOLATION` — Isolation & Confinement
3. `DISTANCE_DELAY` — Distance from Earth & Communication Lag
4. `GRAVITY_FIELDS` — Altered Gravity Fields (Bone/muscle loss & fluid shifts)
5. `CLOSED_ENV` — Hostile Closed Environments (Air quality, CO2 pockets, microbes)

---

## 2. Telemetry Anomaly Evaluation Rules

1. **Baseline Delta Rule 5.2 (Cardiovascular Spike):**
   - If Heart Rate exceeds upper personal baseline (`78 BPM`) by `>= 15 BPM` over rolling window, or single excursion `>= 94 BPM`, trigger `MONITOR` or `CRITICAL` alert citing `Baseline Delta Rule 5.2`.
2. **Sleep Drift Rule (3 Consecutive Cycles):**
   - If Sleep Duration drops below `< 6.0 hrs` for `3` consecutive sleep cycles, escalate alert severity to `CRITICAL` (`ATTENTION REQUIRED`).
3. **Card Priority Sorting Invariant:**
   - Vital telemetry cards automatically sort by severity: `CRITICAL` -> `MONITOR` -> `NORMAL` -> `UNAVAILABLE`.
4. **Offline Queue Reconciliation:**
   - While `isOffline` or in `LINK_BLACKOUT`, manual entries append to a local persistent queue. Upon restoration, queued items reconcile into telemetry history, transitioning header to `[SYNCED]`.
