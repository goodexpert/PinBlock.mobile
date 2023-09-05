package com.example.common

/**
 * Generate PinBlock
 *
 * Ref. https://www.eftlab.com/knowledge-base/complete-list-of-pin-blocks
 */

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmStatic
import kotlin.random.Random

object PinBlock {
    enum class Format(var format: String) {
        ISO_0("ISO-0 (Format 0)"),
        ISO_1("ISO-1 (Format 1)"),
        ISO_2("ISO-2 (Format 2)"),
        ISO_3("ISO-2 (Format 3)")
    }

    /**
     * Encode the pin block
     * @param format PIN block format
     * @param pan the primary account number in string
     * @param pin the personal identification number in string
     * @return the encoded pin block in string
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun encode(format: Format, pan: String, pin: String): String {
        return when (format) {
            Format.ISO_0 -> encodeISO_0(pan, pin)
            Format.ISO_3 -> encodeISO_3(pan, pin)
            else -> throw IllegalArgumentException("$format encoder is not implemented")
        }
    }

    private fun encodeISO_0(pan: String, pin: String): String {
        val blockDigits = 16
        val panDigits = 12

        // Prepare a PIN - L is length of the PIN, P is PIN digit, F is padding value “F”
        val pinBlock = MutableList(blockDigits) { 0xF }
        // Format code: 0
        pinBlock[0] = 0
        // Pin length
        pinBlock[1] = pin.length
        // Copy pin into pinDigits
        pin.map { it.digitToInt(16) }
            .forEachIndexed { i, value ->
                pinBlock[i + 2] = value
            }

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panBlock = pan.substring(pan.lastIndex - panDigits, pan.lastIndex)
            .map { it.digitToInt(16) }
            .toMutableList()
        panBlock.addAll(0, listOf(0, 0, 0, 0))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = panBlock[i] xor pinBlock[i]
        }
        return pinBlock.map{ it.toString(16) }.joinToString("")
    }

    private fun encodeISO_3(pan: String, pin: String): String {
        val blockDigits = 16
        val panDigits = 12

        // Prepare a PIN - L is length of the PIN, P is PIN digit, R is random value from X’0′ to X’F’
        val pinBlock = MutableList(blockDigits) { Random.nextInt(0, 16) }
        // Format code: 3
        pinBlock[0] = 3
        // Pin length
        pinBlock[1] = pin.length
        // Copy pin into pinDigits
        pin.map { it.digitToInt(16) }
            .forEachIndexed { i, value ->
                pinBlock[i + 2] = value
            }

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panBlock = pan.substring(pan.lastIndex - panDigits, pan.lastIndex)
            .map { it.digitToInt(16) }
            .toMutableList()
        panBlock.addAll(0, listOf(0, 0, 0, 0))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = panBlock[i] xor pinBlock[i]
        }
        return pinBlock.map{ it.toString(16) }.joinToString("")
    }

    /**
     * Encode the pin block
     * @param format PIN block format
     * @param pan the primary account number in byte array
     * @param panSize the PAN nibble size
     * @param pin the personal identification number in byte array
     * @param pinSize the PIN nibble size
     * @return the encoded pin block in byte array
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun encode(format: Format, pan: Array<Byte>, panSize: Int, pin: Array<Byte>, pinSize: Int): Array<Byte> {
        return when (format) {
            Format.ISO_0 -> encodeISO_0(pan, panSize, pin, pinSize)
            Format.ISO_3 -> encodeISO_3(pan, panSize, pin, pinSize)
            else -> throw IllegalArgumentException("$format encoder is not implemented")
        }
    }

    private fun encodeISO_0(pan: Array<Byte>, panSize: Int, pin: Array<Byte>, pinSize: Int): Array<Byte> {
        val blockDigits = 16 / 2
        val panDigits = 12 / 2

        // Prepare a PIN - L is length of the PIN, P is PIN digit, F is padding value “F”
        val pinBlock = MutableList(blockDigits) { 0xFF.toByte() }
        // First nibble is zero, second nibble is the length of the PIN
        pinBlock[0] = (0x00 or (pinSize and 0xF)).toByte()
        // Copy pin into pinDigits
        pin.forEachIndexed { i, byte -> pinBlock[i + 1] = byte }
        // If the pinSize is odd number, second nibble will be replaced as a padding value
        if (pinSize % 2 == 1) {
            val lastIndex = pinSize / 2 + 1
            pinBlock[lastIndex + 1] = pin[lastIndex] or 0xFF.toByte()
        }

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panLength = panSize / 2
        val panBlock = pan.copyOfRange(panLength - panDigits, panLength).toMutableList()
        panBlock.addAll(0, listOf(0x00, 0x00))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = (panBlock[i].toInt() xor pinBlock[i].toInt()).toByte()
        }
        return pinBlock.toTypedArray()
    }

    private fun encodeISO_3(pan: Array<Byte>, panSize: Int, pin: Array<Byte>, pinSize: Int): Array<Byte> {
        val blockDigits = 16 / 2
        val panDigits = 12 / 2

        // Prepare a PIN - L is length of the PIN, P is PIN digit, R is random value from X’0′ to X’F’
        val pinBlock = MutableList(blockDigits) { Random.nextInt(0, 256).toByte() }
        // First nibble is zero, second nibble is the length of the PIN
        pinBlock[0] = (0x00 or (pinSize and 0xF)).toByte()
        // Copy pin into pinDigits
        pin.forEachIndexed { i, byte -> pinBlock[i + 1] = byte }
        // If the pinSize is odd number, second nibble will be replaced as a padding value
        if (pinSize % 2 == 1) {
            val lastIndex = pinSize / 2 + 1
            pinBlock[lastIndex + 1] = pin[lastIndex] or (Random.nextInt(0, 256) or 0xF0).toByte()
        }

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panLength = panSize / 2
        val panBlock = pan.copyOfRange(panLength - panDigits, panLength).toMutableList()
        panBlock.addAll(0, listOf(0x00, 0x00))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = (panBlock[i].toInt() xor pinBlock[i].toInt()).toByte()
        }
        return pinBlock.toTypedArray()
    }

    /**
     * Decode the encoded pin block
     * @param format PIN block format
     * @param pan the primary account number in string
     * @param encodedPin the personal identification number in string
     * @return the decoded pin in string
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun decode(format: Format, pan: String, encodedPin: String): String {
        return when (format) {
            Format.ISO_0 -> decodeISO_0(pan, encodedPin)
            Format.ISO_3 -> decodeISO_3(pan, encodedPin)
            else -> throw IllegalArgumentException("$format decoder is not implemented")
        }
    }

    private fun decodeISO_0(pan: String, encodedPin: String): String {
        val blockDigits = 16
        val panDigits = 12

        // Prepare a PIN
        val pinBlock = encodedPin.map { it.digitToInt(16) }.toMutableList()

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panBlock = pan.substring(pan.lastIndex - panDigits, pan.lastIndex)
            .map { it.digitToInt(16) }
            .toMutableList()
        panBlock.addAll(0, listOf(0, 0, 0, 0))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = panBlock[i] xor pinBlock[i]
        }
        return pinBlock.joinToString("").substring(2, pinBlock[1] + 2)
    }

    private fun decodeISO_3(pan: String, encodedPin: String): String {
        val blockDigits = 16
        val panDigits = 12

        // Prepare a PIN
        val pinBlock = encodedPin.map { it.digitToInt(16) }.toMutableList()

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panBlock = pan.substring(pan.lastIndex - panDigits, pan.lastIndex)
            .map { it.digitToInt(16) }
            .toMutableList()
        panBlock.addAll(0, listOf(0, 0, 0, 0))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = panBlock[i] xor pinBlock[i]
        }
        return pinBlock.joinToString("").substring(2, pinBlock[1] + 2)
    }

    /**
     * Decode the encoded pin block
     * @param format PIN block format
     * @param pan the primary account number in byte array
     * @param panSize the PAN nibble size
     * @param encodedPin the personal identification number in byte array
     * @param pinSize the PIN nibble size
     * @return the decoded pin in byte array
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun decode(format: Format, pan: Array<Byte>, panSize: Int, encodedPin: Array<Byte>, pinSize: Int): Array<Byte> {
        return when (format) {
            Format.ISO_0 -> decodeISO_0(pan, panSize, encodedPin, pinSize)
            Format.ISO_3 -> decodeISO_3(pan, panSize, encodedPin, pinSize)
            else -> throw IllegalArgumentException("$format decoder is not implemented")
        }
    }

    private fun decodeISO_0(pan: Array<Byte>, panSize: Int, encodedPin: Array<Byte>, pinSize: Int): Array<Byte> {
        val blockDigits = 16 / 2
        val panDigits = 12 / 2

        // Prepare a PIN
        val pinBlock = encodedPin.toMutableList()

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panLength = panSize / 2
        val panBlock = pan.copyOfRange(panLength - panDigits, panLength).toMutableList()
        panBlock.addAll(0, listOf(0x00, 0x00))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = (panBlock[i].toInt() xor pinBlock[i].toInt()).toByte()
        }
        return pinBlock.toTypedArray().copyOfRange(1, (pinBlock[0] and 0x0F) / 2 + 1)
    }

    private fun decodeISO_3(pan: Array<Byte>, panSize: Int, encodedPin: Array<Byte>, pinSize: Int): Array<Byte> {
        val blockDigits = 16 / 2
        val panDigits = 12 / 2

        // Prepare a PIN
        val pinBlock = encodedPin.toMutableList()

        // Prepare PAN – take 12 rightmost digits of the primary account number (excluding the check digit)
        val panLength = panSize / 2
        val panBlock = pan.copyOfRange(panLength - panDigits, panLength).toMutableList()
        panBlock.addAll(0, listOf(0x00, 0x00))

        // XOR both values
        for (i in 0 until blockDigits) {
            pinBlock[i] = (panBlock[i].toInt() xor pinBlock[i].toInt()).toByte()
        }
        return pinBlock.toTypedArray().copyOfRange(1, (pinBlock[0] and 0x0F) / 2 + 1)
    }
}