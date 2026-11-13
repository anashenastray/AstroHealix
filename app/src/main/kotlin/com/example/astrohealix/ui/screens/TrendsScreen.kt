package com.example.astrohealix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalCategory
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.components.TrendChart
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusNormalBg
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun TrendsScreen(
    state: AstroUiState,
    initialVitalId: VitalId = VitalId.HEART_RATE,
    getHistory: (VitalId) -> List<TelemetryPoint>,
    modifier: Modifier = Modifier
) {
    var selectedVitalId by remember(initialVitalId) { mutableStateOf(initialVitalId) }
    var selectedCategory by remember(initialVitalId) { mutableStateOf<VitalCategory?>(null) }
    var selectedTimeframeIndex by remember { mutableIntStateOf(0) }
    val timeframes = listOf("24h" to 24L, "7d" to 7L * 24L, "30d" to 30L * 24L)

    val baseDef = CatalogData.VITAL_DEFINITIONS[selectedVitalId] ?: return
    val effectiveBaseline = state.baselines[selectedVitalId] ?: baseDef.baseline
    val vitalDef: VitalDefinition = baseDef.copy(baseline = effectiveBaseline)
    val snapshot = state.vitals.find { it.id == selectedVitalId }

    val allHistory = getHistory(selectedVitalId)
    val hoursWindow = timeframes[selectedTimeframeIndex].second
    val anchorTs = allHistory.maxOfOrNull { it.timestampMs } ?: System.currentTimeMillis()
    val cutoffMs = anchorTs - hoursWindow * 3600_000L
    val rawPoints = allHistory.filter { it.timestampMs >= cutoffMs }.ifEmpty { allHistory }

    val validPoints = rawPoints.filter {
        !it.isDisconnected &&
            it.value != null &&
            it.qualityStatus != DataQuality.INVALID &&
            it.qualityStatus != DataQuality.INCOMPLETE
    }
    val validValues = validPoints.mapNotNull { it.value }
    val avgVal = if (validValues.isNotEmpty()) validValues.average().toFloat() else null
    val minVal = validValues.minOrNull()
    val maxVal = validValues.maxOrNull()
    val inRangeCount = validValues.count { it in effectiveBaseline.nominalMin..effectiveBaseline.nominalMax }
    val inRangePct = if (validValues.isNotEmpty()) ((inRangeCount.toFloat() / validValues.size) * 100f).toInt() else null
    val integrityPct = if (rawPoints.isNotEmpty()) ((validPoints.size.toFloat() / rawPoints.size) * 100f).toInt() else null

    val incompleteCount = rawPoints.count { it.isDisconnected || it.value == null || it.qualityStatus == DataQuality.INCOMPLETE }
    val unverifiedCount = rawPoints.count { it.qualityStatus == DataQuality.UNVERIFIED || it.isEstimated }
    val outdatedCount = rawPoints.count { it.qualityStatus == DataQuality.OUTDATED }
    val invalidCount = rawPoints.count { it.qualityStatus == DataQuality.INVALID }

    fun fmt(v: Float?): String {
        if (v == null) return "---"
        return if (vitalDef.precision > 0) "%.${vitalDef.precision}f".format(v) else v.toInt().toString()
    }

    val visibleVitals = selectedCategory?.vitalIds ?: VitalId.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Timeframe Row
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
                    text = "TELEMETRY TRENDS",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).sp
                )
                Text(
                    text = "${vitalDef.title.uppercase()} • ${rawPoints.size} SAMPLES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.05.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                timeframes.forEachIndexed { index, (label, _) ->
                    val isSelected = selectedTimeframeIndex == index
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BgSurfaceHover else BgSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) BorderActive else BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTimeframeIndex = index }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) BorderActive else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Category Selector Chips (>= 48dp touch height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = "All Categories",
                isSelected = selectedCategory == null,
                onClick = { selectedCategory = null }
            )
            VitalCategory.entries.forEach { category ->
                CategoryChip(
                    label = category.title,
                    isSelected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                        if (selectedVitalId !in category.vitalIds) {
                            selectedVitalId = category.vitalIds.first()
                        }
                    }
                )
            }
        }

        // Vital Selector Chips (>= 48dp touch height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleVitals.forEach { vId ->
                val isSelected = vId == selectedVitalId
                Box(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BgSurfaceHover else BgSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) BorderActive else BorderSubtle,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedVitalId = vId }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vId.shortLabel,
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

        // Trend Chart Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            TrendChart(
                vital = vitalDef,
                points = rawPoints,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 24h Min / Avg / Max Summary Card
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
                text = "${timeframes[selectedTimeframeIndex].first.uppercase()} TELEMETRY SUMMARY",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.sp
            )

            // Min / Avg / Max 3-Column Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricTile(
                    label = "MIN",
                    value = fmt(minVal),
                    unit = vitalDef.unit,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricTile(
                    label = "AVG",
                    value = fmt(avgVal),
                    unit = vitalDef.unit,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricTile(
                    label = "MAX",
                    value = fmt(maxVal),
                    unit = vitalDef.unit,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TrendStatRow(
                    label = "Personal Baseline Band",
                    value = "${fmt(effectiveBaseline.nominalMin)}–${fmt(effectiveBaseline.nominalMax)} ${vitalDef.unit}",
                    valueColor = StatusNormal
                )
                TrendStatRow(
                    label = "Time in Nominal Baseline",
                    value = inRangePct?.let { "$it% of valid samples" } ?: "---",
                    valueColor = TextPrimary
                )
                TrendStatRow(
                    label = "Sensor Packet Integrity",
                    value = integrityPct?.let { "$it% valid (${validPoints.size}/${rawPoints.size})" } ?: "---",
                    valueColor = if ((integrityPct ?: 100) < 90) StatusMonitor else StatusNormal
                )
            }
        }

        // Rule 5.2 & Quality Validation Status Note Card
        val isCritical = snapshot?.status == VitalStatus.CRITICAL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCritical) CriticalSurfaceGlow else BgSurface)
                .border(1.dp, if (isCritical) StatusCritical else BorderSubtle, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isCritical) StatusCritical else BorderActive,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RULE & QUALITY VALIDATION",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.sp
                    )
                }

                if (snapshot != null) {
                    StatusPill(status = snapshot.status, compact = true)
                }
            }

            if (snapshot != null) {
                val qualityColor = when (snapshot.qualityStatus) {
                    DataQuality.VALID -> StatusNormal
                    DataQuality.UNVERIFIED, DataQuality.OUTDATED -> StatusMonitor
                    DataQuality.INCOMPLETE, DataQuality.INVALID -> StatusCritical
                }
                val qualityBg = when (snapshot.qualityStatus) {
                    DataQuality.VALID -> StatusNormalBg
                    DataQuality.UNVERIFIED, DataQuality.OUTDATED -> StatusMonitorBg
                    else -> BgPrimary
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(qualityBg)
                            .border(1.dp, qualityColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "QUALITY: ${snapshot.qualityStatus.name}",
                            color = qualityColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = snapshot.ruleApplied ?: "Baseline Nominal Guard",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = snapshot.reason,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            val conciseSummary = "${validPoints.size} verified samples (${integrityPct ?: 100}% integrity)" +
                if (incompleteCount > 0) " • $incompleteCount gap(s)" else ""

            Text(
                text = conciseSummary,
                color = TextSecondary,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BgSurfaceHover else BgSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) BorderActive else BorderSubtle,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) BorderActive else TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SummaryMetricTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgPrimary)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.sp
        )
        Text(
            text = value,
            color = if (value == "---") TextDisabled else TextPrimary,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = unit,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TrendStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
