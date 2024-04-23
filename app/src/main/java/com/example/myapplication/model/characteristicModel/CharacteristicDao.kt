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
}
