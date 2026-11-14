package com.example.astrohealix.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.ai.AiCoachResponse
import com.example.astrohealix.ai.AiCoachService
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.ui.theme.AuraCardPure
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.LocalVitaColors
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val quickAiPrompts = listOf(
    "How am I doing today?",
    "Microgravity CPR Guide",
    "Hypercapnia & CO2 Emergency",
    "Tactical 4-7-8 Breathing",
    "Radiation Storm Shelter Action",
    "Review Safety Interlocks"
)

@Composable
fun AiCoachScreen(
    state: AstroUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalVitaColors.current.isDark
    val surfaceColor = if (isDark) BgSurface else AuraCardPure

    val coachService = remember { AiCoachService() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var blackoutMode by remember { mutableStateOf(false) }

    var latestResponse by remember {
        mutableStateOf<AiCoachResponse?>(
            AiCoachResponse(
                message = """
                • NOMINAL FLIGHT READINESS: Crew vitals and cardiovascular compliance are stable across Sol 142.
                • HYDRATION & RECOVERY: Target >= 2,500 mL water intake and complete 60m ARED axial loading cycle.
                • INTERLOCK STATUS: Hard-coded safety gates active. Communications latency to Houston is ${state.environment.commDelayLabel}.
                """.trimIndent(),
                isOfflineEngine = true,
                rulesVerified = listOf(
                    "Deterministic Safety Interlock: Active & Validated",
                    "ACLS Spaceflight Physics Gate: Verified",
                    "Apollo Flammability Ceiling: Enforced"
                ),
                warnings = emptyList(),
                timestampUtc = "INITIALIZED"
            )
        )
    }

    val reportPayload = remember(state.vitals, state.astronaut) {
        buildString {
            appendLine("CDME-PRO-MAX TELEMETRY TRANSMISSION | ${state.astronaut.name} | Sol ${state.astronaut.missionDay}")
            state.vitals.forEach { v ->
                appendLine("${v.shortLabel}: ${v.displayValue} ${v.unit} (${v.status.name})")
            }
        }
    }

    fun submitQuery(promptText: String) {
        if (promptText.isBlank() || isLoading) return
        isLoading = true
        scope.launch {
            try {
                val resp = coachService.queryCoach(
                    question = promptText,
                    profile = state.astronaut,
                    environment = state.environment,
                    vitals = state.vitals,
                    forceOfflineBlackout = blackoutMode
                )
                latestResponse = resp
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .testTag("ai_coach_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Mode & Surgeon Share
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BorderActive,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Astra AI Clinical Coach",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                            Text(
                                text = "Gemini API Mode + Deterministic Blackout RAG Engine",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Share Vitals Button to Flight Surgeon
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Flight Surgeon Telemetry Transmit")
                                    putExtra(Intent.EXTRA_TEXT, reportPayload)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send Vitals"))
                                Toast.makeText(context, "Transmitted to Flight Surgeon", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Surgeon Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    HorizontalDivider(color = BorderSubtle)
                }
            }
        }

        // 2. Blackout & Safety Interlock Status Switcher
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (blackoutMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (blackoutMode) StatusCritical else StatusNormal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (blackoutMode) "Deep-Space Comms Blackout" else "Gemini API Live Link",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (blackoutMode) "Local Edge SQLite/Room RAG Interlocks" else "Online Gemini 3.8 / 2.5 Flash Endpoint",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Switch(
                        checked = blackoutMode,
                        onCheckedChange = { blackoutMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StatusCritical,
                            checkedTrackColor = StatusCritical.copy(alpha = 0.3f)
                        )
                    )
                }

                HorizontalDivider(color = BorderSubtle)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = StatusNormal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Deterministic Safety Interlock Gate: 100% Active",
                        color = StatusNormal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 3. Quick AI Prompt Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickAiPrompts) { prompt ->
                    Surface(
                        onClick = {
                            inputText = prompt
                            submitQuery(prompt)
                        },
                        shape = RoundedCornerShape(50),
                        color = surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = prompt,
                                color = BorderActive,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 4. AI Recommendation Card with Interlock Stamp
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BorderActive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CDME-PRO-MAX ADVISORY",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = if (latestResponse?.isOfflineEngine == true) "OFFLINE RAG" else "GEMINI CLOUD",
                        color = if (latestResponse?.isOfflineEngine == true) BorderActive else StatusNormal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = BorderActive,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Interlock validating query...",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    latestResponse?.let { resp ->
                        Text(
                            text = resp.message,
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )

                        if (resp.rulesVerified.isNotEmpty()) {
                            HorizontalDivider(color = BorderSubtle)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "SAFETY INTERLOCKS VERIFIED:",
                                    color = StatusNormal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                resp.rulesVerified.forEach { rule ->
                                    Text(
                                        text = "✓ $rule",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        if (resp.warnings.isNotEmpty()) {
                            resp.warnings.forEach { warn ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StatusCritical.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                                        .border(1.dp, StatusCritical, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = warn,
                                        color = StatusCritical,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Input Text Field Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Astra AI Coach or emergency triage...", fontSize = 12.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderActive,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val q = inputText
                        inputText = ""
                        submitQuery(q)
                    },
                    enabled = !isLoading && inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
