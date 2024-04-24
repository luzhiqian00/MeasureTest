package com.example.myapplication.model.measureResultModel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface MeasurementDao {
    @Insert
    fun insertMeasurement(measurement: Measurement?):Long

    @Query("SELECT * FROM measurements WHERE userEmail = :userEmail")
    fun getMeasurementsForUser(userEmail: String): List<Measurement?>?
}