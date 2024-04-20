package com.example.myapplication

import ServiceDataAdapter
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.myapplication.ble.CharacteristicData
import com.example.myapplication.ble.MyBLEService
import com.example.myapplication.ble.ServiceData
import com.example.myapplication.databinding.ActivityDeviceBinding
import java.util.*
import kotlin.collections.ArrayList


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
    private val serviceListView by lazy {binding.serviceListView}
    private val serviceDataAdapter by lazy { ServiceDataAdapter() }

    private var bluetoothService: MyBLEService? = null
    private var mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    private var mServices = ArrayList<ServiceData>()


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

        serviceListView.adapter = serviceDataAdapter
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
                    connectedText.postInvalidate() // 强制更新界面
                }
                MyBLEService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    connectedText.text ="Disconnected"
                    connectedText.postInvalidate() // 强制更新界面
                }
                MyBLEService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    Log.d(TAG,"ACTION_GATT_SERVICES_DISCOVERED")
                    displayGattServices(bluetoothService?.getSupportedGattServices() as List<BluetoothGattService>?)
                    startOperationThread()
                }
                MyBLEService.ACTION_DATA_AVAILABLE -> {
                    Log.d(TAG,"characteristic data read successfully")
                    val extraRawData = intent.getByteArrayExtra(MyBLEService.EXTRA_RAW_DATA)//原始数据
                    val extraHexData = intent.getStringExtra(MyBLEService.EXTRA_HEX_DATA)//转化为十六进制字符串的数据
                    val uuid =intent.getStringExtra(MyBLEService.EXTRA_UUID)
                    // 在这里处理额外的信息，更新页面
                    if (extraRawData != null) {
                        if (uuid != null) {
                            val rawDataString = extraRawData.toString(Charsets.UTF_8)  //
                            updateCharacteristics(UUID.fromString(uuid),rawDataString+"\n"+extraHexData)
                        }
                    }
                }
            }
        }
    }

    //uuid和value
    private fun updateCharacteristics(uuid:UUID,characteristicVal:String) {
        for(serviceItem in mServices){
            for (characterisicItem in serviceItem.characteristics){
                if (uuid == characterisicItem.characteristicUUID){
                    characterisicItem.characteristicVal = characteristicVal
                    serviceDataAdapter.notifyDataSetChanged()
                    return
                }
            }
        }

    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        gattServices?.let {
            for ((index1,service) in gattServices.withIndex()) {
                val serviceUuid = service.uuid
                val characteristics = service.characteristics
                val characteristicList = ArrayList<CharacteristicData>()

                for ((index, characteristic) in characteristics.withIndex()) {
                    val characteristicUuid = characteristic.uuid
                    val characteristicData = CharacteristicData("Characteristic $index", characteristicUuid)
                    // 为 characteristicData 设置名称，格式为 "Characteristic 0", "Characteristic 1", 等等
                    characteristicList.add(characteristicData)
                }

                val serviceData = ServiceData("Service $index1",serviceUuid, characteristicList)
                mServices.add(serviceData)
            }
        }

        Log.d(TAG,"discover successfully")
        serviceDataAdapter.setServiceList(mServices)
        serviceDataAdapter.notifyDataSetChanged()
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
            addAction(MyBLEService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(MyBLEService.ACTION_DATA_AVAILABLE)
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


    private fun startOperationThread() {
        bluetoothService?.getSupportedGattServices()?.forEach {service->
            service?.characteristics?.forEach { characteristic ->
                Log.d(TAG, "Performing operation...")
                bluetoothService?.readCharacteristic(characteristic)
            }
        }
    }

}