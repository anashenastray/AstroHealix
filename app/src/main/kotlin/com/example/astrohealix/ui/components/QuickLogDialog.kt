package com.example.astrohealix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusCriticalBg
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun QuickLogDialog(
    initialVitalId: VitalId = VitalId.HYDRATION_ML,
    onDismiss: () -> Unit,
    onSave: (vitalId: VitalId, value: Float, secondaryValue: Float?, isAdditive: Boolean, note: String?) -> Unit
) {
    var selectedVital by remember { mutableStateOf(initialVitalId) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var valueText by remember(selectedVital) {
        mutableStateOf(defaultValueForVital(selectedVital))
    }
    var secondaryValueText by remember(selectedVital) { mutableStateOf("76") }
    var noteText by remember { mutableStateOf("") }
    var isHydrationAdditive by remember(selectedVital) { mutableStateOf(selectedVital == VitalId.HYDRATION_ML) }

    val parsedPrimary = valueText.trim().toFloatOrNull()
    val parsedSecondary = if (selectedVital == VitalId.BLOOD_PRESSURE) {
        secondaryValueText.trim().toFloatOrNull()
    } else {
        null
    }

    val boundsFeedback = validateQuickLogInput(
        vitalId = selectedVital,
        primary = parsedPrimary,
        secondary = parsedSecondary,
        isAdditive = selectedVital == VitalId.HYDRATION_ML && isHydrationAdditive
    )

    val dialogShape = RoundedCornerShape(8.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = BorderActive,
        unfocusedBorderColor = BorderSubtle,
        errorBorderColor = StatusCritical,
        focusedLabelColor = BorderActive,
        unfocusedLabelColor = TextSecondary,
        errorLabelColor = StatusCritical,
        focusedContainerColor = BgSurfaceHover,
        unfocusedContainerColor = BgSurfaceHover,
        errorContainerColor = BgSurfaceHover,
        cursorColor = BorderActive
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .clip(dialogShape)
                .background(BgSurface, dialogShape)
                .border(1.dp, BorderSubtle, dialogShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Log Health Measurement",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "MANUAL CREW TELEMETRY ENTRY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Vital Dropdown Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PARAMETER",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSurfaceHover)
                                .border(
                                    1.dp,
                                    if (dropdownExpanded) BorderActive else BorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = getVitalIcon(selectedVital),
                                    contentDescription = selectedVital.title,
                                    tint = BorderActive,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${selectedVital.title} (${selectedVital.unit})",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Select vital",
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .background(BgSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        ) {
                            VitalId.entries.forEach { vital ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = getVitalIcon(vital),
                                                contentDescription = null,
                                                tint = if (vital == selectedVital) BorderActive else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${vital.title} (${vital.unit})",
                                                color = if (vital == selectedVital) BorderActive else TextPrimary,
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedVital = vital
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }
                    }
                }

                // [SELF-REPORTED] Notice for Mood & Sleep
                if (selectedVital == VitalId.MOOD_INDEX || selectedVital == VitalId.SLEEP_HOURS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgSurfaceHover)
                            .border(1.dp, BorderActive.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Self-Reported",
                            tint = BorderActive,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "[SELF-REPORTED] Subjective crew observation recorded as self-reported telemetry.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Quick buttons for Hydration
                if (selectedVital == VitalId.HYDRATION_ML) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Water Pouch (+mL Intake)",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(250, 500, 750).forEach { amount ->
                                val isChosen = valueText.trim() == amount.toString()
                                OutlinedButton(
                                    onClick = {
                                        valueText = amount.toString()
                                        isHydrationAdditive = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isChosen) BgSurfaceHover else BgSurface,
                                        contentColor = if (isChosen) BorderActive else TextPrimary
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChosen) BorderActive else BorderSubtle
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 6.dp,
                                        vertical = 8.dp
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text(
                                        text = "+$amount mL",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary Numeric Input Field
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = {
                        Text(
                            text = if (selectedVital == VitalId.BLOOD_PRESSURE) {
                                "Systolic (${selectedVital.unit})"
                            } else {
                                "${selectedVital.title} (${selectedVital.unit})"
                            }
                        )
                    },
                    isError = !boundsFeedback.isPhysiologicallyValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Diastolic Input Field for Blood Pressure
                if (selectedVital == VitalId.BLOOD_PRESSURE) {
                    OutlinedTextField(
                        value = secondaryValueText,
                        onValueChange = { secondaryValueText = it },
                        label = { Text("Diastolic (mmHg)") },
                        isError = !boundsFeedback.isPhysiologicallyValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bounds Check & Baseline Feedback Banner
                val feedbackBg = when {
                    !boundsFeedback.isPhysiologicallyValid -> StatusCriticalBg
                    boundsFeedback.isOutsideNominal -> StatusMonitorBg
                    else -> BgSurfaceHover
                }
                val feedbackBorder = when {
                    !boundsFeedback.isPhysiologicallyValid -> StatusCritical
                    boundsFeedback.isOutsideNominal -> StatusMonitor
                    else -> BorderSubtle
                }
                val feedbackTextColor = when {
                    !boundsFeedback.isPhysiologicallyValid -> StatusCritical
                    boundsFeedback.isOutsideNominal -> StatusMonitor
                    else -> StatusNormal
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(feedbackBg)
                        .border(1.dp, feedbackBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = boundsFeedback.message,
                        color = feedbackTextColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                // Optional Notes Input Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes / Symptoms (Optional)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Buttons (48dp minimum height for microgravity compliance)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val primaryVal = parsedPrimary ?: return@Button
                            onSave(
                                selectedVital,
                                primaryVal,
                                parsedSecondary,
                                selectedVital == VitalId.HYDRATION_ML && isHydrationAdditive,
                                noteText.trim().ifBlank { null }
                            )
                            onDismiss()
                        },
                        enabled = boundsFeedback.isPhysiologicallyValid && parsedPrimary != null,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BorderActive,
                            contentColor = BgPrimary,
                            disabledContainerColor = BorderSubtle,
                            disabledContentColor = TextDisabled
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class QuickLogBoundsFeedback(
    val isPhysiologicallyValid: Boolean,
    val isOutsideNominal: Boolean,
    val message: String
)

private fun defaultValueForVital(vitalId: VitalId): String = when (vitalId) {
    VitalId.HEART_RATE -> "72"
    VitalId.BLOOD_PRESSURE -> "118"
    VitalId.SPO2 -> "98"
    VitalId.RESPIRATORY_RATE -> "15"
    VitalId.BODY_TEMP -> "36.8"
    VitalId.BLOOD_GLUCOSE -> "95"
    VitalId.SLEEP_HOURS -> "7.2"
    VitalId.HYDRATION_ML -> "500"
    VitalId.RADIATION_MSV -> "0.75"
    VitalId.MOOD_INDEX -> "8.0"
    VitalId.BONE_EXERCISE_MIN -> "60"
}

private fun physiologicalBounds(vitalId: VitalId): ClosedFloatingPointRange<Float> = when (vitalId) {
    VitalId.HEART_RATE -> 25f..240f
    VitalId.BLOOD_PRESSURE -> 50f..260f
    VitalId.SPO2 -> 50f..100f
    VitalId.RESPIRATORY_RATE -> 4f..60f
    VitalId.BODY_TEMP -> 30f..44f
    VitalId.BLOOD_GLUCOSE -> 20f..600f
    VitalId.SLEEP_HOURS -> 0f..24f
    VitalId.HYDRATION_ML -> 0f..10000f
    VitalId.RADIATION_MSV -> 0f..500f
    VitalId.MOOD_INDEX -> 1f..10f
    VitalId.BONE_EXERCISE_MIN -> 0f..480f
}

private fun validateQuickLogInput(
    vitalId: VitalId,
    primary: Float?,
    secondary: Float?,
    isAdditive: Boolean = false
): QuickLogBoundsFeedback {
    val range = physiologicalBounds(vitalId)
    if (primary == null || primary.isNaN()) {
        return QuickLogBoundsFeedback(
            isPhysiologicallyValid = false,
            isOutsideNominal = true,
            message = "Enter a valid numeric value (${range.start.toInt()}–${range.endInclusive.toInt()} ${vitalId.unit})."
        )
    }
    if (primary !in range) {
        return QuickLogBoundsFeedback(
            isPhysiologicallyValid = false,
            isOutsideNominal = true,
            message = "Out of physiological bounds (${range.start}–${range.endInclusive} ${vitalId.unit})."
        )
    }
    if (vitalId == VitalId.BLOOD_PRESSURE) {
        if (secondary == null || secondary.isNaN()) {
            return QuickLogBoundsFeedback(
                isPhysiologicallyValid = false,
                isOutsideNominal = true,
                message = "Enter a valid diastolic value (30–160 mmHg)."
            )
        }
        if (secondary !in 30f..160f || secondary >= primary) {
            return QuickLogBoundsFeedback(
                isPhysiologicallyValid = false,
                isOutsideNominal = true,
                message = "Diastolic must be 30–160 mmHg and lower than systolic."
            )
        }
    }

    val def = CatalogData.VITAL_DEFINITIONS[vitalId]
    val baseline = def?.baseline
    if (vitalId == VitalId.HYDRATION_ML && isAdditive) {
        val target = baseline?.targetDaily?.toInt() ?: 2500
        return QuickLogBoundsFeedback(
            isPhysiologicallyValid = true,
            isOutsideNominal = false,
            message = "Additive pouch intake (+${primary.toInt()} mL toward $target mL daily target)."
        )
    }
    if (baseline != null && (primary < baseline.nominalMin || primary > baseline.nominalMax)) {
        return QuickLogBoundsFeedback(
            isPhysiologicallyValid = true,
            isOutsideNominal = true,
            message = "Outside personal nominal baseline (${baseline.nominalMin}–${baseline.nominalMax} ${vitalId.unit})."
        )
    }

    val nominalText = if (baseline != null) {
        " (${baseline.nominalMin}–${baseline.nominalMax} ${vitalId.unit})"
    } else {
        ""
    }
    return QuickLogBoundsFeedback(
        isPhysiologicallyValid = true,
        isOutsideNominal = false,
        message = "Valid reading within personal baseline$nominalText."
    )
}
