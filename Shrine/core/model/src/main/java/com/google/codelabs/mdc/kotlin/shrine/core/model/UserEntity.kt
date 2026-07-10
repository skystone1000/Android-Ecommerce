package com.google.codelabs.mdc.kotlin.shrine.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A local account. Credentials are stored as a salted PBKDF2 hash (never plaintext).
 * Email is unique. Profile fields (phone, DOB, avatar) back the Edit-profile screen.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String? = null,
    val dateOfBirthMillis: Long? = null,
    val avatarUri: String? = null,
    val passwordHash: String,
    val passwordSalt: String,
)
