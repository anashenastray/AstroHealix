package com.example.astrohealix.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.MissionReport
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.StatusUnavailableBg
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun ReportsScreen(
    state: AstroUiState,
    onGenerateReport: (periodHours: Int) -> MissionReport,
    onExportCsv: () -> String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val periodOptions = remember {
        listOf(
            24 to "24h",
            168 to "7d",
            720 to "30d"
        )
    }
    var selectedPeriodHours by remember { mutableIntStateOf(24) }
    var generatedReport by remember(selectedPeriodHours) {
        mutableStateOf(onGenerateReport(selectedPeriodHours))
    }
    var exportedCsvPreview by remember { mutableStateOf<String?>(null) }

    val selectedPeriodLabel = periodOptions.firstOrNull { it.first == selectedPeriodHours }?.second ?: "24h"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (onBack != null) {
            SecondaryTopBar(title = "HEALTH REPORTS", onBack = onBack)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Filter (24h / 7d / 30d)
            item(key = "period-filter") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "REPORTING PERIOD",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        periodOptions.forEach { (hours, label) ->
                            val isSelected = selectedPeriodHours == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BgSurfaceHover else BgSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BorderActive else BorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedPeriodHours = hours
                                        generatedReport = onGenerateReport(hours)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BorderActive else TextSecondary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Generate & Export Buttons
            item(key = "report-actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            generatedReport = onGenerateReport(selectedPeriodHours)
                            Toast.makeText(
                                context,
                                "Health report generated ($selectedPeriodLabel)",
                                Toast.LENGTH_SHORT
                            ).show()
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
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Generate Health Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = onExportCsv()
                            exportedCsvPreview = csv
                            clipboardManager.setText(AnnotatedString(csv))
                            Toast.makeText(
                                context,
                                "CSV exported & copied to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgSurface,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Export CSV",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Report Preview Card: Astronaut Info & Period
            val report = generatedReport
            val unavailableVitals = report.vitals.filter {
                it.status == VitalStatus.UNAVAILABLE || it.value == null
            }

            item(key = "report-astronaut-info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "REPORT PREVIEW",
                        style = MaterialTheme.typography.labelLarge,
                        color = BorderActive
                    )
                    ReportMetaRow("Astronaut", "${report.astronaut.name} (${report.astronaut.id})")
                    ReportMetaRow("Mission", "${report.astronaut.missionName} • Day ${report.astronaut.missionDay}")
                    ReportMetaRow("Environment", report.environment.title)
                    ReportMetaRow("Period", "Last $selectedPeriodLabel")
                    ReportMetaRow("Readiness", "${report.readinessScore}/100")
                    ReportMetaRow("Generated", report.generatedAt)
                }
            }

            // Missing / Unavailable Data Summary
            item(key = "report-missing-data") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MISSING DATA & SENSOR GAPS",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = "${unavailableVitals.size} missing",
                            color = if (unavailableVitals.isNotEmpty()) StatusUnavailable else TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (unavailableVitals.isEmpty()) {
                        Text(
                            text = "All monitored vital streams reported data during $selectedPeriodLabel window.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        unavailableVitals.forEach { missing ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusUnavailableBg)
                                    .border(1.dp, StatusUnavailable, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = missing.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "DATA UNAVAILABLE",
                                    color = StatusUnavailable,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Vital Summary
            item(key = "report-vitals-summary") {
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
                        text = "VITAL SUMMARY (${report.vitals.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )

                    report.vitals.forEach { vital ->
                        val isUnavailable = vital.status == VitalStatus.UNAVAILABLE || vital.value == null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(BgSurfaceHover)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = vital.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isUnavailable) {
                                        "--- • DATA UNAVAILABLE"
                                    } else {
                                        "${vital.displayValue} ${vital.unit} (${vital.deltaFromBaseline})"
                                    },
                                    color = if (isUnavailable) TextDisabled else TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            StatusPill(
                                status = vital.status,
                                compact = true,
                                customLabel = if (isUnavailable) "DATA UNAVAILABLE" else null
                            )
                        }
                    }
                }
            }

            // Alerts Summary
            item(key = "report-alerts-summary") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ALERTS SUMMARY (${report.alerts.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )

                    if (report.alerts.isEmpty()) {
                        Text(
                            text = "No clinical or threshold alerts recorded.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        report.alerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BgSurfaceHover)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = alert.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${alert.triggeredAt} • ${if (alert.isEscalated) "Escalated" else if (alert.isAcknowledged) "Acknowledged" else "Unacknowledged"}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                StatusPill(status = alert.severity, compact = true)
                            }
                        }
                    }
                }
            }

            // Exported CSV Preview (shown after pressing Export CSV)
            if (exportedCsvPreview != null) {
                item(key = "csv-preview") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgSurface)
                            .border(1.dp, BorderActive.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EXPORTED CSV PREVIEW",
                            style = MaterialTheme.typography.labelLarge,
                            color = BorderActive
                        )
                        Text(
                            text = exportedCsvPreview!!,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Disclaimer
            item(key = "report-disclaimer") {
                Text(
                    text = report.disclaimer,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ReportMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
