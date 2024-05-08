package com.example.myapplication.model.dataPointModel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface DataPointDao {
    @Insert
    fun insertDataPoint(dataPoint: DataPoint): Long

    @Query("SELECT * FROM data_points WHERE measurementId = :measurementId AND userEmail = :userEmail")
    fun getDataPointsForMeasurement(measurementId: String, userEmail: String): List<DataPoint>
}
