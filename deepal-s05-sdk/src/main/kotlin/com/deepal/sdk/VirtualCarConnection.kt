package com.deepal.sdk

import android.os.IBinder
import android.os.Parcel
import android.util.Log
import com.openos.virtualcar.IVirturalCarProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Low-level Binder IPC Connection Manager to Changan OpenOS VirtualCar services.
 *
 * Implements the exact reflection and transaction logic from:
 */
class VirtualCarConnection {
    companion object {
        private const val TAG = "VirtualCarConnection"
    }

    private var virtualCarBinder: IBinder? = null
    private var propertyServiceBinder: IBinder? = null
    private var propertyProxy: IVirturalCarProperty? = null

    /**
     * Resolves the root virtualcar_service Binder via ServiceManager reflection.
     */
    fun getRootVirtualCarService(): IBinder? {
        if (virtualCarBinder != null && virtualCarBinder!!.isBinderAlive) {
            return virtualCarBinder
        }
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val checkServiceMethod: Method? = try {
                smClass.getMethod("checkService", String::class.java)
            } catch (e: Throwable) {
                null
            }

            val targetMethod = checkServiceMethod ?: smClass.getMethod("getService", String::class.java)
            val binder = targetMethod.invoke(null, DeepalS05Property.VIRTUALCAR_SERVICE) as? IBinder
            virtualCarBinder = binder
            binder
        } catch (e: Throwable) {
            Log.w(TAG, "VirtualCar ServiceManager resolution failed: ${e.message}")
            null
        }
    }

    /**
     * Obtains the IVirturalCarProperty proxy via Transaction Code 2 on virtualcar_service.
     */
    fun getPropertyService(): IVirturalCarProperty? {
        if (propertyProxy != null && propertyServiceBinder?.isBinderAlive == true) {
            return propertyProxy
        }

        val rootBinder = getRootVirtualCarService() ?: return null

        return try {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR)
                data.writeString(DeepalS05Property.VIRTUALCAR_PROPERTY_SERVICE)

                // Transact 2: FIRST_CALL_TRANSACTION + 1 = getCarService(String)
                val success = rootBinder.transact(2, data, reply, 0)
                if (success) {
                    reply.readException()
                    propertyServiceBinder = reply.readStrongBinder()
                    if (propertyServiceBinder != null) {
                        propertyProxy = IVirturalCarProperty.Stub.asInterface(propertyServiceBinder)
                        Log.i(TAG, "Connected to virtualcar_property_service successfully")
                    }
                }
            } finally {
                data.recycle()
                reply.recycle()
            }
            propertyProxy
        } catch (e: Throwable) {
            Log.e(TAG, "Error acquiring property service: ${e.message}")
            null
        }
    }

    suspend fun getIntProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Int? = withContext(Dispatchers.IO) {
        val bytes = getPropertyRaw(propId, areaId) ?: return@withContext null
        if (bytes.size >= 4) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).int
        } else null
    }

    suspend fun getFloatProperty(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): Float? = withContext(Dispatchers.IO) {
        val bytes = getPropertyRaw(propId, areaId) ?: return@withContext null
        if (bytes.size >= 4) {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).float
        } else null
    }

    suspend fun setProperty(
        propId: Int,
        areaId: Int = DeepalS05Property.AREA_GLOBAL,
        className: String,
        value: Any
    ): Boolean = withContext(Dispatchers.IO) {
        val proxy = getPropertyService()
        val rawBytes = when (value) {
            is Int -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
            is Float -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array()
            is Boolean -> ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(if (value) 1 else 2).array()
            is ByteArray -> value
            else -> return@withContext false
        }

        try {
            if (proxy != null) {
                return@withContext proxy.setProperty(
                    0, 0, propId, areaId, 0,
                    System.currentTimeMillis(),
                    className,
                    rawBytes
                )
            }
        } catch (e: Throwable) {
            Log.w(TAG, "AIDL setProperty call failed: ${e.message}, attempting manual parcel transact")
        }

        // Direct Binder Transact Fallback
        val binder = propertyServiceBinder ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_PROPERTY)
            data.writeInt(0) // flag
            data.writeInt(0) // group
            data.writeInt(propId)
            data.writeInt(areaId)
            data.writeInt(0) // reserved
            data.writeLong(System.currentTimeMillis())
            data.writeString(className)
            data.writeByteArray(rawBytes)

            val ok = binder.transact(2, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext reply.readInt() != 0
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Manual transact setProperty failed for prop $propId: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    suspend fun getPropertyRaw(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): ByteArray? = withContext(Dispatchers.IO) {
        val proxy = getPropertyService()
        try {
            if (proxy != null) {
                val res = proxy.getProperty(propId, areaId)
                if (res != null && res.isNotEmpty()) return@withContext res
            }
        } catch (e: Throwable) {
            Log.w(TAG, "AIDL getProperty call failed: ${e.message}, attempting manual parcel transact")
        }

        // Direct Binder Transact Fallback (Transact 3)
        val binder = propertyServiceBinder ?: return@withContext null
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.DESCRIPTOR_PROPERTY)
            data.writeInt(propId)
            data.writeInt(areaId)
            val ok = binder.transact(3, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext reply.createByteArray()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Manual transact getProperty failed for prop $propId: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        null
    }
}
