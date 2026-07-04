package com.google.codelabs.mdc.kotlin.shrine.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["user_email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val user_id : Long,
    val user_name : String,
    val user_email : String,
    val user_phone : String,
    val user_org : String,
    val user_pass_hash : String,
    val user_pass_salt : String
)
