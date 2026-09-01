# `deepal-s05-sdk` API Reference Manual

Detailed API documentation for the **Changan Deepal S05 Kotlin SDK**.
Reverse engineered 100% against Changan OpenOS system framework and verified with decompiled OEM launcher `com.deepalhome.launcher` (`d+`).

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
- `isSimulatedMode: Boolean`: Toggle to allow off-vehicle UI simulation and testing.

### Lifecycle Methods
- `startMonitoring()`: Initiates background polling of vehicle signals. Speed is polled at 250ms; slow cabin & energy metrics are polled at 1000ms.
- `stopMonitoring()`: Stops polling and releases coroutine jobs.
- `updateTelemetry(update: (DeepalS05Telemetry) -> DeepalS05Telemetry)`: Updates telemetry state directly (useful in simulations).

### Climate & HVAC Methods
- `suspend fun setClimatePower(enabled: Boolean): Boolean`
  - Turns climate power On/Off (`PROP_HVAC_POWER_ON = 0x35400101`, Area 1 & Area 0).
- `suspend fun setClimateTemperature(tempC: Float, area: Int = AREA_DRIVER): Boolean`
  - Adjusts target cabin temperature (17.5°C to 32.5°C in 0.5°C steps via `PROP_HVAC_TEMP_SET = 0x35600105`).
- `suspend fun setFanSpeed(speed: Int): Boolean`
  - Sets fan blower speed from 1 to 8 (`PROP_HVAC_FAN_SPEED = 0x35400109`, Area 1).
- `suspend fun setAcEnabled(enabled: Boolean): Boolean`
  - Toggles A/C compressor (`PROP_HVAC_AC_ON = 0x35400102`, Area 1).
- `suspend fun setAutoClimate(enabled: Boolean): Boolean`
  - Toggles automatic temperature control (`PROP_HVAC_AUTO = 0x35400104`, Area 1 & Area 0).
- `suspend fun setRecirculation(recircOn: Boolean): Boolean`
  - Sets air circulation (`PROP_HVAC_RECIRC = 0x35400108`, Vendor Tri-State: 2=Recirc, 1=Fresh).
- `suspend fun setMaxAc(enabled: Boolean): Boolean`
  - Commands maximum cooling (`PROP_HVAC_MAX_AC = 0x3540010b`, Area 1).
- `suspend fun setSyncMode(enabled: Boolean): Boolean`
  - Synchronizes driver and passenger temperature zones (`PROP_HVAC_SYNC = 0x3540010d`, Area 1).
- `suspend fun setFrontDefrost(enabled: Boolean): Boolean`
  - Toggles front windshield defroster (`PROP_HVAC_DEFROST_FRONT = 0x33400103`, Area 1).
- `suspend fun setRearDefrost(enabled: Boolean): Boolean`
  - Toggles rear window & mirror defroster (`PROP_HVAC_DEFROST_REAR = 0x3540010c`, Area 1 / Area 2).

### Seats & Comfort Methods
- `suspend fun setSeatHeating(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High).
  - Areas: `AREA_DRIVER = 1`, `AREA_PASSENGER = 4`.
  - Properties: `PROP_SEAT_HEATING = 0x3540010f`, `PROP_SEAT_HEATING_CPM = 0x1540050b`.
- `suspend fun setSeatVentilation(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High).
  - Areas: `AREA_DRIVER = 1`, `AREA_PASSENGER = 4`.
  - Property: `PROP_SEAT_VENTILATION = 0x35400111`.
- `suspend fun setSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean`
  - Controls driver pneumatic massage (`PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f`, Mode: `0x31400b31` 1..3, Level: `0x31400b30` 1..8).
- `suspend fun setSteeringWheelHeat(enabled: Boolean): Boolean`
  - Controls steering wheel heating element (`PROP_STEERING_WHEEL_HEAT = 0x314003eb`).

### Access & Body Methods
- `suspend fun setWindows(action: Int): Boolean`
  - Action: `1` = Open, `2` = Close, `0` = Stop (`PROP_WINDOW_MOVE = 0x33400301`). Writes to areas `0x10`, `0x40`, `0x100`, `0x400`.
- `suspend fun setSunroofShade(action: Int): Boolean`
  - Action: `1` = Open, `2` = Close, `3` = Vent (`PROP_SUNROOF_SHADE = 0x31400313`).
- `suspend fun setTailgate(open: Boolean): Boolean`
  - Operates power liftgate (`PROP_TAILGATE = 0x31400314`: 1=Open, 2=Close).
- `suspend fun setDoorLock(locked: Boolean): Boolean`
  - Locks or unlocks central door locks (`PROP_DOOR_LOCK = 0x314003eb`: 1=Locked, 2=Unlocked).

### Next-Gen EV & Automation Methods
- `suspend fun setBatteryPreconditioning(enabled: Boolean): Boolean`
  - Initiates thermal battery preparation for DC fast-charging (`PROP_BATTERY_PRECONDITIONING = 0x314006c6`).
- `suspend fun executeRainGuardian(): Boolean`
  - Automatically commands closure of all windows and roof sunshade upon rain detection.
- `suspend fun applyScene(sceneName: String)`
  - Preset coordinator for `"RAPID_COOL"`, `"NAP"`, `"DEFROST"`, or `"CAMP"`.

---

## 2. Class: `DeepalHudClient`

Manages IPC communication with Changan InCall AR-HUD, cluster, and multi-screen services (`com.incall.SVR_MNG_SERVICE` and `com.incall.double.INTERACTIVE_SERVICE`).

### Methods
- `suspend fun sendNavigateStatus(status: Int): Boolean`
  - Status: `1` = Active guidance, `2` = Arrived, `0` = Idle / cleared. Code `0x16`.
- `suspend fun sendNavigateTurnInfo(turnIcon: Int, turnDistMeters: Int): Boolean`
  - Transmits turn icon ID and countdown distance in meters to AR-HUD. Code `0x18`.
- `suspend fun sendNavigateRoadInfo(nextRoad: String, curRoad: String): Boolean`
  - Transmits road names to the HUD ribbon. Code `0x1a`.
- `suspend fun sendNavigateRemainInfo(remainDistMeters: Int, remainTimeSec: Int): Boolean`
  - Updates remaining trip distance (meters) and ETA (seconds). Code `0x1b`.
- `suspend fun sendNavigateCameraInfo(cameraInfo: String): Boolean`
  - Transmits speed camera and radar alerts. Code `0x1c`.
- `suspend fun sendNavigateLaneInfo(laneInfo: String): Boolean`
  - Transmits multi-lane diagram recommendations. Code `0x19`.
- `suspend fun sendNavigateCrossRoad(crossRoadState: Int): Boolean`
  - Transmits cross road junction view status. Code `0x17`.
- `suspend fun requestNaviFocus(packageName: String = "com.deepalnav"): Boolean`
  - Requests focus for navigation audio and HUD graphics with `INaviFocusCallback` token attached (Transact `0x3f`).
- `suspend fun abandonNaviFocus(packageName: String = "com.deepalnav"): Boolean`
  - Releases navigation HUD graphics focus with `INaviFocusCallback` token attached (Transact `0x40`).
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
const val AREA_SOC = 0x1b              // 27: Battery State of Charge Area

// Doors & Windows Areas
const val AREA_DOOR_FL = 0x01          // Front-Left (Driver) Door
const val AREA_DOOR_FR = 0x04          // Front-Right (Passenger) Door
const val AREA_DOOR_RL = 0x10          // Rear-Left Door
const val AREA_DOOR_RR = 0x40          // Rear-Right Door
const val AREA_WINDOW_FL = 0x010
const val AREA_WINDOW_FR = 0x040
const val AREA_WINDOW_RL = 0x100
const val AREA_WINDOW_RR = 0x400

// Tire Areas
const val AREA_TIRE_FL = 0x01
const val AREA_TIRE_FR = 0x02
const val AREA_TIRE_RL = 0x04
const val AREA_TIRE_RR = 0x08

// Vehicle Properties (Ground Truth)
const val PROP_BATTERY_SOC = 0x3140028c        // Int (Area: 0x1b)
const val PROP_REMAINING_RANGE_EV_DTE = 0x31400501 // Int: EV DTE (km)
const val PROP_REMAINING_RANGE_DISP_DTE = 0x31600205 // Int: Display DTE (km)
const val PROP_ODOMETER = 0x31600204           // Raw meters (scale: / 1000f)
const val PROP_TIRE_PRESSURE = 0x37600211      // Float (Bar, Areas: 1, 2, 4, 8)
const val PROP_GEAR_SELECTION = 0x31400231     // Int: 1=P, 2=R, 3=N, 4=D
const val PROP_VEHICLE_SPEED_VHAL = 0x11600207 // Float
const val PROP_VEHICLE_SPEED_VC = 0x31600202   // Float
const val PROP_EXTERIOR_TEMP = 0x35600403      // Float °C
const val PROP_HVAC_INTERNAL_TEMP = 0x38600112 // Float °C
const val PROP_DOORS = 0x36400311             // Int: 1=Open, 0=Closed (Areas: 1, 4, 16, 64)
const val PROP_TAILGATE = 0x31400314           // Int: 1=Open, 2=Close (Area: 0)
const val PROP_DRIVE_MODE = 0x3140040d         // Int: 1=ECO, 2=COMFORT, 3=SPORT, 4=CUSTOM
const val PROP_HVAC_TEMP_SET = 0x35600105      // Float: 17.5 - 32.5 °C
const val PROP_HVAC_POWER_ON = 0x35400101      // Int: 1=On, 2=Off
const val PROP_HVAC_AC_ON = 0x35400102         // Int: 1=On, 2=Off
const val PROP_HVAC_AUTO = 0x35400104          // Int: 1=Auto, 2=Manual
const val PROP_HVAC_RECIRC = 0x35400108        // Int: 2=Recirc, 1=Fresh
const val PROP_HVAC_FAN_SPEED = 0x35400109     // Int: 1 - 8
const val PROP_HVAC_DEFROST_FRONT = 0x33400103 // Int: 1=On, 2=Off (Area 1: Front, Area 2: Rear)
const val PROP_HVAC_DEFROST_REAR = 0x3540010c  // Int: 1=On, 2=Off
const val PROP_SEAT_HEATING = 0x3540010f       // Int: 0..3
const val PROP_SEAT_HEATING_CPM = 0x1540050b   // Int: 0..3
const val PROP_SEAT_VENTILATION = 0x35400111   // Int: 0..3
const val PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f// Int: 1=On, 2=Off
const val PROP_SEAT_MASSAGE_MODE = 0x31400b31  // Int: 1..3
const val PROP_SEAT_MASSAGE_LEVEL = 0x31400b30 // Int: 1..8
const val PROP_STEERING_WHEEL_HEAT = 0x314003eb// Int: 1=On, 2=Off
const val PROP_WINDOW_MOVE = 0x33400301        // Int: 1=Open, 2=Close, 0=Stop
const val PROP_WINDOW_LOCK = 0x31400303        // Int: 1=Lock, 0=Unlock
const val PROP_SUNROOF_SHADE = 0x31400313      // Int: 1=Open, 2=Close, 3=Vent
const val PROP_DOOR_LOCK = 0x314003eb          // Int: 1=Lock, 2=Unlock
const val PROP_BATTERY_PRECONDITIONING = 0x314006c6 // Int: 1=On, 2=Off
const val PROP_RAIN_SENSOR_STATE = 0x31400277  // Int: 1..3
const val AUDIO_EVENT_OUTSIDE_MUSIC = 0x66     // Int: 102
```
