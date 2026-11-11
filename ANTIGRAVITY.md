# AstroHealix — Android Architecture & Design System Guidelines (`ANTIGRAVITY.md`)

## 1. Ergonomic Invariants (Microgravity & Zero-G Touch Standards)
- **Minimum Touch Targets:** Every interactive button, chip, card, and tab has a minimum touch container area of `48dp x 48dp` (`Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)`) to ensure effortless input in microgravity or when wearing pressurized EVA interface gloves.
- **Dark-Adaptation OLED Palette:**
  - Base: Deep Space Navy (`#0B0F19`)
  - Elevated Cards: Slate Blue-Grey (`#161C2E`)
  - Interactive Hover: (`#1F2840`)
  - Subtle Border: (`#2A354D`)
  - Active Highlight: Cyan (`#00E5FF`)
  - Text Primary: Stark White (`#F4F6FB`, >= 7:1 contrast)
  - Text Secondary: Muted Slate (`#8E9BAE`)
  - Text Disabled: Data Grey (`#4A5568`)
- **Triple-Encoded Status Invariant:** Color is never the sole indicator of health status. Color is always paired with explicit text and unique geometric icons.
- **Zero-Data Ambiguity Invariant:** Disconnected sensors must never display `0` or red alert; they must display `DATA UNAVAILABLE` in neutral `#4A5568` grey.
- **Non-Diagnostic Policy:** AstroHealix provides mission decision support and crew self-assessment; it does not replace clinical diagnosis by a Flight Surgeon.

## 2. Testing & Building Commands
- Run unit tests: `./gradlew test`
- Build debug APK: `./gradlew assembleDebug`
- Launch Android App: `android run` or `./gradlew installDebug`
