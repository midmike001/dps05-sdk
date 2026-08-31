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
        assertEquals(0x31600204, DeepalS05Property.PROP_VEHICLE_SPEED)
        assertEquals(0x3140028c, DeepalS05Property.PROP_GEAR_SELECTION)
        assertEquals(0x314006c4, DeepalS05Property.PROP_BATTERY_SOC)
        assertEquals(0x31410605, DeepalS05Property.PROP_REMAINING_RANGE)
        assertEquals(0x35600105, DeepalS05Property.PROP_HVAC_TEMP_SET)
        assertEquals(0x3540010f, DeepalS05Property.PROP_SEAT_HEATING)
        assertEquals(0x35400111, DeepalS05Property.PROP_SEAT_VENTILATION)
        assertEquals(0x31400b2f, DeepalS05Property.PROP_SEAT_MASSAGE_TOGGLE)
        assertEquals(0x33400301, DeepalS05Property.PROP_WINDOW_MOVE)
        assertEquals(0x31400313, DeepalS05Property.PROP_SUNROOF_SHADE)
        assertEquals(0x3140040d, DeepalS05Property.PROP_TAILGATE)
        assertEquals(0x15400505, DeepalS05Property.PROP_DOOR_LOCK)
        assertEquals(0x314006c6, DeepalS05Property.PROP_BATTERY_PRECONDITIONING)
        assertEquals(0x31400277, DeepalS05Property.PROP_RAIN_SENSOR_STATE)
    }

    @Test
    fun testIncallTransactionCodes() {
        assertEquals("com.incall.SVR_MNG_SERVICE", DeepalS05Property.INCALL_SVR_MNG_SERVICE)
        assertEquals("com.incall.double.INTERACTIVE_SERVICE", DeepalS05Property.INCALL_DOUBLE_INTERACTIVE_SERVICE)
        assertEquals(0x16, DeepalS05Property.INCALL_CMD_NAVIGATE_STATUS)
        assertEquals(0x18, DeepalS05Property.INCALL_CMD_NAVIGATE_TURN_INFO)
        assertEquals(0x1a, DeepalS05Property.INCALL_CMD_NAVIGATE_ROAD_INFO)
        assertEquals(0x1b, DeepalS05Property.INCALL_CMD_NAVIGATE_REMAIN_INFO)
        assertEquals(0x3f, DeepalS05Property.INCALL_CMD_REQUEST_NAVI_FOCUS)
        assertEquals(0x40, DeepalS05Property.INCALL_CMD_ABANDON_NAVI_FOCUS)
    }

    @Test
    fun testTemperatureBounds() {
        assertTrue(DeepalS05Property.TEMP_MIN_C == 17.5f)
        assertTrue(DeepalS05Property.TEMP_MAX_C == 32.5f)
    }
}
