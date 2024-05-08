package com.example.myapplication.model.measureResultModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

import androidx.room.PrimaryKey
import java.util.*


@Entity(
    tableName = "measurements",
    primaryKeys = ["measurementId", "userEmail"]  // 定义联合主键
)
class Measurement {

    @ColumnInfo(name = "measurementId")
    var measurementId: String = UUID.randomUUID().toString()  // 使用 UUID

    @ColumnInfo(name = "userEmail")
    var userEmail: String = ""

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0

    @ColumnInfo(name = "description")
    var description: String? = null
}
