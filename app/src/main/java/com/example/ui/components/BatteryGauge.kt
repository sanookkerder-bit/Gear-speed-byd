package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryCharging
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvElectricBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BatteryGauge(
    socPercent: Int,
    estimatedRangeKm: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Int = 220
) {
    val animatedProgress by animateFloatAsState(
        targetValue = socPercent / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "socProgress"
    )

    val gaugeColor = when {
        isCharging -> EvBatteryCharging
        socPercent >= 50 -> EvCyanPrimary
        socPercent >= 20 -> EvBatteryGreen
        socPercent >= 10 -> EvBatteryAmber
        else -> EvBatteryRed
    }

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = size.minDimension - strokeWidth * 2
            val topLeft = Offset(
                (size.width - arcSize) / 2f,
                (size.height - arcSize) / 2f
            )

            val startAngle = 140f
            val totalSweep = 260f

            // Background Track Arc
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc with Gradient
            val activeSweep = totalSweep * animatedProgress
            if (activeSweep > 0f) {
                val gradient = Brush.sweepGradient(
                    colors = listOf(
                        EvElectricBlue,
                        gaugeColor,
                        gaugeColor
                    )
                )

                drawArc(
                    brush = gradient,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Outer Tick marks
            val radius = arcSize / 2f + strokeWidth * 0.9f
            val center = Offset(size.width / 2f, size.height / 2f)
            val numTicks = 20
            for (i in 0..numTicks) {
                val tickAngleDeg = startAngle + (totalSweep * (i.toFloat() / numTicks))
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val innerR = radius
                val outerR = radius + if (i % 5 == 0) 7.dp.toPx() else 4.dp.toPx()

                val p1 = Offset(
                    (center.x + innerR * cos(tickAngleRad)).toFloat(),
                    (center.y + innerR * sin(tickAngleRad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + outerR * cos(tickAngleRad)).toFloat(),
                    (center.y + outerR * sin(tickAngleRad)).toFloat()
                )

                val tickColor = if (i.toFloat() / numTicks <= animatedProgress) {
                    gaugeColor.copy(alpha = 0.8f)
                } else {
                    Color(0xFF334155)
                }

                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 5 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging",
                    tint = EvBatteryCharging,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "$socPercent%",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "ระยะทาง $estimatedRangeKm กม.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = EvCyanPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isCharging) "กำลังชาร์จไฟ" else "Blade Battery",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
