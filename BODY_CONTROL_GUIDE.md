# Deepal S05 Body & Access Control Development Guide
### Windows, Sunroof Shade, Power Tailgate, Door Locks & Rain Guardian

---

## 1. System Architecture

The Body and Access domain in the **Changan Deepal S05** (C857 platform) interfaces with the door domain controller (DDC), body control module (BCM), and power tailgate actuator (PTG) over the OpenOS VirtualCar property bus.

### Key Property IDs & Area Masks

| Property Constant | Hex ID | Area Mask | Data Type | Action Values |
|:---|:---|:---|:---|:---|
| `PROP_WINDOW_MOVE` | `0x33400301` | Individual FL/FR/RL/RR or `0x0F` (All) | `Int` | `1` = Open, `2` = Close, `0` = Stop |
| `PROP_SUNROOF_SHADE` | `0x31400313` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close, `0` = Stop |
| `PROP_TAILGATE` | `0x3140040d` | Area 0 (Global) | `Int` | `1` = Open, `2` = Close, `0` = Stop |
| `PROP_DOOR_LOCK` | `0x15400505` | Area `0x0F` (All Doors) | `Int` | `1` = Lock, `2` = Unlock |
| `PROP_RAIN_SENSOR_STATE`| `0x31400277` | Area 0 (Global) | `Int` | `0`=Dry, `1`=Light Rain, `2`=Heavy Rain |

### Window Area Bitmasks

```kotlin
const val AREA_DOORS_ALL = 0x0F  // Controls all 4 windows simultaneously
const val AREA_WINDOW_FL = 0x010 // Front-Left Driver Window
const val AREA_WINDOW_FR = 0x040 // Front-Right Passenger Window
const val AREA_WINDOW_RL = 0x100 // Rear-Left Passenger Window
const val AREA_WINDOW_RR = 0x400 // Rear-Right Passenger Window
```

---

## 2. Power Windows Control

Windows can be controlled either individually or all together:

```kotlin
val client = DeepalS05Client()

suspend fun controlWindows() {
    // 1. Fully Open All 4 Windows (Ventilation)
    client.setWindows(action = 1, areaId = DeepalS05Property.AREA_DOORS_ALL)

    // 2. Fully Close All 4 Windows
    client.setWindows(action = 2, areaId = DeepalS05Property.AREA_DOORS_ALL)

    // 3. Open Only Front-Left Driver Window
    client.setWindows(action = 1, areaId = DeepalS05Property.AREA_WINDOW_FL)

    // 4. Halt Window Movement Mid-Travel
    client.setWindows(action = 0, areaId = DeepalS05Property.AREA_DOORS_ALL)
}
```

---

## 3. Electric Sunroof Roller Blind & Power Tailgate

### Sunroof Shade / Blind
```kotlin
suspend fun controlSunroof() {
    // Open Electric Sunroof Roller Blind
    client.setSunroofShade(action = 1)

    // Close Sunroof Shade
    client.setSunroofShade(action = 2)

    // Stop Sunroof Shade at Current Position
    client.setSunroofShade(action = 0)
}
```

### Power Tailgate (Electric Trunk)
```kotlin
suspend fun operateTailgate() {
    // Open Power Tailgate
    client.setTailgate(action = 1)

    // Close Power Tailgate
    client.setTailgate(action = 2)
}
```

---

## 4. Central Door Locks

```kotlin
suspend fun operateDoorLocks() {
    // Lock All Doors
    client.setDoorLock(locked = true)

    // Unlock All Doors
    client.setDoorLock(locked = false)
}
```

---

## 5. Rain-Sensing Auto Guardian System

When the vehicle's optical windshield rain sensor detects raindrops while parked or driving:
1. `PROP_RAIN_SENSOR_STATE` reports `1` (Light Rain) or `2` (Heavy Rain).
2. The Rain Guardian program automatically issues commands to close all 4 power windows and close the electric sunroof blind:

```kotlin
suspend fun executeRainGuardian() {
    // 1. Close all 4 power windows
    client.setWindows(action = 2, areaId = DeepalS05Property.AREA_DOORS_ALL)

    // 2. Close sunroof roller blind
    client.setSunroofShade(action = 2)
}
```
