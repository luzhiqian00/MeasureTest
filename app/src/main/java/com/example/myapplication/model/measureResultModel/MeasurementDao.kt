package com.example.myapplication.model.measureResultModel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface MeasurementDao {
    @Insert
    fun insertMeasurement(measurement: Measurement?):Long

    @Query("SELECT * FROM measurements WHERE userEmail = :userEmail")
    fun getMeasurementsForUser(userEmail: String): List<Measurement?>?

    @Query("SELECT EXISTS(SELECT 1 FROM measurements WHERE measurementId = :measurementId AND userEmail = :userEmail)")
    fun doesMeasurementExist(measurementId: String, userEmail: String): Boolean

    @Query("DELETE FROM measurements WHERE userEmail = :userEmail")
    fun deleteMeasurementsForUser(userEmail: String): Int

    @Query("DELETE FROM measurements WHERE measurementId = :measurementId")
    fun deleteMeasurementById(measurementId: Long): Int

    @Query("DELETE FROM measurements WHERE measurementId IN (:measurementIds)")
    fun deleteMeasurementsByIds(measurementIds: List<String>): Int

    @Query("DELETE FROM measurements WHERE measurementId = :measurementId AND userEmail = :userEmail")
    fun deleteMeasurementByMeasurementIdAndUserEmail(measurementId: String, userEmail: String): Int

}