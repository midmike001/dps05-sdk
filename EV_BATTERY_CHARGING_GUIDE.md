# Deepal S05 EV Battery, Preconditioning & Charging Guide
### High-Voltage Battery Telemetry, Fast-Charge Thermal Preconditioning, and Range Dynamics

---

## 1. System Architecture

The **Changan Deepal S05** features a dedicated battery management system (BMS) and thermal management domain communicating via OpenOS VirtualCar property buses.

### Key Property IDs

| Property Constant | Hex ID | Data Type | Description |
|:---|:---|:---|:---|
| `PROP_BATTERY_SOC` | `0x314006c4` | `Int` (0 to 100) | High-voltage battery state of charge (%) |
| `PROP_REMAINING_RANGE` | `0x31410605` | `Int` (km) | Dynamic estimated remaining driving range |
| `PROP_BATTERY_PRECONDITIONING` | `0x314006c6` | `Int` (1=On, 0=Off) | DC Fast-Charging Thermal Preconditioning |
| `PROP_CHARGING_STATUS` | `0x314006c7` | `Int` | 0=Disconnected, 1=Connected, 2=Fast Charging, 3=AC Charging, 4=Completed |

---

## 2. Battery Telemetry & Reactive Flow

The SDK exposes continuous battery telemetry through `StateFlow<DeepalS05Telemetry>`:

```kotlin
val client = DeepalS05Client()

// Monitor battery status in CoroutineScope
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        val soc = telemetry.batterySocPercent
        val rangeKm = telemetry.remainingRangeKm
        val isPreconditioning = telemetry.isBatteryPreconditioning

        println("Battery SoC: $soc%, Remaining Range: $rangeKm km")
        println("Battery Thermal Preconditioning: $isPreconditioning")
    }
}
```

---

## 3. DC Fast-Charging Battery Preconditioning

### Why Battery Preconditioning is Required
Lithium Iron Phosphate (LFP) and Ternary NMC battery cells require an internal core temperature between **25°C and 35°C** to safely accept maximum DC fast-charging power (up to **120 kW / 150 kW** on Deepal S05). 

When approaching a fast-charging station in cold or ambient conditions:
- Turning on `PROP_BATTERY_PRECONDITIONING` activates the vehicle's heat pump / PTC coolant heater to bring the battery pack to peak charging temperature.
- This reduces 30% to 80% DC charge times from 45 minutes down to **15-20 minutes**.

### Enabling / Disabling Preconditioning
```kotlin
suspend fun controlPreconditioning() {
    // 1. Activate Preconditioning 15 minutes before reaching DC charger
    client.setBatteryPreconditioning(enabled = true)

    // 2. Disable Preconditioning once charging begins
    client.setBatteryPreconditioning(enabled = false)
}
```

---

## 4. Smart Range Estimator & Charging Station Waypoints

When a destination is set, the navigation engine calculates the arrival battery SoC based on remaining route distance and real-time power consumption:

```kotlin
fun checkBatteryFeasibility(routeDistanceKm: Float, currentRangeKm: Int): Boolean {
    // Reserve safety buffer of 30 km
    val requiredWithBuffer = routeDistanceKm + 30
    return currentRangeKm >= requiredWithBuffer
}
```

If remaining range is insufficient, the app recommends intermediate EV charging stations along the route with connector types (CCS2, GB/T, Type 2) and power ratings (e.g. 60 kW, 120 kW, 160 kW).
