package com.google.codelabs.mdc.kotlin.shrine.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.google.codelabs.mdc.kotlin.shrine.models.User

@Dao
interface UserDAO {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE user_email=:email")
    suspend fun getLogin(email: String) : User?
}