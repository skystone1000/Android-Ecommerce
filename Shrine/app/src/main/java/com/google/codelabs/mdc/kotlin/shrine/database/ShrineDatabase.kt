package com.google.codelabs.mdc.kotlin.shrine.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.models.User

@Database(entities = [CartItem::class, User::class, Product::class], version = 2)
abstract class ShrineDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDAO
    abstract fun productDao(): ProductDAO
    abstract fun userDao(): UserDAO

    companion object{
        @Volatile
        private var INSTANCE: ShrineDatabase? = null

        fun getDatabase(context: Context): ShrineDatabase {
            if(INSTANCE == null){
                synchronized(this){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ShrineDatabase::class.java,
                        "contactDB")
                        // v1 -> v2 changed the `users` schema (plaintext password replaced by
                        // salt + hash). This is a throwaway local store, so we drop and recreate
                        // rather than ship a migration; products are re-seeded and users re-register.
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}