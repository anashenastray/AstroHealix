package com.example.astrohealix.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.ManualLogEntry
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusNormalBg
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun LogsScreen(
    state: AstroUiState,
    onForceSync: () -> Unit,
    onExportCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showCsvPreview by remember { mutableStateOf(false) }
    var csvPreviewText by remember { mutableStateOf("") }
    val filters = listOf("ALL LOGS", "QUEUED (${state.pendingSyncQueue.size})", "ROUTINE")

    val copyCsvAction: () -> Unit = {
        val csv = onExportCsv()
        csvPreviewText = csv
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("AstroHealix Telemetry CSV", csv))
        Toast.makeText(context, "Telemetry CSV copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    val togglePreviewAction: () -> Unit = {
        if (!showCsvPreview) {
            csvPreviewText = onExportCsv()
        }
        showCsvPreview = !showCsvPreview
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(16.dp)
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LogsHeader(state = state)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LogsFilterRow(
                            filters = filters,
                            selectedIndex = selectedFilterIndex,
                            onSelect = { selectedFilterIndex = it }
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (selectedFilterIndex) {
                                0 -> {
                                    if (state.recentLogs.isEmpty()) {
                                        item { EmptyLogsCard("No manual telemetry logs recorded yet.") }
                                    } else {
                                        items(state.recentLogs, key = { it.id }) { log ->
                                            LogEntryCard(log = log)
                                        }
                                    }
                                }
                                1 -> {
                                    if (state.pendingSyncQueue.isEmpty()) {
                                        item { EmptyLogsCard("Offline sync queue is empty. All records synchronized.") }
                                    } else {
                                        items(state.pendingSyncQueue, key = { it.id }) { log ->
                                            LogEntryCard(log = log)
                                        }
                                    }
                                }
                                else -> {
                                    items(state.routine, key = { it.id }) { task ->
                                        RoutineLogCard(task = task)
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OfflineSyncCard(
                            state = state,
                            onForceSync = {
                                onForceSync()
                                Toast.makeText(context, "Reconciling offline telemetry queue...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        CsvExportCard(
                            showPreview = showCsvPreview,
                            csvPreview = csvPreviewText,
                            onTogglePreview = togglePreviewAction,
                            onCopyCsv = copyCsvAction
                        )
                        LogsDisclaimerCard(disclaimer = state.disclaimer)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { LogsHeader(state = state) }

                item {
                    OfflineSyncCard(
                        state = state,
                        onForceSync = {
                            onForceSync()
                            Toast.makeText(context, "Reconciling offline telemetry queue...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    CsvExportCard(
                        showPreview = showCsvPreview,
                        csvPreview = csvPreviewText,
                        onTogglePreview = togglePreviewAction,
                        onCopyCsv = copyCsvAction
                    )
                }

                item {
                    LogsFilterRow(
                        filters = filters,
                        selectedIndex = selectedFilterIndex,
                        onSelect = { selectedFilterIndex = it }
                    )
                }

                when (selectedFilterIndex) {
                    0 -> {
                        if (state.recentLogs.isEmpty()) {
                            item { EmptyLogsCard("No manual telemetry logs recorded yet.") }
                        } else {
                            items(state.recentLogs, key = { it.id }) { log ->
                                LogEntryCard(log = log)
                            }
                        }
                    }
                    1 -> {
                        if (state.pendingSyncQueue.isEmpty()) {
                            item { EmptyLogsCard("Offline sync queue is empty. All records synchronized.") }
                        } else {
                            items(state.pendingSyncQueue, key = { it.id }) { log ->
                                LogEntryCard(log = log)
                            }
                        }
                    }
                    else -> {
                        items(state.routine, key = { it.id }) { task ->
                            RoutineLogCard(task = task)
                        }
                    }
                }

                item { LogsDisclaimerCard(disclaimer = state.disclaimer) }
            }
        }
    }
}

@Composable
private fun LogsHeader(state: AstroUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "MISSION LOGS & SYNC",
                color = TextPrimary,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp
            )
            Text(
                text = "LOCAL-FIRST CACHE • ${state.recentLogs.size} RECORDED ENTRIES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.05.sp
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (state.isOffline) StatusMonitorBg else StatusNormalBg)
                .border(1.dp, if (state.isOffline) StatusMonitor else StatusNormal, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (state.isOffline) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                contentDescription = null,
                tint = if (state.isOffline) StatusMonitor else StatusNormal,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (state.isOffline) "OFFLINE" else "LINKED",
                color = if (state.isOffline) StatusMonitor else StatusNormal,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OfflineSyncCard(
    state: AstroUiState,
    onForceSync: () -> Unit
) {
    val queuedSupportCount = state.supportRequests.count { it.statusLabel.contains("OFFLINE", ignoreCase = true) }

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
            text = "DEEP SPACE TELEMETRY LINK",
            color = BorderActive,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SyncRow("Environment", state.environment.title)
            SyncRow("Comm Delay", state.environment.commDelayLabel)
            SyncRow(
                "Pending Sync Queue",
                "${state.pendingSyncQueue.size} vitals" + if (queuedSupportCount > 0) " • $queuedSupportCount private" else ""
            )
            SyncRow("Last Ground Sync", state.lastSyncedAt)
        }

        Button(
            onClick = onForceSync,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isOffline || state.pendingSyncQueue.isNotEmpty()) BorderActive else BgSurfaceHover,
                contentColor = if (state.isOffline || state.pendingSyncQueue.isNotEmpty()) BgPrimary else TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Force Ground Telemetry Uplink",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CsvExportCard(
    showPreview: Boolean,
    csvPreview: String,
    onTogglePreview: () -> Unit,
    onCopyCsv: () -> Unit
) {
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
                text = "FLIGHT SURGEON CSV EXPORT",
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.sp
            )
            Icon(Icons.Default.Description, contentDescription = null, tint = BorderActive, modifier = Modifier.size(16.dp))
        }

        Text(
            text = "Exports current snapshots (including DATA UNAVAILABLE flags), baseline deltas, and alert audit trail.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontFamily = FontFamily.SansSerif
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onTogglePreview,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BgSurfaceHover,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showPreview) "Hide CSV" else "Preview CSV",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onCopyCsv,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BorderActive,
                    contentColor = BgPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy CSV",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showPreview && csvPreview.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgPrimary)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = csvPreview.lines().take(18).joinToString("\n"),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun LogsFilterRow(
    filters: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) BgSurfaceHover else BgSurface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) BorderActive else BorderSubtle,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) BorderActive else TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun LogEntryCard(log: ManualLogEntry) {
    val formattedValue = when (log.vitalId) {
        VitalId.BLOOD_PRESSURE -> {
            val sys = log.value.roundToInt()
            val dia = log.secondaryValue?.roundToInt() ?: 76
            "$sys/$dia"
        }
        VitalId.BODY_TEMP, VitalId.SLEEP_HOURS, VitalId.MOOD_INDEX -> "%.1f".format(log.value)
        VitalId.RADIATION_MSV -> "%.2f".format(log.value)
        else -> log.value.roundToInt().toString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Text(
                    text = log.vitalId.shortLabel,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$formattedValue ${log.vitalId.unit}",
                    color = BorderActive,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (log.isSynced) StatusNormalBg else StatusMonitorBg)
                        .border(1.dp, if (log.isSynced) StatusNormal else StatusMonitor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (log.isSynced) "SYNCED" else "QUEUED",
                        color = if (log.isSynced) StatusNormal else StatusMonitor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = log.recordedAt,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (!log.notes.isNullOrBlank()) {
            Text(
                text = log.notes,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
private fun RoutineLogCard(task: RoutineTask) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.isCompleted) StatusNormal else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = task.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${task.cadence} • ${task.targetLabel}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = if (task.isCompleted) task.completedAt ?: "DONE" else "PENDING",
            color = if (task.isCompleted) StatusNormal else StatusMonitor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyLogsCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
private fun LogsDisclaimerCard(disclaimer: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Text(
            text = disclaimer,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
private fun SyncRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp, fontFamily = FontFamily.SansSerif)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
