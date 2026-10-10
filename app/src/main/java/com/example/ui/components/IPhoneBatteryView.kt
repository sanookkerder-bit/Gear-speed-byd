package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Authentic iPhone-style statusbar battery indicator:
 * Features the signature iOS rounded capsule, battery terminal nub,
 * smooth fill gauge, and high-visibility percentage readout.
 */
@Composable
fun IPhoneBatteryView(
    socPercent: Int,
    isCharging: Boolean = false,
    showPercentageText: Boolean = true,
    modifier: Modifier = Modifier
) {
    val clampedSoc = socPercent.coerceIn(0, 100)

    val fillColor by animateColorAsState(
        targetValue = when {
            isCharging -> Color(0xFF34C759) // iOS charging green
            clampedSoc <= 20 -> Color(0xFFFF3B30) // iOS Low battery red
            clampedSoc <= 35 -> Color(0xFFFFCC00) // iOS Low power yellow
            else -> Color(0xFF34C759) // Standard healthy green
        },
        label = "iphoneBatteryColor"
    )

    val animatedFillFraction by animateFloatAsState(
        targetValue = clampedSoc / 100f,
        label = "iphoneBatteryFill"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // iOS Battery Capsule Body
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main rounded shell
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .border(
                        width = 1.3.dp,
                        color = Color.White.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(3.5.dp)
                    )
                    .padding(1.8.dp)
            ) {
                // Inner Fill Bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFillFraction)
                        .clip(RoundedCornerShape(1.8.dp))
                        .background(fillColor)
                )

                // Bolt icon inside if charging
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = Color.White,
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // iOS Battery Terminal Nub (+ terminal on the right side)
            Box(
                modifier = Modifier
                    .width(1.8.dp)
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 1.2.dp,
                            bottomEnd = 1.2.dp
                        )
                    )
                    .background(Color.White.copy(alpha = 0.75f))
            )
        }

        if (showPercentageText) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$clampedSoc%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.2.sp
            )
        }
    }
}
