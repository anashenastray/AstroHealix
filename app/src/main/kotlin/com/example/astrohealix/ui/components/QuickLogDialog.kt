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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.GlassElevatedBorderBrush
import com.example.astrohealix.ui.theme.GlassModalFillBrush
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
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GlassModalFillBrush)
                .border(1.dp, GlassElevatedBorderBrush, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        color = TextPrimary,
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x35FFFFFF) else Color(0x0EFFFFFF))
                                .border(1.dp, if (isSelected) Color(0x70FFFFFF) else Color(0x18FFFFFF), RoundedCornerShape(6.dp))
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = vital.shortLabel,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
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
                            val isChosen = valueText == amount.toString()
                            OutlinedButton(
                                onClick = {
                                    valueText = amount.toString()
                                    isHydrationAdditive = true
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isChosen) Color(0x30FFFFFF) else Color(0x10FFFFFF),
                                    contentColor = TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChosen) Color(0x60FFFFFF) else Color(0x20FFFFFF)
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
                        focusedBorderColor = Color(0x80FFFFFF),
                        unfocusedBorderColor = Color(0x24FFFFFF),
                        focusedLabelColor = TextPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = Color(0x10FFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF)
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
                            focusedBorderColor = Color(0x80FFFFFF),
                            unfocusedBorderColor = Color(0x24FFFFFF),
                            focusedLabelColor = TextPrimary,
                            unfocusedLabelColor = TextSecondary,
                            focusedContainerColor = Color(0x10FFFFFF),
                            unfocusedContainerColor = Color(0x0AFFFFFF)
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
                        focusedBorderColor = Color(0x80FFFFFF),
                        unfocusedBorderColor = Color(0x24FFFFFF),
                        focusedLabelColor = TextPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = Color(0x10FFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF)
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
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x10FFFFFF),
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF)),
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
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x30FFFFFF),
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x70FFFFFF)),
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
