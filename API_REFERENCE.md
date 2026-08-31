# `deepal-s05-sdk` API Reference Manual

Detailed API documentation for the **Changan Deepal S05 Kotlin SDK**.

---

## 1. Class: `DeepalS05Client`

The primary access point for monitoring vehicle telemetry and actuating cabin controls.

### Constructor
```kotlin
class DeepalS05Client(
    val connection: VirtualCarConnection = VirtualCarConnection(),
    val hudClient: DeepalHudClient = DeepalHudClient(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)
```

### Properties
- `telemetry: StateFlow<DeepalS05Telemetry>`: Reactive flow of real-time vehicle signals.
- `hudClient: DeepalHudClient`: InCall AR-HUD navigation guidance manager.

### Lifecycle Methods
- `startMonitoring()`: Initiates background polling of vehicle signals.
- `stopMonitoring()`: Stops polling and releases coroutine jobs.

### Climate & HVAC Methods
- `suspend fun setClimatePower(enabled: Boolean): Boolean`
  - Turns climate power On/Off (`PROP_HVAC_POWER_ON = 0x3540010b`).
- `suspend fun setClimateTemperature(tempC: Float, area: Int = AREA_DRIVER): Boolean`
  - Adjusts target cabin temperature (17.5°C to 32.5°C in 0.5°C steps).
- `suspend fun setFanSpeed(speed: Int): Boolean`
  - Sets fan blower speed from 1 to 8 (`PROP_HVAC_FAN_SPEED = 0x35400109`).
- `suspend fun setAcEnabled(enabled: Boolean): Boolean`
  - Toggles A/C compressor (`PROP_HVAC_AC_ON = 0x35400104`).
- `suspend fun setFrontDefrost(enabled: Boolean): Boolean`
  - Toggles front windscreen defroster (`PROP_HVAC_DEFROST_FRONT = 0x33400103`).
- `suspend fun setRearDefrost(enabled: Boolean): Boolean`
  - Toggles rear window defroster (`PROP_HVAC_DEFROST_REAR = 0x3540010c`).
- `suspend fun setAutoClimate(enabled: Boolean): Boolean`
  - Toggles automatic temperature control (`PROP_HVAC_AUTO = 0x35400101`).

### Seats & Comfort Methods
- `suspend fun setSeatHeating(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High).
  - Areas: `AREA_DRIVER = 1`, `AREA_PASSENGER = 4`.
- `suspend fun setSeatVentilation(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High).
- `suspend fun setSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean`
  - Controls driver pneumatic massage presets (`0x31400b2f`, `0x31400b31`, `0x31400b30`).
- `suspend fun setSteeringWheelHeat(enabled: Boolean): Boolean`
  - Controls steering wheel heating element (`0x314003eb`).

### Access & Body Methods
- `suspend fun setWindows(action: Int): Boolean`
  - Action: `1` = Open, `2` = Close, `0` = Stop. Affects all 4 windows.
- `suspend fun setSunroofShade(action: Int): Boolean`
  - Action: `1` = Open, `2` = Close, `0` = Stop (`0x31400313`).
- `suspend fun setTailgate(open: Boolean): Boolean`
  - Operates power liftgate (`0x3140040d`).
- `suspend fun setDoorLock(locked: Boolean): Boolean`
  - Locks or unlocks central door locks (`0x15400505`).

### Next-Gen EV & Automation Methods
- `suspend fun setBatteryPreconditioning(enabled: Boolean): Boolean`
  - Initiates thermal battery preparation for DC fast-charging (`0x314006c6`).
- `suspend fun executeRainGuardian(): Boolean`
  - Automatically commands closure of all windows and roof sunshade upon rain detection.
- `suspend fun applyScene(sceneName: String)`
  - Preset coordinator for `"RAPID_COOL"`, `"NAP"`, `"DEFROST"`, or `"CAMP"`.

---

## 2. Class: `DeepalHudClient`

Manages IPC communication with Changan InCall AR-HUD and digital cluster services (`com.incall.SVR_MNG_SERVICE` and `com.incall.double.INTERACTIVE_SERVICE`).

### Methods
- `suspend fun sendNavigateStatus(status: Int): Boolean`
  - Status: `1` = Active, `2` = Arrived, `0` = Inactive.
- `suspend fun sendNavigateTurnInfo(turnIcon: Int, turnDistMeters: Int): Boolean`
  - Transmits turn icon ID (1: Straight, 2: Right, 3: Left, 4: Slight Right, 5: Slight Left, 6: U-Turn, 7: Roundabout) and distance to next maneuver.
- `suspend fun sendNavigateRoadInfo(nextRoad: String, curRoad: String): Boolean`
  - Transmits road names to the HUD ribbon.
- `suspend fun sendNavigateRemainInfo(remainDistMeters: Int, remainTimeSec: Int): Boolean`
  - Updates remaining trip distance and ETA.
- `suspend fun requestNaviFocus(packageName: String = "com.deepalnav"): Boolean`
  - Requests focus for navigation audio and HUD graphics (Transact `0x3f`).
- `suspend fun abandonNaviFocus(packageName: String = "com.deepalnav"): Boolean`
  - Releases navigation HUD graphics focus (Transact `0x40`).
- `suspend fun clear(): Boolean`
  - Resets HUD and cluster to clean idle state.

---

## 3. Object: `DeepalS05Property`

Complete hardware property map definitions for the Deepal S05 platform.

```kotlin
// Areas
const val AREA_GLOBAL = 0
const val AREA_DRIVER = 1
const val AREA_PASSENGER = 4
const val AREA_DOORS_ALL = 0x0F
const val AREA_WINDOW_FL = 0x010
const val AREA_WINDOW_FR = 0x040
const val AREA_WINDOW_RL = 0x100
const val AREA_WINDOW_RR = 0x400

// Properties
const val PROP_VEHICLE_SPEED = 0x31600204
const val PROP_GEAR_SELECTION = 0x3140028c
const val PROP_BATTERY_SOC = 0x314006c4
const val PROP_REMAINING_RANGE = 0x31410605
const val PROP_HVAC_TEMP_SET = 0x35600105
const val PROP_SEAT_HEATING = 0x3540010f
const val PROP_SEAT_VENTILATION = 0x35400111
const val PROP_WINDOW_MOVE = 0x33400301
const val PROP_SUNROOF_SHADE = 0x31400313
const val PROP_TAILGATE = 0x3140040d
const val PROP_DOOR_LOCK = 0x15400505
const val PROP_BATTERY_PRECONDITIONING = 0x314006c6
const val PROP_RAIN_SENSOR_STATE = 0x31400277
```
