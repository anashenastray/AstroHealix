# AstroHealix

### Health Monitoring & Autonomous Assessment Software for Astronauts on Space Missions
**2026 NASA Space Apps Challenge** | Human Exploration & Spaceflight Software

AstroHealix is a space-grade Android application built with **Kotlin** and **Jetpack Compose**, implementing the **NASA VITA-SYS Design System** for autonomous astronaut health assessment, guided exams, and trend monitoring during long-duration spaceflight.

---

## Key Features & Challenge Solutions

1. **Space-Grade OLED HUD (NASA VITA-SYS):**
   - Deep Space Navy (`#0B0F19`) dark-adaptation palette to preserve night vision on orbital night cycles.
   - Microgravity ergonomics with strict `48dp x 48dp` touch targets for gloved and fatigued crew interaction.
   - Triple-encoded accessibility: every status pairs Color + Explicit Text + Geometric Structural Icon.

2. **Interactive Astronaut Physiological Body Map:**
   - Vector canvas anatomical figure divided into selectable body zones (Neuro/Ocular, Cardiovascular, Pulmonary, Metabolic/Renal, Musculoskeletal, Immune/Dosimetry).
   - Dynamic status rings glowing with physiological status; tap any body region to inspect spaceflight deconditioning, mapped NASA HRP hazards, and guided exam instructions.

3. **NASA Human Research Program (HRP) 5 Hazards Matrix:**
   - Real-time risk modeling across all 5 hazards: Space Radiation, Isolation & Confinement, Distance from Earth (Comm Lag), Altered Gravity Fields, and Hostile Closed Environments.
   - Mission Environment Switcher (`ISS / LEO`, `Deep Space Transit`, `Lunar / Martian Surface Base`, `Pressurized Rover / EVA`).

4. **Telemetry Trend Analytics (SCR-03):**
   - Canvas-based telemetry graph plotting readings against personal baseline nominal zones (`nominalMin..nominalMax`), critical safety bounds, live cyan lines, dashed estimated points, and explicit `[DISCONNECTED PERIOD]` / `[GAP]` shaded regions.
   - Baseline Delta Rule 5.2 (Cardiovascular Spike) and 3-Cycle Sleep Drift anomaly logging.

5. **Multi-Modal Logging & Offline-First Sync Queue:**
   - Quick manual logging with single-tap +500 mL hydration increments, blood glucose (mg/dL), BP (Sys/Dia), temperature, sleep, and mood.
   - Offline cache resilient to satellite blackouts; records queue locally and automatically reconcile upon link restoration.
   - Exportable Flight Surgeon Medical Telemetry Reports (CSV and clinical summary).

6. **Interactive Mission Simulator (Built-in Demo Controls):**
   - One-tap scenario switcher:
     - `Nominal Mission Day`
     - `Chest Strap & Dosimeter Disconnect` (simulates sensor dropout -> grey UNAVAILABLE)
     - `Satellite Link Blackout` (activates OFFLINE CACHE mode and local queue)
     - `Link Restoration` (reconciles queue -> SYNCED green light)
     - `EVA Cardio Spike` (Rule 5.2 spike alert)
     - `Solar Radiation Event` (SPE storm-shelter warning)

---

## Project Structure

```
AstroHealix/
├── app/
│   ├── src/main/kotlin/com/example/astrohealix/
│   │   ├── MainActivity.kt               # Entrypoint Activity
│   │   ├── model/
│   │   │   ├── VitalModels.kt            # 11 Vitals, Statuses, Hazards, Environments
│   │   │   └── CatalogData.kt            # Baselines, Exam Guides, Routine Tasks
│   │   ├── evaluator/
│   │   │   ├── VitalEvaluator.kt         # Delta Rule 5.2, Sleep Drift, Priority Sorter
│   │   │   └── HazardEvaluator.kt        # NASA 5 Hazards Risk Scoring & Readiness
│   │   ├── simulator/
│   │   │   └── MissionSimulator.kt       # Telemetry Generator & Mission Scenarios
│   │   ├── data/
│   │   │   └── AstroRepository.kt        # Offline-first Cache & Queue Engine
│   │   └── ui/
│   │       ├── theme/                    # NASA VITA-SYS Colors, Typography, Theme
│   │       ├── components/               # HealthCard, AlertBanner, TrendChart, BodyMapHUD
│   │       └── screens/                  # Dashboard, BodyMap, Trends, Logs
│   └── src/test/kotlin/                  # Automated Unit Tests
├── CONTRACT.md                           # System Specifications & Data Contracts
└── ANTIGRAVITY.md                        # Ergonomic Invariants & Architecture Rules
```

---

## Build & Test Instructions

```bash
# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug

# Run on connected device or emulator
android run
```
