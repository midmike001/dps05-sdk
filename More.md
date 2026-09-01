# Changan OpenOS VirtualCar Subsystem Reference

`com.openos.virtualcar` is the **core Vehicle Hardware Abstraction Layer (VHAL) and system service** built into Changan Deepal vehicles (including the Deepal S05, S07, and L07 running Changan OpenOS).

It acts as the **bridge between Android applications and the physical vehicle microcontrollers (ECUs)** across the car's CAN, LIN, and Automotive Ethernet buses.

---

## 1. System Architecture & Topology

```
┌────────────────────────────────────────────────────────┐
│             DeepalNav Application / SDK                │
└───────────────────────────┬────────────────────────────┘
                            │ (Android Binder IPC)
                            ▼
┌────────────────────────────────────────────────────────┐
│        com.openos.virtualcar.IVirtualCar               │
│     Registered in ServiceManager as "virtualcar_service"│
└───────────────────────────┬────────────────────────────┘
                            │ (Transact Code 2)
                            ▼
┌────────────────────────────────────────────────────────┐
│     com.openos.virtualcar.IVirturalCarProperty         │
│     (OEM Property Broker & Hardware Dispatcher)        │
└───────────────────────────┬────────────────────────────┘
                            │ (CAN / Ethernet Bus Gateway)
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
   ┌───────────┐      ┌───────────┐      ┌───────────┐
   │ HVAC & AC │      │  Body &   │      │ Battery & │
   │  Domain   │      │  Windows  │      │ Powertrain│
   └───────────┘      └───────────┘      └───────────┘
```

---

## 2. Key AIDL Interfaces

### 1. `IVirtualCar` (Service Root)
Registered in the Android system server under the service name `"virtualcar_service"`.
* **Resolution**: Acquired via `ServiceManager.getService("virtualcar_service")`.
* **Function**: Exposes `getCarService(String)` (Transact Code `2`), which returns the `IVirturalCarProperty` binder when called with `"virtualcar_property_service"`.

### 2. `IVirturalCarProperty` (Hardware Property Broker)
*(Note: Uses the OEM spelling with 'r')*
* **Reading Signals (Transact Code 3)**:
  * `getProperty(propId, areaId)`: Reads typed vehicle telemetry (e.g. Battery SoC %, Gear, Speed, Cabin Temp).
* **Writing Commands (Transact Code 2)**:
  * `setProperty(flag, group, propId, areaId, reserved, timestamp, className, value)`: Actuates vehicle hardware with polymorphic value serialization. Status code `0 == OK / success`.

---

## 3. What Domains Does `com.openos.virtualcar` Control?

| Domain | What It Controls | Verified Property IDs |
|:---|:---|:---|
| ⚡ **Powertrain & Battery** | Live Speed, Gear (`P/R/N/D`), Battery SoC %, Range, Odometer, Battery Preconditioning | `0x11600207`, `0x31400231`, `0x3140028c`, `0x314006c4`, `0x31600204`, `0x314006c6` |
| ❄️ **Climate & HVAC** | Dual-Zone Temperature (17.5°C-32.5°C), 8 Fan Speeds, AC, Front/Rear Defrost, Recirculation, Auto | `0x35600105`, `0x35400109`, `0x35400102`, `0x33400103`, `0x3540010c`, `0x35400108`, `0x35400104` |
| 💺 **Seat Comfort** | 3-Level Heating, 3-Level Ventilation, Pneumatic Massage (Modes 1-3, Levels 1-8), Steering Heat | `0x3540010f`, `0x35400111`, `0x31400b2f`, `0x31400b31`, `0x31400b30`, `0x314003eb` |
| 🚪 **Body & Access** | 4 Power Windows, Sunroof Roller Blind, Power Tailgate, Central Locks | `0x33400301`, `0x31400313`, `0x3140040d`, `0x314003eb` |
| 💡 **Cabin Lighting** | Ambient Light Color Presets & Brightness, Air Purifier | `0x3140039a`, `0x3140039b`, `0x35400122` |
