package com.deepal.sdk.vehicle

/**
 * Hardware CAN and VHAL Signal Definition for Deepal S05 C857 bus abstraction.
 * Corresponds to smali: G2.E0.
 */
data class CanSignalDefinition(
    val propertyId: Int,
    val areaId: Int = 0,
    val changeMode: Int = 0,
    val status: Int = 0,
    val sampleRate: Int = 0
)

/**
 * Vendor Type abstraction for vehicle telemetry and control dispatching.
 */
enum class VendorType {
    GENERIC,
    VENDOR
}

/**
 * Channel routing for vehicle signal reading and writing.
 */
enum class VehicleType {
    CAR,
    VENDOR
}

enum class ProfileOrigin {
    BUILT_IN,
    DISCOVERED
}

enum class WriteChannel {
    CAR,
    VENDOR
}

enum class SignalLanguage {
    AIR_SOURCE_INVERTED,
    BOOLEAN,
    VENDOR_TRI_STATE,
    VENDOR_TRI_STATE_INVERTED
}

enum class HudType {
    HUD_50_INCH,
    HUD_100_INCH,
    UNSURE
}

/**
 * Vehicle Signal specification for VHAL and OpenOS property mapping.
 * Corresponds to smali: LG2/H0;
 */
data class VehicleSignal(
    val propId: Int,
    val area: Int = 0,
    val scaleToCanonical: Int = 1,
    val scaleDivisor: Int = 1
) {
    fun toCanonical(rawValue: Number): Float {
        return (rawValue.toFloat() * scaleToCanonical) / scaleDivisor
    }
}

/**
 * Full Cabin telemetry reading definitions.
 * Corresponds to smali: LG2/k;
 */
data class CabinSignals(
    val driverTemp: VehicleSignal? = null,
    val passengerTemp: VehicleSignal? = null,
    val fanSpeed: VehicleSignal? = null,
    val acOn: VehicleSignal? = null,
    val maxAcOn: VehicleSignal? = null,
    val recircOn: VehicleSignal? = null,
    val autoOn: VehicleSignal? = null,
    val dualOn: VehicleSignal? = null,
    val autoRecircOn: VehicleSignal? = null,
    val maxDefrostOn: VehicleSignal? = null,
    val frontDefrost: VehicleSignal? = null,
    val rearDefrost: VehicleSignal? = null,
    val steeringWheelHeat: VehicleSignal? = null,
    val seatHeat: List<VehicleSignal> = emptyList(),
    val seatVent: List<VehicleSignal> = emptyList(),
    val windows: List<VehicleSignal> = emptyList(),
    val doorLocks: List<VehicleSignal> = emptyList(),
    val outsideTemp: VehicleSignal? = null,
    val gear: VehicleSignal? = null,
    val recircLanguage: SignalLanguage = SignalLanguage.VENDOR_TRI_STATE_INVERTED,
    val doorLockLanguage: SignalLanguage = SignalLanguage.VENDOR_TRI_STATE
)

// ==========================================
// Cabin Property Write Handlers
// ==========================================

data class CabinGearRead(
    val propId: Int = 0x11400400, // VehiclePropertyIds.GEAR_SELECTION
    val area: Int = 0,
    val parkRaw: Int = 4
)

data class CabinTempWrite(
    val propId: Int = 0x15600503,
    val area: Int = 1,
    val minC: Float = 17.5f,
    val maxC: Float = 32.5f,
    val stepC: Float = 0.5f,
    val channel: WriteChannel = WriteChannel.CAR
)

data class CabinToggleWrite(
    val propId: Int,
    val area: Int = 1,
    val offRaw: Int = 1,
    val onRaw: Int = 2,
    val channel: WriteChannel = WriteChannel.CAR
)

data class CabinLevelWrite(
    val propId: Int,
    val area: Int = 0,
    val min: Int = 1,
    val max: Int = 3
)

data class CabinPositionWrite(
    val propId: Int,
    val area: Int = 0,
    val min: Int = 0,
    val max: Int = 100
)

data class CabinChoiceWrite(
    val propId: Int,
    val choices: Set<Int>,
    val area: Int = 1
)

data class CabinCommandWrite(
    val propId: Int,
    val area: Int = 0,
    val onCommand: Int = 1,
    val offCommand: Int = 0,
    val stateId: Int = propId,
    val stateArea: Int = area,
    val onState: Int = 1,
    val offState: Int = 0,
    val transientStates: Set<Int> = emptySet(),
    val parkedOnly: Boolean = false
)

/**
 * Write dispatch definition for cabin components.
 * Corresponds to smali: LG2/F;
 */
data class CabinWrites(
    val gear: CabinGearRead = CabinGearRead(),
    val driverTemp: CabinTempWrite? = null,
    val passengerTemp: CabinTempWrite? = null,
    val ac: CabinToggleWrite? = null,
    val auto: CabinToggleWrite? = null,
    val maxAc: CabinToggleWrite? = null,
    val recirc: CabinToggleWrite? = null,
    val frontDefrost: CabinToggleWrite? = null,
    val rearDefrost: CabinToggleWrite? = null,
    val sync: CabinToggleWrite? = null,
    val fan: CabinLevelWrite? = null,
    val driverWindow: CabinPositionWrite? = null,
    val passengerWindow: CabinPositionWrite? = null,
    val rearLeftWindow: CabinPositionWrite? = null,
    val rearRightWindow: CabinPositionWrite? = null,
    val sunshade: CabinPositionWrite? = null,
    val climatePower: CabinCommandWrite? = null,
    val lock: CabinCommandWrite? = null,
    val tailgate: CabinCommandWrite? = null,
    val steeringWheelHeat: CabinCommandWrite? = null,
    val driverSeatHeat: CabinLevelWrite? = null,
    val passengerSeatHeat: CabinLevelWrite? = null,
    val driverSeatVent: CabinLevelWrite? = null,
    val passengerSeatVent: CabinLevelWrite? = null,
    val driverMassage: CabinCommandWrite? = null,
    val passengerMassage: CabinCommandWrite? = null,
    val driverMassageLevel: CabinLevelWrite? = null,
    val passengerMassageLevel: CabinLevelWrite? = null,
    val ambientLight: CabinCommandWrite? = null,
    val ambientPattern: CabinLevelWrite? = null,
    val ambientColour: CabinChoiceWrite? = null,
    val driveMode: CabinChoiceWrite? = null,
    val driverMassageMode: CabinLevelWrite? = null,
    val passengerMassageMode: CabinLevelWrite? = null,
    val autoEmergencyBraking: CabinCommandWrite? = null
)

/**
 * Top-level vehicle profile and signal definition.
 * Corresponds to smali: LG2/C0;
 */
data class VehicleProfile(
    val id: String,
    val soc: VehicleSignal,
    val range: VehicleSignal,
    val odometer: VehicleSignal,
    val drivingTime: VehicleSignal? = null,
    val remainingEnergy: VehicleSignal? = null,
    val tyrePressure: VehicleSignal? = null,
    val packKwh: Float? = null,
    val setpointPlausibleC: ClosedFloatingPointRange<Float> = 17.5f..32.5f,
    val readChannel: VehicleType = VehicleType.CAR,
    val origin: ProfileOrigin = ProfileOrigin.BUILT_IN,
    val cabin: CabinSignals? = null,
    val cabinWrites: CabinWrites? = null
)

// ==========================================
// Write Intent, Plan & Result Hierarchy
// ==========================================

sealed interface WriteIntent {
    data class Choice(val gear: CabinGearRead, val write: CabinChoiceWrite, val target: Int) : WriteIntent
    data class Command(val gear: CabinGearRead, val write: CabinCommandWrite, val desiredOn: Boolean) : WriteIntent
    data class LevelStep(val gear: CabinGearRead, val write: CabinLevelWrite, val targetLevel: Int) : WriteIntent
    data class Position(val gear: CabinGearRead, val write: CabinPositionWrite, val targetPct: Int?) : WriteIntent
    data class TempSet(val gear: CabinGearRead, val write: CabinTempWrite, val targetC: Float) : WriteIntent
    data class TempStep(val gear: CabinGearRead, val write: CabinTempWrite, val deltaC: Float) : WriteIntent
}

sealed interface WritePlan {
    data class AlreadyThere(val raw: Number) : WritePlan
    data class Proceed(val valueToWrite: Number) : WritePlan
    data class Refused(val reason: String) : WritePlan
}

sealed interface WriteResult {
    data class Confirmed(val settledRaw: Any?) : WriteResult
    data class Failed(val reason: String) : WriteResult
    data class Refused(val reason: String) : WriteResult
}

// ==========================================
// Hardware CAN & Profile Config Definition
// ==========================================

data class VehicleProfileConfig(
    val id: String,
    val speedSignal: CanSignalDefinition,
    val gearSignal: CanSignalDefinition,
    val steeringSignal: CanSignalDefinition,
    val socSignal: CanSignalDefinition?,
    val rangeSignal: CanSignalDefinition?,
    val hvacSignal: CanSignalDefinition,
    val batteryCapacityKwh: Float?,
    val tempRangeCelsius: ClosedFloatingPointRange<Float>,
    val vendorType: VendorType,
    val customHandler: Any? = null,
    val audioConfig: Any? = null,
    val extraFlags: Any? = null,
    val flags: Int = 0
)

/**
 * Hardware Vehicle Configurations (Corresponds to smali: G2.E0).
 */
object VehicleConfigurations {

    val DEEPAL_S05_C857 = VehicleProfileConfig(
        id = "deepal-s05-c857",
        speedSignal = CanSignalDefinition(propertyId = 0x21410605, areaId = 0, changeMode = 3, status = 4),
        gearSignal = CanSignalDefinition(propertyId = 0x21410605, areaId = 0, changeMode = 3, status = 4),
        steeringSignal = CanSignalDefinition(propertyId = 0x31600204, areaId = 0, changeMode = 8, status = 1000, sampleRate = 0),
        socSignal = null,
        rangeSignal = null,
        hvacSignal = CanSignalDefinition(propertyId = 0x31410605, areaId = 0, changeMode = 4, status = 3),
        batteryCapacityKwh = null,
        tempRangeCelsius = 17.5f..32.5f,
        vendorType = VendorType.VENDOR,
        flags = 0x1030
    )
}

/**
 * Built-in vehicle profiles and cabin control mappings (LG2/E0).
 */
object BuiltInProfiles {

    val DEEPAL_S05_CABIN_WRITES: CabinWrites = CabinWrites(
        gear = CabinGearRead(propId = 0x11400400, area = 0, parkRaw = 4),
        driverTemp = CabinTempWrite(propId = 0x35600105, area = 1, minC = 17.5f, maxC = 32.5f, channel = WriteChannel.CAR),
        passengerTemp = CabinTempWrite(propId = 0x35600105, area = 4, minC = 17.5f, maxC = 32.5f, channel = WriteChannel.CAR),
        auto = CabinToggleWrite(propId = 0x35400104, area = 2, offRaw = 1, onRaw = 1, channel = WriteChannel.CAR),
        maxAc = CabinToggleWrite(propId = 0x3540010b, area = 2, offRaw = 1, onRaw = 1, channel = WriteChannel.CAR),
        recirc = CabinToggleWrite(propId = 0x35400108, area = 1, offRaw = 1, onRaw = 2, channel = WriteChannel.CAR),
        frontDefrost = CabinToggleWrite(propId = 0x33400103, area = 2, offRaw = 1, onRaw = 1, channel = WriteChannel.CAR),
        fan = CabinLevelWrite(propId = 0x35400109, area = 1, min = 1, max = 8),
        driverWindow = CabinPositionWrite(propId = 0x33400301, area = 16),
        passengerWindow = CabinPositionWrite(propId = 0x33400301, area = 64),
        rearLeftWindow = CabinPositionWrite(propId = 0x33400301, area = 256),
        rearRightWindow = CabinPositionWrite(propId = 0x33400301, area = 1024),
        sunshade = CabinPositionWrite(propId = 0x31400303, area = 0),
        climatePower = CabinCommandWrite(
            propId = 0x35400101, area = 1,
            onCommand = 2, offCommand = 1,
            stateId = 0x35400101, stateArea = 1,
            onState = 2, offState = 1,
            transientStates = emptySet(),
            parkedOnly = false
        ),
        lock = CabinCommandWrite(
            propId = 0x314003eb, area = 0,
            onCommand = 2, offCommand = 1,
            stateId = 0x314003eb, stateArea = 0,
            onState = 2, offState = 1,
            transientStates = emptySet(),
            parkedOnly = false
        ),
        tailgate = CabinCommandWrite(
            propId = 0x31400313, area = 0,
            onCommand = 2, offCommand = 1,
            stateId = 0x31400313, stateArea = 0,
            onState = 1, offState = 2,
            transientStates = setOf(3, 4),
            parkedOnly = true
        ),
        driverSeatHeat = CabinLevelWrite(propId = 0x3540010f, area = 1, min = 0, max = 3),
        passengerSeatHeat = CabinLevelWrite(propId = 0x3540010f, area = 4, min = 0, max = 3),
        driverSeatVent = CabinLevelWrite(propId = 0x35400111, area = 1, min = 0, max = 3),
        passengerSeatVent = CabinLevelWrite(propId = 0x35400111, area = 4, min = 0, max = 3),
        driverMassage = CabinCommandWrite(
            propId = 0x31400b2f, area = 0,
            onCommand = 2, offCommand = 1,
            stateId = 0x31400b2f, stateArea = 0,
            onState = 2, offState = 1,
            transientStates = emptySet(),
            parkedOnly = false
        ),
        passengerMassage = CabinCommandWrite(
            propId = 0x31400b2f, area = 4,
            onCommand = 2, offCommand = 1,
            stateId = 0x31400b2f, stateArea = 4,
            onState = 2, offState = 1,
            transientStates = emptySet(),
            parkedOnly = false
        ),
        driverMassageLevel = CabinLevelWrite(propId = 0x31400b31, area = 0, min = 1, max = 3),
        passengerMassageLevel = CabinLevelWrite(propId = 0x31400b31, area = 4, min = 1, max = 3),
        ambientLight = CabinCommandWrite(
            propId = 0x3140039a, area = 0,
            onCommand = 1, offCommand = 0,
            stateId = 0x3140039a, stateArea = 0,
            onState = 1, offState = 0,
            transientStates = emptySet(),
            parkedOnly = false
        ),
        ambientPattern = CabinLevelWrite(propId = 0x31400677, area = 0, min = 1, max = 3),
        ambientColour = CabinChoiceWrite(propId = 0x3140039b, choices = setOf(54, 42, 33, 12, 6, 1)),
        driveMode = CabinChoiceWrite(propId = 0x314003f5, choices = setOf(1, 2, 3)),
        driverMassageMode = CabinLevelWrite(propId = 0x31400b30, area = 0, min = 1, max = 8),
        passengerMassageMode = CabinLevelWrite(propId = 0x31400b30, area = 4, min = 1, max = 8),
        autoEmergencyBraking = CabinCommandWrite(
            propId = 0x3140040d, area = 0,
            onCommand = 2, offCommand = 1,
            stateId = 0x3140040d, stateArea = 0,
            onState = 2, offState = 1,
            transientStates = emptySet(),
            parkedOnly = false
        )
    )

    val DEEPAL_S05_C857: VehicleProfile = VehicleProfile(
        id = "deepal-s05-c857",
        soc = VehicleSignal(propId = 0x3140028c, area = 27, scaleToCanonical = 1, scaleDivisor = 1),
        range = VehicleSignal(propId = 0x314006c4, area = 0, scaleToCanonical = 1, scaleDivisor = 1),
        odometer = VehicleSignal(propId = 0x31600204, area = 0, scaleToCanonical = 1000, scaleDivisor = 1),
        drivingTime = null,
        remainingEnergy = null,
        tyrePressure = VehicleSignal(propId = 0x31410605, area = 0, scaleToCanonical = 4, scaleDivisor = 3),
        packKwh = null,
        setpointPlausibleC = 17.5f..32.5f,
        readChannel = VehicleType.VENDOR,
        origin = ProfileOrigin.BUILT_IN,
        cabin = CabinSignals(
            driverTemp = VehicleSignal(propId = 0x35600105, area = 1),
            passengerTemp = VehicleSignal(propId = 0x35600105, area = 4),
            fanSpeed = VehicleSignal(propId = 0x35400109, area = 1),
            acOn = VehicleSignal(propId = 0x35400102, area = 1),
            maxAcOn = VehicleSignal(propId = 0x3540010b, area = 1),
            recircOn = VehicleSignal(propId = 0x35400108, area = 1),
            autoOn = VehicleSignal(propId = 0x35400104, area = 1),
            dualOn = VehicleSignal(propId = 0x3540010d, area = 1),
            frontDefrost = VehicleSignal(propId = 0x33400103, area = 1),
            seatHeat = listOf(
                VehicleSignal(propId = 0x3540010f, area = 1),
                VehicleSignal(propId = 0x3540010f, area = 4)
            ),
            seatVent = listOf(
                VehicleSignal(propId = 0x35400111, area = 1),
                VehicleSignal(propId = 0x35400111, area = 4)
            ),
            windows = listOf(
                VehicleSignal(propId = 0x33400301, area = 16),
                VehicleSignal(propId = 0x33400301, area = 64),
                VehicleSignal(propId = 0x33400301, area = 256),
                VehicleSignal(propId = 0x33400301, area = 1024)
            ),
            doorLocks = listOf(
                VehicleSignal(propId = 0x314003eb, area = 0)
            ),
            recircLanguage = SignalLanguage.VENDOR_TRI_STATE_INVERTED,
            doorLockLanguage = SignalLanguage.VENDOR_TRI_STATE
        ),
        cabinWrites = DEEPAL_S05_CABIN_WRITES
    )

    val ALL_PROFILES = listOf(DEEPAL_S05_C857)
}
