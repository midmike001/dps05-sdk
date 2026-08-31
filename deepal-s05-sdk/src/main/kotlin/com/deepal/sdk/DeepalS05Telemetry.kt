package com.deepal.sdk

/**
 * Immutable vehicle telemetry data model representing the live state of Deepal S05.
 */
data class DeepalS05Telemetry(
    val speedKmh: Float = 0f,
    val gear: String = "P",
    val batterySocPercent: Int = 0,
    val remainingRangeKm: Int = 0,
    val odometerKm: Float = 0f,
    val exteriorTempC: Float = 25.0f,
    val driveMode: String = "COMFORT",

    // Climate
    val isClimatePowerOn: Boolean = false,
    val isAcOn: Boolean = true,
    val isAutoClimateOn: Boolean = true,
    val climateTempC: Float = 24.0f,
    val passengerTempC: Float = 24.0f,
    val fanSpeed: Int = 1,
    val isRecirculationOn: Boolean = false,
    val isFrontDefrostOn: Boolean = false,
    val isRearDefrostOn: Boolean = false,
    val isSteeringWheelHeatOn: Boolean = false,

    // Seats & Comfort
    val driverSeatHeat: Int = 0,     // 0=Off, 1=Low, 2=Med, 3=High
    val passengerSeatHeat: Int = 0,
    val driverSeatVent: Int = 0,     // 0=Off, 1=Low, 2=Med, 3=High
    val passengerSeatVent: Int = 0,
    val isSeatMassageOn: Boolean = false,
    val seatMassageMode: Int = 1,
    val seatMassageLevel: Int = 1,

    // Windows & Access
    val isDoorLocked: Boolean = true,
    val isTailgateOpen: Boolean = false,
    val isSunroofOpen: Boolean = false,
    val isWindowsOpen: Boolean = false,
    val windowFlOpen: Boolean = false,
    val windowFrOpen: Boolean = false,
    val windowRlOpen: Boolean = false,
    val windowRrOpen: Boolean = false,

    // Lighting & Environment
    val ambientLightColor: Int = 1,
    val ambientLightBrightness: Int = 60,
    val isAirPurifierOn: Boolean = false,
    val isAebOn: Boolean = true,

    // Intelligent Automation Signals
    val isBatteryPreconditioning: Boolean = false,
    val rainSensorState: Int = 1, // 1=No Rain, 2=Light Rain, 3=Heavy Rain

    // Connection Status
    val isVirtualCarConnected: Boolean = false
)
