package com.example.myapplication.UI

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.R

class LeDeviceListAdapter : BaseAdapter() {

    private val deviceList = ArrayList<BluetoothDevice?>()

    fun addDevice(device: BluetoothDevice?) {
        if (device != null && !deviceList.contains(device)) {
            deviceList.add(device)
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return deviceList.size
    }

    override fun getItem(position: Int): Any {
        return deviceList[position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("MissingPermission")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Implement your custom view for each item in the list
        // For example:
        val device = deviceList[position]
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(
            R.layout.bluetooth_device_layout, parent, false
        )
        // Find the TextView in your layout
        val deviceNameTextView = view.findViewById<TextView>(R.id.deviceNameTextView)

        // Set the text of the TextView to the device name
        deviceNameTextView.text = device?.name ?: "Unknown Device"

        return view
    }

    fun clear() {
        deviceList.clear()
        notifyDataSetChanged()
    }
}
