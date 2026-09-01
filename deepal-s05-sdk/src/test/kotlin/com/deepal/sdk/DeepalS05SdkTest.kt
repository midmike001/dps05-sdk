package com.deepal.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepalS05SdkTest {

    @Test
    fun testPlatformConstants() {
        assertEquals("deepal-s05", DeepalS05Property.PLATFORM_ID)
        assertEquals("C857", DeepalS05Property.INTERNAL_CODE)
        assertEquals("virtualcar_service", DeepalS05Property.VIRTUALCAR_SERVICE)
        assertEquals("virtualcar_property_service", DeepalS05Property.VIRTUALCAR_PROPERTY_SERVICE)
        assertEquals("com.openos.virtualcar.IVirturalCarProperty", DeepalS05Property.DESCRIPTOR_PROPERTY)
    }

    @Test
    fun testPropertyIds() {
        assertEquals(0x11600207, DeepalS05Property.PROP_VEHICLE_SPEED_VHAL)
        assertEquals(0x31600202, DeepalS05Property.PROP_VEHICLE_SPEED_VC)
        assertEquals(0x31400231, DeepalS05Property.PROP_GEAR_SELECTION)
        assertEquals(0x3140028c, DeepalS05Property.PROP_BATTERY_SOC)
        assertEquals(0x1b, DeepalS05Property.AREA_SOC)
        assertEquals(0x31400501, DeepalS05Property.PROP_REMAINING_RANGE_EV_DTE)
        assertEquals(0x31600205, DeepalS05Property.PROP_REMAINING_RANGE_DISP_DTE)
        assertEquals(0x31600204, DeepalS05Property.PROP_ODOMETER)
        assertEquals(1000f, DeepalS05Property.ODOMETER_SCALE_DIVISOR, 0.001f)

        // Tire pressure & areas
        assertEquals(0x37600211, DeepalS05Property.PROP_TIRE_PRESSURE)
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

        // Tailgate & Drive Mode
        assertEquals(0x31400314, DeepalS05Property.PROP_TAILGATE)
        assertEquals(0x3140040d, DeepalS05Property.PROP_DRIVE_MODE)

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

        // Seats & Comfort
        assertEquals(0x3540010f, DeepalS05Property.PROP_SEAT_HEATING)
        assertEquals(0x1540050b, DeepalS05Property.PROP_SEAT_HEATING_CPM)
        assertEquals(0x35400111, DeepalS05Property.PROP_SEAT_VENTILATION)
        assertEquals(0x31400b2f, DeepalS05Property.PROP_SEAT_MASSAGE_TOGGLE)
        assertEquals(0x31400b31, DeepalS05Property.PROP_SEAT_MASSAGE_MODE)
        assertEquals(0x31400b30, DeepalS05Property.PROP_SEAT_MASSAGE_LEVEL)
        assertEquals(0x314003eb, DeepalS05Property.PROP_STEERING_WHEEL_HEAT)

        // Windows & Body
        assertEquals(0x33400301, DeepalS05Property.PROP_WINDOW_MOVE)
        assertEquals(0x31400303, DeepalS05Property.PROP_WINDOW_LOCK)
        assertEquals(0x31400313, DeepalS05Property.PROP_SUNROOF_SHADE)
        assertEquals(0x314003eb, DeepalS05Property.PROP_DOOR_LOCK)
        assertEquals(0x314006c6, DeepalS05Property.PROP_BATTERY_PRECONDITIONING)
        assertEquals(0x31400277, DeepalS05Property.PROP_RAIN_SENSOR_STATE)

        // Audio & Outside Speaker
        assertEquals(0x66, DeepalS05Property.AUDIO_EVENT_OUTSIDE_MUSIC)
    }

    @Test
    fun testIncallTransactionCodes() {
        assertEquals("com.incall.SVR_MNG_SERVICE", DeepalS05Property.INCALL_SVR_MNG_SERVICE)
        assertEquals("com.incall.double.INTERACTIVE_SERVICE", DeepalS05Property.INCALL_DOUBLE_INTERACTIVE_SERVICE)
        assertEquals(0x16, DeepalS05Property.INCALL_CMD_NAVIGATE_STATUS)
        assertEquals(0x17, DeepalS05Property.INCALL_CMD_NAVIGATE_CROSS_ROAD)
        assertEquals(0x18, DeepalS05Property.INCALL_CMD_NAVIGATE_TURN_INFO)
        assertEquals(0x19, DeepalS05Property.INCALL_CMD_NAVIGATE_LANE_INFO)
        assertEquals(0x1a, DeepalS05Property.INCALL_CMD_NAVIGATE_ROAD_INFO)
        assertEquals(0x1b, DeepalS05Property.INCALL_CMD_NAVIGATE_REMAIN_INFO)
        assertEquals(0x1c, DeepalS05Property.INCALL_CMD_NAVIGATE_CAMERA_INFO)
        assertEquals(0x3f, DeepalS05Property.INCALL_CMD_REQUEST_NAVI_FOCUS)
        assertEquals(0x40, DeepalS05Property.INCALL_CMD_ABANDON_NAVI_FOCUS)
    }

    @Test
    fun testTemperatureBounds() {
        assertTrue(DeepalS05Property.TEMP_MIN_C == 17.5f)
        assertTrue(DeepalS05Property.TEMP_MAX_C == 32.5f)
    }
}
