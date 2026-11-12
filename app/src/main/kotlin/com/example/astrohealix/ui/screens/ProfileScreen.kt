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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.AppThemeMode
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusNormalBg
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    state: AstroUiState,
    onUpdateProfile: (name: String, missionName: String, missionDay: Int) -> Unit,
    onUpdateBaseline: (VitalId, PersonalBaseline) -> Unit,
    onCompleteOnboarding: () -> Unit,
    onBack: (() -> Unit)? = null,
    onSelectThemeMode: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nameInput by remember(state.astronaut.name) { mutableStateOf(state.astronaut.name) }
    var missionNameInput by remember(state.astronaut.missionName) { mutableStateOf(state.astronaut.missionName) }
    var missionDayInput by remember(state.astronaut.missionDay) { mutableStateOf(state.astronaut.missionDay.toString()) }
    var profileFeedback by remember { mutableStateOf<String?>(null) }

    var selectedVital by remember { mutableStateOf(VitalId.HEART_RATE) }
    val defaultCatalogBaseline = CatalogData.VITAL_DEFINITIONS[selectedVital]?.baseline
    val activeBaseline = state.baselines[selectedVital] ?: defaultCatalogBaseline

    var nominalMinText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.nominalMin?.toString() ?: "")
    }
    var nominalMaxText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.nominalMax?.toString() ?: "")
    }
    var monitorLowText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.monitorLow?.toString() ?: "")
    }
    var monitorHighText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.monitorHigh?.toString() ?: "")
    }
    var criticalLowText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.criticalLow?.toString() ?: "")
    }
    var criticalHighText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.criticalHigh?.toString() ?: "")
    }
    var targetDailyText by remember(selectedVital, activeBaseline) {
        mutableStateOf(activeBaseline?.targetDaily?.toString() ?: "")
    }
    var baselineFeedback by remember(selectedVital) { mutableStateOf<String?>(null) }
    var baselineError by remember(selectedVital) { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (onBack != null) {
            SecondaryTopBar(title = "PROFILE & BASELINES", onBack = onBack)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Astronaut Identity & Mission Duration Card
            item(key = "profile-identity") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "ASTRONAUT PROFILE",
                                style = MaterialTheme.typography.labelLarge,
                                color = BorderActive
                            )
                            Text(
                                text = state.astronaut.role,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BgSurfaceHover)
                                .border(1.dp, BorderActive.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = state.astronaut.id,
                                color = BorderActive,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mission Duration: Day ${state.astronaut.missionDay} / ${state.astronaut.missionDurationDays}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = state.astronaut.createdAtLabel,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    ProfileTextField(
                        label = "Astronaut Name",
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            profileFeedback = null
                        }
                    )

                    ProfileTextField(
                        label = "Mission Designation",
                        value = missionNameInput,
                        onValueChange = {
                            missionNameInput = it
                            profileFeedback = null
                        }
                    )

                    ProfileTextField(
                        label = "Current Mission Day",
                        value = missionDayInput,
                        keyboardType = KeyboardType.Number,
                        onValueChange = {
                            missionDayInput = it
                            profileFeedback = null
                        }
                    )

                    profileFeedback?.let { feedbackMsg ->
                        Text(
                            text = feedbackMsg,
                            color = StatusNormal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = {
                            val parsedDay = missionDayInput.toIntOrNull()?.coerceIn(1, state.astronaut.missionDurationDays)
                                ?: state.astronaut.missionDay
                            val safeName = nameInput.trim().ifEmpty { state.astronaut.name }
                            val safeMission = missionNameInput.trim().ifEmpty { state.astronaut.missionName }
                            onUpdateProfile(safeName, safeMission, parsedDay)
                            profileFeedback = "Profile updated ($safeName • Day $parsedDay)"
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
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Access & Onboarding Check Card
            item(key = "access-onboarding") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Crew Access",
                                tint = if (state.astronaut.isAuthenticated) StatusNormal else StatusMonitor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CREW ACCESS & ONBOARDING",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(StatusNormalBg)
                                .border(1.dp, StatusNormal, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (state.hasCompletedOnboarding) "VERIFIED" else "PENDING",
                                color = if (state.hasCompletedOnboarding) StatusNormal else StatusMonitor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (state.astronaut.isAuthenticated) {
                            "Crew credential ${state.astronaut.id} authenticated. Local personal baselines active."
                        } else {
                            "Crew authentication pending."
                        },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedButton(
                        onClick = onCompleteOnboarding,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgSurfaceHover,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (state.hasCompletedOnboarding) {
                                "Re-verify Setup & Access Check"
                            } else {
                                "Complete First-Time Setup"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 2b. Display Theme Selector Card
            item(key = "display-theme") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DISPLAY THEME",
                        style = MaterialTheme.typography.labelLarge,
                        color = BorderActive
                    )
                    Text(
                        text = "Switch between Dark OLED, Light cabin console, or System default.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = state.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BgSurfaceHover else BgPrimary)
                                    .border(
                                        1.dp,
                                        if (isSelected) BorderActive else BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectThemeMode(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) BorderActive else TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Personal Baseline Editor Card
            item(key = "baseline-editor") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PERSONAL BASELINE EDITOR",
                        style = MaterialTheme.typography.labelLarge,
                        color = BorderActive
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(VitalId.entries, key = { it.name }) { vital ->
                            val isSelected = selectedVital == vital
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BgSurfaceHover else BgPrimary)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BorderActive else BorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedVital = vital }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = vital.shortLabel,
                                    color = if (isSelected) BorderActive else TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = "${selectedVital.title} (${selectedVital.unit})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileTextField(
                            label = "Nominal Min",
                            value = nominalMinText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                nominalMinText = it
                                baselineError = null
                                baselineFeedback = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTextField(
                            label = "Nominal Max",
                            value = nominalMaxText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                nominalMaxText = it
                                baselineError = null
                                baselineFeedback = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileTextField(
                            label = "Monitor Low",
                            value = monitorLowText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                monitorLowText = it
                                baselineError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTextField(
                            label = "Monitor High",
                            value = monitorHighText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                monitorHighText = it
                                baselineError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileTextField(
                            label = "Critical Low",
                            value = criticalLowText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                criticalLowText = it
                                baselineError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTextField(
                            label = "Critical High",
                            value = criticalHighText,
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = {
                                criticalHighText = it
                                baselineError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ProfileTextField(
                        label = "Daily Target (Optional)",
                        value = targetDailyText,
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = {
                            targetDailyText = it
                            baselineError = null
                        }
                    )

                    baselineError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = StatusCritical,
                            fontSize = 12.sp
                        )
                    }

                    baselineFeedback?.let { feedbackMsg ->
                        Text(
                            text = feedbackMsg,
                            color = StatusNormal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (defaultCatalogBaseline != null) {
                                    onUpdateBaseline(selectedVital, defaultCatalogBaseline)
                                    nominalMinText = defaultCatalogBaseline.nominalMin.toString()
                                    nominalMaxText = defaultCatalogBaseline.nominalMax.toString()
                                    monitorLowText = defaultCatalogBaseline.monitorLow?.toString() ?: ""
                                    monitorHighText = defaultCatalogBaseline.monitorHigh?.toString() ?: ""
                                    criticalLowText = defaultCatalogBaseline.criticalLow?.toString() ?: ""
                                    criticalHighText = defaultCatalogBaseline.criticalHigh?.toString() ?: ""
                                    targetDailyText = defaultCatalogBaseline.targetDaily?.toString() ?: ""
                                    baselineError = null
                                    baselineFeedback = "Restored HRP default for ${selectedVital.shortLabel}"
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Reset Default")
                        }

                        Button(
                            onClick = {
                                val minVal = nominalMinText.trim().toFloatOrNull()
                                val maxVal = nominalMaxText.trim().toFloatOrNull()
                                if (minVal == null || maxVal == null || minVal >= maxVal) {
                                    baselineError = "Nominal Min must be a valid number less than Nominal Max."
                                    return@Button
                                }
                                val optionalFields = listOf(
                                    monitorLowText,
                                    monitorHighText,
                                    criticalLowText,
                                    criticalHighText,
                                    targetDailyText
                                )
                                if (optionalFields.any { it.isNotBlank() && it.trim().toFloatOrNull() == null }) {
                                    baselineError = "Optional threshold fields must be valid numbers or left blank."
                                    return@Button
                                }
                                val updated = PersonalBaseline(
                                    nominalMin = minVal,
                                    nominalMax = maxVal,
                                    monitorLow = monitorLowText.trim().toFloatOrNull(),
                                    monitorHigh = monitorHighText.trim().toFloatOrNull(),
                                    criticalLow = criticalLowText.trim().toFloatOrNull(),
                                    criticalHigh = criticalHighText.trim().toFloatOrNull(),
                                    targetDaily = targetDailyText.trim().toFloatOrNull()
                                )
                                onUpdateBaseline(selectedVital, updated)
                                baselineError = null
                                baselineFeedback = "Saved baseline for ${selectedVital.shortLabel}"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BorderActive,
                                contentColor = BgPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Save Baseline", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = BgSurfaceHover,
            unfocusedContainerColor = BgSurfaceHover,
            focusedBorderColor = BorderActive,
            unfocusedBorderColor = BorderSubtle
        ),
        modifier = modifier.fillMaxWidth()
    )
}
