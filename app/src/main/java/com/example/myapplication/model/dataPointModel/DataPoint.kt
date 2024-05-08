package com.example.myapplication.model.dataPointModel

import androidx.room.*

import com.example.myapplication.model.measureResultModel.Measurement


@Entity(
    tableName = "data_points",
    foreignKeys = [
        ForeignKey(
            entity = Measurement::class,
            parentColumns = arrayOf("measurementId", "userEmail"),  // 对应 Measurement 的联合主键
            childColumns = arrayOf("measurementId", "userEmail"),   // DataPoint 中的对应字段
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["measurementId", "userEmail"])  // 为联合外键创建索引
    ]
)
data class DataPoint(
    @PrimaryKey(autoGenerate = true)
    var dataPointId: Long = 0,

    @ColumnInfo(name = "measurementId")
    var measurementId: String,  // 保持 String 类型，与 Measurement 的 UUID 类型一致

    @ColumnInfo(name = "userEmail")
    var userEmail: String,      // 添加 userEmail 字段

    @ColumnInfo(name = "value")
    var value: String
)