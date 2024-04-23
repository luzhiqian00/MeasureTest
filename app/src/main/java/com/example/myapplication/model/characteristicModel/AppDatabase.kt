package com.example.myapplication.model.characteristicModel

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [Characteristic::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characteristicDao(): CharacteristicDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            // 如果实例非空，则返回现有实例
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
            AppDatabase::class.java,"app_database").
            build().apply {
                instance = this
            }
        }
    }
}

