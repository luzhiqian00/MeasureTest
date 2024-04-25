package com.example.myapplication.model.measureResultModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

import androidx.room.PrimaryKey


@Entity(
    tableName = "measurements",
)
class Measurement {
    @PrimaryKey(autoGenerate = true)
    var measurementId:Long  = 0

    @ColumnInfo(name = "userEmail")
    var userEmail :String?=null

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0

    @ColumnInfo(name = "description")
    var description: String? = null
}
