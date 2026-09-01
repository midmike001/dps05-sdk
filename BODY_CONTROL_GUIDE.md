# Deepal S05 Body & Access Control Development Guide
### Windows, Sunroof Shade, Power Tailgate, Door Locks & Rain Guardian

---

## 1. System Architecture

The Body and Access domain in the **Changan Deepal S05** (C857 platform) interfaces with the door domain controller (DDC), body control module (BCM), and power tailgate actuator (PTG) over the OpenOS VirtualCar property bus.

### Key Property IDs & Area Masks (Ground Truth)

| Property Constant | Hex ID | Area Mask | Data Type | Action Values |
|:---|:---|:---|:---|:---|
| `PROP_WINDOW_MOVE` | `0x33400301` | Individual FL (`0x10`), FR (`0x40`), RL (`0x100`), RR (`0x400`) | `Int` | `1` = Open, `2` = Close, `0` = Stop |
| `PROP_WINDOW_LOCK` | `0x31400303` | Area 0 (Global) | `Int` | `1` = Locked, `0` = Unlocked |
| `PROP_SUNROOF_SHADE` | `0x31400313` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close, `3` = Vent |
| `PROP_TAILGATE` | `0x3140040d` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close |
| `PROP_DOOR_LOCK` | `0x314003eb` | Area 0 (Global) | `Int` | `1` = Lock, `2` = Unlock |
| `PROP_RAIN_SENSOR_STATE`| `0x31400277` | Area 0 (Global) | `Int` | `1`=Dry / No Rain, `2`=Light Rain, `3`=Heavy Rain |

### Window Area Identifiers

```kotlin
const val AREA_WINDOW_FL = 0x010 // Front-Left Driver Window
const val AREA_WINDOW_FR = 0x040 // Front-Right Passenger Window
const val AREA_WINDOW_RL = 0x100 // Rear-Left Passenger Window
const val AREA_WINDOW_RR = 0x400 // Rear-Right Passenger Window
```

---

## 2. Power Windows Control

Windows are actuated by dispatching to the four window area masks:

```kotlin
val client = DeepalS05Client()

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

## 3. Electric Sunroof Roller Blind & Power Tailgate

### Sunroof Shade / Blind (`PROP_SUNROOF_SHADE = 0x31400313`)
```kotlin
suspend fun controlSunroof() {
    // 1. Open Sunshade
    client.setSunroofShade(action = 1)

    // 2. Close Sunshade
    client.setSunroofShade(action = 2)
}
```

### Power Tailgate (`PROP_TAILGATE = 0x3140040d`)
```kotlin
suspend fun controlTailgate() {
    // Open Tailgate Lift
    client.setTailgate(open = true)

    // Close Tailgate Lift
    client.setTailgate(open = false)
}
```

---

## 4. Central Door Locks (`PROP_DOOR_LOCK = 0x314003eb`)

```kotlin
suspend fun controlLocks() {
    // Lock vehicle
    client.setDoorLock(locked = true)

    // Unlock vehicle
    client.setDoorLock(locked = false)
}
```

---

## 5. Rain-Sensing Auto Guardian

Monitors `PROP_RAIN_SENSOR_STATE = 0x31400277` (where `2` = Light Rain, `3` = Heavy Rain) and automatically executes emergency closure of all windows and the sunroof shade:

```kotlin
suspend fun checkRainAndProtect(rainLevel: Int) {
    if (rainLevel >= 2) {
        client.executeRainGuardian()
    }
}
```
