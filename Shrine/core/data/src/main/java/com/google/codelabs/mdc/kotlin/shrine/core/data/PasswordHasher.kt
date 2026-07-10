package com.google.codelabs.mdc.kotlin.shrine.core.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * On-device password hashing for the local demo account store (ported as-is from the legacy app).
 *
 * Uses salted PBKDF2 (HMAC-SHA1). Salts and hashes are stored Base64-encoded. This hardens local
 * credential storage; it is NOT a substitute for server-side authentication.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun newSalt(): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(password: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
