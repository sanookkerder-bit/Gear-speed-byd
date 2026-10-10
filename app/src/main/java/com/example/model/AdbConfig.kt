package com.example.model

data class AdbConfig(
    val ipAddress: String = "192.168.43.1",
    val port: Int = 5555,
    val autoPollSeconds: Int = 5,
    val isAutoPollEnabled: Boolean = true,
    val isDemoMode: Boolean = false
) {
    val address: String
        get() = "$ipAddress:$port"
}

sealed class AdbConnectionStatus {
    object Disconnected : AdbConnectionStatus()
    data class Connecting(val message: String = "กำลังเชื่อมต่อไปยัง DiLink...") : AdbConnectionStatus()
    data class Connected(
        val ip: String,
        val port: Int,
        val pingMs: Long,
        val message: String = "เชื่อมต่อสำเร็จ (ADB ไร้สายพร้อมใช้งาน)"
    ) : AdbConnectionStatus()
    data class Error(val errorMessage: String) : AdbConnectionStatus()
}

data class AdbConsoleLog(
    val timestamp: Long = System.currentTimeMillis(),
    val command: String,
    val output: String,
    val isError: Boolean = false
)
