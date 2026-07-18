package com.skystone1000.shrine.core.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * On-device password hashing for the local demo account store.
 *
 * Uses salted PBKDF2. The **current** scheme (v2) is `PBKDF2WithHmacSHA256` at 600k iterations,
 * aligned with the OWASP 2023 guidance. Stored hashes carry a small version tag (e.g. `v2$…`) so
 * the algorithm/iteration count is self-describing and can evolve again later.
 *
 * The **legacy** scheme (v1: `PBKDF2WithHmacSHA1` at 100k, no tag) is still *verified* so accounts
 * created before the upgrade keep working; [AuthRepository] transparently re-hashes them to v2 on
 * the next successful login (see [needsUpgrade]).
 *
 * This hardens local credential storage; it is NOT a substitute for server-side authentication.
 */
object PasswordHasher {

    // Legacy scheme (v1) — kept only to verify pre-upgrade accounts; never produced for new hashes.
    private const val V1_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val V1_ITERATIONS = 100_000

    // Current scheme (v2) — OWASP-aligned.
    private const val V2_TAG = "v2"
    private const val V2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val V2_ITERATIONS = 600_000

    private const val TAG_SEPARATOR = "$"
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun newSalt(): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /** Hashes [password] with the current (v2) scheme; the result carries its version tag. */
    fun hash(password: String, saltBase64: String): String {
        val digest = pbkdf2(password, saltBase64, V2_ALGORITHM, V2_ITERATIONS)
        return "$V2_TAG$TAG_SEPARATOR$digest"
    }

    /**
     * Verifies [password] against a [storedHash], dispatching on the stored hash's version tag
     * (v2) or falling back to the legacy (v1, untagged) scheme. Constant-time comparison.
     */
    fun verify(password: String, saltBase64: String, storedHash: String): Boolean {
        val recomputed = if (storedHash.startsWith("$V2_TAG$TAG_SEPARATOR")) {
            val digest = pbkdf2(password, saltBase64, V2_ALGORITHM, V2_ITERATIONS)
            "$V2_TAG$TAG_SEPARATOR$digest"
        } else {
            pbkdf2(password, saltBase64, V1_ALGORITHM, V1_ITERATIONS)
        }
        return constantTimeEquals(recomputed, storedHash)
    }

    /** True if [storedHash] uses an older scheme and should be re-hashed to the current one. */
    fun needsUpgrade(storedHash: String): Boolean =
        !storedHash.startsWith("$V2_TAG$TAG_SEPARATOR")

    private fun pbkdf2(password: String, saltBase64: String, algorithm: String, iterations: Int): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}
