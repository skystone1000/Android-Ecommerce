package com.skystone1000.shrine.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [PasswordHasher] uses `android.util.Base64`, so it runs under Robolectric. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PasswordHasherTest {

    @Test
    fun saltsAreRandomAndNonEmpty() {
        val a = PasswordHasher.newSalt()
        val b = PasswordHasher.newSalt()
        assertTrue(a.isNotEmpty())
        assertNotEquals(a, b)
    }

    @Test
    fun hashIsDeterministicForSamePasswordAndSalt() {
        val salt = PasswordHasher.newSalt()
        assertEquals(PasswordHasher.hash("secret123", salt), PasswordHasher.hash("secret123", salt))
    }

    @Test
    fun hashDiffersForWrongPasswordOrDifferentSalt() {
        val salt = PasswordHasher.newSalt()
        val reference = PasswordHasher.hash("secret123", salt)
        assertNotEquals(reference, PasswordHasher.hash("secret124", salt))
        assertNotEquals(reference, PasswordHasher.hash("secret123", PasswordHasher.newSalt()))
    }

    @Test
    fun passwordIsNotTrimmed() {
        val salt = PasswordHasher.newSalt()
        assertNotEquals(PasswordHasher.hash("secret123", salt), PasswordHasher.hash(" secret123 ", salt))
    }
}
