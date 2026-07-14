package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.database.UserDao
import com.skystone1000.shrine.core.model.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of an auth attempt. */
sealed interface AuthResult {
    data class Success(val userId: Long) : AuthResult
    data object InvalidCredentials : AuthResult
    data object EmailTaken : AuthResult
}

/** Local account registration / login over the Room user table, hashing with [PasswordHasher]. */
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String, phone: String? = null): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun getUser(userId: Long): UserEntity?
    suspend fun updateProfile(
        userId: Long,
        name: String,
        email: String,
        phone: String?,
        dateOfBirthMillis: Long?,
        avatarUri: String?,
    )
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val userDao: UserDao,
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String, phone: String?): AuthResult {
        val normalisedEmail = email.trim().lowercase()
        if (userDao.getByEmail(normalisedEmail) != null) return AuthResult.EmailTaken
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt) // password is intentionally not trimmed
        val id = userDao.insert(
            UserEntity(
                name = name.trim(),
                email = normalisedEmail,
                phone = phone,
                passwordHash = hash,
                passwordSalt = salt,
            ),
        )
        return AuthResult.Success(id)
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.getByEmail(email.trim().lowercase()) ?: return AuthResult.InvalidCredentials
        val computed = PasswordHasher.hash(password, user.passwordSalt)
        return if (computed == user.passwordHash) AuthResult.Success(user.id) else AuthResult.InvalidCredentials
    }

    override suspend fun getUser(userId: Long): UserEntity? = userDao.getById(userId)

    override suspend fun updateProfile(
        userId: Long,
        name: String,
        email: String,
        phone: String?,
        dateOfBirthMillis: Long?,
        avatarUri: String?,
    ) {
        val existing = userDao.getById(userId) ?: return
        userDao.update(
            existing.copy(
                name = name.trim(),
                email = email.trim().lowercase(),
                phone = phone,
                dateOfBirthMillis = dateOfBirthMillis,
                avatarUri = avatarUri,
            ),
        )
    }
}
