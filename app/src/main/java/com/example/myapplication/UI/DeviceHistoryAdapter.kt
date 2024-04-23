package com.example.myapplication.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView
import com.example.myapplication.MeasureApplication
import com.example.myapplication.R
import com.example.myapplication.ble.CharacteristicData
import com.example.myapplication.model.characteristicModel.AppDatabase
import com.example.myapplication.model.characteristicModel.Characteristic

class DeviceHistoryAdapter : BaseAdapter(){
    private val characteristicList = ArrayList<Characteristic>()

    fun setCharacteristicList(characteristicDataList:ArrayList<Characteristic>){
        characteristicList.addAll(characteristicDataList)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return characteristicList.size
    }

    override fun getItem(position: Int): Any {
        return characteristicList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val characteristic = characteristicList[position]
        val view = LayoutInflater.from(parent?.context).inflate(
            R.layout.bluetooth_characteristics_item_layout, parent, false
        )

        val deviceAddressTextView = view.findViewById<TextView>(R.id.bleAddress)
        val serviceUUIDTextView = view.findViewById<TextView>(R.id.serviceUUIDTextView)
        val characteristicNameTextView = view.findViewById<TextView>(R.id.characteristicNameTextView)
        val characteristicUUIDTextView = view.findViewById<TextView>(R.id.characteristicUUIDTextView)
        val characteristicValueTextView = view.findViewById<TextView>(R.id.characteristicValue)

        deviceAddressTextView.text ="设备IP地址:  "+characteristic.deviceAddress ?:"Unknown Device Address"
        serviceUUIDTextView.text = "ServiceUUID:  "+characteristic.serviceUUID?: "Unknown Service UUID"
        characteristicUUIDTextView.text = "CharacteristicUUID:  "+characteristic.characteristicUUID ?: "Unknown Characteristic UUID"
        characteristicNameTextView.text = "特征默认名"
        characteristicValueTextView.text = "NULL"

        return view
    }

}