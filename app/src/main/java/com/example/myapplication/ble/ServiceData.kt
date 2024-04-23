package com.example.myapplication.ble

import java.util.*

import java.util.*
import kotlin.collections.ArrayList

class ServiceData() {
    var serviceName: String? = null
    var serviceUUID: UUID? = null
    var characteristics = ArrayList<CharacteristicData>()

    constructor(serviceName: String?, serviceUUID: UUID?, characteristics: MutableList<CharacteristicData>) : this() {
        this.serviceName = serviceName
        this.serviceUUID = serviceUUID
        this.characteristics.addAll(characteristics)
    }


}

class CharacteristicData() {
    var bleAddress:String? =null
    var serviceUUID:UUID? =null
    var characteristicName: String? = null
    var characteristicUUID: UUID? = null
    var characteristicVal: String? = null

    constructor(characteristicName: String?, characteristicUUID: UUID?, characteristicVal: String?) : this() {
        this.characteristicName = characteristicName
        this.characteristicUUID = characteristicUUID
        this.characteristicVal = characteristicVal
    }

    constructor(characteristicName: String?, characteristicUUID: UUID) : this() {
        this.bleAddress = "NULL"
        this.characteristicName = characteristicName
        this.characteristicUUID = characteristicUUID
        this.characteristicVal = "NULL"
    }

    constructor(serviceUUID: UUID?,characteristicName: String?, characteristicUUID: UUID) : this() {
        this.serviceUUID = serviceUUID
        this.characteristicName = characteristicName
        this.characteristicUUID = characteristicUUID
        this.characteristicVal = "NULL"
    }

    constructor(deviceAddress:String?, serviceUUID: UUID?,characteristicName: String?, characteristicUUID: UUID) : this() {
        this.bleAddress = deviceAddress
        this.serviceUUID = serviceUUID
        this.characteristicName = characteristicName
        this.characteristicUUID = characteristicUUID
        this.characteristicVal = "NULL"
    }
}
