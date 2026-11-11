package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.GlassElevatedBorderBrush
import com.example.astrohealix.ui.theme.GlassModalFillBrush
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun ExamGuideSheet(
    vitalId: VitalId,
    onDismiss: () -> Unit,
    onLogNow: (VitalId) -> Unit,
    onViewTrends: (VitalId) -> Unit
) {
    val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: return
    val guide = def.guide

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GlassModalFillBrush)
                .border(1.dp, GlassElevatedBorderBrush, RoundedCornerShape(14.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASTRONAUT EXAM GUIDE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${def.title} (${def.unit})",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.defaultMinSize(48.dp, 48.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Nominal Baseline Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal Nominal Baseline:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "${def.baseline.nominalMin.toInt()}–${def.baseline.nominalMax.toInt()} ${def.unit}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Section 1: Why Exam Matters
                GuideSection(
                    title = "WHY THIS EXAM MATTERS IN SPACE",
                    content = guide.whyExamMatters
                )

                // Section 2: How to Measure in Microgravity
                GuideSection(
                    title = "HOW TO PERFORM MEASUREMENT",
                    content = guide.howToMeasure
                )

                // Section 3: NASA Hazards
                GuideSection(
                    title = "NASA HRP HAZARDS & PHYSIOLOGICAL IMPACT",
                    content = guide.spaceHazardEffects
                )

                // Section 4: Self Care Steps
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SELF-CARE PROTOCOL ACTIONS:",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    guide.selfCareSteps.forEachIndexed { i, step ->
                        Text(
                            text = "${i + 1}. $step",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                // Section 5: When to Escalate
                GuideSection(
                    title = "CRITERIA FOR FLIGHT SURGEON ESCALATION",
                    content = guide.escalateCriteria
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action CTAs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onViewTrends(vitalId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x10FFFFFF),
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF)),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("View Trends", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLogNow(vitalId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x30FFFFFF),
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x70FFFFFF)),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Log Reading", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}
