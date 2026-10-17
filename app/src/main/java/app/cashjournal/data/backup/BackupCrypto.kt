package app.cashjournal.data.backup

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM Authenticated Encryption for CashJournal backups.
 * 
 * Header specification (34 bytes):
 * - Magic bytes (6 bytes): "CJENC1" (CashJournal Encrypted v1)
 * - Iterations (4 bytes, Big-Endian): 100,000 (PBKDF2-HMAC-SHA256)
 * - Salt (16 bytes): Random cryptographically secure salt
 * - IV (12 bytes): Random cryptographically secure nonce for AES-GCM
 * - Payload: Ciphertext + 128-bit authentication tag
 */
object BackupCrypto {
    private const val MAGIC_STRING = "CJENC1"
    private val MAGIC_BYTES = MAGIC_STRING.toByteArray(Charsets.US_ASCII)
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256

    private val secureRandom = SecureRandom()

    class InvalidPasswordException(message: String = "Incorrect password or corrupted backup file", cause: Throwable? = null) :
        IllegalArgumentException(message, cause)

    class CorruptedBackupException(message: String = "Invalid or corrupted backup format", cause: Throwable? = null) :
        IllegalArgumentException(message, cause)

    /**
     * Checks if the byte array starts with the CJENC1 magic header.
     */
    fun isEncrypted(bytes: ByteArray): Boolean {
        if (bytes.size < MAGIC_BYTES.size) return false
        for (i in MAGIC_BYTES.indices) {
            if (bytes[i] != MAGIC_BYTES[i]) return false
        }
        return true
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with key derived via PBKDF2-HMAC-SHA256.
     */
    fun encrypt(plainBytes: ByteArray, password: CharArray): ByteArray {
        require(password.isNotEmpty()) { "Password cannot be empty" }

        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { secureRandom.nextBytes(it) }

        val keySpec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = keyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(plainBytes)

        // Structure: MAGIC (6) + ITERATIONS (4) + SALT (16) + IV (12) + CIPHERTEXT
        val totalSize = MAGIC_BYTES.size + 4 + SALT_LENGTH + IV_LENGTH + ciphertext.size
        val output = ByteArray(totalSize)
        var offset = 0

        System.arraycopy(MAGIC_BYTES, 0, output, offset, MAGIC_BYTES.size)
        offset += MAGIC_BYTES.size

        // Write iterations as 4-byte big endian
        output[offset] = (PBKDF2_ITERATIONS shr 24).toByte()
        output[offset + 1] = (PBKDF2_ITERATIONS shr 16).toByte()
        output[offset + 2] = (PBKDF2_ITERATIONS shr 8).toByte()
        output[offset + 3] = PBKDF2_ITERATIONS.toByte()
        offset += 4

        System.arraycopy(salt, 0, output, offset, SALT_LENGTH)
        offset += SALT_LENGTH

        System.arraycopy(iv, 0, output, offset, IV_LENGTH)
        offset += IV_LENGTH

        System.arraycopy(ciphertext, 0, output, offset, ciphertext.size)

        return output
    }

    /**
     * Decrypts AES-256-GCM ciphertext using key derived via PBKDF2-HMAC-SHA256.
     * Throws InvalidPasswordException if authentication tag fails (wrong password or altered data).
     */
    fun decrypt(encryptedBytes: ByteArray, password: CharArray): ByteArray {
        val minHeaderSize = MAGIC_BYTES.size + 4 + SALT_LENGTH + IV_LENGTH + (TAG_LENGTH_BITS / 8)
        if (encryptedBytes.size < minHeaderSize) {
            throw CorruptedBackupException("Backup file is too small or truncated")
        }

        if (!isEncrypted(encryptedBytes)) {
            throw CorruptedBackupException("Backup file does not have a valid CJENC1 header")
        }

        var offset = MAGIC_BYTES.size

        val iterations = ((encryptedBytes[offset].toInt() and 0xFF) shl 24) or
                ((encryptedBytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((encryptedBytes[offset + 2].toInt() and 0xFF) shl 8) or
                (encryptedBytes[offset + 3].toInt() and 0xFF)
        offset += 4

        if (iterations <= 0 || iterations > 1_000_000) {
            throw CorruptedBackupException("Unsupported PBKDF2 iteration count: $iterations")
        }

        val salt = ByteArray(SALT_LENGTH)
        System.arraycopy(encryptedBytes, offset, salt, 0, SALT_LENGTH)
        offset += SALT_LENGTH

        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(encryptedBytes, offset, iv, 0, IV_LENGTH)
        offset += IV_LENGTH

        val ciphertextSize = encryptedBytes.size - offset
        val ciphertext = ByteArray(ciphertextSize)
        System.arraycopy(encryptedBytes, offset, ciphertext, 0, ciphertextSize)

        val keySpec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = keyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.doFinal(ciphertext)
        } catch (e: javax.crypto.AEADBadTagException) {
            throw InvalidPasswordException("Incorrect password or corrupted backup file", e)
        } catch (e: Exception) {
            throw InvalidPasswordException("Decryption failed: ${e.message}", e)
        }
    }
}
