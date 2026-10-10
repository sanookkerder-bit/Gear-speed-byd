package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarGear
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvCyanPrimary

/**
 * Tesla-inspired minimalist top status bar:
 * Displays car gears (P R N D) next to live GPS Speed in the top-right corner.
 */
@Composable
fun TeslaTopBar(
    currentSpeedKmH: Int,
    currentGear: CarGear,
    isGpsActive: Boolean,
    onGearSelected: (CarGear) -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleOrientation: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Branding / BYD DiLink Telemetry & GPS indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isGpsActive) EvBatteryGreen else EvCyanPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "BYD",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "NAV",
                color = EvCyanPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                contentDescription = "GPS Status",
                tint = if (isGpsActive) EvBatteryGreen else Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
            )

            if (onToggleOrientation != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onToggleOrientation,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Toggle Landscape / Portrait",
                        tint = EvCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Right side: Tesla-style P R N D Gear Selector + Speed Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("tesla_speed_gear_cluster")
        ) {
            // Tesla-style P R N D Gears (To the left of Speed)
            TeslaGearCluster(
                currentGear = currentGear,
                onGearSelected = onGearSelected
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Subtle vertical divider line
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(Color(0xFF334155))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Tesla-style Speed Display (Top Right)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clickable { onSpeedClick() }
                    .testTag("tesla_speed_display")
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentSpeedKmH",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "km/h",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tesla UI signature P R N D gear selector:
 * Equal-sized uppercase letters, active gear brightly lit in pure white or cyan,
 * inactive gears dimmed in elegant dark grey.
 */
@Composable
fun TeslaGearCluster(
    currentGear: CarGear,
    onGearSelected: (CarGear) -> Unit,
    modifier: Modifier = Modifier
) {
    val gears = listOf(CarGear.PARK, CarGear.REVERSE, CarGear.NEUTRAL, CarGear.DRIVE)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131D33))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        gears.forEach { gear ->
            val isSelected = currentGear == gear
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF475569),
                animationSpec = tween(durationMillis = 200),
                label = "gearColor"
            )

            Box(
                modifier = Modifier
                    .clickable { onGearSelected(gear) }
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gear.symbol,
                    fontSize = 17.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = textColor,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
