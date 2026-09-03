package com.deepal.sdk.device

import android.os.Build
import com.deepal.sdk.vehicle.BuiltInProfiles
import com.deepal.sdk.vehicle.VehicleConfigurations
import com.deepal.sdk.vehicle.VehicleProfile
import com.deepal.sdk.vehicle.VehicleProfileConfig

/**
 * Detection field targets for hardware fingerprint matching.
 */
enum class DetectionField {
    MODEL,
    PRODUCT,
    MANUFACTURER,
    BOARD,
    DEVICE
}

/**
 * Fingerprint matching rule against Android build properties.
 */
data class FingerprintRule(
    val field: DetectionField,
    val equalsIgnoreCase: String
) {
    fun matches(deviceInfo: DeviceInfo): Boolean {
        val target = when (field) {
            DetectionField.MODEL -> deviceInfo.model
            DetectionField.PRODUCT -> deviceInfo.product
            DetectionField.MANUFACTURER -> deviceInfo.manufacturer
            DetectionField.BOARD -> deviceInfo.board
            DetectionField.DEVICE -> deviceInfo.device
        }
        return target.equals(equalsIgnoreCase, ignoreCase = true)
    }
}

/**
 * Device information extracted from android.os.Build.
 */
data class DeviceInfo(
    val model: String = getSafeBuildField { Build.MODEL },
    val product: String = getSafeBuildField { Build.PRODUCT },
    val manufacturer: String = getSafeBuildField { Build.MANUFACTURER },
    val board: String = getSafeBuildField { Build.BOARD },
    val device: String = getSafeBuildField { Build.DEVICE }
) {
    companion object {
        private inline fun getSafeBuildField(fieldAccessor: () -> String?): String {
            return try {
                fieldAccessor() ?: ""
            } catch (_: Throwable) {
                ""
            }
        }
    }
}

/**
 * Hardware capability flags and transmission orientation.
 */
data class VehicleCapabilities(
    val flags: Int = 0xd80,
    val isReverse: Boolean = true
)

data class VehicleDeviceProfile(
    val id: String,
    val label: String,
    val fingerprintRules: List<FingerprintRule>,
    val capabilities: VehicleCapabilities,
    val deviceProfileId: String,
    val batteryCapacityKwh: Float? = null,
    val isSupported: Boolean = true
)

/**
 * Registry of vehicle hardware profile and auto-detection engine for Deepal S05.   
 */
object VehicleProfiles {

    // Deepal S05 (Model "C857")
    val DEEPAL_S05 = VehicleDeviceProfile(
        id = "deepal-s05",
        label = "Deepal S05",
        fingerprintRules = listOf(
            FingerprintRule(DetectionField.MODEL, "C857")
        ),
        capabilities = VehicleCapabilities(flags = 0xd80, isReverse = true),
        deviceProfileId = "deepal-s05-c857",
        batteryCapacityKwh = null,
        isSupported = true
    )

    val GENERIC = VehicleDeviceProfile(
        id = "generic",
        label = "Other car",
        fingerprintRules = emptyList(),
        capabilities = VehicleCapabilities(flags = 0xd80, isReverse = true),
        deviceProfileId = "deepal-s05-c857",
        batteryCapacityKwh = null,
        isSupported = false
    )

    val ALL = listOf(DEEPAL_S05, GENERIC)

    /**
     * Detects current vehicle from Build info.
     */
    fun detectCurrent(deviceInfo: DeviceInfo = DeviceInfo()): VehicleDeviceProfile {
        return ALL.firstOrNull { profile ->
            profile.fingerprintRules.isNotEmpty() &&
                    profile.fingerprintRules.all { rule -> rule.matches(deviceInfo) }
        } ?: GENERIC
    }

    /**
     * Maps platformId to the DEEPAL_S05_C857 hardware VehicleProfileConfig.
     */
    @Suppress("UNUSED_PARAMETER")
    fun resolveVehicleConfig(platformId: String = "deepal-s05"): VehicleProfileConfig {
        return VehicleConfigurations.DEEPAL_S05_C857
    }

    /**
     * Maps platformId to the DEEPAL_S05_C857 VehicleProfile model.
     */
    @Suppress("UNUSED_PARAMETER")
    fun resolveVehicleProfile(platformId: String = "deepal-s05"): VehicleProfile {
        return BuiltInProfiles.DEEPAL_S05_C857
    }
}
