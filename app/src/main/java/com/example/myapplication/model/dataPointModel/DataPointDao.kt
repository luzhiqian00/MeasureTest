package com.example.myapplication.model.dataPointModel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface DataPointDao {
    @Insert
    fun insertDataPoint(dataPoint: DataPoint?):Long

    @Query("SELECT * FROM data_points WHERE measurementId = :measurementId")
    fun getDataPointsForMeasurement(measurementId: Int): List<DataPoint?>?
}