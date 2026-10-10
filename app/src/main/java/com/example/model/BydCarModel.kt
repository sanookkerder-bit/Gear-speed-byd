package com.example.model

data class BydCarModel(
    val id: String,
    val name: String,
    val series: String,
    val batteryCapacityKwh: Double,
    val ratedRangeKm: Int,
    val baseConsumptionKwhPer100Km: Double,
    val bladeBatteryCells: Int = 126
) {
    companion object {
        val ALL_MODELS = listOf(
            BydCarModel(
                id = "atto3_extended",
                name = "BYD ATTO 3 (Extended Range)",
                series = "ATTO 3",
                batteryCapacityKwh = 60.48,
                ratedRangeKm = 480,
                baseConsumptionKwhPer100Km = 14.8
            ),
            BydCarModel(
                id = "atto3_standard",
                name = "BYD ATTO 3 (Standard Range)",
                series = "ATTO 3",
                batteryCapacityKwh = 49.92,
                ratedRangeKm = 410,
                baseConsumptionKwhPer100Km = 14.5
            ),
            BydCarModel(
                id = "dolphin_extended",
                name = "BYD DOLPHIN (Extended Range)",
                series = "DOLPHIN",
                batteryCapacityKwh = 60.48,
                ratedRangeKm = 490,
                baseConsumptionKwhPer100Km = 13.6
            ),
            BydCarModel(
                id = "dolphin_standard",
                name = "BYD DOLPHIN (Standard Range)",
                series = "DOLPHIN",
                batteryCapacityKwh = 44.90,
                ratedRangeKm = 410,
                baseConsumptionKwhPer100Km = 13.0
            ),
            BydCarModel(
                id = "seal_dynamic",
                name = "BYD SEAL (Dynamic RWD)",
                series = "SEAL",
                batteryCapacityKwh = 61.44,
                ratedRangeKm = 510,
                baseConsumptionKwhPer100Km = 14.5
            ),
            BydCarModel(
                id = "seal_premium",
                name = "BYD SEAL (Premium RWD)",
                series = "SEAL",
                batteryCapacityKwh = 82.56,
                ratedRangeKm = 650,
                baseConsumptionKwhPer100Km = 15.2
            ),
            BydCarModel(
                id = "seal_awd",
                name = "BYD SEAL (AWD Performance)",
                series = "SEAL",
                batteryCapacityKwh = 82.56,
                ratedRangeKm = 580,
                baseConsumptionKwhPer100Km = 16.8
            ),
            BydCarModel(
                id = "sealion6_dmi",
                name = "BYD SEALION 6 (DM-i Super Hybrid)",
                series = "SEALION",
                batteryCapacityKwh = 18.30,
                ratedRangeKm = 92,
                baseConsumptionKwhPer100Km = 15.8
            )
        )

        val DEFAULT = ALL_MODELS[0] // ATTO 3 Extended is the most popular in Thailand
    }
}
