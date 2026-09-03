# Deepal S05 Vehicle Management SDK (`deepal-s05-sdk`)
### Native Kotlin Automotive Library for Changan Deepal S05 (Model C857 / EPA OpenOS Platform)

`deepal-s05-sdk` is an automotive-grade, standalone Kotlin library designed to interface directly with the **Changan Deepal S05** head unit, cockpit domain controller, and vehicle body controllers. 

Reverse engineered 100% from the Changan OpenOS system framework(`DEEPAL_S05_C857` and `DEEPAL_S05_CABIN_WRITES`), and low-level Binder IPC services.

---

## 1. Features

- **Automatic Device Detection & Fingerprint Matching (`com.deepal.sdk.device`)**:
  - Automatically identifies vehicle hardware by inspecting `android.os.Build` properties (`Build.MODEL == "C857"` -> `deepal-s05`).
  - Resolves hardware CAN and signal configurations (`VehicleConfigurations.DEEPAL_S05_C857` and `BuiltInProfiles.DEEPAL_S05_C857`).
- **Unified WriteIntent Planning & Execution Pipeline (`com.deepal.sdk.vehicle`)**:
  - Complete execution engine for `WriteIntent` (`TempSet`, `TempStep`, `LevelStep`, `Position`, `Command`, `Choice`).
  - Pre-execution validation (`WritePlan.Proceed`, `WritePlan.AlreadyThere`, `WritePlan.Refused`) enforcing physical vehicle rules (e.g. Parked-only commands).
- **Direct OpenOS AIDL IPC**:
  - `com.openos.virtualcar.IVirtualCar` (`virtualcar_service`, Transact 2 `getVirtualCarService`)
  - `com.openos.virtualcar.IVirturalCarProperty` (OEM spelling with 'r', Transact 2 `setValue`, Transact 3 `getValue`, Transact 5 `register`)
  - `com.openos.virtualcar.entity.VirtualCarValue` (polymorphic parcel serialization)
- **Reactive Vehicle Telemetry (`StateFlow<DeepalS05Telemetry>`)**:
  - High-frequency live speed (`0x31600202` / `0x11600207`, km/h) & current gear (`0x31400231` / `0x11400400`: `P`, `R`, `N`, `D`).
  - Battery SoC % (`0x3140028c`, Area `0x1b` / 27), remaining range (`PROP_REMAINING_RANGE_C857 = 0x314006c4`, EV DTE `0x31400501`, Display DTE `0x31600205`, km), total odometer (`0x31600204`, raw meters / `1000f` = km).
  - Real-time Tyre Pressure Monitoring (TPMS) in Bar (`0x37600211`, Areas: FL `0x01`, FR `0x02`, RL `0x04`, RR `0x08`).
  - Individual 4-Door open/closed sensing (`0x36400311`, Areas: FL `0x01`, FR `0x04`, RL `0x10`, RR `0x40`).
  - Power window states, central door locks (`0x314003eb`), power tailgate actuation (`0x31400313`) & position (`0x31400314`), and electric sunroof shade (`wt.vehiclesetting` Transact `0x40` & `0x31400303`).
  - REEV & Journey energy consumption metrics (kWh/100km `0x314005a6`, L/100km `0x314005ce`, electric/fuel distances and drive durations).
  - Cabin internal thermometer (`0x38600112`) and exterior temperature (`0x35600403`).
- **Complete Vehicle Actuation**:
  - **Dual-Zone Climate Control**: 17.5°C to 32.5°C (`0x35600105`) in 0.5°C steps, 8 fan blower speeds (`0x35400109`), AC toggle (`0x35400102`), front defrost (`0x33400103`, Area 1), rear defrost (`0x3540010c` / `0x33400103`, Area 2), auto climate (`0x35400104`), max AC (`0x3540010b`), air recirculation (`0x35400108`: 2=Recirc, 1=Fresh), sync mode (`0x3540010d`).
  - **Seat Comfort**: 3-level seat ventilation (`0x35400111`) & heating (`0x3540010f` & `0x1540050b`) for driver (Area 1) and passenger (Area 4), plus pneumatic seat massage toggle (`0x31400b2f`), pattern modes 1-8 (`PROP_SEAT_MASSAGE_MODE = 0x31400b30`), and intensity levels 1-3 (`PROP_SEAT_MASSAGE_LEVEL = 0x31400b31`), steering wheel heating (`0x314003eb`).
  - **Body & Access**: Power window control (FL `0x10`, FR `0x40`, RL `0x100`, RR `0x400` via `0x33400301`), window lock (`0x31400303`), electric sunroof shade (0..100% via `wt.vehiclesetting` & `0x31400303`), power tailgate (`0x31400313` / `0x31400314`), central locks (`0x314003eb`).
  - **Cabin Environment**: Ambient lighting colors (`0x3140039a`), color choices (`0x3140039b` codes: 54, 42, 33, 12, 6, 1), dynamic light patterns 1-3 (`0x31400677`), PM2.5 air purifier (`0x35400122`).
  - **Driving Dynamics & ADAS**: Drive mode (`0x3140040d` / `0x314003f5`: Comfort, Sport, Eco), Auto Emergency Braking (`0x3140040d` / `0x31400244`).
  - **Outside Speaker Audio & Speech TTS**: Outside music event (`0x66`), In-Cabin TTS (`0x1b`), Outside Speaker TTS broadcast (`0x62`) via `VrLogicService`.
  - **Smart Scenes**: Rapid Cool, Nap/Rest, Quick Defrost, Camp Mode.
- **Next-Gen EV Features**:
  - **Battery Fast-Charging Thermal Preconditioning** (`PROP_BATTERY_PRECONDITIONING = 0x314006c6`).
  - **Rain-Sensing Auto Guardian** (`PROP_RAIN_SENSOR_STATE = 0x31400277`).
- **OEM InCall AR-HUD & Windshield Presentation**:
  - Full hardware AR-HUD secondary display support (800x480 resolution, optical window crop `x=573, y=167, w=227, h=188`).
  - Transmits turn maneuver icons (`0x18`), countdown distances (`0x18`), road names (`0x1a`), ETA/remain distance (`0x1b`), cross road junction views (`0x17`), lane guidance (`0x19`), camera warnings (`0x1c`), and navigation status (`0x16`) via `com.incall.double.INTERACTIVE_SERVICE`.
  - Navigation focus management (`0x3f` request focus, `0x40` abandon focus) with `INaviFocusCallback` token attachment.

---

## 2. In-Depth Developer Documentation

The SDK includes dedicated, in-depth architectural and development manuals:

| Guide Document | Focus Area | Contents |
|:---|:---|:---|
| 🖥️ **[HUD Development Guide](HUD_DEVELOPMENT_GUIDE.md)** | Windshield Presentation & AR-HUD | 800x480 secondary display, optical window crop (`573, 167`), centered turn arrows, InCall IPC codes (`0x16`, `0x17`, `0x18`, `0x19`, `0x1a`, `0x1b`, `0x1c`, `0x3f`, `0x40`). |
| ❄️ **[Climate & Seats Guide](CLIMATE_AND_SEATS_GUIDE.md)** | HVAC & Comfort | Dual-zone climate, 17.5°C-32.5°C in 0.5°C steps, cabin internal thermometer, 8 fan speeds, 3-level seat ventilation & heating, pneumatic massage pattern modes (1-8) and intensity levels (1-3), memory presets. |
| 🚪 **[Body & Access Control Guide](BODY_CONTROL_GUIDE.md)** | Body Domains, Doors & Windows | 4-door position sensing (`0x36400311`), power windows (FL, FR, RL, RR), window lock, electric sunroof roller blind (`wt.vehiclesetting`), power tailgate (`0x31400313` / `0x31400314`), door locks, Rain-Sensing Auto Guardian. |
| ⚡ **[EV Battery & Charging Guide](EV_BATTERY_CHARGING_GUIDE.md)** | BMS & Power Dynamics | Battery SoC % (Area `0x1b`), C857 Range (`0x314006c4`), EV & Display DTE range estimation, TPMS Float pressures in Bar, REEV trip energy consumption, DC fast-charging thermal preconditioning (`0x314006c6`). |
| 🎙️ **[Voice & Cockpit Scenes Guide](VOICE_AND_SCENES_GUIDE.md)** | Voice AI, Outside Audio & Scenes | "Hello Deepal" wake word, spoken phrase dispatcher, outside speaker music (`0x66`) and speech TTS broadcast (`0x62`), Rapid Cool, Nap, Defrost, and Camp mode automations. |
| 🔌 **[AIDL Property Bus Guide](AIDL_PROPERTY_BUS_GUIDE.md)** | Low-Level Hardware Interface | Direct Binder IPC, `IVirtualCar`, `IVirturalCarProperty`, `VirtualCarValue` parcelable, area masks, reflection, and reverse engineering mappings. |
| 📚 **[Subsystem Reference](More.md)** | Vehicle Topology & Matrix | Complete bus matrix, domain controllers, and system service contracts (`virtualcar_service`, `wt.vehiclesetting`, `com.incall.double.INTERACTIVE_SERVICE`, `VrLogicService`). |

---

## 3. Installation & Gradle Setup

Add the library module to your Android Studio project:

### `settings.gradle.kts`:
```kotlin
include(":deepal-s05-sdk")
```

### `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":deepal-s05-sdk"))
}
```

---

## 4. Quick Start Guide

### Initializing the Client
```kotlin
val client = DeepalS05Client()

// Start monitoring live telemetry
client.startMonitoring()

// Observe reactive telemetry in Coroutines / Compose / Lifecycle
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        Log.i("Vehicle", "Speed: ${telemetry.speedKmh} km/h, Gear: ${telemetry.gear}, SoC: ${telemetry.batterySocPercent}%")
    }
}
```

### Actuating Cabin Controls
```kotlin
lifecycleScope.launch {
    // Set cabin temperature to 22.5°C
    client.setClimateTemperature(22.5f, DeepalS05Property.AREA_DRIVER)

    // Set fan speed to level 4
    client.setFanSpeed(4)

    // Activate driver pneumatic massage (pattern mode 2, intensity level 3)
    client.setSeatMassage(enabled = true, mode = 2, level = 3)

    // Set ambient dynamic lighting pattern
    client.setAmbientLightPattern(pattern = 2)
}
```

### Using WriteIntent Pipeline
```kotlin
lifecycleScope.launch {
    val tempWrite = BuiltInProfiles.DEEPAL_S05_CABIN_WRITES.driverTemp!!
    val intent = WriteIntent.TempSet(CabinGearRead(), tempWrite, targetC = 23.0f)
    
    val result = client.executeWriteIntent(intent)
    when (result) {
        is WriteResult.Confirmed -> Log.i("Vehicle", "Temperature set successfully: ${result.settledRaw}")
        is WriteResult.Refused -> Log.w("Vehicle", "Write refused: ${result.reason}")
        is WriteResult.Failed -> Log.e("Vehicle", "Write failed: ${result.reason}")
    }
}
```
