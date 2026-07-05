package com.google.codelabs.mdc.kotlin.shrine.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.models.User

@Database(entities = [CartItem::class, User::class, Product::class], version = 3)
abstract class ShrineDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDAO
    abstract fun productDao(): ProductDAO
    abstract fun userDao(): UserDAO

    companion object{
        @Volatile
        private var INSTANCE: ShrineDatabase? = null

        fun getDatabase(context: Context): ShrineDatabase =
            // Idiomatic double-checked locking: re-check INSTANCE *inside* the lock so two racing
            // threads can never each build (and overwrite) a separate database instance.
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShrineDatabase::class.java,
                    "contactDB")
                    // Schema changes on this throwaway local store drop and recreate rather than
                    // ship a migration; products are re-seeded and users re-register.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}