package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ArrivalSafetyStatus
import com.example.model.CarGear
import com.example.model.SeatbeltStatus
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Newly arranged Cockpit Statusbar:
 * - ซ้ายมือสุด: เวลา วันที่ เดือน ปี ตัวหนังสือสีขาว
 * - ถัดมาทางซ้ายของเกียร์: แบตรถและเปอร์เซ็น คงเหลือ (ฟังก์ชัน: เมื่อกด จะสลับแสดง ระยะทางคงเหลือ 1% ≈ 2.9 กม.)
 * - ถัดมาทางซ้ายของความเร็ว: เกียร์ (P R N D)
 * - ขวามือสุด: ความเร็ว ตัวหนังสือ ใหญ่
 * - ที่เหลือ ลบออกทั้งหมด
 */
@Composable
fun TeslaTopBar(
    currentSpeedKmH: Int,
    currentGear: CarGear,
    isGpsActive: Boolean = true,
    socPercent: Int = 85,
    arrivalSocPercent: Double? = null,
    safetyStatus: ArrivalSafetyStatus = ArrivalSafetyStatus.SAFE,
    seatbeltStatus: SeatbeltStatus = SeatbeltStatus(),
    isCharging: Boolean = false,
    onGearSelected: (CarGear) -> Unit = {},
    onSpeedClick: () -> Unit = {},
    onBatteryClick: () -> Unit = {},
    onToggleSeatbelt: (Int) -> Unit = {},
    onToggleOverlay: () -> Unit = {},
    isOverlayActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 1. Live ticking clock: เวลา วันที่ เดือน ปี ซ้ายมือสุด ตัวหนังสือสีขาว
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE d MMM yyyy", Locale("th", "TH")) }
    val timeText = timeFormatter.format(calendar.time)
    val dateText = dateFormatter.format(calendar.time)

    // 2. แบตรถและเปอร์เซ็นคงเหลือ -> เมื่อกด สลับแสดงระยะทางคงเหลือ (1% = 2.9 กม. โดยประมาณ)
    var showDistanceMode by remember { mutableStateOf(false) }
    val remainingRangeKm = socPercent * 2.9

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [ซ้ายมือสุด] เวลา วันที่ เดือน ปี ตัวหนังสือสีขาว
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onToggleOverlay() }
                .testTag("statusbar_datetime_display")
        ) {
            Text(
                text = timeText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(15.dp)
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = dateText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
        }

        // [กลุ่มทางขวา] แบตรถ & %คงเหลือ (กดสลับระยะทางได้) -> เกียร์ -> ขวามือสุด: ความเร็วตัวหนังสือใหญ่
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // แบตรถและเปอร์เซ็น คงเหลือ (กดเพื่อสลับแสดงระยะทางคงเหลือ)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF131D33))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .clickable {
                        showDistanceMode = !showDistanceMode
                        onBatteryClick()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("battery_range_toggle_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IPhoneBatteryView(
                    socPercent = socPercent,
                    isCharging = isCharging,
                    showPercentageText = false
                )

                Spacer(modifier = Modifier.width(7.dp))

                if (!showDistanceMode) {
                    // แสดงตัวเลข เปอร์เซ็นต์แบตรถ คงเหลือ
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$socPercent%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "⇄",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // สลับแสดง ระยะทางคงเหลือ (1% วิ่งได้ 2.9 กม. โดยเฉลี่ย)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚗", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", remainingRangeKm)} กม.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "⇄",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ถัดมาทางซ้ายของความเร็ว: เกียร์ (P R N D)
            TeslaGearCluster(
                currentGear = currentGear,
                onGearSelected = onGearSelected,
                modifier = Modifier.testTag("statusbar_gear_selector")
            )

            // ขวามือสุด: เป็นความเร็ว ตัวหนังสือ ใหญ่
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .clickable { onSpeedClick() }
                    .testTag("statusbar_speed_large")
            ) {
                Text(
                    text = "$currentSpeedKmH",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "km/h",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SeatStatusDot(isFastened: Boolean) {
    Box(
        modifier = Modifier
            .size(6.5.dp)
            .clip(CircleShape)
            .background(if (isFastened) EvBatteryGreen else EvBatteryRed)
    )
}

/**
 * Dialog displaying BYD 5-point cabin seatbelt telemetry:
 * Allows driver to inspect each seat and toggle simulation testing.
 */
@Composable
fun Seatbelt5PointDialog(
    status: SeatbeltStatus,
    onToggleSeat: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💺", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "เข็มขัดนิรภัยห้องโดยสาร 5 จุด",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "อ่านข้อมูลผ่าน ADB (DiLink CAN Telemetry)",
                                color = EvCyanPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cabin Seat Diagram
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "หน้ารถ (FRONT)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )

                        // Front Row (2 Seats: Driver & Passenger)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            SeatItemCard(
                                label = "คนขับ (FL)",
                                isFastened = status.driverFastened,
                                onClick = { onToggleSeat(0) }
                            )
                            SeatItemCard(
                                label = "ผู้โดยสารหน้า (FR)",
                                isFastened = status.passengerFastened,
                                onClick = { onToggleSeat(1) }
                            )
                        }

                        // Rear Row (3 Seats: RL, RC, RR)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SeatItemCard(
                                label = "หลังซ้าย (RL)",
                                isFastened = status.rearLeftFastened,
                                onClick = { onToggleSeat(2) }
                            )
                            SeatItemCard(
                                label = "หลังกลาง (RC)",
                                isFastened = status.rearCenterFastened,
                                onClick = { onToggleSeat(3) }
                            )
                            SeatItemCard(
                                label = "หลังขวา (RR)",
                                isFastened = status.rearRightFastened,
                                onClick = { onToggleSeat(4) }
                            )
                        }

                        Text(
                            text = "ท้ายรถ (REAR)",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ADB Telemetry Command readout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF090D1A))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "ADB",
                        tint = EvCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "adb getprop persist.byd.seatbelts -> [${if (status.driverFastened) "1" else "0"},${if (status.passengerFastened) "1" else "0"},${if (status.rearLeftFastened) "1" else "0"},${if (status.rearCenterFastened) "1" else "0"},${if (status.rearRightFastened) "1" else "0"}]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyanPrimary)
                ) {
                    Text(
                        text = "ปิดหน้าต่าง",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SeatItemCard(
    label: String,
    isFastened: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFastened) Color(0x2210B981) else Color(0x22EF4444))
            .border(
                1.dp,
                if (isFastened) EvBatteryGreen else EvBatteryRed,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "💺",
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = if (isFastened) "คาดแล้ว ✓" else "ไม่ได้คาด ✕",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFastened) EvBatteryGreen else EvBatteryRed
        )
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
