package com.example.astrohealix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.AnatomySystem
import com.example.astrohealix.model.BodySystemsCatalog
import com.example.astrohealix.model.Countermeasure
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.theme.AuraCardPure
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.LocalVitaColors
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import kotlin.math.hypot

/**
 * Computes anatomical organ coordinate inside the human silhouette.
 */
private fun resolveAnatomicalOrganOffset(
    systemId: String,
    w: Float,
    h: Float
): Offset {
    val cx = w * 0.5f
    val s = h * 0.88f
    val topY = h * 0.05f
    fun bx(nx: Float): Float = cx + nx * s
    fun by(ny: Float): Float = topY + ny * s

    return when (systemId) {
        "sys_neuro_sans" -> Offset(bx(0.0f), by(0.072f))     // Cranium / Brain & Optic center
        "sys_respiratory" -> Offset(bx(-0.052f), by(0.265f)) // Right lung lobe
        "sys_cardio" -> Offset(bx(0.022f), by(0.295f))       // Heart apex
        "sys_immune_rad" -> Offset(bx(0.054f), by(0.375f))   // Spleen & Lymphatic plexus
        "sys_renal_gi" -> Offset(bx(-0.038f), by(0.405f))    // Kidney / Renal cortex
        "sys_musculo" -> Offset(bx(0.0f), by(0.515f))        // Lumbar Spine & Pelvic core
        else -> Offset(cx, by(0.35f))
    }
}

/**
 * Complete Spaceflight Anatomical Bio-Scanner HUD taken entirely from Vita SYS.
 * Features 10 visual layers including live heart pulsation, laser sweep, organ reticles,
 * and clinical countermeasure inspection.
 */
@Composable
fun BodyMapScreen(
    systems: List<AnatomySystem> = remember { BodySystemsCatalog.getDefaultSystems() },
    onInspectVital: (VitalId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSystemId by remember { mutableStateOf(systems.firstOrNull()?.id ?: "sys_cardio") }
    val currentSystem = systems.find { it.id == selectedSystemId } ?: systems.first()
    var checkedSymptoms by remember { mutableStateOf(setOf<String>()) }

    val isDark = LocalVitaColors.current.isDark
    val surfaceColor = if (isDark) BgSurface else AuraCardPure

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .testTag("body_systems_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Compact Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = currentSystem.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${currentSystem.anatomicalRegion} • ${currentSystem.primaryRisk}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        StatusPill(status = currentSystem.status, compact = true)
                    }
                    HorizontalDivider(color = BorderSubtle)
                }
            }
        }

        // 2. Interactive Anatomical 10-Layer Bio-Scanner Canvas
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(350.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Continuous Scanner Animations
                val infiniteTransition = rememberInfiniteTransition(label = "bioScannerTransitions")
                val scannerYRatio by infiniteTransition.animateFloat(
                    initialValue = 0.04f,
                    targetValue = 0.96f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scannerYRatio"
                )

                val heartPulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.32f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(780, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heartPulseScale"
                )

                val pulseDotRatio by infiniteTransition.animateFloat(
                    initialValue = 0.0f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseDotRatio"
                )

                val reticleAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(9000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "reticleAngle"
                )

                val activeStatusColor by animateColorAsState(
                    targetValue = when (currentSystem.status) {
                        VitalStatus.CRITICAL -> StatusCritical
                        VitalStatus.MONITOR -> StatusMonitor
                        VitalStatus.NORMAL -> StatusNormal
                        VitalStatus.UNAVAILABLE -> StatusUnavailable
                    },
                    animationSpec = tween(300),
                    label = "activeStatusColor"
                )

                BioScannerCanvas(
                    systems = systems,
                    selectedSystemId = selectedSystemId,
                    onSelectSystem = { selectedSystemId = it },
                    scannerYRatio = scannerYRatio,
                    heartPulseScale = heartPulseScale,
                    pulseDotRatio = pulseDotRatio,
                    reticleAngle = reticleAngle,
                    activeStatusColor = activeStatusColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. Organ Selector Pills
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(systems) { sys ->
                    val isSel = sys.id == selectedSystemId
                    Surface(
                        onClick = { selectedSystemId = sys.id },
                        shape = RoundedCornerShape(50),
                        color = if (isSel) BorderActive else surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BorderActive else BorderSubtle),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = sys.shortLabel,
                                color = if (isSel) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 4. Detailed Clinical Advice & Spaceflight Risk Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLINICAL ASSESSMENT",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    StatusPill(status = currentSystem.status, compact = true)
                }

                Text(
                    text = currentSystem.clinicalAdvice,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = BorderSubtle)

                Text(
                    text = "PHYSIOLOGICAL SPACEFLIGHT EFFECTS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                currentSystem.spaceflightEffects.forEach { effect ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "• ",
                            color = BorderActive,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = effect,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 5. Active Medical Countermeasures
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PRESCRIBED COUNTERMEASURES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                currentSystem.countermeasures.forEach { cm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgPrimary)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cm.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = cm.description,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                        Text(
                            text = cm.dosageOrFrequency,
                            color = BorderActive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 6. Interactive Symptom Checklist
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SYMPTOM SURVEILLANCE CHECKLIST",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                currentSystem.symptomChecklist.forEach { prompt ->
                    val isChecked = checkedSymptoms.contains(prompt.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                checkedSymptoms = if (isChecked) checkedSymptoms - prompt.id
                                else checkedSymptoms + prompt.id
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { check ->
                                checkedSymptoms = if (check) checkedSymptoms + prompt.id
                                else checkedSymptoms - prompt.id
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BorderActive,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = prompt.symptomText,
                                color = if (isChecked) TextSecondary else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (isChecked) {
                                Text(
                                    text = "Action: ${prompt.recommendedAction}",
                                    color = StatusMonitor,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BioScannerCanvas(
    systems: List<AnatomySystem>,
    selectedSystemId: String,
    onSelectSystem: (String) -> Unit,
    scannerYRatio: Float,
    heartPulseScale: Float,
    pulseDotRatio: Float,
    reticleAngle: Float,
    activeStatusColor: Color,
    modifier: Modifier = Modifier
) {
    val isCardioSelected = selectedSystemId == "sys_cardio"
    val statusCrit = StatusCritical
    val statusMon = StatusMonitor
    val statusNorm = StatusNormal
    val statusUnav = StatusUnavailable
    val borderAct = BorderActive
    val borderSub = BorderSubtle

    Canvas(
        modifier = modifier
            .pointerInput(systems) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val hit = systems.minByOrNull { sys ->
                        val organPos = resolveAnatomicalOrganOffset(sys.id, w, h)
                        hypot(tapOffset.x - organPos.x, tapOffset.y - organPos.y)
                    }
                    if (hit != null) {
                        val pos = resolveAnatomicalOrganOffset(hit.id, w, h)
                        if (hypot(tapOffset.x - pos.x, tapOffset.y - pos.y) <= 44.dp.toPx()) {
                            onSelectSystem(hit.id)
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val s = h * 0.88f
        val topY = h * 0.05f

        fun bx(nx: Float): Float = cx + nx * s
        fun by(ny: Float): Float = topY + ny * s

        // LAYER 1: Subtle Holographic Coordinate Grid
        val gridStep = 22.dp.toPx()
        val gridCol = Color(0xFF6C7A9C).copy(alpha = 0.08f)
        var gx = 0f
        while (gx < w) {
            drawLine(gridCol, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
            gx += gridStep
        }
        var gy = 0f
        while (gy < h) {
            drawLine(gridCol, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
            gy += gridStep
        }

        // LAYER 2: Contoured Human Silhouette Path
        val bodyPath = Path().apply {
            moveTo(bx(0.0f), by(0.015f))
            cubicTo(bx(0.048f), by(0.015f), bx(0.070f), by(0.048f), bx(0.068f), by(0.090f))
            cubicTo(bx(0.066f), by(0.125f), bx(0.042f), by(0.145f), bx(0.028f), by(0.155f))
            cubicTo(bx(0.038f), by(0.165f), bx(0.085f), by(0.178f), bx(0.148f), by(0.198f))
            cubicTo(bx(0.165f), by(0.205f), bx(0.185f), by(0.270f), bx(0.192f), by(0.355f))
            cubicTo(bx(0.198f), by(0.420f), bx(0.205f), by(0.490f), bx(0.198f), by(0.535f))
            lineTo(bx(0.175f), by(0.535f))
            cubicTo(bx(0.178f), by(0.460f), bx(0.160f), by(0.370f), bx(0.142f), by(0.295f))
            cubicTo(bx(0.120f), by(0.320f), bx(0.110f), by(0.380f), bx(0.102f), by(0.465f))
            cubicTo(bx(0.095f), by(0.525f), bx(0.082f), by(0.585f), bx(0.075f), by(0.615f))
            cubicTo(bx(0.072f), by(0.660f), bx(0.068f), by(0.755f), bx(0.062f), by(0.855f))
            cubicTo(bx(0.058f), by(0.915f), bx(0.052f), by(0.965f), bx(0.045f), by(0.985f))
            lineTo(bx(0.015f), by(0.985f))
            cubicTo(bx(0.020f), by(0.930f), bx(0.022f), by(0.840f), bx(0.018f), by(0.745f))
            cubicTo(bx(0.014f), by(0.670f), bx(0.008f), by(0.620f), bx(0.000f), by(0.590f))
            cubicTo(bx(-0.008f), by(0.620f), bx(-0.014f), by(0.670f), bx(-0.018f), by(0.745f))
            cubicTo(bx(-0.022f), by(0.840f), bx(-0.020f), by(0.930f), bx(-0.015f), by(0.985f))
            lineTo(bx(-0.045f), by(0.985f))
            cubicTo(bx(-0.052f), by(0.965f), bx(-0.058f), by(0.915f), bx(-0.062f), by(0.855f))
            cubicTo(bx(-0.068f), by(0.755f), bx(-0.072f), by(0.660f), bx(-0.075f), by(0.615f))
            cubicTo(bx(-0.082f), by(0.585f), bx(-0.095f), by(0.525f), bx(-0.102f), by(0.465f))
            cubicTo(bx(-0.110f), by(0.380f), bx(-0.120f), by(0.320f), bx(-0.142f), by(0.295f))
            cubicTo(bx(-0.160f), by(0.370f), bx(-0.178f), by(0.460f), bx(-0.175f), by(0.535f))
            lineTo(bx(-0.198f), by(0.535f))
            cubicTo(bx(-0.205f), by(0.490f), bx(-0.198f), by(0.420f), bx(-0.192f), by(0.355f))
            cubicTo(bx(-0.185f), by(0.270f), bx(-0.165f), by(0.205f), bx(-0.148f), by(0.198f))
            cubicTo(bx(-0.085f), by(0.178f), bx(-0.038f), by(0.165f), bx(-0.028f), by(0.155f))
            cubicTo(bx(-0.042f), by(0.145f), bx(-0.066f), by(0.125f), bx(-0.068f), by(0.090f))
            cubicTo(bx(-0.070f), by(0.048f), bx(-0.048f), by(0.015f), bx(0.0f), by(0.015f))
            close()
        }

        drawPath(
            path = bodyPath,
            color = Color(0xFF2563EB).copy(alpha = 0.05f)
        )
        drawPath(
            path = bodyPath,
            color = Color(0xFF2563EB).copy(alpha = 0.40f),
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // LAYER 3: Glowing Skeletal Ribcage & Clavicles
        val boneCol = Color(0xFF6C7A9C).copy(alpha = 0.25f)
        drawLine(boneCol, Offset(bx(-0.11f), by(0.19f)), Offset(bx(0.11f), by(0.19f)), strokeWidth = 1.2.dp.toPx())
        for (i in 0 until 5) {
            val ribY = by(0.23f + i * 0.028f)
            val ribHalfW = s * (0.075f - i * 0.005f)
            drawArc(
                color = boneCol,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - ribHalfW, ribY - 6.dp.toPx()),
                size = Size(ribHalfW * 2f, 12.dp.toPx()),
                style = Stroke(width = 1f)
            )
        }

        // LAYER 4: Neural & Optic Pathways
        val neuralCol = Color(0xFF00E5FF).copy(alpha = 0.35f)
        drawLine(neuralCol, Offset(bx(0f), by(0.04f)), Offset(bx(0f), by(0.48f)), strokeWidth = 1.5.dp.toPx())

        // LAYER 5: Circulatory Pulsing Aorta & Beating Heart
        val heartCenter = resolveAnatomicalOrganOffset("sys_cardio", w, h)
        val arteryCol = Color(0xFFFF3D00).copy(alpha = 0.45f)
        drawLine(arteryCol, Offset(cx, by(0.22f)), Offset(cx, by(0.46f)), strokeWidth = 2.dp.toPx())

        // Animated Blood Flow Pulse Dot
        val pulseDotY = by(0.22f + (0.24f * pulseDotRatio))
        drawCircle(
            color = Color(0xFFFF3D00),
            radius = 2.8.dp.toPx(),
            center = Offset(cx, pulseDotY)
        )

        // Beating Heart with Live Systolic/Diastolic Scaling
        withTransform({
            scale(scaleX = heartPulseScale, scaleY = heartPulseScale, pivot = heartCenter)
        }) {
            drawCircle(
                color = statusCrit.copy(alpha = 0.22f),
                radius = 11.dp.toPx(),
                center = heartCenter
            )
            drawCircle(
                color = if (isCardioSelected) statusCrit else Color(0xFFC84B4B),
                radius = 6.5.dp.toPx(),
                center = heartCenter
            )
        }

        // LAYER 6: Continuous Vertical Bio-Scanner Laser Sweep
        val scanY = h * scannerYRatio
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    borderAct.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                startY = scanY - 18.dp.toPx(),
                endY = scanY + 18.dp.toPx()
            ),
            topLeft = Offset(w * 0.18f, scanY - 18.dp.toPx()),
            size = Size(w * 0.64f, 36.dp.toPx())
        )
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    borderAct.copy(alpha = 0.55f),
                    borderAct,
                    borderAct.copy(alpha = 0.55f),
                    Color.Transparent
                )
            ),
            start = Offset(w * 0.18f, scanY),
            end = Offset(w * 0.82f, scanY),
            strokeWidth = 1.5.dp.toPx()
        )

        // LAYER 7: Active Organ Target Reticles & Surgical Leader Callout Lines
        systems.forEach { sys ->
            val isSelected = sys.id == selectedSystemId
            val nodePos = resolveAnatomicalOrganOffset(sys.id, w, h)
            val nodeColor = when (sys.status) {
                VitalStatus.CRITICAL -> statusCrit
                VitalStatus.MONITOR -> statusMon
                VitalStatus.NORMAL -> statusNorm
                VitalStatus.UNAVAILABLE -> statusUnav
            }

            val leaderEnd = when (sys.id) {
                "sys_neuro_sans" -> Offset(nodePos.x + 24.dp.toPx(), nodePos.y - 4.dp.toPx())
                "sys_respiratory" -> Offset(nodePos.x - 22.dp.toPx(), nodePos.y)
                "sys_cardio" -> Offset(nodePos.x + 24.dp.toPx(), nodePos.y)
                "sys_immune_rad" -> Offset(nodePos.x + 24.dp.toPx(), nodePos.y + 2.dp.toPx())
                "sys_renal_gi" -> Offset(nodePos.x - 22.dp.toPx(), nodePos.y)
                "sys_musculo" -> Offset(nodePos.x, nodePos.y + 14.dp.toPx())
                else -> nodePos
            }

            drawLine(
                color = if (isSelected) borderAct else borderSub,
                start = nodePos,
                end = leaderEnd,
                strokeWidth = if (isSelected) 1.5.dp.toPx() else 1.dp.toPx()
            )

            if (isSelected) {
                drawCircle(
                    color = activeStatusColor.copy(alpha = 0.18f),
                    radius = 20.dp.toPx(),
                    center = nodePos
                )
                val ringRadius = 13.dp.toPx()
                for (seg in 0 until 4) {
                    val startDeg = reticleAngle + seg * 90f
                    drawArc(
                        color = activeStatusColor,
                        startAngle = startDeg,
                        sweepAngle = 50f,
                        useCenter = false,
                        topLeft = Offset(nodePos.x - ringRadius, nodePos.y - ringRadius),
                        size = Size(ringRadius * 2f, ringRadius * 2f),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = nodePos
                )
                drawCircle(
                    color = activeStatusColor,
                    radius = 3.5.dp.toPx(),
                    center = nodePos
                )
            } else {
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = nodePos
                )
                drawCircle(
                    color = nodeColor,
                    radius = 2.5.dp.toPx(),
                    center = nodePos
                )
            }
        }
    }
}
