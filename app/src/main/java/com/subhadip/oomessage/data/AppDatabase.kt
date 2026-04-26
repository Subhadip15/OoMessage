package com.subhadip.oomessage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.subhadip.oomessage.data.dao.MessageDao
import com.subhadip.oomessage.data.dao.UserDao
import com.subhadip.oomessage.data.model.Message
import com.subhadip.oomessage.data.model.User

@Database(entities = [User::class, Message::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it. Otherwise, create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oomessage_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true) // Wipes DB if you change the schema (good for dev)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}