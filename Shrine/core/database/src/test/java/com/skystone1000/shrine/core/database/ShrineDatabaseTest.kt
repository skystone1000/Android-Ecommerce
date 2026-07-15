package com.skystone1000.shrine.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.skystone1000.shrine.core.model.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Database gate for [ShrineDatabase]. The schema is at **version 1** with `exportSchema = false`,
 * so there are no historical schemas to migrate between yet — a multi-version Room migration test
 * (via `MigrationTestHelper`) becomes meaningful only at version 2. Until then this verifies the
 * declared version and that data **survives a close/reopen** of a real on-disk database (the
 * property a migration would have to preserve).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShrineDatabaseTest {

    private lateinit var context: Context
    private val dbName = "shrine_test.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun open(): ShrineDatabase =
        Room.databaseBuilder(context, ShrineDatabase::class.java, dbName).build()

    @Test
    fun databaseOpensAtVersionOne() {
        val db = open()
        assertEquals(1, db.openHelper.readableDatabase.version)
        db.close()
    }

    @Test
    fun writtenDataSurvivesCloseAndReopen() = runTest {
        val first = open()
        val id = first.userDao().insert(
            UserEntity(name = "Ava", email = "ava@shrine.com", passwordHash = "h", passwordSalt = "s"),
        )
        first.close()

        val reopened = open()
        val user = reopened.userDao().getById(id)
        assertNotNull(user)
        assertEquals("ava@shrine.com", user!!.email)
        reopened.close()
    }
}
