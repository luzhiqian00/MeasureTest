package com.example.myapplication.UI

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class CharacteristicDataAdapter : BaseAdapter(){
    private val characteristicList = ArrayList<CharacteristicData>()

    fun setCharacteristicList(characteristicDataList:ArrayList<CharacteristicData>){
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

        deviceAddressTextView.text =characteristic.bleAddress ?:"Unknown Device Address"
        serviceUUIDTextView.text = characteristic.serviceUUID.toString() ?: "Unknown Service UUID"
        characteristicNameTextView.text = characteristic.characteristicName ?: "Unknown Characteristic Name"
        characteristicUUIDTextView.text = characteristic.characteristicUUID?.toString() ?: "Unknown Characteristic UUID"
        characteristicValueTextView.text = characteristic.characteristicVal ?: "No Characteristic Value"

        // 为每个视图设置长按监听器
        view.setOnLongClickListener {
            // 创建弹出菜单
            val popupMenu = PopupMenu(parent?.context, view)
            popupMenu.menuInflater.inflate(R.menu.characteristicitem_popup_menu, popupMenu.menu)

            // 设置菜单项点击监听器
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_item_store -> {
                        val userDao = AppDatabase.getDatabase(MeasureApplication.context).characteristicDao()
                        val deviceAddress = characteristic.bleAddress
                        val characteristicUUID = characteristic.characteristicUUID
                        val serviceUUID = characteristic.serviceUUID
//
                        if (deviceAddress != null && characteristicUUID != null && serviceUUID != null) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val count = userDao.countCharacteristics(deviceAddress!!, characteristicUUID!!.toString(), serviceUUID!!.toString())
                                if (count == 0) {
                                    val newCharacteristic = Characteristic(
                                        deviceAddress = deviceAddress,
                                        characteristicUUID = characteristicUUID.toString(),
                                        serviceUUID = serviceUUID.toString()
                                    )
                                    userDao.insert(newCharacteristic)
                                } else {
                                    // 已存在相同的键，不执行插入操作
                                    Log.d("KeyExists","Key has existed!")
                                }
                            }

//                            val newCharacteristic = Characteristic(
//                                deviceAddress = deviceAddress,
//                                characteristicUUID = characteristicUUID.toString(),
//                                serviceUUID = serviceUUID.toString()
//                            )
//                            thread {
//                                //userDao.insert(newCharacteristic)
//                            }

                        }
                        true
                    }
                    else -> false
                }
            }
            // 显示弹出菜单
            popupMenu.show()
            true
        }


        return view
    }

}