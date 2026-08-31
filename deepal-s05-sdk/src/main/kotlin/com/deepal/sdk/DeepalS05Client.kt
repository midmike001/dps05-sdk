package com.deepal.sdk

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
 * Primary developer client and actuator for managing the Deepal S05 vehicle.
 *
 * Provides reactive StateFlow telemetry updates and high-level, developer-friendly
 * suspend methods for:
 * - Dual-Zone Automatic Climate Control & Defrost
 * - Seat Comfort (Ventilation, Heating, Massage presets)
 * - Power Windows, Electric Tailgate, Sunroof Sunshade & Locks
 * - 64-Color Ambient Lighting & PM2.5 Air Purifier
 * - Smart Automotive Scenes (Quick Defrost, Rapid Cool, Nap, Camp)
 * - Next-Gen EV Fast-Charging Battery Thermal Preconditioning
 * - Rain-Sensing Auto Guardian
 */
class DeepalS05Client(
    private val connection: VirtualCarConnection = VirtualCarConnection(),
    val hudClient: DeepalHudClient = DeepalHudClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    companion object {
        private const val TAG = "DeepalS05Client"
    }

    private val _telemetry = MutableStateFlow(DeepalS05Telemetry())
    val telemetry: StateFlow<DeepalS05Telemetry> = _telemetry.asStateFlow()

    private var pollingJob: Job? = null
    var isSimulatedMode: Boolean = false

    /**
     * Directly update telemetry state, especially useful in simulation or demo mode.
     */
    fun updateTelemetry(update: (DeepalS05Telemetry) -> DeepalS05Telemetry) {
        _telemetry.value = update(_telemetry.value)
    }

    /**
     * Starts continuous real-time vehicle signal polling.
     * Speed and critical metrics poll at 250ms; slow cabin metrics poll at 1000ms.
     */
    fun startMonitoring() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            Log.i(TAG, "Starting Deepal S05 telemetry monitor...")
            var slowTick = 0
            while (isActive) {
                try {
                    val rootBinder = connection.getRootVirtualCarService()
                    val isConnected = rootBinder?.isBinderAlive == true

                    if (isConnected) {
                        // High frequency signals (every 250ms)
                        val rawSpeed = connection.getFloatProperty(
                            DeepalS05Property.PROP_VEHICLE_SPEED,
                            DeepalS05Property.AREA_GLOBAL
                        )
                        val speedKmh = if (rawSpeed != null) {
                            if (rawSpeed > 0f && rawSpeed < 70f) (rawSpeed * 3.6f).roundToInt().toFloat() else rawSpeed
                        } else _telemetry.value.speedKmh

                        val rawGear = connection.getIntProperty(
                            DeepalS05Property.PROP_GEAR_SELECTION,
                            DeepalS05Property.AREA_GLOBAL
                        )
                        val gearStr = when (rawGear) {
                            1 -> "P"
                            2 -> "R"
                            3 -> "N"
                            4 -> "D"
                            else -> _telemetry.value.gear
                        }

                        // Slower signals (every 1s)
                        if (slowTick % 4 == 0) {
                            val soc = connection.getIntProperty(
                                DeepalS05Property.PROP_BATTERY_SOC,
                                DeepalS05Property.AREA_GLOBAL
                            ) ?: _telemetry.value.batterySocPercent

                            val range = connection.getIntProperty(
                                DeepalS05Property.PROP_REMAINING_RANGE,
                                DeepalS05Property.AREA_GLOBAL
                            ) ?: _telemetry.value.remainingRangeKm

                            val temp = connection.getFloatProperty(
                                DeepalS05Property.PROP_HVAC_TEMP_SET,
                                DeepalS05Property.AREA_DRIVER
                            ) ?: _telemetry.value.climateTempC

                            val fan = connection.getIntProperty(
                                DeepalS05Property.PROP_HVAC_FAN_SPEED,
                                DeepalS05Property.AREA_GLOBAL
                            ) ?: _telemetry.value.fanSpeed

                            val precondRaw = connection.getIntProperty(
                                DeepalS05Property.PROP_BATTERY_PRECONDITIONING,
                                DeepalS05Property.AREA_GLOBAL
                            )
                            val isPrecond = if (precondRaw != null) precondRaw == 1 else _telemetry.value.isBatteryPreconditioning

                            val rainRaw = connection.getIntProperty(
                                DeepalS05Property.PROP_RAIN_SENSOR_STATE,
                                DeepalS05Property.AREA_GLOBAL
                            )
                            val rainState = rainRaw ?: _telemetry.value.rainSensorState

                            _telemetry.value = _telemetry.value.copy(
                                speedKmh = speedKmh,
                                gear = gearStr,
                                batterySocPercent = soc,
                                remainingRangeKm = range,
                                climateTempC = temp,
                                fanSpeed = fan,
                                isBatteryPreconditioning = isPrecond,
                                rainSensorState = rainState,
                                isVirtualCarConnected = true
                            )
                        } else {
                            _telemetry.value = _telemetry.value.copy(
                                speedKmh = speedKmh,
                                gear = gearStr,
                                isVirtualCarConnected = true
                            )
                        }
                    } else {
                        if (!isSimulatedMode) {
                            _telemetry.value = _telemetry.value.copy(isVirtualCarConnected = false)
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Polling error: ${e.message}")
                }
                slowTick++
                delay(250)
            }
        }
    }

    fun stopMonitoring() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ==========================================
    // 1. Dual-Zone Climate & Defrost Controls
    // ==========================================

    suspend fun setClimatePower(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isClimatePowerOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_POWER_ON,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setClimateTemperature(tempC: Float, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = tempC.coerceIn(DeepalS05Property.TEMP_MIN_C, DeepalS05Property.TEMP_MAX_C)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(climateTempC = clamped)
        } else {
            _telemetry.value.copy(passengerTempC = clamped)
        }
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_TEMP_SET,
            areaId = area,
            className = "java.lang.Float",
            value = clamped
        )
    }

    suspend fun setFanSpeed(speed: Int): Boolean = withContext(Dispatchers.IO) {
        val clamped = speed.coerceIn(1, 8)
        _telemetry.value = _telemetry.value.copy(fanSpeed = clamped)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_FAN_SPEED,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = clamped
        )
    }

    suspend fun setAcEnabled(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAcOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_AC_ON,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setFrontDefrost(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isFrontDefrostOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_DEFROST_FRONT,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setRearDefrost(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isRearDefrostOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_DEFROST_REAR,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setAutoClimate(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAutoClimateOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_AUTO,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    // ==========================================
    // 2. Seats & Cabin Comfort
    // ==========================================

    suspend fun setSeatHeating(level: Int, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = level.coerceIn(0, 3)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(driverSeatHeat = clamped)
        } else {
            _telemetry.value.copy(passengerSeatHeat = clamped)
        }
        connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_HEATING,
            areaId = area,
            className = "java.lang.Integer",
            value = clamped
        )
    }

    suspend fun setSeatVentilation(level: Int, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = level.coerceIn(0, 3)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(driverSeatVent = clamped)
        } else {
            _telemetry.value.copy(passengerSeatVent = clamped)
        }
        connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_VENTILATION,
            areaId = area,
            className = "java.lang.Integer",
            value = clamped
        )
    }

    suspend fun setSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(
            isSeatMassageOn = enabled,
            seatMassageMode = mode,
            seatMassageLevel = level
        )
        connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_MASSAGE_TOGGLE,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
        if (enabled) {
            connection.setProperty(DeepalS05Property.PROP_SEAT_MASSAGE_MODE, DeepalS05Property.AREA_GLOBAL, "java.lang.Integer", mode)
            connection.setProperty(DeepalS05Property.PROP_SEAT_MASSAGE_LEVEL, DeepalS05Property.AREA_GLOBAL, "java.lang.Integer", level)
        }
        true
    }

    suspend fun setSteeringWheelHeat(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isSteeringWheelHeatOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_STEERING_WHEEL_HEAT,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    // ==========================================
    // 3. Windows, Sunroof, Tailgate & Central Locks
    // ==========================================

    suspend fun setWindows(action: Int): Boolean = withContext(Dispatchers.IO) {
        val isOpen = action == 1
        _telemetry.value = _telemetry.value.copy(
            isWindowsOpen = isOpen,
            windowFlOpen = isOpen,
            windowFrOpen = isOpen,
            windowRlOpen = isOpen,
            windowRrOpen = isOpen
        )
        val areas = listOf(
            DeepalS05Property.AREA_WINDOW_FL,
            DeepalS05Property.AREA_WINDOW_FR,
            DeepalS05Property.AREA_WINDOW_RL,
            DeepalS05Property.AREA_WINDOW_RR
        )
        for (area in areas) {
            connection.setProperty(
                propId = DeepalS05Property.PROP_WINDOW_MOVE,
                areaId = area,
                className = "java.lang.Integer",
                value = action
            )
        }
        true
    }

    suspend fun setSunroofShade(action: Int): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isSunroofOpen = action == 1)
        connection.setProperty(
            propId = DeepalS05Property.PROP_SUNROOF_SHADE,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = action
        )
    }

    suspend fun setTailgate(open: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isTailgateOpen = open)
        connection.setProperty(
            propId = DeepalS05Property.PROP_TAILGATE,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (open) 1 else 2
        )
    }

    suspend fun setDoorLock(locked: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isDoorLocked = locked)
        connection.setProperty(
            propId = DeepalS05Property.PROP_DOOR_LOCK,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (locked) 1 else 2
        )
    }

    // ==========================================
    // 4. Lighting & Air Quality
    // ==========================================

    suspend fun setAmbientLight(colorIndex: Int, brightness: Int = 60): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(
            ambientLightColor = colorIndex,
            ambientLightBrightness = brightness
        )
        connection.setProperty(DeepalS05Property.PROP_AMBIENT_LIGHT, DeepalS05Property.AREA_GLOBAL, "java.lang.Integer", colorIndex)
        connection.setProperty(DeepalS05Property.PROP_AMBIENT_LIGHT_BRIGHTNESS, DeepalS05Property.AREA_GLOBAL, "java.lang.Integer", brightness)
    }

    suspend fun setAirPurifier(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAirPurifierOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_AIR_PURIFIER,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    // ==========================================
    // 5. Intelligent EV Next-Gen Features
    // ==========================================

    /**
     * Fast-Charging Battery Thermal Preconditioning.
     * Brings battery pack to optimum fast-charging temperature prior to DC charger arrival.
     */
    suspend fun setBatteryPreconditioning(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isBatteryPreconditioning = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_BATTERY_PRECONDITIONING,
            areaId = DeepalS05Property.AREA_GLOBAL,
            className = "java.lang.Integer",
            value = if (enabled) 1 else 2
        )
    }

    /**
     * Rain-Sensing Guardian.
     * Automatically seals all 4 electric windows and the sunroof sunshade.
     */
    suspend fun executeRainGuardian(): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "Rain Guardian triggered: closing all windows and sunroof shade")
        setWindows(2)      // 2 = Close
        setSunroofShade(2) // 2 = Close
        true
    }

    /**
     * Executes coordinated multi-system smart automotive scenes.
     */
    suspend fun applyScene(sceneName: String) {
        when (sceneName.uppercase()) {
            "RAPID_COOL" -> {
                setClimatePower(true)
                setClimateTemperature(18.0f)
                setFanSpeed(7)
                setSeatVentilation(3)
                setWindows(2)
                setSunroofShade(2)
            }
            "NAP", "REST" -> {
                setWindows(2)
                setSunroofShade(2)
                setClimateTemperature(24.0f)
                setFanSpeed(1)
                setSeatHeating(1)
                setSeatMassage(true, mode = 1, level = 1)
                setAmbientLight(2, 30) // Sunset Amber dim
            }
            "DEFROST" -> {
                setFrontDefrost(true)
                setRearDefrost(true)
                setSteeringWheelHeat(true)
                setSeatHeating(3)
            }
            "CAMP" -> {
                setClimatePower(true)
                setClimateTemperature(23.0f)
                setFanSpeed(2)
                setAmbientLight(1, 50) // Forest Emerald
            }
        }
    }
}
