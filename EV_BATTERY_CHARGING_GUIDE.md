# Deepal S05 EV Battery, Preconditioning & Charging Guide
### High-Voltage Battery Telemetry, Fast-Charge Thermal Preconditioning, and Range Dynamics

---

## 1. System Architecture

The **Changan Deepal S05** features a dedicated battery management system (BMS) and thermal management domain communicating via OpenOS VirtualCar property buses.

### Key Property IDs (Ground Truth)

| Property Constant | Hex ID | Area Mask | Data Type | Description |
|:---|:---|:---|:---|:---|
| `PROP_BATTERY_SOC` | `0x3140028c` | Area `0x1b` (27) | `Int` (0 to 100) | High-voltage battery state of charge (%) |
| `PROP_REMAINING_RANGE` | `0x314006c4` | Area 0 (Global) | `Int` (km) | Dynamic estimated remaining driving range |
| `PROP_ODOMETER` | `0x31600204` | Area 0 (Global) | `Float` (raw in meters) | Vehicle odometer (scaled by dividing by `1000f` for km) |
| `PROP_TIRE_PRESSURE` | `0x31410605` | Area 0 (Global) | `Int` / `Float` | Tyre pressure telemetry (Scale: 4, Divisor: 3) |
| `PROP_BATTERY_PRECONDITIONING` | `0x314006c6` | Area 0 (Global) | `Int` (1=On, 2=Off) | DC Fast-Charging Thermal Preconditioning |

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
- Turning on `PROP_BATTERY_PRECONDITIONING = 0x314006c6` activates the vehicle's heat pump / PTC coolant heater to bring the battery pack to peak charging temperature.
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
