package com.skystone1000.shrine.core.data

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.skystone1000.shrine.core.database.ShrineDatabase
import com.skystone1000.shrine.core.model.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Covers the PBKDF2 upgrade (plan_9 Phase C / item 14): the current scheme is verifiable and
 * version-tagged, **legacy** SHA1/100k hashes still verify, and a successful login transparently
 * re-hashes a legacy account to the current scheme.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PasswordHasherMigrationTest {

    private lateinit var db: ShrineDatabase
    private lateinit var auth: AuthRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShrineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        auth = DefaultAuthRepository(db.userDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun currentSchemeHashIsTaggedAndVerifies() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("secret123", salt)

        assertTrue("current hashes carry the v2 tag", hash.startsWith("v2\$"))
        assertFalse(PasswordHasher.needsUpgrade(hash))
        assertTrue(PasswordHasher.verify("secret123", salt, hash))
        assertFalse(PasswordHasher.verify("wrong", salt, hash))
    }

    @Test
    fun legacyV1HashStillVerifies_andIsFlaggedForUpgrade() {
        val salt = PasswordHasher.newSalt()
        val legacy = legacyV1Hash("secret123", salt)

        assertTrue("legacy hashes have no version tag", PasswordHasher.needsUpgrade(legacy))
        assertTrue(PasswordHasher.verify("secret123", salt, legacy))
        assertFalse(PasswordHasher.verify("wrong", salt, legacy))
    }

    @Test
    fun login_migratesLegacyHashToCurrentScheme() = runTest {
        val salt = PasswordHasher.newSalt()
        db.userDao().insert(
            UserEntity(
                name = "Ava",
                email = "ava@shrine.com",
                phone = null,
                passwordHash = legacyV1Hash("secret123", salt), // pre-upgrade account
                passwordSalt = salt,
            ),
        )

        // Logging in with the legacy account succeeds...
        assertTrue(auth.login("ava@shrine.com", "secret123") is AuthResult.Success)

        // ...and the stored hash is silently rewritten to the current (v2) scheme.
        val migrated = db.userDao().getByEmail("ava@shrine.com")
        assertNotNull(migrated)
        assertTrue(migrated!!.passwordHash.startsWith("v2\$"))

        // The account keeps working after migration (wrong password still rejected).
        assertTrue(auth.login("ava@shrine.com", "secret123") is AuthResult.Success)
        assertTrue(auth.login("ava@shrine.com", "nope") is AuthResult.InvalidCredentials)
    }

    /** Reproduces the pre-upgrade hash (PBKDF2WithHmacSHA1 @ 100k, untagged) for the migration path. */
    private fun legacyV1Hash(password: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, 100_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return Base64.encodeToString(factory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }
}
