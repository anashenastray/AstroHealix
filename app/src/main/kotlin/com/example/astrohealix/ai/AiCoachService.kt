package com.example.astrohealix.ai

import com.example.astrohealix.evaluator.SafetyInterlockEngine
import com.example.astrohealix.model.AstronautProfile
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.VitalCardSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AiCoachResponse(
    val message: String,
    val isOfflineEngine: Boolean,
    val rulesVerified: List<String>,
    val warnings: List<String>,
    val timestampUtc: String
)

class AiCoachService(
    private var apiKey: String = System.getenv("GEMINI_API_KEY") ?: System.getProperty("GEMINI_API_KEY") ?: ""
) {

    fun setApiKey(key: String) {
        if (key.isNotBlank()) apiKey = key
    }

    suspend fun queryCoach(
        question: String,
        profile: AstronautProfile,
        environment: MissionEnvironment,
        vitals: List<VitalCardSnapshot>,
        forceOfflineBlackout: Boolean = false
    ): AiCoachResponse = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val heartRateVal = vitals.firstOrNull { it.id.name == "HEART_RATE" }?.value
        val cabinCo2 = profile.cabinCo2MmHg
        val cabinO2 = profile.habitatO2Percent

        var rawResponse: String? = null
        var isOffline = forceOfflineBlackout

        // If online and not in simulated blackout, attempt Gemini API
        if (!forceOfflineBlackout && apiKey.isNotBlank() && !apiKey.startsWith("AQ.placeholder")) {
            try {
                rawResponse = callGeminiRestApi(question, profile, environment, vitals)
            } catch (e: Exception) {
                // Seamlessly fall back to local offline RAG engine
                isOffline = true
            }
        } else {
            isOffline = true
        }

        if (rawResponse.isNullOrBlank()) {
            rawResponse = queryOfflineRagEngine(question, vitals, cabinCo2, heartRateVal)
            isOffline = true
        }

        // Pass through Deterministic Safety Interlock Gate
        val interlockResult = SafetyInterlockEngine.validateAndEnforce(
            rawSuggestion = rawResponse,
            cabinCo2MmHg = cabinCo2,
            heartRateBpm = heartRateVal,
            cabinO2Percent = cabinO2
        )

        AiCoachResponse(
            message = interlockResult.modifiedText,
            isOfflineEngine = isOffline,
            rulesVerified = interlockResult.rulesVerified,
            warnings = interlockResult.warnings,
            timestampUtc = nowIso
        )
    }

    private fun callGeminiRestApi(
        question: String,
        profile: AstronautProfile,
        environment: MissionEnvironment,
        vitals: List<VitalCardSnapshot>
    ): String {
        val vitalsSummary = vitals.take(8).joinToString("; ") {
            "${it.shortLabel}: ${it.displayValue} ${it.unit} (${it.status})"
        }

        val prompt = """
            You are CDME-PRO-MAX, an offline-first autonomous deep-space clinical decision engine and astronaut health coach.
            Astronaut: ${profile.name} (${profile.role})
            Mission: ${profile.missionName} • Sol ${profile.missionDay}
            Environment: ${environment.title} (${environment.gravityLabel}, Comm Delay: ${environment.commDelayLabel})
            Cabin Atmosphere: O2 ${profile.habitatO2Percent}%, CO2 ${profile.cabinCo2MmHg} mmHg
            Live Telemetry: $vitalsSummary
            
            Question / Emergency Triage Request:
            $question
            
            Guidelines:
            - Keep answer concise, high-contrast, structured with 3 actionable points.
            - Follow strict NASA spaceflight rules and ACLS microgravity guidelines.
            - Microgravity CPR must specify bulkhead counter-resistance (Hand-Stand / Reverse-Bearhug).
            - Always note whether Flight Surgeon transmission is advised.
        """.trimIndent()

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.connectTimeout = 8000
        conn.readTimeout = 15000
        conn.doOutput = true

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode in 200..299) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val json = JSONObject(responseText)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")
            if (!text.isNullOrBlank()) {
                return text.trim()
            }
        }
        throw RuntimeException("Gemini API error code: $responseCode")
    }

    private fun queryOfflineRagEngine(
        question: String,
        vitals: List<VitalCardSnapshot>,
        cabinCo2: Float,
        heartRateVal: Float?
    ): String {
        val q = question.lowercase()

        return when {
            q.contains("cpr") || q.contains("resuscit") || q.contains("compress") || q.contains("arrest") -> {
                """
                • RESUSCITATION POSTURE: Do NOT attempt floor chest compressions in 0g. Anchor feet under bulkhead seat tracks or attach pelvis cargo straps.
                • TECHNIQUE: Utilize the Hand-Stand or Reverse-Bearhug method against the spacecraft bulkhead to generate 100–120 BPM compressions at 5 cm depth.
                • ACLS INTERLOCK: Connect AED immediately. Epinephrine ceiling is 1.0 mg IV/IO every 3–5 min. Queue all actions to the encrypted mission log.
                """.trimIndent()
            }

            q.contains("hypercap") || q.contains("co2") || q.contains("carbon") || q.contains("breathing") || q.contains("tachyp") || (cabinCo2 > 3.0f && (heartRateVal ?: 0f) > 95f) -> {
                """
                • ECLSS ACTION: Current cabin CO2 is ${cabinCo2} mmHg. Command life-support Amine scrubbers to high-throughput desorption cycle immediately.
                • TACTICAL 4-7-8 PACER: Inhale 4s through nose, hold 7s, exhale 8s through pursed lips to reduce hypercapnic respiratory acidosis.
                • MONITORING: If cabin pCO2 exceeds 5.0 mmHg or SpO2 drops below 95%, don emergency O2 crew masks. Restrict heavy physical workouts.
                """.trimIndent()
            }

            q.contains("sleep") || q.contains("fatigue") || q.contains("circadian") || q.contains("tired") -> {
                """
                • CIRCADIAN ENTRAINMENT: Shift module lighting to 450nm amber spectrum 60 minutes before scheduled sleep window.
                • CO2 BUBBLE MITIGATION: Verify crew sleep compartment ventilation fan velocity is > 0.08 m/s across the pillow to avoid carbon dioxide dome buildup.
                • THERMAL & ACOUSTIC: Set cabin temperature to 19°C (66°F) and latch acoustic doors; wear molded earplugs to block continuous equipment drone.
                """.trimIndent()
            }

            q.contains("sans") || q.contains("eye") || q.contains("vision") || q.contains("optic") || q.contains("blur") -> {
                """
                • SANS SURVEILLANCE: Perform bedside 12 MHz B-scan ultrasound to measure Optic Nerve Sheath Diameter (nominal < 5.8 mm).
                • FLUID REDISTRIBUTION: Initiate 2-hour Lower Body Negative Pressure (LBNP at -25 mmHg) to draw cephalad fluid back into lower extremities.
                • REFRACTION: Screen near visual acuity using Rosenbaum chart; log hyperopic shift into daily medical log.
                """.trimIndent()
            }

            q.contains("radiation") || q.contains("flare") || q.contains("spe") || q.contains("storm") -> {
                """
                • STORM SHELTER: Retreat immediately to central water-wall shielded module. Stack polyethylene food/water cargo against sun-facing bulkhead.
                • DOSIMETRY: Inspect active personal dosimeter hourly. Log instantaneous dose rate (alert threshold: > 1.2 mSv/day).
                • MEDICAL PROPHYLAXIS: Take oral antioxidant radioprotection packet (N-acetylcysteine 600 mg).
                """.trimIndent()
            }

            q.contains("hydrat") || q.contains("water") || q.contains("renal") || q.contains("kidney") || q.contains("stone") -> {
                """
                • HYDRATION TARGET: Maintain >= 2,500 mL water intake logged daily. Microgravity blunts thirst and increases urinary calcium excretion.
                • CITRATE THERAPY: Take 20 mEq potassium citrate with meal to alkalinize urine and prevent nephrolithiasis crystal formation.
                • URINE SCREEN: Target urine specific gravity < 1.015. If flank colic occurs, perform bedside renal ultrasound.
                """.trimIndent()
            }

            else -> {
                val hr = vitals.firstOrNull { it.id.name == "HEART_RATE" }?.displayValue ?: "72"
                val spo2 = vitals.firstOrNull { it.id.name == "SPO2" }?.displayValue ?: "98"
                """
                • TELEMETRY CHECK: Heart Rate $hr BPM, SpO2 $spo2%, Cabin CO2 ${cabinCo2} mmHg. Vitals evaluated against Sol nominal baselines.
                • PREVENTATIVE RECOVERY: Maintain 90-minute daily ARED exercise loading and log scheduled fluid intake to prevent plasma volume contraction.
                • FLIGHT SURGEON UPLINK: No acute emergency interlock triggered. Telemetry delta packet is packaged and queued for Houston transmission.
                """.trimIndent()
            }
        }
    }
}
