package com.example.astrohealix.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.ManualLogEntry
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun LogsScreen(
    state: AstroUiState,
    onForceSync: () -> Unit,
    onExportCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("ALL LOGS", "MANUAL VITALS", "ROUTINE TASKS")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgApp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MISSION HEALTH LOGS & DEEP SPACE TELEMETRY LINK",
                color = BorderActive,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "IDEMPOTENT LOCAL STORAGE • NASA HRP TELEMETRY GATEWAY",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Two-Column Layout for Tablet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- LEFT COLUMN: LOG HISTORY & FILTER CHIPS (55% width) ---
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.forEachIndexed { index, title ->
                        val isSelected = selectedFilterIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x30FFFFFF) else Color(0x0EFFFFFF))
                                .border(1.dp, if (isSelected) Color(0x60FFFFFF) else Color(0x18FFFFFF), RoundedCornerShape(6.dp))
                                .clickable { selectedFilterIndex = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Text(
                    text = "RECORDED CREW MEASUREMENTS & OBSERVATIONS (${state.recentLogs.size})",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Log entries list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.recentLogs) { log ->
                        LogEntryCard(log = log)
                    }

                    // Simulated default system logs for rich NASA fidelity
                    item {
                        SystemEventCard(
                            time = "07:00 UTC",
                            title = "MORNING ROUTINE COMPLETED",
                            detail = "Cuff Blood Pressure & Core Temp taken. Normative resting profile recorded.",
                            isSynced = true
                        )
                    }
                    item {
                        SystemEventCard(
                            time = "06:15 UTC",
                            title = "POLYSOMNOGRAPHY SLEEP SHIFT LOGGED",
                            detail = "Actigraphy sensor sync complete. Sleep duration 5.3h (Under baseline 6.0h).",
                            isSynced = true
                        )
                    }
                    item {
                        SystemEventCard(
                            time = "Yesterday 21:00 UTC",
                            title = "ARED RESISTIVE EXERCISE LOGGED",
                            detail = "Squat & deadlift load 60 min. Bone density mitigation countermeasure achieved.",
                            isSynced = true
                        )
                    }
                }
            }

            // --- RIGHT COLUMN: SYNC GATEWAY & TELEMETRY EXPORT (45% width) ---
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deep Space Comm Link Status Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassCardFillBrush)
                        .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DEEP SPACE TELEMETRY LINK",
                                color = BorderActive,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isOffline) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (state.isOffline) StatusMonitor else StatusNormal,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (state.isOffline) "OFFLINE CACHE" else "COMM LINK NOMINAL",
                                    color = if (state.isOffline) StatusMonitor else StatusNormal,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SyncRow("Mission Environment:", state.environment.title)
                            SyncRow("Light-Time Delay:", state.environment.commDelayLabel)
                            SyncRow("Pending Sync Queue:", "${state.pendingSyncQueue.size} Records Queued")
                            SyncRow("Last Ground Sync:", state.lastSyncedAt)
                            SyncRow("Sync Architecture:", "Idempotent GUID local-first cache")
                        }

                        OutlinedButton(
                            onClick = {
                                onForceSync()
                                Toast.makeText(context, "Initiating Deep Space Comm Sync...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x25FFFFFF),
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x60FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 38.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Force Ground Telemetry Uplink", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Flight Surgeon Telemetry Spec Export Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassCardFillBrush)
                        .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FLIGHT SURGEON TELEMETRY SPEC",
                                color = BorderActive,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.Description, contentDescription = null, tint = BorderActive, modifier = Modifier.size(13.dp))
                        }

                        Text(
                            text = "Generates clinical summary report conforming to NASA Space Apps Challenge standards. Contains 24h vitals, Rule 5.2 events, and habitability metrics.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        OutlinedButton(
                            onClick = {
                                val csv = onExportCsv()
                                clipboardManager.setText(AnnotatedString(csv))
                                Toast.makeText(context, "Telemetry Spec CSV Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x14FFFFFF),
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 38.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Copy CSV Telemetry Spec", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Clinical Disclaimer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x0CFFFFFF))
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Text(
                            text = "MISSION DECISION SUPPORT ONLY: AstroHealix assists crew self-assessment during spaceflight. Autonomous evaluation does not replace certified Flight Surgeon diagnosis or direct Clinical Medical Officer intervention.",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(log: ManualLogEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(GlassCardFillBrush)
            .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = log.vitalId.shortLabel,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${log.value} ${log.vitalId.unit}",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (log.isSynced) {
                        Text(text = "SYNCED", color = StatusNormal, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        Text(text = "QUEUED", color = StatusMonitor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = log.recordedAt,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (!log.notes.isNullOrBlank()) {
                Text(
                    text = "Observation: ${log.notes}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
private fun SystemEventCard(time: String, title: String, detail: String, isSynced: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x10FFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = detail,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
private fun SyncRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
        Text(text = value, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
