package com.example.astrohealix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.HealthCard
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.OnColoredButton
import com.example.astrohealix.ui.theme.PrivateAccent
import com.example.astrohealix.ui.theme.PrivateButtonContainer
import com.example.astrohealix.ui.theme.PrivateSurface
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun WellbeingScreen(
    state: AstroUiState,
    onLogWellbeingCheckIn: (moodScore: Int, stressLevel: Int, fatigueLevel: Int, notes: String?) -> Unit,
    onRequestConfidentialSupport: (category: String, note: String?) -> Unit,
    onTapVital: (VitalId) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodSnapshot = remember(state.vitals) { state.vitals.find { it.id == VitalId.MOOD_INDEX } }
    val sleepSnapshot = remember(state.vitals) { state.vitals.find { it.id == VitalId.SLEEP_HOURS } }

    var moodSlider by remember {
        mutableFloatStateOf((moodSnapshot?.value ?: 7f).coerceIn(1f, 10f))
    }
    var stressSlider by remember { mutableFloatStateOf(2f) }
    var fatigueSlider by remember { mutableFloatStateOf(2f) }
    var checkInNote by remember { mutableStateOf("") }
    var checkInSavedMessage by remember { mutableStateOf<String?>(null) }

    var showSupportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item(key = "wellbeing-title") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PSYCHOLOGICAL WELLBEING",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Behavioral health & sleep recovery",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Privacy Notice
        item(key = "privacy-notice") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrivateSurface)
                    .border(1.dp, PrivateAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Private Data",
                    tint = PrivateAccent,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "PRIVATE & SENSITIVE",
                        color = PrivateAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This information is treated as private and sensitive.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Prominent MOOD_INDEX & SLEEP_HOURS Vital Cards
        if (moodSnapshot != null) {
            item(key = "mood-card") {
                HealthCard(
                    snapshot = moodSnapshot,
                    onTapCard = onTapVital,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (sleepSnapshot != null) {
            item(key = "sleep-card") {
                HealthCard(
                    snapshot = sleepSnapshot,
                    onTapCard = onTapVital,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Self-Report Quick Entry Card
        item(key = "self-report-card") {
            val moodInt = moodSlider.roundToInt().coerceIn(1, 10)
            val stressInt = stressSlider.roundToInt().coerceIn(1, 5)
            val fatigueInt = fatigueSlider.roundToInt().coerceIn(1, 5)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgSurfaceHover)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "[SELF-REPORTED]",
                            color = BorderActive,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mood Slider (1-10)
                LabeledScoreSlider(
                    label = "Mood Rating",
                    value = moodSlider,
                    displayScore = "$moodInt / 10",
                    range = 1f..10f,
                    steps = 8,
                    lowAnchor = "1 Low",
                    highAnchor = "10 Optimal",
                    onValueChange = {
                        moodSlider = it
                        checkInSavedMessage = null
                    }
                )

                // Stress Slider (1-5)
                LabeledScoreSlider(
                    label = "Stress Level",
                    value = stressSlider,
                    displayScore = "$stressInt / 5",
                    range = 1f..5f,
                    steps = 3,
                    lowAnchor = "1 Calm",
                    highAnchor = "5 High",
                    onValueChange = {
                        stressSlider = it
                        checkInSavedMessage = null
                    }
                )

                // Fatigue Slider (1-5)
                LabeledScoreSlider(
                    label = "Fatigue Level",
                    value = fatigueSlider,
                    displayScore = "$fatigueInt / 5",
                    range = 1f..5f,
                    steps = 3,
                    lowAnchor = "1 Rested",
                    highAnchor = "5 Exhausted",
                    onValueChange = {
                        fatigueSlider = it
                        checkInSavedMessage = null
                    }
                )

                OutlinedTextField(
                    value = checkInNote,
                    onValueChange = { checkInNote = it },
                    label = { Text("Private Reflection (Optional)", color = TextSecondary) },
                    placeholder = { Text("Sleep quality, workload, isolation notes...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = BgSurfaceHover,
                        unfocusedContainerColor = BgSurfaceHover,
                        focusedBorderColor = BorderActive,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                checkInSavedMessage?.let { savedMsg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusNormal,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = savedMsg,
                            color = StatusNormal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = {
                        onLogWellbeingCheckIn(
                            moodInt,
                            stressInt,
                            fatigueInt,
                            checkInNote.trim().ifEmpty { null }
                        )
                        checkInNote = ""
                        checkInSavedMessage = "Saved private [SELF-REPORTED] check-in"
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BorderActive,
                        contentColor = BgPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = "Save Private Check-In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Confidential Support Section (distinctly styled)
        item(key = "confidential-support") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrivateSurface)
                    .border(1.dp, PrivateAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Confidential Support",
                        tint = PrivateAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Confidential Psychological Support",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Direct, encrypted Flight Psychologist or Peer Support channel. Support requests remain separated from operational mission telemetry.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Button(
                    onClick = { showSupportDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrivateButtonContainer,
                        contentColor = OnColoredButton
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Request Confidential Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (state.supportRequests.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.supportRequests.take(3).forEach { req ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BgSurface)
                                    .border(1.dp, PrivateAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = req.category,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = req.requestedAt,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = req.statusLabel,
                                    color = PrivateAccent,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Private Check-Ins
        if (state.wellbeingCheckIns.isNotEmpty()) {
            item(key = "recent-checkins-header") {
                Text(
                    text = "RECENT PRIVATE CHECK-INS",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }

            items(
                items = state.wellbeingCheckIns.take(5),
                key = { "checkin-${it.id}" }
            ) { entry ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mood ${entry.moodScore}/10 • Stress ${entry.stressLevel}/5 • Fatigue ${entry.fatigueLevel}/5",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "[SELF-REPORTED] • ${entry.recordedAt}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    entry.notes?.takeIf { it.isNotBlank() }?.let { noteText ->
                        Text(
                            text = noteText,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    if (showSupportDialog) {
        ConfidentialSupportDialog(
            onDismiss = { showSupportDialog = false },
            onSubmit = { category, note ->
                onRequestConfidentialSupport(category, note)
                showSupportDialog = false
            }
        )
    }
}

@Composable
private fun LabeledScoreSlider(
    label: String,
    value: Float,
    displayScore: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    lowAnchor: String,
    highAnchor: String,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayScore,
                color = BorderActive,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = BorderActive,
                activeTrackColor = BorderActive,
                inactiveTrackColor = BorderSubtle
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = lowAnchor, color = TextSecondary, fontSize = 11.sp)
            Text(text = highAnchor, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ConfidentialSupportDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, note: String?) -> Unit
) {
    val categories = listOf(
        "Flight Psychologist Consult",
        "Isolation & Confinement Stress",
        "Sleep & Circadian Coaching",
        "Private Medical Inquiry"
    )
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PrivateSurface)
                .border(1.dp, PrivateAccent, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Request Confidential Support",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "This information is treated as private and sensitive.",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrivateButtonContainer.copy(alpha = 0.25f) else BgSurface)
                            .border(
                                1.dp,
                                if (isSelected) PrivateAccent else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Confidential Note (Optional)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BgSurface,
                    unfocusedContainerColor = BgSurface,
                    focusedBorderColor = PrivateAccent,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onSubmit(selectedCategory, note.trim().ifEmpty { null }) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrivateButtonContainer,
                        contentColor = OnColoredButton
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Send Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
