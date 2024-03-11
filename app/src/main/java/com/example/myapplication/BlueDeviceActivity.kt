package com.example.myapplication

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.myapplication.ble.MyBLEService
import com.example.myapplication.databinding.ActivityDeviceBinding


class BlueDeviceActivity : AppCompatActivity() {
    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "BlueDeviceActivity"
    }

    private lateinit var bluetoothDevice: BluetoothDevice // 延迟初始化 BluetoothDevice

    private val binding by lazy { ActivityDeviceBinding.inflate(layoutInflater) }
    private val connectionButton by lazy { binding.connectButton }
    private val disconnectButton by lazy { binding.disconnectButton }
    private val deviceName by lazy { binding.deviceNameTextView }
    private val deviceAddress by lazy { binding.deviceAddressTextView }
    private val connectedText by lazy { binding.connected }
    private var bluetoothService: MyBLEService? = null

    // 管理service的生命周期,不用管，重写了一些必要的方法而已
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as MyBLEService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                Log.d(TAG,"Bluetooth initialized successfully")
                // perform device connection
                BLEaddress?.let { bluetooth.connect(it) }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService?.disconnect()
        }
    }

    private var BLEaddress: String? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 获取传递过来的 BluetoothDevice 对象
        bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>("bluetoothDevice")!!
        deviceName.text = "Name: " + bluetoothDevice?.name
        deviceAddress.text = "Address: " + bluetoothDevice?.address
        connectedText.text ="Disconnected"
        BLEaddress = bluetoothDevice?.address

        connectionButton.setOnClickListener {
            val gattServiceIntent = Intent(this, MyBLEService::class.java)
            bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG,"try to connect")

        }



        disconnectButton.setOnClickListener {
            if (connected) {
                unbindService(serviceConnection)
                Log.d(TAG, "try to disconnect")
            }
        }
    }

    private var connected = false
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                MyBLEService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    connectedText.text ="Connected"
                    connectedText.invalidate() // 强制更新界面
                }
                MyBLEService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    connectedText.text ="Disconnected"
                    connectedText.invalidate() // 强制更新界面

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (bluetoothService != null) {
            val result = bluetoothService!!.connect(bluetoothDevice.address)
            Log.d(TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(MyBLEService.ACTION_GATT_CONNECTED)
            addAction(MyBLEService.ACTION_GATT_DISCONNECTED)
        }
    }


    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        Log.d(TAG, "unbindService called")
    }


}