package com.deepal.sdk

import android.os.IBinder
import android.os.Parcel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import java.util.HashMap

/**
 * Result model returned by Tinnove Polymeric Service (`com.tinnove.polymericservice`).
 */
data class PolymericResult(
    val code: String,
    val msg: String?,
    val data: Any?
) {
    val isSuccess: Boolean get() = code == "000000"
}

/**
 * High-performance IPC Client for Changan Tinnove Polymeric Service (`polymeric_service`).
 *
 * Implements direct Binder IPC transactions against `com.tinnove.polymericservice.IPolymericService`
 * for accessing and mutating vehicle properties via canonical `vc_alias_*` identifiers.
 */
class TinnovePolymericClient {
    companion object {
        private const val TAG = "TinnovePolymericClient"
        const val SERVICE_NAME = "polymeric_service"
        const val DESCRIPTOR = "com.tinnove.polymericservice.IPolymericService"

        // Transaction Codes
        const val TRANSACT_CALL_METHOD = 1
        const val TRANSACT_ASYNC_CALL_METHOD = 2
        const val TRANSACT_REGISTER_EVENT_LISTENER = 3
        const val TRANSACT_UNREGISTER_EVENT_LISTENER = 4

        // Ability & Method Codes
        const val ABILITY_CAR_CONTROL = 0x44d // 1101
        const val ABILITY_CAR_INFO = 0x44e    // 1102

        const val METHOD_GET_VALUE = 0x3eb    // 1003: psGetValueSync
        const val METHOD_SET_VALUE = 0x3ec    // 1004: psSetValue

        // Request / Response Map Keys
        const val KEY_REQUEST_VS_ALIAS = "request_params_vs_alias"
        const val KEY_REQUEST_VALUE = "request_params_value"
        const val KEY_RESULT_VALUE = "result_params_value"
    }

    @Volatile
    private var serviceBinder: IBinder? = null

    /**
     * Resolves the polymeric_service IBinder via ServiceManager reflection.
     */
    fun getService(): IBinder? {
        val existing = serviceBinder
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
            val binder = targetMethod.invoke(null, SERVICE_NAME) as? IBinder
            if (binder != null && binder.isBinderAlive) {
                serviceBinder = binder
                binder
            } else null
        } catch (e: Throwable) {
            Log.w(TAG, "polymeric_service resolution failed: ${e.message}")
            null
        }
    }

    /**
     * Executes a synchronous callMethod transaction against IPolymericService.
     */
    suspend fun callMethod(
        abilityCode: Int,
        methodCode: Int,
        params: Map<String, Any?> = emptyMap()
    ): PolymericResult? = withContext(Dispatchers.IO) {
        val binder = getService() ?: return@withContext null

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DESCRIPTOR)
            data.writeInt(1) // Non-null WTRequestModel flag

            // Write WTRequestModel to parcel
            data.writeInt(abilityCode)
            data.writeInt(methodCode)
            data.writeMap(params)
            data.writeInt(1) // sdkVersionCode

            val ok = binder.transact(TRANSACT_CALL_METHOD, data, reply, 0)
            if (!ok) {
                Log.e(TAG, "callMethod($abilityCode, $methodCode) transact returned false")
                return@withContext null
            }

            reply.readException()
            val hasResult = reply.readInt()
            if (hasResult == 0) return@withContext null

            // Read WTResultModel from reply parcel
            val code = reply.readString() ?: ""
            val msg = reply.readString()
            val resultData = reply.readValue(javaClass.classLoader)

            PolymericResult(code = code, msg = msg, data = resultData)
        } catch (e: Throwable) {
            Log.e(TAG, "callMethod($abilityCode, $methodCode) failed: ${e.message}", e)
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Reads a vehicle property value synchronously using its canonical `vc_alias_*` identifier.
     * Example aliases: "vc_alias_drive_style", "vc_alias_vehicle_speed", "vc_alias_ac_internal_temp".
     */
    suspend fun getValue(alias: String): Any? = withContext(Dispatchers.IO) {
        val params = HashMap<String, Any?>()
        params[KEY_REQUEST_VS_ALIAS] = alias

        val result = callMethod(ABILITY_CAR_CONTROL, METHOD_GET_VALUE, params)
        if (result != null && result.isSuccess) {
            val dataMap = result.data as? Map<*, *>
            dataMap?.get(KEY_RESULT_VALUE)
        } else null
    }

    /**
     * Writes a vehicle property value using its canonical `vc_alias_*` identifier.
     */
    suspend fun setValue(alias: String, value: Any): Boolean = withContext(Dispatchers.IO) {
        val params = HashMap<String, Any?>()
        params[KEY_REQUEST_VS_ALIAS] = alias
        params[KEY_REQUEST_VALUE] = value

        val result = callMethod(ABILITY_CAR_CONTROL, METHOD_SET_VALUE, params)
        result?.isSuccess == true
    }
}
