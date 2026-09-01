package com.deepal.sdk

import android.os.IBinder
import android.os.Parcel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method

/**
 * Low-level Binder IPC Connection Bridge to Changan OpenOS VirtualCar services.
 */
class VirtualCarConnection {
    companion object {
        private const val TAG = "VirtualCarConnection"
        private const val TRANSACT_GET_CAR_SERVICE = 2
        private const val TRANSACT_SET_PROPERTY = 2
        private const val TRANSACT_GET_PROPERTY = 3
    }

    @Volatile
    private var virtualCarBinder: IBinder? = null

    @Volatile
    private var propertyServiceBinder: IBinder? = null

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
     * Reads a car property value by propId and areaId.
     */
    fun getProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Any? {
        val binder = getPropertyBinder() ?: return null

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_PROPERTY)
            data.writeInt(propId)
            data.writeInt(areaId)

            val ok = binder.transact(TRANSACT_GET_PROPERTY, data, reply, 0)
            if (!ok) return null

            reply.readException()
            val status = reply.readInt()
            if (status == 0) return null

            // Read CarPropertyValue parcel header:
            reply.readInt() // propId
            reply.readInt() // areaId
            reply.readInt() // status
            reply.readInt() // reserved
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
        } catch (e: Throwable) {
            Log.w(TAG, "getProperty($propId, $areaId) failed: ${e.message}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    suspend fun getIntProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Int? = withContext(Dispatchers.IO) {
        val raw = getProperty(propId, areaId)
        when (raw) {
            is Number -> raw.toInt()
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

    /**
     * Writes a car property value through OpenOS VirtualCar property service.
     */
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
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_PROPERTY)
            data.writeInt(1) // Flag: 1
            data.writeInt(0xFF00 and propId) // Group mask
            data.writeInt(propId)
            data.writeInt(areaId)
            data.writeInt(0) // Reserved
            data.writeLong(0L) // Timestamp
            data.writeString(className)
            data.writeValue(value)

            val ok = binder.transact(TRANSACT_SET_PROPERTY, data, reply, 0)
            if (!ok) return@withContext false

            reply.readException()
            val resultCode = reply.readInt()
            resultCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "setProperty($propId, $areaId, $value) failed: ${e.message}")
            false
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
