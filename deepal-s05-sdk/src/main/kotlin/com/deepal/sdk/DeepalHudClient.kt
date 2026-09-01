package com.deepal.sdk

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method

/**
 * Native Binder IPC Client to Changan InCall AR-HUD & Digital Instrument Cluster.
 */
class DeepalHudClient {
    companion object {
        private const val TAG = "DeepalHudClient"
    }

    @Volatile
    private var svrManagerBinder: IBinder? = null

    @Volatile
    private var interactiveServiceBinder: IBinder? = null

    private val naviFocusCallback = object : Binder() {
        init {
            attachInterface(null, "com.incall.serversdk.interactive.callback.INaviFocusCallback")
        }
    }

    /**
     * Resolves com.incall.SVR_MNG_SERVICE from Android ServiceManager.
     */
    private fun getSvrManager(): IBinder? {
        if (svrManagerBinder != null && svrManagerBinder!!.isBinderAlive) {
            return svrManagerBinder
        }
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod: Method = try {
                smClass.getMethod("checkService", String::class.java)
            } catch (_: Throwable) {
                smClass.getMethod("getService", String::class.java)
            }
            val binder = getServiceMethod.invoke(null, DeepalS05Property.INCALL_SVR_MNG_SERVICE) as? IBinder
                ?: run {
                    val fallback = smClass.getMethod("getService", String::class.java)
                    fallback.invoke(null, DeepalS05Property.INCALL_SVR_MNG_SERVICE) as? IBinder
                }
            svrManagerBinder = binder
            binder
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to resolve com.incall.SVR_MNG_SERVICE: ${e.message}")
            null
        }
    }

    /**
     * Resolves the IDouInteractiveManager binder token.
     * Transaction 6 on ISvrManager with descriptor "com.incall.double.INTERACTIVE_SERVICE".
     */
    private fun getInteractiveService(): IBinder? {
        if (interactiveServiceBinder != null && interactiveServiceBinder!!.isBinderAlive) {
            return interactiveServiceBinder
        }

        val svrBinder = getSvrManager() ?: return null

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_SVR_MANAGER)
            data.writeString(DeepalS05Property.INCALL_DOUBLE_INTERACTIVE_SERVICE)
            val success = svrBinder.transact(6, data, reply, 0)
            if (success) {
                reply.readException()
                interactiveServiceBinder = reply.readStrongBinder()
                Log.i(TAG, "Acquired com.incall.double.INTERACTIVE_SERVICE binder")
            }
            interactiveServiceBinder
        } catch (e: Throwable) {
            Log.e(TAG, "Error acquiring double interactive service: ${e.message}")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Sends navigation state to Deepal AR-HUD / cluster (Transact 0x16 / 22).
     * @param status 1 = Active guidance, 2 = Arrived, 0 = Inactive / cleared
     */
    suspend fun sendNavigateStatus(status: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeInt(status)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_NAVIGATE_STATUS, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "sendNavigateStatus failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Sends next maneuver icon and countdown distance to HUD (Transact 0x18 / 24).
     */
    suspend fun sendNavigateTurnInfo(turnIcon: Int, turnDistMeters: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeInt(turnIcon)
            data.writeInt(turnDistMeters)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_NAVIGATE_TURN_INFO, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "sendNavigateTurnInfo failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Sends road names to HUD banner (Transact 0x1a / 26).
     */
    suspend fun sendNavigateRoadInfo(nextRoad: String, curRoad: String): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeString(nextRoad)
            data.writeString(curRoad)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_NAVIGATE_ROAD_INFO, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "sendNavigateRoadInfo failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Sends total remaining distance and ETA to HUD (Transact 0x1b / 27).
     */
    suspend fun sendNavigateRemainInfo(remainDistMeters: Int, remainTimeSec: Int): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeInt(remainDistMeters)
            data.writeInt(remainTimeSec)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_NAVIGATE_REMAIN_INFO, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "sendNavigateRemainInfo failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Requests HUD navigation audio and display focus (Transact 0x3f / 63).
     */
    suspend fun requestNaviFocus(packageName: String = "com.deepalnav"): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeString(packageName)
            data.writeStrongBinder(naviFocusCallback)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_REQUEST_NAVI_FOCUS, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "requestNaviFocus failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Releases HUD navigation graphics focus (Transact 0x40 / 64).
     */
    suspend fun abandonNaviFocus(packageName: String = "com.deepalnav"): Boolean = withContext(Dispatchers.IO) {
        val binder = getInteractiveService() ?: return@withContext false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.INCALL_DESCRIPTOR_INTERACTIVE_MANAGER)
            data.writeString(packageName)
            data.writeStrongBinder(naviFocusCallback)
            val ok = binder.transact(DeepalS05Property.INCALL_CMD_ABANDON_NAVI_FOCUS, data, reply, 0)
            if (ok) {
                reply.readException()
                return@withContext true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "abandonNaviFocus failed: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
        false
    }

    /**
     * Clears HUD and cluster guidance display upon route completion.
     */
    suspend fun clear(): Boolean {
        val a = sendNavigateTurnInfo(0, 0)
        val b = sendNavigateRoadInfo("", "")
        val c = sendNavigateRemainInfo(0, 0)
        val d = sendNavigateStatus(0)
        return a || b || c || d
    }
}
