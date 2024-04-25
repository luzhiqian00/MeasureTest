package com.example.myapplication.model.dataPointModel

import androidx.room.*

import com.example.myapplication.model.measureResultModel.Measurement


@Entity(
    tableName = "data_points",
    foreignKeys = [ForeignKey(
        entity = Measurement::class,
        parentColumns = arrayOf("measurementId"),
        childColumns = arrayOf("measurementId"),
        onDelete = ForeignKey.CASCADE
    )] ,
    indices = [Index(value = ["measurementId"])]
)

data class DataPoint(
    @PrimaryKey(autoGenerate = true)
    var dataPointId: Long = 0,

    @ColumnInfo(name = "measurementId")
    var measurementId: Long,

    @ColumnInfo(name = "value")
    var value: String
)