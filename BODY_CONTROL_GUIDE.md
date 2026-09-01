# Deepal S05 Body, Doors & Access Control Development Guide
### 4-Door Sensing, Tailgate, Windows, Sunroof Shade, Central Locks & Rain Guardian

---

## 1. System Architecture

The Body and Access domain in the **Changan Deepal S05** (Platform Model C857) interfaces with the Door Domain Controller (DDC), Body Control Module (BCM), and Power Tailgate Actuator (PTG) over the OpenOS VirtualCar property bus.

### Key Property IDs & Area Masks (Ground Truth from `d+` Disassembly)

| Property Constant | Hex ID | Area Mask | Data Type | Action / State Values |
|:---|:---|:---|:---|:---|
| `PROP_DOORS` | `0x36400311` | FL (`0x01`), FR (`0x04`), RL (`0x10`), RR (`0x40`) | `Int` | `1` = Open, `0` / `2` = Closed (`vc_alias_door_pos_*`) |
| `PROP_TAILGATE` | `0x31400314` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close (`vc_alias_door_trunk_pos`) |
| `PROP_WINDOW_MOVE` | `0x33400301` | FL (`0x10`), FR (`0x40`), RL (`0x100`), RR (`0x400`) | `Int` | `1` = Open, `2` = Close, `0` = Stop |
| `PROP_WINDOW_LOCK` | `0x31400303` | Area 0 (Global) | `Int` | `1` = Locked, `0` = Unlocked |
| `PROP_SUNROOF_SHADE` | `0x31400313` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close, `3` = Vent |
| `PROP_DOOR_LOCK` | `0x314003eb` | Area 0 (Global) | `Int` | `1` = Lock, `2` = Unlock |
| `PROP_RAIN_SENSOR_STATE`| `0x31400277` | Area 0 (Global) | `Int` | `1` = Dry / No Rain, `2` = Light Rain, `3` = Heavy Rain |

---

## 2. Individual Door Open/Closed Sensing

The Deepal S05 transmits door open states on property `0x36400311` using dedicated area IDs:

```kotlin
// Area Constants
const val AREA_DOOR_FL = 0x01 // Front-Left (Driver) Door
const val AREA_DOOR_FR = 0x04 // Front-Right (Passenger) Door
const val AREA_DOOR_RL = 0x10 // Rear-Left Passenger Door
const val AREA_DOOR_RR = 0x40 // Rear-Right Passenger Door
```

### Reading Door States
```kotlin
val client = DeepalS05Client()

// Monitored automatically in client.telemetry
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        println("Driver Door Open: ${telemetry.doorFlOpen}")
        println("Passenger Door Open: ${telemetry.doorFrOpen}")
        println("Rear Left Door Open: ${telemetry.doorRlOpen}")
        println("Rear Right Door Open: ${telemetry.doorRrOpen}")
        println("Trunk / Tailgate Open: ${telemetry.isTailgateOpen}")
    }
}
```

---

## 3. Power Windows Control

Windows are actuated by dispatching to the four window area masks:

```kotlin
suspend fun controlWindows() {
    // 1. Fully Open All 4 Windows (Ventilation)
    client.setWindows(action = 1)

    // 2. Fully Close All 4 Windows
    client.setWindows(action = 2)

    // 3. Halt Window Movement Mid-Travel
    client.setWindows(action = 0)
}
```

---

## 4. Electric Sunroof Sunshade & Power Tailgate

### Sunroof Sunshade (`PROP_SUNROOF_SHADE = 0x31400313`)
```kotlin
suspend fun controlSunroof() {
    // 1. Open Sunshade
    client.setSunroofShade(action = 1)

    // 2. Close Sunshade
    client.setSunroofShade(action = 2)
}
```

### Power Tailgate (`PROP_TAILGATE = 0x31400314`)
```kotlin
suspend fun controlTailgate() {
    // Open Power Tailgate Lift
    client.setTailgate(open = true)

    // Close Power Tailgate Lift
    client.setTailgate(open = false)
}
```

---

## 5. Central Door Locks (`PROP_DOOR_LOCK = 0x314003eb`)

```kotlin
suspend fun controlLocks() {
    // Lock vehicle
    client.setDoorLock(locked = true)

    // Unlock vehicle
    client.setDoorLock(locked = false)
}
```

---

## 6. Rain-Sensing Auto Guardian

Monitors `PROP_RAIN_SENSOR_STATE = 0x31400277` (where `2` = Light Rain, `3` = Heavy Rain) and automatically executes emergency closure of all windows and the sunroof shade:

```kotlin
suspend fun checkRainAndProtect(rainLevel: Int) {
    if (rainLevel >= 2) {
        client.executeRainGuardian()
    }
}
```
