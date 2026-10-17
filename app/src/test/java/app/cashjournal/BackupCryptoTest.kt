package app.cashjournal

import app.cashjournal.data.backup.BackupCrypto
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupCryptoTest {

    @Test
    fun testEncryptionDecryptionRoundTrip() {
        val originalText = "{\"appVersion\":\"1.0.34-dev\",\"accounts\":[{\"name\":\"BCA Main\",\"balance\":5000000}]}"
        val plainBytes = originalText.toByteArray(Charsets.UTF_8)
        val password = "MySecretPassphrase123!".toCharArray()

        val encrypted = BackupCrypto.encrypt(plainBytes, password)

        assertTrue(encrypted.size > plainBytes.size)
        assertTrue(BackupCrypto.isEncrypted(encrypted))

        val decrypted = BackupCrypto.decrypt(encrypted, password)
        assertArrayEquals(plainBytes, decrypted)
        assertEquals(originalText, String(decrypted, Charsets.UTF_8))
    }

    @Test
    fun testWrongPasswordFails() {
        val originalText = "Sensitive financial records"
        val plainBytes = originalText.toByteArray(Charsets.UTF_8)
        val correctPassword = "CorrectPassword".toCharArray()
        val wrongPassword = "WrongPassword".toCharArray()

        val encrypted = BackupCrypto.encrypt(plainBytes, correctPassword)

        try {
            BackupCrypto.decrypt(encrypted, wrongPassword)
            fail("Expected InvalidPasswordException was not thrown")
        } catch (e: BackupCrypto.InvalidPasswordException) {
            assertTrue(e.message?.contains("Incorrect password") == true)
        }
    }

    @Test
    fun testTamperedCiphertextFails() {
        val originalText = "Unmodified text"
        val plainBytes = originalText.toByteArray(Charsets.UTF_8)
        val password = "StrongPassword456".toCharArray()

        val encrypted = BackupCrypto.encrypt(plainBytes, password)

        // Flip a byte in the ciphertext payload
        encrypted[encrypted.size - 5] = (encrypted[encrypted.size - 5].toInt() xor 0xFF).toByte()

        try {
            BackupCrypto.decrypt(encrypted, password)
            fail("Expected InvalidPasswordException due to authentication tag mismatch")
        } catch (e: BackupCrypto.InvalidPasswordException) {
            // Expected
        }
    }

    @Test
    fun testIsEncryptedHeaderDetection() {
        val jsonBytes = "{\"version\":1}".toByteArray(Charsets.UTF_8)
        assertFalse(BackupCrypto.isEncrypted(jsonBytes))

        val emptyBytes = ByteArray(0)
        assertFalse(BackupCrypto.isEncrypted(emptyBytes))

        val shortBytes = "CJEN".toByteArray(Charsets.US_ASCII)
        assertFalse(BackupCrypto.isEncrypted(shortBytes))

        val encrypted = BackupCrypto.encrypt("sample".toByteArray(), "pwd".toCharArray())
        assertTrue(BackupCrypto.isEncrypted(encrypted))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyPasswordThrows() {
        BackupCrypto.encrypt("test".toByteArray(), "".toCharArray())
    }
}
