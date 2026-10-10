package com.example.model

enum class WirelessProtocol(val titleThai: String, val subtitleThai: String) {
    OBD2_WIFI("OBD2 ไร้สาย (Wi-Fi)", "เชื่อมต่ออะแดปเตอร์ ELM327 / Vgate ผ่าน Wi-Fi (192.168.0.10:35000)"),
    OBD2_BLUETOOTH("OBD2 ไร้สาย (Bluetooth)", "จับคู่อะแดปเตอร์ OBD2 Bluetooth (ELM327 / OBDLink)"),
    ADB_WIFI("ADB ไร้สาย (DiLink)", "เชื่อมต่อหน้าจอกลาง BYD DiLink โดยตรง (192.168.43.1:5555)")
}

data class Obd2Config(
    val wifiIp: String = "192.168.0.10",
    val wifiPort: Int = 35000,
    val bluetoothDeviceAddress: String = "",
    val bluetoothDeviceName: String = "OBDII",
    val protocol: WirelessProtocol = WirelessProtocol.OBD2_WIFI,
    val pidMode: String = "220101", // BYD BMS State of Charge PID
    val queryIntervalSeconds: Int = 2,
    val isAutoQueryEnabled: Boolean = true
)
