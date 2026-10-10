package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DestinationPresets
import com.example.model.AdbConfig
import com.example.model.AdbConnectionStatus
import com.example.model.AdbConsoleLog
import com.example.model.ArrivalSafetyStatus
import com.example.model.BydBatteryData
import com.example.model.BydCarModel
import com.example.model.CarGear
import com.example.model.DestinationCategory
import com.example.model.DrivingMode
import com.example.model.RouteCalculationResult
import com.example.model.SeatbeltStatus
import com.example.model.TripDestination
import com.example.service.adb.AdbWirelessClient
import com.example.service.location.LocationSpeedTracker
import com.example.service.overlay.FloatingOverlayService
import com.example.service.overlay.OverlayStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

data class BydNavUiState(
    val carModel: BydCarModel = BydCarModel.DEFAULT,
    val batteryData: BydBatteryData = BydBatteryData(),
    val adbConfig: AdbConfig = AdbConfig(),
    val adbStatus: AdbConnectionStatus = AdbConnectionStatus.Disconnected,
    val consoleLogs: List<AdbConsoleLog> = emptyList(),
    val destinations: List<TripDestination> = DestinationPresets.POPULAR_DESTINATIONS,
    val selectedDestination: TripDestination? = DestinationPresets.POPULAR_DESTINATIONS[0],
    val searchDestinationQuery: String = "",
    val customDistanceKm: Double = 18.5,
    val drivingMode: DrivingMode = DrivingMode.NORMAL,
    val isAcOn: Boolean = true,
    val passengerCount: Int = 2,
    val safetyReservePercent: Int = 15,
    val routeCalculation: RouteCalculationResult? = null,
    val isHudModeOpen: Boolean = false,
    val isSimulatingDrive: Boolean = false,
    val currentSpeedKmH: Int = 0,
    val currentGear: CarGear = CarGear.DRIVE,
    val isGpsActive: Boolean = false,
    val isOverlayActive: Boolean = false,
    val seatbeltStatus: SeatbeltStatus = SeatbeltStatus()
)

class BydNavViewModel : ViewModel() {

    private val adbClient = AdbWirelessClient()
    private var speedTracker: LocationSpeedTracker? = null
    private val _uiState = MutableStateFlow(BydNavUiState())
    val uiState: StateFlow<BydNavUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private var simulationJob: Job? = null

    init {
        calculateRoute()
        addLog("SYSTEM", "BYD EV Nav System Initialized. พร้อมเชื่อมต่อ ADB Wireless (Android Debug Bridge)")

        // Observe overlay status
        viewModelScope.launch {
            OverlayStateManager.isOverlayRunning.collect { isRunning ->
                _uiState.update { it.copy(isOverlayActive = isRunning) }
            }
        }
    }

    fun initLocationTracker(context: Context) {
        if (speedTracker != null) return
        val tracker = LocationSpeedTracker(context.applicationContext)
        speedTracker = tracker
        tracker.startTracking { location ->
            handleLocationUpdate(location)
        }
        viewModelScope.launch {
            tracker.currentSpeedKmH.collect { speed ->
                _uiState.update { current ->
                    current.copy(
                        currentSpeedKmH = speed,
                        currentGear = if (speed > 0 && current.currentGear == CarGear.PARK) CarGear.DRIVE else current.currentGear,
                        isGpsActive = tracker.isGpsActive.value
                    )
                }
                syncOverlayData()
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        val dest = _uiState.value.selectedDestination ?: return
        val destLoc = Location("").apply {
            latitude = dest.latitude
            longitude = dest.longitude
        }
        val distanceMeters = location.distanceTo(destLoc)
        val distanceKm = (distanceMeters / 1000.0).coerceAtLeast(0.1)
        _uiState.update { it.copy(customDistanceKm = distanceKm) }
        calculateRoute()
    }

    fun setGear(gear: CarGear) {
        _uiState.update { it.copy(currentGear = gear) }
        syncOverlayData()
    }

    fun setSpeedManually(speedKmH: Int) {
        val clamped = speedKmH.coerceIn(0, 220)
        _uiState.update {
            it.copy(
                currentSpeedKmH = clamped,
                currentGear = if (clamped > 0 && it.currentGear == CarGear.PARK) CarGear.DRIVE else it.currentGear
            )
        }
        syncOverlayData()
    }

    fun toggleFloatingOverlay(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(intent)
            Toast.makeText(context, "กรุณาเปิด 'อนุญาตให้แสดงทับแอปอื่น' เพื่อให้ Statusbar แสดงทับ Google Maps ได้", Toast.LENGTH_LONG).show()
            return
        }

        if (OverlayStateManager.isOverlayRunning.value) {
            FloatingOverlayService.stop(context)
            Toast.makeText(context, "ปิดแถบแสดงทับแอพอื่นแล้ว", Toast.LENGTH_SHORT).show()
        } else {
            syncOverlayData()
            FloatingOverlayService.start(context)
            Toast.makeText(context, "เปิดแถบ Statusbar แสดงทับแอพอื่นแล้ว (พร้อมเปิด Google Maps นำทาง)", Toast.LENGTH_LONG).show()
        }
    }

    fun syncOverlayData() {
        val state = _uiState.value
        val route = state.routeCalculation
        OverlayStateManager.update(
            speedKmH = state.currentSpeedKmH,
            gear = state.currentGear,
            socPercent = state.batteryData.socPercent,
            arrivalSocPercent = route?.arrivalSocPercent ?: (state.batteryData.socPercent - 15.0),
            destinationName = route?.destination?.nameThai ?: "จุดหมายปลายทาง",
            distanceKm = route?.distanceKm ?: state.customDistanceKm,
            isSafe = route?.isSafe ?: true,
            safetyStatus = route?.safetyStatus ?: ArrivalSafetyStatus.SAFE,
            isAdbConnected = state.adbStatus is AdbConnectionStatus.Connected,
            seatbeltStatus = state.seatbeltStatus,
            estimatedRangeKm = state.batteryData.socPercent * 2.9
        )
    }

    fun toggleSeatbelt(seatIndex: Int) {
        val updated = _uiState.value.seatbeltStatus.toggleSeat(seatIndex)
        _uiState.update { it.copy(seatbeltStatus = updated) }
        val seatName = when (seatIndex) {
            0 -> "คนขับ (FL)"
            1 -> "ผู้โดยสารหน้า (FR)"
            2 -> "หลังซ้าย (RL)"
            3 -> "หลังกลาง (RC)"
            4 -> "หลังขวา (RR)"
            else -> "ที่นั่ง $seatIndex"
        }
        val isFastened = when (seatIndex) {
            0 -> updated.driverFastened
            1 -> updated.passengerFastened
            2 -> updated.rearLeftFastened
            3 -> updated.rearCenterFastened
            4 -> updated.rearRightFastened
            else -> false
        }
        val statusText = if (isFastened) "คาดเข็มขัดแล้ว ✓" else "ปลดเข็มขัด ✕"
        addLog("ADB_SEATBELT", "ตรวจจับเซ็นเซอร์ $seatName: $statusText (อ่านผ่าน ADB)")
        syncOverlayData()
    }

    fun updateCarModel(model: BydCarModel) {
        _uiState.update { it.copy(carModel = model) }
        calculateRoute()
    }

    fun updateAdbIp(ip: String) {
        _uiState.update { it.copy(adbConfig = it.adbConfig.copy(ipAddress = ip)) }
    }

    fun updateAdbPort(port: Int) {
        _uiState.update { it.copy(adbConfig = it.adbConfig.copy(port = port)) }
    }

    fun toggleDemoMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                adbConfig = it.adbConfig.copy(isDemoMode = enabled),
                adbStatus = if (enabled) {
                    AdbConnectionStatus.Connected(
                        ip = "127.0.0.1",
                        port = 5555,
                        pingMs = 12,
                        message = "เชื่อมต่อโหมดจำลอง (Demo Simulator Connected)"
                    )
                } else AdbConnectionStatus.Disconnected
            )
        }
        if (enabled) {
            addLog("SIMULATOR", "เปิดใช้งานโหมดจำลอง BYD DiLink Telemetry สำเร็จ")
        }
        calculateRoute()
    }

    fun connectAdb() {
        val config = _uiState.value.adbConfig
        if (config.isDemoMode) {
            _uiState.update {
                it.copy(
                    adbStatus = AdbConnectionStatus.Connected(
                        ip = "127.0.0.1",
                        port = 5555,
                        pingMs = 8,
                        message = "โหมดจำลอง (DiLink Demo Active)"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(adbStatus = AdbConnectionStatus.Connecting("กำลังเชื่อมต่อไปยัง ${config.address}...")) }
            addLog("ADB_CONNECT", "เริ่มต้นเชื่อมต่อไปยัง DiLink: ${config.address}")

            val result = adbClient.fetchBatteryStatus(config.ipAddress, config.port)
            if (result.success) {
                val newBattery = result.batteryData ?: _uiState.value.batteryData
                val newSeatbelts = result.seatbeltStatus ?: _uiState.value.seatbeltStatus
                _uiState.update {
                    it.copy(
                        adbStatus = AdbConnectionStatus.Connected(
                            ip = config.ipAddress,
                            port = config.port,
                            pingMs = result.pingMs,
                            message = "เชื่อมต่อสำเร็จ (Ping: ${result.pingMs} ms)"
                        ),
                        batteryData = newBattery,
                        seatbeltStatus = newSeatbelts
                    )
                }
                addLog("ADB_SUCCESS", "รับข้อมูลแบตเตอรี่สำเร็จ: SoC=${newBattery.socPercent}%, เซ็นเซอร์เข็มขัด 5 จุด: ${newSeatbelts.fastenedCount}/5 คาดแล้ว")
                calculateRoute()
                syncOverlayData()
                startAutoPolling()
            } else {
                _uiState.update {
                    it.copy(
                        adbStatus = AdbConnectionStatus.Error(result.errorMessage ?: "การเชื่อมต่อล้มเหลว")
                    )
                }
                addLog("ADB_ERROR", result.output, isError = true)
            }
        }
    }

    fun disconnectAdb() {
        pollJob?.cancel()
        pollJob = null
        _uiState.update {
            it.copy(adbStatus = AdbConnectionStatus.Disconnected)
        }
        addLog("ADB_DISCONNECT", "ตัดการเชื่อมต่อ ADB Wireless แล้ว")
    }

    fun refreshBatteryNow() {
        val config = _uiState.value.adbConfig
        if (config.isDemoMode) {
            val current = _uiState.value.batteryData
            val updated = current.copy(
                temperatureCelsius = (current.temperatureCelsius + ((-2..2).random() * 0.1f)).coerceIn(25f, 40f),
                lastSyncTimestamp = System.currentTimeMillis()
            )
            _uiState.update { it.copy(batteryData = updated) }
            addLog("DEMO_SYNC", "อัปเดตข้อมูลจำลอง: SoC=${updated.socPercent}%")
            calculateRoute()
            return
        }

        viewModelScope.launch {
            val result = adbClient.fetchBatteryStatus(config.ipAddress, config.port)
            if (result.success && result.batteryData != null) {
                _uiState.update {
                    it.copy(
                        batteryData = result.batteryData,
                        seatbeltStatus = result.seatbeltStatus ?: it.seatbeltStatus
                    )
                }
                addLog("ADB_SYNC", "อัปเดตข้อมูลเรียบร้อย (SoC=${result.batteryData.socPercent}%, เข็มขัด 5 จุด: ${result.seatbeltStatus?.fastenedCount ?: 5}/5)")
                calculateRoute()
                syncOverlayData()
            } else {
                addLog("ADB_SYNC_ERR", result.errorMessage ?: "อัปเดตล้มเหลว", isError = true)
            }
        }
    }

    private fun startAutoPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive && _uiState.value.adbConfig.isAutoPollEnabled) {
                delay(_uiState.value.adbConfig.autoPollSeconds * 1000L)
                if (_uiState.value.adbStatus is AdbConnectionStatus.Connected && !_uiState.value.adbConfig.isDemoMode) {
                    val result = adbClient.fetchBatteryStatus(_uiState.value.adbConfig.ipAddress, _uiState.value.adbConfig.port)
                    if (result.success && result.batteryData != null) {
                        _uiState.update {
                            it.copy(
                                batteryData = result.batteryData,
                                seatbeltStatus = result.seatbeltStatus ?: it.seatbeltStatus
                            )
                        }
                        calculateRoute()
                        syncOverlayData()
                    }
                }
            }
        }
    }

    fun setBatteryLevelManually(level: Int) {
        val clamped = level.coerceIn(1, 100)
        val car = _uiState.value.carModel
        val estRange = ((clamped / 100.0) * car.ratedRangeKm).toInt()
        val updated = _uiState.value.batteryData.copy(
            socPercent = clamped,
            estimatedRangeKm = estRange,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(batteryData = updated) }
        addLog("MANUAL_SET", "กำหนดระดับแบตเตอรี่: $clamped% (ระยะทางประเมิน $estRange กม.)")
        calculateRoute()
    }

    fun toggleDriveSimulation() {
        val isSim = !_uiState.value.isSimulatingDrive
        _uiState.update { it.copy(isSimulatingDrive = isSim) }
        if (isSim) {
            addLog("SIM_DRIVE", "เริ่มต้นจำลองการขับขี่จริง (แบตเตอรี่ลดลงตามเวลา)")
            simulationJob?.cancel()
            simulationJob = viewModelScope.launch {
                while (isActive && _uiState.value.isSimulatingDrive) {
                    delay(3000L)
                    val currentSoc = _uiState.value.batteryData.socPercent
                    if (currentSoc > 5) {
                        val newSoc = currentSoc - 1
                        val car = _uiState.value.carModel
                        val newRange = ((newSoc / 100.0) * car.ratedRangeKm).toInt()
                        _uiState.update {
                            it.copy(
                                batteryData = it.batteryData.copy(
                                    socPercent = newSoc,
                                    estimatedRangeKm = newRange,
                                    lastSyncTimestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        calculateRoute()
                    } else {
                        _uiState.update { it.copy(isSimulatingDrive = false) }
                        break
                    }
                }
            }
        } else {
            simulationJob?.cancel()
            simulationJob = null
            addLog("SIM_DRIVE", "หยุดจำลองการขับขี่")
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchDestinationQuery = query) }
    }

    fun selectDestination(destination: TripDestination) {
        _uiState.update {
            it.copy(
                selectedDestination = destination,
                customDistanceKm = destination.distanceKm
            )
        }
        calculateRoute()
    }

    fun setCustomDistance(km: Double) {
        val clamped = max(0.5, km)
        _uiState.update {
            it.copy(
                customDistanceKm = clamped,
                selectedDestination = it.selectedDestination?.copy(distanceKm = clamped)
                    ?: TripDestination(
                        id = "custom",
                        nameThai = "จุดหมายปลายทางกำหนดเอง",
                        nameEn = "Custom Destination",
                        category = DestinationCategory.CUSTOM,
                        distanceKm = clamped,
                        latitude = 13.7563,
                        longitude = 100.5018
                    )
            )
        }
        calculateRoute()
    }

    fun addCustomSearchDestination(name: String, distanceKm: Double) {
        val newDest = TripDestination(
            id = "custom_${System.currentTimeMillis()}",
            nameThai = name.ifBlank { "จุดหมายที่ค้นหา" },
            nameEn = "Searched Location",
            category = DestinationCategory.CUSTOM,
            distanceKm = distanceKm.coerceAtLeast(0.5),
            latitude = 13.7563,
            longitude = 100.5018
        )
        _uiState.update {
            it.copy(
                destinations = listOf(newDest) + it.destinations,
                selectedDestination = newDest,
                customDistanceKm = newDest.distanceKm,
                searchDestinationQuery = ""
            )
        }
        calculateRoute()
        addLog("SEARCH", "เพิ่มจุดหมาย: $name ($distanceKm กม.)")
    }

    fun setDrivingMode(mode: DrivingMode) {
        _uiState.update { it.copy(drivingMode = mode) }
        calculateRoute()
    }

    fun toggleAc(isOn: Boolean) {
        _uiState.update { it.copy(isAcOn = isOn) }
        calculateRoute()
    }

    fun setPassengerCount(count: Int) {
        _uiState.update { it.copy(passengerCount = count.coerceIn(1, 7)) }
        calculateRoute()
    }

    fun setSafetyReserve(percent: Int) {
        _uiState.update { it.copy(safetyReservePercent = percent.coerceIn(5, 30)) }
        calculateRoute()
    }

    fun setHudMode(isOpen: Boolean) {
        _uiState.update { it.copy(isHudModeOpen = isOpen) }
    }

    fun calculateRoute() {
        val state = _uiState.value
        val dest = state.selectedDestination ?: return
        val distance = state.customDistanceKm
        val currentSoc = state.batteryData.socPercent
        val car = state.carModel

        val acFactor = if (state.isAcOn) 1.06 else 1.00
        val passengerFactor = 1.0 + ((state.passengerCount - 1) * 0.02)
        val modeFactor = state.drivingMode.factor

        val actualConsumptionPer100Km = car.baseConsumptionKwhPer100Km * modeFactor * acFactor * passengerFactor
        val totalKwhUsed = (distance / 100.0) * actualConsumptionPer100Km

        val consumedPercent = (totalKwhUsed / car.batteryCapacityKwh) * 100.0
        val arrivalSoc = currentSoc - consumedPercent
        val remainingRangeAtDest = max(0.0, state.batteryData.estimatedRangeKm - distance)

        val safetyStatus = when {
            arrivalSoc >= 25.0 -> ArrivalSafetyStatus.SAFE
            arrivalSoc >= 15.0 -> ArrivalSafetyStatus.MODERATE
            arrivalSoc >= 5.0 -> ArrivalSafetyStatus.LOW_WARNING
            else -> ArrivalSafetyStatus.CRITICAL_UNREACHABLE
        }

        val recommendation = when (safetyStatus) {
            ArrivalSafetyStatus.SAFE -> "✅ แบตเตอรี่เหลือสบายๆ ไม่ต้องแวะชาร์จระหว่างทาง"
            ArrivalSafetyStatus.MODERATE -> "ℹ️ เดินทางถึงได้ปลอดภัย แต่ควรชาร์จที่จุดหมายปลายทาง"
            ArrivalSafetyStatus.LOW_WARNING -> "⚠️ เตือน: ปลายทางจะเหลือเพียง ${String.format("%.1f", arrivalSoc)}% แนะนำแวะชาร์จ DC ที่จุดพัก 15-20 นาที"
            ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> "🚨 วิกฤต: แบตเตอรี่ไม่เพียงพอสำหรับระยะทาง ${String.format("%.1f", distance)} กม.! จำเป็นต้องชาร์จระหว่างทาง"
        }

        val result = RouteCalculationResult(
            destination = dest,
            distanceKm = distance,
            currentSoc = currentSoc,
            drivingMode = state.drivingMode,
            acFactor = acFactor,
            passengerFactor = passengerFactor,
            carModel = car,
            consumedKwh = totalKwhUsed,
            consumedSocPercent = consumedPercent,
            arrivalSocPercent = arrivalSoc,
            remainingRangeAtDestinationKm = remainingRangeAtDest,
            isSafe = arrivalSoc >= state.safetyReservePercent,
            safetyStatus = safetyStatus,
            chargingRecommendation = recommendation
        )

        _uiState.update { it.copy(routeCalculation = result) }
        syncOverlayData()
    }

    fun launchGoogleMapsNavigation(context: Context, destination: TripDestination) {
        try {
            // Automatically start floating overlay so speed, gears, and arrival SoC stay on top of Google Maps!
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                syncOverlayData()
                FloatingOverlayService.start(context)
            }

            val navUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                Toast.makeText(context, "กำลังเปิด Google Maps นำทาง (Statusbar ลอยแสดงทับด้านบน)", Toast.LENGTH_SHORT).show()
                addLog("NAV_LAUNCH", "เปิด Google Maps นำทางไปยัง: ${destination.nameThai} (${destination.distanceKm} กม.)")
            } else {
                val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                addLog("NAV_LAUNCH", "เปิดแผนที่ผ่านเว็บ: ${destination.nameThai}")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "ไม่สามารถเปิดแผนที่ได้: ${e.message}", Toast.LENGTH_LONG).show()
            addLog("NAV_ERROR", "เปิดแผนที่ล้มเหลว: ${e.message}", isError = true)
        }
    }

    fun runCustomAdbCommand(cmd: String) {
        if (cmd.isBlank()) return
        val config = _uiState.value.adbConfig
        addLog("EXEC", "> $cmd")
        if (config.isDemoMode) {
            when {
                cmd.contains("battery") -> {
                    addLog("ADB_RESP", "Current Battery: level=${_uiState.value.batteryData.socPercent}, voltage=412000, temp=285")
                }
                cmd.contains("soc") -> {
                    addLog("ADB_RESP", "persist.sys.byd.soc: [${_uiState.value.batteryData.socPercent}]")
                }
                else -> {
                    addLog("ADB_RESP", "Demo output for: $cmd (Executed OK)")
                }
            }
            return
        }

        viewModelScope.launch {
            val res = adbClient.fetchBatteryStatus(config.ipAddress, config.port)
            addLog("ADB_RESP", res.output.take(500))
        }
    }

    private fun addLog(command: String, output: String, isError: Boolean = false) {
        val newLog = AdbConsoleLog(
            command = command,
            output = output,
            isError = isError
        )
        _uiState.update {
            val updatedList = (listOf(newLog) + it.consoleLogs).take(40)
            it.copy(consoleLogs = updatedList)
        }
    }
}
