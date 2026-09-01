package com.deepal.sdk

import android.os.IBinder
import android.os.Parcel
import android.util.Log
import com.openos.virtualcar.entity.VirtualCarValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method

/**
 * Low-level Binder IPC Connection Bridge to Changan OpenOS VirtualCar & VehicleSetting services.
 *
 * Implements direct transact calls to:
 * - `com.openos.virtualcar.IVirtualCar` (registered as "virtualcar_service")
 * - `com.openos.virtualcar.IVirturalCarProperty` (retrieved via transact code 2 "virtualcar_property_service")
 * - `com.openos.settings.vehiclesettings.IVehicleSettingInterface` (registered as "wt.vehiclesetting")
 */
class VirtualCarConnection {
    companion object {
        private const val TAG = "VirtualCarConnection"
        const val TRANSACT_GET_CAR_SERVICE = 2
        const val TRANSACT_SET_PROPERTY = 2
        const val TRANSACT_GET_PROPERTY = 3
    }

    @Volatile
    private var virtualCarBinder: IBinder? = null

    @Volatile
    private var propertyServiceBinder: IBinder? = null

    @Volatile
    private var vehicleSettingBinder: IBinder? = null

    /**
     * Resolves the root virtualcar_service Binder via ServiceManager reflection.
     */
    fun getRootVirtualCarService(): IBinder? {
        val existing = virtualCarBinder
        if (existing != null && existing.isBinderAlive) {
            return existing
        }
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val checkServiceMethod: Method? = try {
                smClass.getMethod("checkService", String::class.java)
            } catch (_: Throwable) {
                null
            }

            val targetMethod = checkServiceMethod ?: smClass.getMethod("getService", String::class.java)
            val binder = targetMethod.invoke(null, DeepalS05Property.VIRTUALCAR_SERVICE) as? IBinder
            if (binder != null && binder.isBinderAlive) {
                virtualCarBinder = binder
                binder
            } else {
                val getMethod = smClass.getMethod("getService", String::class.java)
                val fallback = getMethod.invoke(null, DeepalS05Property.VIRTUALCAR_SERVICE) as? IBinder
                if (fallback != null && fallback.isBinderAlive) {
                    virtualCarBinder = fallback
                    fallback
                } else null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "VirtualCar ServiceManager resolution failed: ${e.message}")
            null
        }
    }

    /**
     * Obtains the IVirturalCarProperty strong binder from IVirtualCar.
     */
    @Synchronized
    fun getPropertyBinder(): IBinder? {
        val existing = propertyServiceBinder
        if (existing != null && existing.isBinderAlive) {
            return existing
        }

        val rootBinder = getRootVirtualCarService() ?: return null

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR)
            data.writeString(DeepalS05Property.VIRTUALCAR_PROPERTY_SERVICE)

            val success = rootBinder.transact(TRANSACT_GET_CAR_SERVICE, data, reply, 0)
            if (!success) {
                Log.e(TAG, "transact TRANSACT_GET_CAR_SERVICE returned false")
                return null
            }

            reply.readException()
            val propertyBinder = reply.readStrongBinder()
            propertyServiceBinder = propertyBinder
            propertyBinder
        } catch (e: Throwable) {
            Log.e(TAG, "Error acquiring property service: ${e.message}", e)
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Resolves the wt.vehiclesetting Binder for Sunshade, Sunroof, and Chassis preferences.
     */
    fun getVehicleSettingService(): IBinder? {
        val existing = vehicleSettingBinder
        if (existing != null && existing.isBinderAlive) {
            return existing
        }
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getMethod = smClass.getMethod("getService", String::class.java)
            val binder = getMethod.invoke(null, DeepalS05Property.VEHICLE_SETTING_SERVICE) as? IBinder
            if (binder != null && binder.isBinderAlive) {
                vehicleSettingBinder = binder
                binder
            } else null
        } catch (e: Throwable) {
            Log.w(TAG, "wt.vehiclesetting resolution failed: ${e.message}")
            null
        }
    }

    /**
     * Reads a car property value object by propId and areaId.
     */
    fun getVirtualCarValue(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): VirtualCarValue? {
        val binder = getPropertyBinder() ?: return null

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR_PROPERTY)
            data.writeInt(propId)
            data.writeInt(areaId)

            val ok = binder.transact(TRANSACT_GET_PROPERTY, data, reply, 0)
            if (!ok) return null

            reply.readException()
            val hasValue = reply.readInt()
            if (hasValue == 0) return null

            VirtualCarValue.CREATOR.createFromParcel(reply)
        } catch (e: Throwable) {
            Log.w(TAG, "getVirtualCarValue($propId, $areaId) failed: ${e.message}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Reads a car property raw inner value by propId and areaId.
     */
    fun getProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Any? {
        val v = getVirtualCarValue(propId, areaId)
        return v?.mValue
    }

    suspend fun getIntProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Int? = withContext(Dispatchers.IO) {
        val raw = getProperty(propId, areaId)
        when (raw) {
            is Number -> raw.toInt()
            is Boolean -> if (raw) 1 else 0
            else -> null
        }
    }

    suspend fun getFloatProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Float? = withContext(Dispatchers.IO) {
        val raw = getProperty(propId, areaId)
        when (raw) {
            is Number -> raw.toFloat()
            else -> null
        }
    }

    suspend fun getBooleanProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Boolean? = withContext(Dispatchers.IO) {
        val raw = getProperty(propId, areaId)
        when (raw) {
            is Boolean -> raw
            is Number -> raw.toInt() == 1
            else -> null
        }
    }

    /**
     * Writes a VirtualCarValue object through OpenOS VirtualCar property service.
     */
    suspend fun setVirtualCarValue(value: VirtualCarValue): Boolean = withContext(Dispatchers.IO) {
        val binder = getPropertyBinder() ?: return@withContext false

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR_PROPERTY)
            data.writeInt(1) // Has value flag
            value.writeToParcel(data, 0)

            val ok = binder.transact(TRANSACT_SET_PROPERTY, data, reply, 0)
            if (!ok) return@withContext false

            reply.readException()
            val resultCode = reply.readInt()
            resultCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "setVirtualCarValue(${value.mFuncId}, ${value.mAreaId}, ${value.mValue}) failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Writes a car property value through OpenOS VirtualCar property service.
     */
    suspend fun setProperty(
        propId: Int,
        areaId: Int = DeepalS05Property.AREA_GLOBAL,
        value: Any
    ): Boolean = withContext(Dispatchers.IO) {
        val carValue = VirtualCarValue(
            mCategoryId = 0xFF00 and propId,
            mFuncId = propId,
            mAreaId = areaId,
            mCode = 0,
            mTimestamp = 0L,
            mValue = value
        )
        setVirtualCarValue(carValue)
    }

    /**
     * Sets the electric sunroof shade position (0..100 percent) via wt.vehiclesetting service.
     * 100 = Fully Open, 0 = Fully Closed.
     */
    suspend fun setSunshadePos(posPercent: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(posPercent.coerceIn(0, 100))
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_SUNSHADE_POS, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setSunshadePos($posPercent) failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Reads current sunroof sunshade position (0..100 percent) from wt.vehiclesetting service.
     */
    suspend fun getSunshadePos(): Int? = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext null
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            val ok = binder.transact(DeepalS05Property.TRANSACT_GET_SUNSHADE_POS, data, reply, 0)
            if (ok) {
                reply.readException()
                reply.readInt()
            } else null
        } catch (e: Throwable) {
            Log.w(TAG, "getSunshadePos failed: ${e.message}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets the sunroof glass position (0..100 percent) via wt.vehiclesetting service.
     */
    suspend fun setSunroofPos(posPercent: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(posPercent.coerceIn(0, 100))
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_SUNROOF_POS, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setSunroofPos($posPercent) failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Reads current sunroof glass position (0..100 percent) from wt.vehiclesetting service.
     */
    suspend fun getSunroofPos(): Int? = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext null
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            val ok = binder.transact(DeepalS05Property.TRANSACT_GET_SUNROOF_POS, data, reply, 0)
            if (ok) {
                reply.readException()
                reply.readInt()
            } else null
        } catch (e: Throwable) {
            Log.w(TAG, "getSunroofPos failed: ${e.message}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets sunroof tilt state (1=Tilt Up / Vent, 0=Closed) via wt.vehiclesetting service.
     */
    suspend fun setSunroofTiltStatus(tilt: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(tilt)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_SUNROOF_TILT, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setSunroofTiltStatus($tilt) failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets mirror auto-fold on lock preference switch via wt.vehiclesetting service.
     */
    suspend fun setMirrorAutofoldSw(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(if (enabled) 1 else 0)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_MIRROR_AUTOFOLD, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setMirrorAutofoldSw failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets walk-away smart auto-locking switch via wt.vehiclesetting service.
     */
    suspend fun setSmartLeavingLockSw(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(if (enabled) 1 else 0)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_SMART_LEAVING_LOCK, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setSmartLeavingLockSw failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets HUD hardware on/off switch status via wt.vehiclesetting service.
     */
    suspend fun setHudSwitchStatus(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(if (enabled) 1 else 0)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_HUD_SWITCH, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setHudSwitchStatus failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets HUD optical brightness level via wt.vehiclesetting service.
     */
    suspend fun setHudBright(brightness: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(brightness)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_HUD_BRIGHT, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setHudBright failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets HUD optical height level via wt.vehiclesetting service.
     */
    suspend fun setHudHeight(height: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(height)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_HUD_HEIGHT, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setHudHeight failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets HUD navigation guidance display switch via wt.vehiclesetting service.
     */
    suspend fun setHudDisplayNavSw(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(if (enabled) 1 else 0)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_HUD_DISPLAY_NAV, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setHudDisplayNavSw failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sets HUD incoming phone call alert switch via wt.vehiclesetting service.
     */
    suspend fun setHudDisplayPhoneSw(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val binder = getVehicleSettingService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
            data.writeInt(if (enabled) 1 else 0)
            val ok = binder.transact(DeepalS05Property.TRANSACT_SET_HUD_DISPLAY_PHONE, data, reply, 0)
            if (ok) {
                reply.readException()
                true
            } else false
        } catch (e: Throwable) {
            Log.e(TAG, "setHudDisplayPhoneSw failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
