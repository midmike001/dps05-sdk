package com.deepal.sdk

import com.deepal.sdk.device.DetectionField
import com.deepal.sdk.device.DeviceInfo
import com.deepal.sdk.device.FingerprintRule
import com.deepal.sdk.device.VehicleProfiles
import com.deepal.sdk.vehicle.BuiltInProfiles
import com.deepal.sdk.vehicle.CabinGearRead
import com.deepal.sdk.vehicle.CabinLevelWrite
import com.deepal.sdk.vehicle.CabinPositionWrite
import com.deepal.sdk.vehicle.CabinTempWrite
import com.deepal.sdk.vehicle.VehicleConfigurations
import com.deepal.sdk.vehicle.VendorType
import com.deepal.sdk.vehicle.WriteChannel
import com.deepal.sdk.vehicle.WriteIntent
import com.deepal.sdk.vehicle.WritePlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepalS05SdkTest {

    @Test
    fun testPlatformConstants() {
        assertEquals("virtualcar_service", DeepalS05Property.VIRTUALCAR_SERVICE)
        assertEquals("virtualcar_property_service", DeepalS05Property.VIRTUALCAR_PROPERTY_SERVICE)
        assertEquals("com.openos.virtualcar.IVirturalCarProperty", DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR_PROPERTY)
        assertEquals("com.openos.virtualcar.IVirtualCar", DeepalS05Property.DESCRIPTOR_VIRTUAL_CAR)
        assertEquals("wt.vehiclesetting", DeepalS05Property.VEHICLE_SETTING_SERVICE)
        assertEquals("com.openos.settings.vehiclesettings.IVehicleSettingInterface", DeepalS05Property.DESCRIPTOR_VEHICLE_SETTING)
    }

    @Test
    fun testPropertyIds() {
        assertEquals(0x11600207, DeepalS05Property.PROP_VEHICLE_SPEED_VHAL)
        assertEquals(0x31600202, DeepalS05Property.PROP_VEHICLE_SPEED_VC)
        assertEquals(0x31400231, DeepalS05Property.PROP_GEAR_SELECTION)
        assertEquals(0x11400400, DeepalS05Property.PROP_GEAR_SELECTION_VHAL)
        assertEquals(0x3140028c, DeepalS05Property.PROP_BATTERY_SOC)
        assertEquals(0x1b, DeepalS05Property.AREA_SOC)
        assertEquals(0x314006c4, DeepalS05Property.PROP_REMAINING_RANGE_C857)
        assertEquals(0x31400501, DeepalS05Property.PROP_REMAINING_RANGE_EV_DTE)
        assertEquals(0x31600205, DeepalS05Property.PROP_REMAINING_RANGE_DISP_DTE)
        assertEquals(0x31600204, DeepalS05Property.PROP_ODOMETER)
        assertEquals(1000f, DeepalS05Property.ODOMETER_SCALE_DIVISOR, 0.001f)

        // CAN Bus signal definitions for Deepal S05 C857
        assertEquals(0x21410605, DeepalS05Property.PROP_CAN_SPEED_GEAR)
        assertEquals(0x31600204, DeepalS05Property.PROP_CAN_STEERING)
        assertEquals(0x31410605, DeepalS05Property.PROP_CAN_HVAC_TPMS)

        // Tire pressure & areas
        assertEquals(0x37600211, DeepalS05Property.PROP_TIRE_PRESSURE)
        assertEquals(0x31410605, DeepalS05Property.PROP_TIRE_PRESSURE_LEGACY)
        assertEquals(0x01, DeepalS05Property.AREA_TIRE_FL)
        assertEquals(0x02, DeepalS05Property.AREA_TIRE_FR)
        assertEquals(0x04, DeepalS05Property.AREA_TIRE_RL)
        assertEquals(0x08, DeepalS05Property.AREA_TIRE_RR)

        // Doors & areas
        assertEquals(0x36400311, DeepalS05Property.PROP_DOORS)
        assertEquals(0x01, DeepalS05Property.AREA_DOOR_FL)
        assertEquals(0x04, DeepalS05Property.AREA_DOOR_FR)
        assertEquals(0x10, DeepalS05Property.AREA_DOOR_RL)
        assertEquals(0x40, DeepalS05Property.AREA_DOOR_RR)

        // Tailgate & Drive Mode & AEB
        assertEquals(0x31400313, DeepalS05Property.PROP_TAILGATE_CONTROL)
        assertEquals(0x31400314, DeepalS05Property.PROP_TAILGATE_STATUS)
        assertEquals(0x31400314, DeepalS05Property.PROP_TAILGATE)
        assertEquals(0x3140040d, DeepalS05Property.PROP_DRIVE_MODE)
        assertEquals(0x314003f5, DeepalS05Property.PROP_DRIVE_MODE_CHOICE)
        assertEquals(0x3140040d, DeepalS05Property.PROP_AEB_COMMAND)
        assertEquals(0x31400244, DeepalS05Property.PROP_AEB_SWITCH)

        // wt.vehiclesetting Sunshade, Sunroof, HUD & Chassis Transacts
        assertEquals(0x40, DeepalS05Property.TRANSACT_SET_SUNSHADE_POS)
        assertEquals(0x3f, DeepalS05Property.TRANSACT_GET_SUNSHADE_POS)
        assertEquals(0x3b, DeepalS05Property.TRANSACT_SET_SUNROOF_POS)
        assertEquals(0x3a, DeepalS05Property.TRANSACT_GET_SUNROOF_POS)
        assertEquals(0x3c, DeepalS05Property.TRANSACT_SET_SUNROOF_TILT)
        assertEquals(0x43, DeepalS05Property.TRANSACT_SET_SUNROOF_RAIN_DETECT)
        assertEquals(0x42, DeepalS05Property.TRANSACT_GET_SUNROOF_RAIN_DETECT)
        assertEquals(0x4f, DeepalS05Property.TRANSACT_SET_SMART_LEAVING_LOCK)
        assertEquals(0x4e, DeepalS05Property.TRANSACT_GET_SMART_LEAVING_LOCK)
        assertEquals(0x4d, DeepalS05Property.TRANSACT_SET_SMART_WELCOME_UNLOCK)
        assertEquals(0x4c, DeepalS05Property.TRANSACT_GET_SMART_WELCOME_UNLOCK)
        assertEquals(0x59, DeepalS05Property.TRANSACT_SET_MIRROR_AUTOFOLD)
        assertEquals(0x58, DeepalS05Property.TRANSACT_GET_MIRROR_AUTOFOLD)
        assertEquals(0x5b, DeepalS05Property.TRANSACT_SET_WIRELESS_CHARGE)
        assertEquals(0x5a, DeepalS05Property.TRANSACT_GET_WIRELESS_CHARGE)
        assertEquals(0x83, DeepalS05Property.TRANSACT_SET_HUD_SWITCH)
        assertEquals(0x82, DeepalS05Property.TRANSACT_GET_HUD_SWITCH)
        assertEquals(0x85, DeepalS05Property.TRANSACT_SET_HUD_BRIGHT)
        assertEquals(0x84, DeepalS05Property.TRANSACT_GET_HUD_BRIGHT)
        assertEquals(0x87, DeepalS05Property.TRANSACT_SET_HUD_HEIGHT)
        assertEquals(0x86, DeepalS05Property.TRANSACT_GET_HUD_HEIGHT)
        assertEquals(0x8f, DeepalS05Property.TRANSACT_SET_HUD_DISPLAY_PHONE)
        assertEquals(0x8e, DeepalS05Property.TRANSACT_GET_HUD_DISPLAY_PHONE)
        assertEquals(0x91, DeepalS05Property.TRANSACT_SET_HUD_DISPLAY_NAV)
        assertEquals(0x90, DeepalS05Property.TRANSACT_GET_HUD_DISPLAY_NAV)

        // Trip & Energy consumption
        assertEquals(0x314005a6, DeepalS05Property.PROP_THIS_TRIP_ELEC_AVG_CONSUMPTION)
        assertEquals(0x314005ce, DeepalS05Property.PROP_THIS_TRIP_OIL_AVG_CONSUMPTION)

        // Climate
        assertEquals(0x35600105, DeepalS05Property.PROP_HVAC_TEMP_SET)
        assertEquals(0x35400101, DeepalS05Property.PROP_HVAC_POWER_ON)
        assertEquals(0x35400102, DeepalS05Property.PROP_HVAC_AC_ON)
        assertEquals(0x35400104, DeepalS05Property.PROP_HVAC_AUTO)
        assertEquals(0x35400108, DeepalS05Property.PROP_HVAC_RECIRC)
        assertEquals(0x35400109, DeepalS05Property.PROP_HVAC_FAN_SPEED)
        assertEquals(0x3540010b, DeepalS05Property.PROP_HVAC_MAX_AC)
        assertEquals(0x3540010d, DeepalS05Property.PROP_HVAC_SYNC)
        assertEquals(0x33400103, DeepalS05Property.PROP_HVAC_DEFROST_FRONT)
        assertEquals(0x3540010c, DeepalS05Property.PROP_HVAC_DEFROST_REAR)
        assertEquals(0x38600112, DeepalS05Property.PROP_HVAC_INTERNAL_TEMP)

        // Seats & Comfort (Verified against G2/E0)
        assertEquals(0x3540010f, DeepalS05Property.PROP_SEAT_HEATING)
        assertEquals(0x1540050b, DeepalS05Property.PROP_SEAT_HEATING_CPM)
        assertEquals(0x35400111, DeepalS05Property.PROP_SEAT_VENTILATION)
        assertEquals(0x31400b2f, DeepalS05Property.PROP_SEAT_MASSAGE_TOGGLE)
        assertEquals(0x31400b30, DeepalS05Property.PROP_SEAT_MASSAGE_MODE) // Pattern 1..8
        assertEquals(0x31400b31, DeepalS05Property.PROP_SEAT_MASSAGE_LEVEL) // Intensity 1..3
        assertEquals(0x314003eb, DeepalS05Property.PROP_STEERING_WHEEL_HEAT)

        // Ambient lighting
        assertEquals(0x3140039a, DeepalS05Property.PROP_AMBIENT_LIGHT)
        assertEquals(0x3140039b, DeepalS05Property.PROP_AMBIENT_LIGHT_BRIGHTNESS)
        assertEquals(0x31400677, DeepalS05Property.PROP_AMBIENT_LIGHT_PATTERN)
        assertTrue(DeepalS05Property.AMBIENT_COLOR_CHOICES.contains(54))
        assertTrue(DeepalS05Property.AMBIENT_COLOR_CHOICES.contains(1))

        // Windows & Body
        assertEquals(0x33400300, DeepalS05Property.PROP_WINDOW_POS)
        assertEquals(0x31400300, DeepalS05Property.PROP_WINDOW_POS_VC)
        assertEquals(0x33400301, DeepalS05Property.PROP_WINDOW_MOVE)
        assertEquals(0x31400303, DeepalS05Property.PROP_WINDOW_LOCK)
        assertEquals(0x31400303, DeepalS05Property.PROP_SUNSHADE_POS_VC)
        assertEquals(0x314003eb, DeepalS05Property.PROP_DOOR_LOCK)
        assertEquals(0x314006c6, DeepalS05Property.PROP_BATTERY_PRECONDITIONING)
        assertEquals(0x31400277, DeepalS05Property.PROP_RAIN_SENSOR_STATE)

        // Audio & Outside Speaker
        assertEquals(0x66, DeepalS05Property.AUDIO_EVENT_OUTSIDE_MUSIC)

        // Wind direction & Drive Mode
        assertEquals(0x35400107, DeepalS05Property.PROP_HVAC_FAN_DIRECTION)
        assertEquals(8, DeepalS05Property.WIND_DIRECTION_DEFROST)
        assertEquals(9, DeepalS05Property.WIND_DIRECTION_FACE)
        assertEquals(10, DeepalS05Property.WIND_DIRECTION_FEET)
        assertEquals(11, DeepalS05Property.WIND_DIRECTION_FACE_FEET)

        assertEquals(1, DeepalS05Property.DRIVE_MODE_COMFORT)
        assertEquals(2, DeepalS05Property.DRIVE_MODE_SPORT)
        assertEquals(3, DeepalS05Property.DRIVE_MODE_ECO)
        assertEquals(4, DeepalS05Property.DRIVE_MODE_CUSTOM)

        assertEquals(4, DeepalS05Property.GEAR_RAW_PARK)
        assertEquals(1, DeepalS05Property.GEAR_RAW_NEUTRAL)
        assertEquals(2, DeepalS05Property.GEAR_RAW_REVERSE)
        assertEquals(3, DeepalS05Property.GEAR_RAW_DRIVE)
    }

    @Test
    fun testDeviceFingerprintDetection() {
        // Deepal S05 (Model "C857")
        val s05Info = DeviceInfo(model = "C857", product = "c857_car", manufacturer = "Changan")
        val s05Detected = VehicleProfiles.detectCurrent(s05Info)
        assertEquals("deepal-s05", s05Detected.id)
        assertEquals("Deepal S05", s05Detected.label)
        assertEquals("deepal-s05-c857", s05Detected.deviceProfileId)
        assertTrue(s05Detected.isSupported)

        // Generic fallback
        val genericInfo = DeviceInfo(model = "UnknownModel", board = "other")
        val genericDetected = VehicleProfiles.detectCurrent(genericInfo)
        assertEquals("generic", genericDetected.id)

        // Config resolution
        val c857Config = VehicleProfiles.resolveVehicleConfig("deepal-s05")
        assertNotNull(c857Config)
        assertEquals("deepal-s05-c857", c857Config.id)
        assertEquals(VendorType.VENDOR, c857Config.vendorType)
        assertEquals(0x1030, c857Config.flags)

        val c857Profile = VehicleProfiles.resolveVehicleProfile("deepal-s05")
        assertEquals("deepal-s05-c857", c857Profile.id)
    }

    @Test
    fun testBuiltInProfilesGroundTruth() {
        val s05 = BuiltInProfiles.DEEPAL_S05_C857
        assertEquals("deepal-s05-c857", s05.id)
        assertEquals(0x3140028c, s05.soc.propId)
        assertEquals(27, s05.soc.area)
        assertEquals(0x314006c4, s05.range.propId)
        assertEquals(0x31600204, s05.odometer.propId)
        assertEquals(1000, s05.odometer.scaleToCanonical)

        val writes = BuiltInProfiles.DEEPAL_S05_CABIN_WRITES
        assertEquals(0x35600105, writes.driverTemp?.propId)
        assertEquals(1, writes.driverTemp?.area)
        assertEquals(17.5f, writes.driverTemp?.minC ?: 0f, 0.01f)
        assertEquals(32.5f, writes.driverTemp?.maxC ?: 0f, 0.01f)
        assertEquals(0x35400109, writes.fan?.propId)
        assertEquals(1, writes.fan?.min)
        assertEquals(8, writes.fan?.max)
        assertEquals(0x31400b30, writes.driverMassageMode?.propId) // Pattern 1..8
        assertEquals(8, writes.driverMassageMode?.max)
        assertEquals(0x31400b31, writes.driverMassageLevel?.propId) // Intensity 1..3
        assertEquals(3, writes.driverMassageLevel?.max)
        assertEquals(0x31400677, writes.ambientPattern?.propId)
        assertEquals(0x3140040d, writes.autoEmergencyBraking?.propId)
    }

    @Test
    fun testWriteIntentPlanning() {
        val client = DeepalS05Client()

        // 1. TempSet planning
        val tempWrite = CabinTempWrite(propId = 0x35600105, area = 1, minC = 17.5f, maxC = 32.5f)
        val tempIntent = WriteIntent.TempSet(CabinGearRead(), tempWrite, targetC = 22.0f)
        val tempPlan = client.planWrite(tempIntent, currentValue = 22.0f)
        assertTrue(tempPlan is WritePlan.AlreadyThere)

        val tempPlanProceed = client.planWrite(tempIntent, currentValue = 24.0f)
        assertTrue(tempPlanProceed is WritePlan.Proceed)
        assertEquals(22.0f, (tempPlanProceed as WritePlan.Proceed).valueToWrite.toFloat(), 0.01f)

        // 2. Choice planning
        val choiceWrite = BuiltInProfiles.DEEPAL_S05_CABIN_WRITES.ambientColour!!
        val validChoiceIntent = WriteIntent.Choice(CabinGearRead(), choiceWrite, target = 54)
        val choicePlan = client.planWrite(validChoiceIntent)
        assertTrue(choicePlan is WritePlan.Proceed)

        val invalidChoiceIntent = WriteIntent.Choice(CabinGearRead(), choiceWrite, target = 999)
        val invalidChoicePlan = client.planWrite(invalidChoiceIntent)
        assertTrue(invalidChoicePlan is WritePlan.Refused)

        // 3. Command planning (parked only check)
        val tailgateWrite = BuiltInProfiles.DEEPAL_S05_CABIN_WRITES.tailgate!!
        val tailgateIntent = WriteIntent.Command(CabinGearRead(), tailgateWrite, desiredOn = true)
        val tailgatePlan = client.planWrite(tailgateIntent) // default telemetry gear is "P"
        assertTrue(tailgatePlan is WritePlan.Proceed)

        // 4. Level step planning
        val fanWrite = CabinLevelWrite(propId = 0x35400109, area = 1, min = 1, max = 8)
        val fanIntent = WriteIntent.LevelStep(CabinGearRead(), fanWrite, targetLevel = 10) // out of range
        val fanPlan = client.planWrite(fanIntent)
        assertTrue(fanPlan is WritePlan.Proceed)
        assertEquals(8, (fanPlan as WritePlan.Proceed).valueToWrite.toInt()) // clamped to 8
    }

    @Test
    fun testTelemetryDefaults() {
        val telemetry = DeepalS05Telemetry()
        assertEquals("P", telemetry.gear)
        assertEquals("COMFORT", telemetry.driveMode)
        assertEquals(9, telemetry.windDirection)
        assertEquals(false, telemetry.doorHandlesExpanded)
        assertEquals(false, telemetry.mirrorsFolded)
        assertEquals(false, telemetry.isTailgateOpen)
        assertEquals(false, telemetry.isSunroofOpen)
        assertEquals(false, telemetry.isSeatMassageOn)
        assertEquals(1, telemetry.seatMassageMode)
        assertEquals(1, telemetry.seatMassageLevel)
        assertEquals(false, telemetry.isPassengerSeatMassageOn)
        assertEquals(1, telemetry.ambientLightPattern)
        assertEquals(54, telemetry.ambientLightColorChoice)
        assertEquals("deepal-s05-c857", telemetry.detectedProfileId)
    }

    @Test
    fun testIncallTransactionCodes() {
        assertEquals("com.incall.SVR_MNG_SERVICE", DeepalS05Property.INCALL_SVR_MNG_SERVICE)
        assertEquals("com.incall.double.INTERACTIVE_SERVICE", DeepalS05Property.INCALL_DOUBLE_INTERACTIVE_SERVICE)
        assertEquals(0x04, DeepalS05Property.INCALL_CMD_LOCATION_INFO)
        assertEquals(0x0e, DeepalS05Property.INCALL_CMD_WEATHER_TIME_INFO)
        assertEquals(0x16, DeepalS05Property.INCALL_CMD_NAVIGATE_STATUS)
        assertEquals(0x17, DeepalS05Property.INCALL_CMD_CROSS_ROAD)
        assertEquals(0x18, DeepalS05Property.INCALL_CMD_TURN_INFO)
        assertEquals(0x19, DeepalS05Property.INCALL_CMD_LANE_INFO)
        assertEquals(0x1a, DeepalS05Property.INCALL_CMD_ROAD_INFO)
        assertEquals(0x1b, DeepalS05Property.INCALL_CMD_REMAIN_INFO)
        assertEquals(0x1c, DeepalS05Property.INCALL_CMD_CAMERA_INFO)
        assertEquals(0x23, DeepalS05Property.INCALL_CMD_VR_STATUS)
        assertEquals(0x24, DeepalS05Property.INCALL_CMD_VR_RESULT)
        assertEquals(0x25, DeepalS05Property.INCALL_CMD_MEDIA_SOURCE)
        assertEquals(0x26, DeepalS05Property.INCALL_CMD_MEDIA_TIME)
        assertEquals(0x27, DeepalS05Property.INCALL_CMD_MEDIA_ALBUM_ICON)
        assertEquals(0x2a, DeepalS05Property.INCALL_CMD_CALL_INFO)
        assertEquals(0x2b, DeepalS05Property.INCALL_CMD_CALL_TIME)
        assertEquals(0x2c, DeepalS05Property.INCALL_CMD_CALL_AVATAR)
        assertEquals(0x32, DeepalS05Property.INCALL_CMD_PERCENT)
        assertEquals(0x3f, DeepalS05Property.INCALL_CMD_REQUEST_FOCUS)
        assertEquals(0x40, DeepalS05Property.INCALL_CMD_ABANDON_FOCUS)
    }

    @Test
    fun testTinnovePolymericConstants() {
        assertEquals("polymeric_service", TinnovePolymericClient.SERVICE_NAME)
        assertEquals("com.tinnove.polymericservice.IPolymericService", TinnovePolymericClient.DESCRIPTOR)
        assertEquals(1, TinnovePolymericClient.TRANSACT_CALL_METHOD)
        assertEquals(2, TinnovePolymericClient.TRANSACT_ASYNC_CALL_METHOD)
        assertEquals(3, TinnovePolymericClient.TRANSACT_REGISTER_EVENT_LISTENER)
        assertEquals(4, TinnovePolymericClient.TRANSACT_UNREGISTER_EVENT_LISTENER)
        assertEquals(0x44d, TinnovePolymericClient.ABILITY_CAR_CONTROL)
        assertEquals(0x44e, TinnovePolymericClient.ABILITY_CAR_INFO)
        assertEquals(0x3eb, TinnovePolymericClient.METHOD_GET_VALUE)
        assertEquals(0x3ec, TinnovePolymericClient.METHOD_SET_VALUE)
    }
}
