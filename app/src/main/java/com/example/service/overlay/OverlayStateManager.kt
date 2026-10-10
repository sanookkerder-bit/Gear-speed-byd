package com.example.service.overlay

import com.example.model.ArrivalSafetyStatus
import com.example.model.CarGear
import com.example.model.SeatbeltStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OverlayData(
    val speedKmH: Int = 0,
    val gear: CarGear = CarGear.DRIVE,
    val socPercent: Int = 85,
    val arrivalSocPercent: Double = 60.5,
    val destinationName: String = "จุดหมายปลายทาง",
    val distanceKm: Double = 18.5,
    val isSafe: Boolean = true,
    val safetyStatus: ArrivalSafetyStatus = ArrivalSafetyStatus.SAFE,
    val isAdbConnected: Boolean = true,
    val estimatedRangeKm: Double = 85 * 2.9,
    val seatbeltStatus: SeatbeltStatus = SeatbeltStatus()
)

object OverlayStateManager {
    private val _data = MutableStateFlow(OverlayData())
    val data = _data.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning = _isOverlayRunning.asStateFlow()

    fun update(
        speedKmH: Int,
        gear: CarGear,
        socPercent: Int,
        arrivalSocPercent: Double,
        destinationName: String,
        distanceKm: Double,
        isSafe: Boolean,
        safetyStatus: ArrivalSafetyStatus,
        isAdbConnected: Boolean,
        seatbeltStatus: SeatbeltStatus = SeatbeltStatus(),
        estimatedRangeKm: Double = socPercent * 2.9
    ) {
        _data.value = OverlayData(
            speedKmH = speedKmH,
            gear = gear,
            socPercent = socPercent,
            arrivalSocPercent = arrivalSocPercent,
            destinationName = destinationName,
            distanceKm = distanceKm,
            isSafe = isSafe,
            safetyStatus = safetyStatus,
            isAdbConnected = isAdbConnected,
            estimatedRangeKm = estimatedRangeKm,
            seatbeltStatus = seatbeltStatus
        )
    }

    fun setOverlayRunning(running: Boolean) {
        _isOverlayRunning.value = running
    }
}
