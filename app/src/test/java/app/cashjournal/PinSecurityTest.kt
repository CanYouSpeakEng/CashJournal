package app.cashjournal

import app.cashjournal.data.preferences.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class PinSecurityTest {

    @Test
    fun testSaltedPinHashFormat() {
        val pin = "1234"
        val saltedHash = UserPreferencesRepository.createSaltedPinHash(pin)
        val parts = saltedHash.split(":")

        assertEquals("Salted hash must have salt and hash parts separated by colon", 2, parts.size)
        assertEquals("Salt must be exactly 32 hex characters (16 bytes)", 32, parts[0].length)
        assertEquals("SHA-256 output must be exactly 64 hex characters (32 bytes)", 64, parts[1].length)
        assertTrue("Salt must contain only valid hex characters", parts[0].all { it.isDigit() || it in 'a'..'f' })
        assertTrue("Hash must contain only valid hex characters", parts[1].all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun testSaltRandomness() {
        val pin = "4321"
        val hashA = UserPreferencesRepository.createSaltedPinHash(pin)
        val hashB = UserPreferencesRepository.createSaltedPinHash(pin)

        assertNotEquals("Different hash computations must generate different salts and results", hashA, hashB)
    }

    @Test
    fun testVerifyCorrectSaltedPin() {
        val pin = "7890"
        val storedHash = UserPreferencesRepository.createSaltedPinHash(pin)

        assertTrue("Correct PIN must verify successfully", UserPreferencesRepository.verifyPin(pin, storedHash))
        assertFalse("Wrong PIN must fail verification", UserPreferencesRepository.verifyPin("7891", storedHash))
        assertFalse("Different PIN must fail verification", UserPreferencesRepository.verifyPin("0000", storedHash))
        assertFalse("Empty PIN must fail verification", UserPreferencesRepository.verifyPin("", storedHash))
    }

    @Test
    fun testVerifyLegacyUnsaltedSha256Fallback() {
        val pin = "9999"
        val md = MessageDigest.getInstance("SHA-256")
        val legacyHash = md.digest(pin.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        assertEquals(64, legacyHash.length)
        assertTrue("Legacy 64-hex unsalted hash must verify successfully for backward compatibility", UserPreferencesRepository.verifyPin(pin, legacyHash))
        assertFalse("Wrong PIN against legacy hash must fail verification", UserPreferencesRepository.verifyPin("8888", legacyHash))
    }

    @Test
    fun testVerifyMalformedHashSafely() {
        assertFalse("Empty stored hash must return false without exception", UserPreferencesRepository.verifyPin("1234", ""))
        assertFalse("Malformed text without colon must return false", UserPreferencesRepository.verifyPin("1234", "corrupted_hash"))
        assertFalse("Invalid salt length must return false", UserPreferencesRepository.verifyPin("1234", "1234:abcdef"))
        assertFalse("Non-hex salt must return false safely", UserPreferencesRepository.verifyPin("1234", "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz:0000000000000000000000000000000000000000000000000000000000000000"))
        assertFalse("Non-hex hash must return false safely", UserPreferencesRepository.verifyPin("1234", "00000000000000000000000000000000:zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"))
    }
}
