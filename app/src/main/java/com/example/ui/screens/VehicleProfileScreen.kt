package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BydCarModel
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvCyanPrimary
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel

@Composable
fun VehicleProfileScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "เลือกรุ่นรถยนต์ไฟฟ้า BYD ของคุณ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "ระบบจะคำนวณความจุแบตเตอรี่ Blade Battery และอัตราการกินไฟที่แม่นยำตามรุ่น",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Active Car Model Specs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_car_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "รถยนต์ที่เลือกใช้งานอยู่",
                                fontSize = 11.sp,
                                color = EvCyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.carModel.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EvCyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Active Car",
                                tint = EvCyanPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SpecChip(
                            title = "ความจุแบตเตอรี่",
                            value = "${uiState.carModel.batteryCapacityKwh} kWh",
                            icon = Icons.Default.ElectricBolt,
                            modifier = Modifier.weight(1f)
                        )
                        SpecChip(
                            title = "ระยะทางสูงสุด",
                            value = "${uiState.carModel.ratedRangeKm} กม.",
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f)
                        )
                        SpecChip(
                            title = "อัตราเฉลี่ย",
                            value = "${uiState.carModel.baseConsumptionKwhPer100Km} kWh/100km",
                            icon = Icons.Default.Shield,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Blade Battery technology badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Blade Battery",
                            tint = EvBatteryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "BYD FinDreams Blade Battery (LFP Technology)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ปลอดภัยสูงสุด ผ่านการทดสอบเจาะด้วยตะปู (Nail Penetration Test)",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Safety Reserve Margin Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ระดับแบตเตอรี่สำรองความปลอดภัย (Safety Buffer)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${uiState.safetyReservePercent}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EvCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ระบบจะแจ้งเตือนให้แวะชาร์จ หากคำนวณแล้วแบตเตอรี่ ณ จุดหมายต่ำกว่าค่านี้",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = uiState.safetyReservePercent.toFloat(),
                        onValueChange = { viewModel.setSafetyReserve(it.toInt()) },
                        valueRange = 5f..30f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = EvCyanPrimary,
                            activeTrackColor = EvCyanPrimary
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "รายการรุ่นรถยนต์ไฟฟ้า BYD ทั้งหมด",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // List of all BYD models
        items(BydCarModel.ALL_MODELS) { model ->
            val isSelected = uiState.carModel.id == model.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.updateCarModel(model) }
                    .testTag("car_model_${model.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF00363D) else Color(0xFF0F172A)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = model.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "แบตเตอรี่ ${model.batteryCapacityKwh} kWh • ระยะสูงสุด ${model.ratedRangeKm} กม.",
                            fontSize = 11.sp,
                            color = if (isSelected) EvCyanPrimary else Color(0xFF94A3B8)
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EvBatteryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF003914),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SpecChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .padding(8.dp)
    ) {
        Column {
            Text(text = title, fontSize = 9.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
