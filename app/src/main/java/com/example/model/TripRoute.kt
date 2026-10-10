package com.example.model

enum class DrivingMode(val titleThai: String, val factor: Double) {
    ECO("Eco (ประหยัดพลังงาน)", 0.90),
    NORMAL("Normal (ปกติ)", 1.00),
    SPORT("Sport (สปอร์ต เร่งเร็ว)", 1.18)
}

data class TripDestination(
    val id: String,
    val nameThai: String,
    val nameEn: String,
    val category: DestinationCategory,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val hasDcFastCharger: Boolean = false,
    val chargingProvider: String? = null // e.g. "PTT EV Station PluX 120kW"
)

enum class DestinationCategory(val labelThai: String) {
    CHARGING_HUB("สถานีชาร์จเร็ว DC"),
    MALL("ห้างสรรพสินค้า / จุดพัก"),
    PROVINCE("ต่างจังหวัด / ท่องเที่ยว"),
    CUSTOM("กำหนดเอง")
}

data class RouteCalculationResult(
    val destination: TripDestination,
    val distanceKm: Double,
    val currentSoc: Int,
    val drivingMode: DrivingMode,
    val acFactor: Double, // e.g. 1.05 for AC ON
    val passengerFactor: Double, // e.g. 1.03 for 3+ passengers
    val carModel: BydCarModel,
    val consumedKwh: Double,
    val consumedSocPercent: Double,
    val arrivalSocPercent: Double,
    val remainingRangeAtDestinationKm: Double,
    val isSafe: Boolean,
    val safetyStatus: ArrivalSafetyStatus,
    val chargingRecommendation: String?
)

enum class ArrivalSafetyStatus(val labelThai: String, val descriptionThai: String) {
    SAFE("ปลอดภัย (เหลือ > 25%)", "แบตเตอรี่เพียงพอต่อการเดินทาง ไม่จำเป็นต้องแวะชาร์จ"),
    MODERATE("ปานกลาง (เหลือ 15-25%)", "แบตเตอรี่พอถึงปลายทาง แนะนำชาร์จเมื่อถึงที่หมาย"),
    LOW_WARNING("เตือน! แบตเตอรี่ต่ำ (เหลือ 5-15%)", "เสี่ยงต่อแบตหมด แนะนำวางแผนแวะชาร์จระหว่างทาง"),
    CRITICAL_UNREACHABLE("อันตราย! แบตไม่พอถึง (< 5%)", "จำเป็นต้องแวะชาร์จ DC Fast Charge ระหว่างทาง!")
}
