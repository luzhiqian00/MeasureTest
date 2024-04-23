package com.example.myapplication.model.characteristicModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "characteristics", primaryKeys = ["deviceAddress", "characteristicUUID", "serviceUUID"])
data class Characteristic(
    var deviceAddress: String,
    var characteristicUUID: String,
    var serviceUUID: String
)