# Deepal S05 OpenOS AIDL Property Bus Architecture Guide
### Direct Binder IPC, ServiceManager Reflection, and Low-Level Hardware Interface

---

## 1. OpenOS VirtualCar Subsystem Architecture

On the **Changan Deepal S05**, hardware signals and actuators are mediated by the `com.openos.virtualcar` service running in the Android system server.

### IPC Topology
```
[ DeepalNav Application / SDK ]
               │
               ▼ (ServiceManager.checkService("virtualcar"))
[ com.openos.virtualcar.IVirtualCar ] (IBinder)
               │
               ▼ (transact code 2 -> getCarPropertyService())
[ com.openos.virtualcar.IVirturalCarProperty ] (OEM Spelling)
               │
               ├──> [ Vehicle Speed & Gear (PROP_VEHICLE_SPEED / PROP_GEAR) ]
               ├──> [ HVAC Climate Domain (PROP_HVAC_TEMP_SET / PROP_FAN) ]
               ├──> [ Body Domain (PROP_WINDOW_MOVE / PROP_SUNROOF) ]
               └──> [ Battery BMS Domain (PROP_BATTERY_SOC / PROP_PRECOND) ]
```

---

## 2. Low-Level AIDL Interface Definitions

### `IVirtualCar.aidl`
```java
package com.openos.virtualcar;

interface IVirtualCar {
    IBinder getCarPropertyService(); // Transact code 2
}
```

### `IVirturalCarProperty.aidl`
```java
package com.openos.virtualcar;

interface IVirturalCarProperty {
    int getIntProperty(int propId, int areaId);
    float getFloatProperty(int propId, int areaId);
    String getStringProperty(int propId, int areaId);
    int[] getIntArrayProperty(int propId, int areaId);

    void setProperty(int propId, int areaId, String className, Object value);
}
```

---

## 3. Reverse Engineering Bytecode Mapping

The SDK implementation matches the decompiled Changan OpenOS system framework:

| Framework Class | SDK Implementation |
|:---|:---|:---|
| `com.openos.virtualcar.VirtualCarManager` | `VirtualCarConnection.kt` |
| `com.openos.virtualcar.CarPropertyIds` | `DeepalS05Property.kt` |
| `com.openos.virtualcar.CarTelemetry` | `DeepalS05Telemetry.kt` |
| `com.incall.serversdk.interactive.IDouInteractiveManager` | `DeepalHudClient.kt` |

---

## 4. Property Setter Implementation (Reflection & Typed Parcel)

```kotlin
suspend fun setRawVehicleProperty(
    propId: Int,
    areaId: Int,
    className: String,
    value: Any
): Boolean = withContext(Dispatchers.IO) {
    val propertyService = connection.getPropertyService() ?: return@withContext false
    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeInterfaceToken("com.openos.virtualcar.IVirturalCarProperty")
        data.writeInt(propId)
        data.writeInt(areaId)
        data.writeString(className)
        
        when (value) {
            is Int -> data.writeInt(value)
            is Float -> data.writeFloat(value)
            is String -> data.writeString(value)
            is Boolean -> data.writeInt(if (value) 1 else 0)
        }

        val success = propertyService.asBinder().transact(
            DeepalS05Property.TRANSACT_SET_PROPERTY,
            data,
            reply,
            0
        )
        if (success) {
            reply.readException()
            return@withContext true
        }
    } catch (e: Throwable) {
        Log.e("AIDL_BUS", "Property set failed: ${e.message}")
    } finally {
        data.recycle()
        reply.recycle()
    }
    false
}
```
