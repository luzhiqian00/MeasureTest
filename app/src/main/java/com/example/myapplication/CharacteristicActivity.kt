package com.example.myapplication

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.loginlibrary.secure.SecureStorage
import com.example.myapplication.ble.MyBLEService
import com.example.myapplication.databinding.ActivityCharacteristicBinding
import com.example.myapplication.model.AppDatabase
import com.example.myapplication.model.characteristicModel.Characteristic
import com.example.myapplication.model.dataPointModel.DataPoint
import com.example.myapplication.model.measureResultModel.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CharacteristicActivity : AppCompatActivity() {

    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "CharacteristicActivity"
    }

    private val binding by lazy { ActivityCharacteristicBinding.inflate(layoutInflater) }

    private val connectedText by lazy { binding.connected }
    private val connectButton by lazy { binding.connectButton }
    private val disconnectButton by lazy { binding.disconnectButton }
    private val characteristicValueView by lazy { binding.characteristicValueTextView }
    private val characteristicUUIDTextView by lazy{binding.characteristicUUIDTextView}
    private val serviceUUIDTextView by lazy{binding.serviceUUIDTextView}
    private val deviceAddressTextView by lazy{binding.deviceAddressTextView}

    private var bluetoothService: MyBLEService? = null

    private val storeManyButton by lazy { binding.storeManyButton }
    private val storeOneButton by lazy { binding.storeOneButton }

    private var mCharacteristic : Characteristic? =null
    private var BLEaddress:String ? =null
    private var characteristicUUID:String?=null
    private var serviceUUID:String? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        SecureStorage.init(this)
        // 检查是否存在记住的凭据
        val storedUserEmail = SecureStorage.decrypt("userEmailKey")

        // 获取Intent中的额外数据
        BLEaddress = intent.getStringExtra("deviceAddress")
        characteristicUUID = intent.getStringExtra("characteristicUUID")
        serviceUUID = intent.getStringExtra("serviceUUID")


        deviceAddressTextView.text = BLEaddress
        serviceUUIDTextView.text = serviceUUID
        characteristicUUIDTextView.text = characteristicUUID


        connectButton.setOnClickListener {
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

        storeOneButton.setOnClickListener{
            if(connected){
                GlobalScope.launch(Dispatchers.IO){
                    saveMeasurementAndDataPoints(storedUserEmail,characteristicValueView.text.toString())
                }
            }else{
                Toast.makeText(this,"设备未连接，不能记录数据",Toast.LENGTH_LONG)
            }
        }

        storeManyButton.setOnClickListener {

        }

    }

    // 管理service的生命周期,不用管，重写了一些必要的方法而已
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as MyBLEService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(CharacteristicActivity.TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                Log.d(CharacteristicActivity.TAG,"Bluetooth initialized successfully")
                // perform device connection
                BLEaddress?.let { bluetooth.connect(it) }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService?.disconnect()
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
                    Log.d(CharacteristicActivity.TAG,"ACTION_GATT_SERVICES_DISCOVERED")
                    //displayGattServices(bluetoothService?.getSupportedGattServices() as List<BluetoothGattService>?)
                    //displayGattCharacteristics(bluetoothService?.getSupportedGattServices() as List<BluetoothGattService>?)

                }
                MyBLEService.ACTION_DATA_AVAILABLE -> {
                    Log.d(CharacteristicActivity.TAG,"characteristic data read successfully")
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


    private fun updateCharacteristics(uuid:UUID,characteristicVal:String) {
            if (uuid.toString() == characteristicUUID){
                characteristicValueView.text = characteristicVal
                characteristicValueView.postInvalidate()
                return
            }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (bluetoothService != null) {
            val result = bluetoothService!!.connect(BLEaddress!!)
            Log.d(CharacteristicActivity.TAG, "Connect request result=$result")
        }
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
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        Log.d(CharacteristicActivity.TAG, "unbindService called")
    }

    private fun saveMeasurementAndDataPoints(userEmail: String, value: String, description: String = "Optional description") {
        val db = AppDatabase.getDatabase(MeasureApplication.context)
        val measurementDao = db.measurementDao()
        val dataPointDao = db.dataPointDao()

        // 创建 Measurement 实体
        val measurement = Measurement().apply {
            this.userEmail = userEmail
            this.timestamp = System.currentTimeMillis()
            this.description = description
        }

        // 插入 Measurement 实体并获取 ID
        val measurementId = measurementDao?.insertMeasurement(measurement)
        if (measurementId!=null){
            Log.d(TAG,"成功存入了measures表")
        }else{
            Log.d(TAG,"无法成功插入measures表，不执行后续操作")
        }
        // 创建 DataPoint 实体
        val dataPoint = DataPoint(
            measurementId = measurementId!!, // 确保类型匹配
            value = value
        )

        // 插入 DataPoint 实体
        val dataPointId = dataPointDao?.insertDataPoint(dataPoint)
        if (dataPointId!=null){
            Log.d(TAG,"dataPoint表插入成功!")
        }
    }


}