package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun QuickLogDialog(
    initialVitalId: VitalId = VitalId.HYDRATION_ML,
    onDismiss: () -> Unit,
    onSave: (vitalId: VitalId, value: Float, secondaryValue: Float?, isAdditive: Boolean, note: String?) -> Unit
) {
    val tabVitals = listOf(
        VitalId.HYDRATION_ML,
        VitalId.BLOOD_GLUCOSE,
        VitalId.BLOOD_PRESSURE,
        VitalId.BODY_TEMP,
        VitalId.SLEEP_HOURS,
        VitalId.MOOD_INDEX
    )

    var selectedTabIndex by remember {
        val initialIdx = tabVitals.indexOf(initialVitalId)
        mutableIntStateOf(if (initialIdx >= 0) initialIdx else 0)
    }

    val currentVital = tabVitals[selectedTabIndex]

    var valueText by remember(currentVital) {
        mutableStateOf(
            when (currentVital) {
                VitalId.HYDRATION_ML -> "500"
                VitalId.BLOOD_GLUCOSE -> "95"
                VitalId.BLOOD_PRESSURE -> "118"
                VitalId.BODY_TEMP -> "36.8"
                VitalId.SLEEP_HOURS -> "7.2"
                VitalId.MOOD_INDEX -> "8.0"
                else -> "0"
            }
        )
    }

    var secondaryValueText by remember(currentVital) { mutableStateOf("76") }
    var noteText by remember { mutableStateOf("") }
    var isHydrationAdditive by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANUAL TELEMETRY LOG",
                        color = BorderActive,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MICROGRAVITY CADENCE",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Vital Type Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabVitals.forEachIndexed { index, vital ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) BorderActive.copy(alpha = 0.15f) else BgApp)
                                .border(1.dp, if (isSelected) BorderActive else BorderSubtle, RoundedCornerShape(4.dp))
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = vital.shortLabel,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BorderActive else TextSecondary
                            )
                        }
                    }
                }

                // Quick buttons for Hydration
                if (currentVital == VitalId.HYDRATION_ML) {
                    Text(
                        text = "Quick Water Pouch (+mL Intake):",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(250, 500, 750).forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    valueText = amount.toString()
                                    isHydrationAdditive = true
                                },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (valueText == amount.toString()) BorderActive.copy(alpha = 0.15f) else BgApp,
                                    contentColor = if (valueText == amount.toString()) BorderActive else TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (valueText == amount.toString()) BorderActive else BorderSubtle
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 40.dp)
                            ) {
                                Text(text = "+$amount mL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Value Inputs
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = {
                        Text(
                            text = if (currentVital == VitalId.BLOOD_PRESSURE) "Systolic (${currentVital.unit})" else "${currentVital.title} (${currentVital.unit})"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BorderActive,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = BorderActive,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentVital == VitalId.BLOOD_PRESSURE) {
                    OutlinedTextField(
                        value = secondaryValueText,
                        onValueChange = { secondaryValueText = it },
                        label = { Text("Diastolic (mmHg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BorderActive,
                            unfocusedBorderColor = BorderSubtle,
                            focusedLabelColor = BorderActive,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Observation note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Clinical Note / Symptom (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BorderActive,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = BorderActive,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgApp,
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                    ) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif)
                    }

                    OutlinedButton(
                        onClick = {
                            val v = valueText.toFloatOrNull() ?: 0f
                            val sec = if (currentVital == VitalId.BLOOD_PRESSURE) secondaryValueText.toFloatOrNull() else null
                            onSave(currentVital, v, sec, currentVital == VitalId.HYDRATION_ML && isHydrationAdditive, noteText.ifBlank { null })
                            onDismiss()
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgApp,
                            contentColor = BorderActive
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                    ) {
                        Text("Record & Sync", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
