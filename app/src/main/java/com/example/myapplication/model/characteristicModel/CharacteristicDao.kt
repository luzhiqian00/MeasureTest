package com.example.myapplication.model.characteristicModel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CharacteristicDao {

    @Insert
    fun insert(characteristic: Characteristic)

    @Query("SELECT * FROM characteristics")
    fun getAllCharacteristics(): List<Characteristic>

    @Query("SELECT COUNT(*) FROM characteristics WHERE deviceAddress = :deviceAddress AND characteristicUUID = :characteristicUUID AND serviceUUID = :serviceUUID")
    fun countCharacteristics(deviceAddress: String, characteristicUUID: String, serviceUUID: String): Int
}
