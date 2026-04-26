package com.subhadip.oomessage.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(sharedSecret: ByteArray) {

    // Ensure the key is exactly 32 bytes for AES-256
    private val secretKey = SecretKeySpec(sharedSecret.copyOf(32), "AES")

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // AES-GCM requires a unique 12-byte Initialization Vector (IV) for every message
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine the IV and the encrypted text so the receiver can decrypt it
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)

        // Extract the 12-byte IV from the front
        val iv = combined.copyOfRange(0, 12)
        // Extract the actual encrypted message
        val encryptedBytes = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}