package com.example.common

import kotlin.test.Test
import kotlin.test.assertEquals

class PinBlockTests {

    companion object {
        const val PAN = "43219876543210987"
        const val PIN = "1234"
        const val ENCODED_PIN_0 = "0412AC89ABCDEF67"

        val PAN_BYTES = arrayOf(0x43, 0x21, 0x98, 0x76, 0x54, 0x32, 0x10, 0x98, 0x70).map { it.toByte() }.toTypedArray()
        val PAN_SIZE = 17
        val PIN_BYTES = arrayOf(0x12, 0x34).map { it.toByte() }.toTypedArray()
        val PIN_SIZE = 4
        val ENCODED_PIN_BYTES_0 = arrayOf(0x04, 0x12, 0xAC, 0x89, 0xAB, 0xCD, 0xEF, 0x67).map { it.toByte() }.toTypedArray()
    }

    @Test
    fun testISOFormat0() {
        val encodedPin = PinBlock.encode(PinBlock.Format.ISO_0, PAN, PIN)
        assertEquals(ENCODED_PIN_0, encodedPin.uppercase(), "We're expected $ENCODED_PIN_0, but it returns $encodedPin")

        val pin = PinBlock.decode(PinBlock.Format.ISO_0, PAN, encodedPin)
        assertEquals(PIN, pin.uppercase(), "We're expected $PIN, but it returns $pin")

        val encodedPinBytes = PinBlock.encode(PinBlock.Format.ISO_0, PAN_BYTES, PAN_SIZE, PIN_BYTES, PIN_SIZE)
        assertEquals(true, ENCODED_PIN_BYTES_0 contentEquals encodedPinBytes, "We're expected $ENCODED_PIN_BYTES_0, but it returns $encodedPinBytes")

        val pinBytes = PinBlock.decode(PinBlock.Format.ISO_0, PAN_BYTES, PAN_SIZE, encodedPinBytes)
        assertEquals(true, PIN_BYTES contentEquals pinBytes, "We're expected $PIN_BYTES, but it returns $pinBytes")
    }

    @Test
    fun testISOFormat3() {
        val encodedPin = PinBlock.encode(PinBlock.Format.ISO_3, PAN, PIN)
        val pin = PinBlock.decode(PinBlock.Format.ISO_3, PAN, encodedPin)
        assertEquals(PIN, pin.uppercase(), "We're expected $PIN, but it returns $pin")

        val encodedPinBytes = PinBlock.encode(PinBlock.Format.ISO_3, PAN_BYTES, PAN_SIZE, PIN_BYTES, PIN_SIZE)
        val pinBytes = PinBlock.decode(PinBlock.Format.ISO_3, PAN_BYTES, PAN_SIZE, encodedPinBytes)
        assertEquals(true, PIN_BYTES contentEquals pinBytes, "We're expected $PIN_BYTES, but it returns $pinBytes")
    }
}