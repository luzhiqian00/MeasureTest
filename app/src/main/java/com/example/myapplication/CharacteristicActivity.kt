package com.example.myapplication

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.loginlibrary.secure.SecureStorage
import com.example.myapplication.UI.CharacteristicActivityAdapter
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
import kotlin.collections.ArrayList

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
    private val storeDataHiddenButton by lazy { binding.storeDataHiddenButton }

    private val characteristicInTimeListView by lazy{binding.characteristicInTimeListView}
    private var mAdapter:CharacteristicActivityAdapter? = null
    private var valueList:ArrayList<String>? =null

    private var isGattUpdateReceiverRegistered = false
    private var isServiceConnected = false
    private var recordAllowed = false

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

        storeDataHiddenButton.visibility = View.INVISIBLE
        storeDataHiddenButton.isEnabled = false

        connectButton.setOnClickListener {
            val gattServiceIntent = Intent(this, MyBLEService::class.java)
            bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            isServiceConnected = true
            Log.d(TAG,"try to connect")

        }

        disconnectButton.setOnClickListener {
            if (connected) {
                unbindService(serviceConnection)
                isServiceConnected = false
                Log.d(TAG, "try to disconnect")
            }
        }

        storeOneButton.setOnClickListener{
            if(connected){
                showInputDialog { enteredText ->
                    if (enteredText != null) {
                        // 用户输入了文本，可以在这里处理
                        GlobalScope.launch(Dispatchers.IO){
                            saveMeasurementAndDataPoint(storedUserEmail,characteristicValueView.text.toString(),enteredText)
                        }
                        Toast.makeText(this, "Entered text: $enteredText", Toast.LENGTH_SHORT).show()
                    } else {
                        // 用户取消了输入
                        Toast.makeText(this, "Input canceled", Toast.LENGTH_SHORT).show()
                    }
                }

            }else{
                Toast.makeText(this,"设备未连接，不能记录数据",Toast.LENGTH_LONG)
            }
        }

        storeManyButton.setOnClickListener {
            if(!recordAllowed){
                valueList = ArrayList<String>()
                mAdapter =CharacteristicActivityAdapter(this,valueList!!)
                characteristicInTimeListView.adapter =mAdapter
                recordAllowed = true
            }

            if(connected){
                storeDataHiddenButton.visibility = View.VISIBLE
                storeDataHiddenButton.isEnabled = true
            }
        }

        storeDataHiddenButton.setOnClickListener {

            val valuesToStore = mAdapter?.getCheckedItems()
            if (!valuesToStore.isNullOrEmpty()) {
                showInputDialog { enteredText ->
                    if (enteredText != null) {
                        // 用户输入了文本，可以在这里处理
                        GlobalScope.launch(Dispatchers.IO){
                            saveMeasurementAndDataPoints(storedUserEmail,valuesToStore,enteredText)
                        }
                        Toast.makeText(this, "Entered text: $enteredText", Toast.LENGTH_SHORT).show()
                    } else {
                        // 用户取消了输入
                        Toast.makeText(this, "Input canceled", Toast.LENGTH_SHORT).show()
                    }
                }

            }else{
                Toast.makeText(this, "No data to store", Toast.LENGTH_SHORT).show()
            }
            storeDataHiddenButton.visibility = View.INVISIBLE
            storeDataHiddenButton.isEnabled = false
            recordAllowed = false
            valueList!!.clear()
            mAdapter!!.notifyDataSetChanged()

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
            }
        if(recordAllowed){
            valueList!!.add(characteristicVal)
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isGattUpdateReceiverRegistered) {
            unregisterReceiver(gattUpdateReceiver)
            isGattUpdateReceiverRegistered = false
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        isGattUpdateReceiverRegistered = true
        if (bluetoothService != null) {
            val result = bluetoothService!!.connect(BLEaddress!!)
            Log.d(TAG, "Connect request result=$result")
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
        if (isGattUpdateReceiverRegistered) {
            unregisterReceiver(gattUpdateReceiver)
            isGattUpdateReceiverRegistered = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isServiceConnected){
            unbindService(serviceConnection)
            isServiceConnected = false
            Log.d(TAG, "unbindService called")
        }
    }

    private fun saveMeasurementAndDataPoint(userEmail: String, value: String, description: String = "Optional description") {
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

    private fun saveMeasurementAndDataPoints(userEmail: String, values: List<String>, description: String = "Optional description") {
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
        if (measurementId != null) {
            Log.d(TAG, "Successfully inserted into measures table")
        } else {
            Log.d(TAG, "Failed to insert into measures table, skipping further operations")
            return
        }

        // 创建 DataPoint 实体列表
        values.map { value ->
            val dataPoint = DataPoint(
                measurementId = measurementId,
                value = value
            )
            dataPointDao?.insertDataPoint(dataPoint)
        }

    }


    private fun showInputDialog(callback: (String?) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("输入测量值的名称")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val enteredText = input.text.toString()
            // 在这里处理用户输入的文本
            callback.invoke(enteredText)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            callback.invoke(null)
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }


}