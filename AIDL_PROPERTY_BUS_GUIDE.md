# Deepal S05 OpenOS AIDL Property Bus Architecture Guide
### Direct Binder IPC, ServiceManager Reflection, and Low-Level Hardware Interface

---

## 1. OpenOS VirtualCar Subsystem Architecture

On the **Changan Deepal S05** (Model C857 / EPA Platform), hardware signals and actuators are mediated by the `virtualcar_service` running in the Android system server.

### IPC Topology
```
[ DeepalNav Application / SDK ]
               │
               ▼ (ServiceManager.checkService("virtualcar_service"))
[ com.openos.virtualcar.IVirtualCar ] (IBinder)
               │
               ▼ (transact code 2 -> getCarService("virtualcar_property_service"))
[ com.openos.virtualcar.IVirturalCarProperty ] (OEM Spelling with 'r')
               │
               ├──> Transact 2: setProperty(flag=1, group, propId, areaId, reserved=0, timestamp=0L, className, value)
               └──> Transact 3: getProperty(propId, areaId) -> CarPropertyValue
```

---

## 2. Low-Level AIDL Interface Definitions

### `IVirtualCar.aidl`
```java
package com.openos.virtualcar;

import android.os.IBinder;

interface IVirtualCar {
    int getVersion();
    IBinder getCarService(String serviceName); // Transact code 2
}
```

### `IVirturalCarProperty.aidl`
```java
package com.openos.virtualcar;

interface IVirturalCarProperty {
    int getVersion();
    int setProperty(int flag, int group, int propId, int areaId, int reserved, long timestamp, String className);
    int getProperty(int propId, int areaId);
}
```

---

## 3. Reverse Engineering Bytecode Mapping 

The SDK implementation matches the decompiled Changan OpenOS system framework:

| Framework Class | SDK Implementation | Role |
|:---|:---|:---|
| | `VirtualCarConnection.kt` | Low-level Binder IPC for `virtualcar_service` & `IVirturalCarProperty` |
|  | `DeepalS05Property.kt` | Constants, property IDs, area masks, scaling divisors |
|  | `DeepalS05Telemetry.kt` | Vehicle telemetry model |
|  | `DeepalHudClient.kt` | InCall AR-HUD & digital cluster IPC bridge (`0x16`, `0x18`, `0x1a`, `0x1b`, `0x3f`, `0x40`) |
|  | `VirtualCarConnection.kt` | Callers validating return status (`0 == OK / success`) |

---

## 4. Property Setter Implementation (Reflection & Typed Parcel)

```kotlin
suspend fun setProperty(
    propId: Int,
    areaId: Int = DeepalS05Property.AREA_GLOBAL,
    className: String,
    value: Any
): Boolean = withContext(Dispatchers.IO) {
    val binder = getPropertyBinder() ?: return@withContext false

    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeInterfaceToken("com.openos.virtualcar.IVirturalCarProperty")
        data.writeInt(1)                         // Flag: 1
        data.writeInt(0xFF00 and propId)         // Group mask
        data.writeInt(propId)                    // Property ID
        data.writeInt(areaId)                    // Area ID
        data.writeInt(0)                         // Reserved
        data.writeLong(0L)                       // Timestamp
        data.writeString(className)              // e.g. "java.lang.Integer", "java.lang.Float"
        data.writeValue(value)                   // Dynamic Polymorphic Value

        val ok = binder.transact(2, data, reply, 0)
        if (!ok) return@withContext false

        reply.readException()
        val resultCode = reply.readInt()
        resultCode == 0                          // 0 = Success 
    } finally {
        data.recycle()
        reply.recycle()
    }
}
```

---

## 5. Property Getter Implementation (Parcel Unpacking)


```kotlin
fun getProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Any? {
    val binder = getPropertyBinder() ?: return null

    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeInterfaceToken("com.openos.virtualcar.IVirturalCarProperty")
        data.writeInt(propId)
        data.writeInt(areaId)

        val ok = binder.transact(3, data, reply, 0)
        if (!ok) return null

        reply.readException()
        val status = reply.readInt()
        if (status == 0) return null

        reply.readInt()  // propId
        reply.readInt()  // areaId
        reply.readInt()  // status
        reply.readInt()  // reserved
        reply.readLong() // timestamp

        val typeString = reply.readString()
        val classLoader = if (!typeString.isNullOrEmpty() && typeString != "null") {
            try {
                Class.forName(typeString).classLoader
            } catch (_: Throwable) {
                null
            }
        } else null

        reply.readValue(classLoader)
    } finally {
        data.recycle()
        reply.recycle()
    }
}
```
