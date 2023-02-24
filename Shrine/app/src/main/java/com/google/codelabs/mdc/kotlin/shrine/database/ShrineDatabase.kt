package com.google.codelabs.mdc.kotlin.shrine.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.models.User

@Database(entities = [CartItem::class, User::class, Product::class], version = 1)
abstract class ShrineDatabase : RoomDatabase() {
    abstract fun cartItemDao(): CartItemDAO
    abstract fun productDao(): PrductDAO
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
                        "contactDB").build()
                }
            }
            return INSTANCE!!
        }
    }
}