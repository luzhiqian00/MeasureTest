package com.example.myapplication

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityDeviceBinding

class BlueDeviceActivity : AppCompatActivity() {
    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "BlueDeviceActivity"
    }
    private lateinit var bluetoothDevice: BluetoothDevice // 延迟初始化 BluetoothDevice

    private val binding  by lazy{ActivityDeviceBinding.inflate(layoutInflater) }
    private val connectionButton by lazy { binding.connectButton }
    private val disconnectButton by lazy { binding.disconnectButton }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)




    }
}