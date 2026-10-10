package com.example.model

data class BydBatteryData(
    val socPercent: Int = 85,
    val voltageMv: Int = 412500, // in millivolts -> 412.5V high voltage pack
    val temperatureCelsius: Float = 28.5f,
    val isCharging: Boolean = false,
    val chargeStatus: String = "Discharging (ขับขี่)",
    val chargePowerKw: Double = 0.0,
    val stateOfHealthPercent: Int = 99,
    val estimatedRangeKm: Int = 408,
    val bladeCellMaxTemp: Float = 29.2f,
    val bladeCellMinTemp: Float = 27.9f,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val rawLog: String = "dumpsys battery: level=85, voltage=412500, temp=285"
) {
    val voltageVolts: Double
        get() = voltageMv / 1000.0

    val socColorCategory: SocCategory
        get() = when {
            socPercent >= 50 -> SocCategory.HIGH
            socPercent >= 20 -> SocCategory.NORMAL
            socPercent >= 10 -> SocCategory.LOW
            else -> SocCategory.CRITICAL
        }
}

enum class SocCategory {
    HIGH, NORMAL, LOW, CRITICAL
}
