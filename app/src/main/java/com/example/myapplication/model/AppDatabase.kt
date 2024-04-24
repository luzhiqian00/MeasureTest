package com.example.myapplication.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.characteristicModel.Characteristic
import com.example.myapplication.model.characteristicModel.CharacteristicDao
import com.example.myapplication.model.dataPointModel.DataPoint
import com.example.myapplication.model.dataPointModel.DataPointDao
import com.example.myapplication.model.measureResultModel.Measurement
import com.example.myapplication.model.measureResultModel.MeasurementDao


@Database(entities = [Characteristic::class, Measurement::class,DataPoint::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dataPointDao(): DataPointDao?
    abstract fun measurementDao(): MeasurementDao?
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

