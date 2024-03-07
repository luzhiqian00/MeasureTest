package com.example.myapplication.ble

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.myapplication.event.BLEEnum
import com.example.myapplication.event.BLEEvent
import com.example.myapplication.event.BLEServiceEvent
import com.example.myapplication.event.EMEventBus
import java.lang.StringBuilder
import java.util.*

class BLEService : Service() {
    /**
     * 伴生类
     */
    companion object{
        private const val TAG = "BLEService"
        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }

    private var mBluetoothManager // 蓝牙管理器
            : BluetoothManager? = null
    private var mBluetoothAdapter // 蓝牙适配器
            : BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null

    private val STATE_DISCONNECTED = 0
    private val STATE_CONNECTING = 1
    private val STATE_CONNECTED = 2

    private val mBinder: IBinder = LocalBinder()

    private var mConnectionState = STATE_DISCONNECTED


    val UUID_HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        // 连接/断开时发送广播
        /**
         * onConnectionStateChange 方法:
        在连接状态发生变化时调用，根据 newState 的值来处理连接状态的变化。
        如果 newState 是 BluetoothProfile.STATE_CONNECTED，表示已连接到 GATT 服务器，执行相应操作并发送连接成功的广播。
        如果 newState 是 BluetoothProfile.STATE_DISCONNECTED，表示已从 GATT 服务器断开连接，执行相应操作并发送断开连接的广播。
         */
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                //broadcastUpdate(intentAction)
                eventBusUpdate(BLEEnum.CONNECTED)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(
                    TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt!!.discoverServices()
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                eventBusUpdate(BLEEnum.DISCONNECTED)
            }
        }

        /**
         * onServicesDiscovered 方法:

        在发现蓝牙设备的服务时调用，根据 status 的值来处理服务发现的结果。
        如果 status 是 BluetoothGatt.GATT_SUCCESS，表示服务发现成功，发送服务发现成功的广播。
        如果 status 不是 BluetoothGatt.GATT_SUCCESS，可能发生了错误，记录警告日志。
         */
        // 发现设备时广播
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                eventBusUpdate(BLEEnum.SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        /**
         * onCharacteristicRead 方法:

        在读取特征值时调用，根据 status 的值来处理特征值读取的结果。
        如果 status 是 BluetoothGatt.GATT_SUCCESS，表示读取成功，执行相应操作并发送特征值可用的广播。
         */
        // 读取特征值时广播
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead: ")
                eventBusUpdate(BLEEnum.DATA_AVAILABLE, characteristic)
            }
        }

        /**
         * onCharacteristicWrite 方法:

        在写入特征值时调用，根据 status 的值来处理特征值写入的结果。
        如果 status 是 BluetoothGatt.GATT_SUCCESS，表示写入成功，记录调试日志。
         */
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite: sssss")
            }
        }

        /**
         * onCharacteristicChanged 方法
        当特征值发生变化时调用，根据变化的特征值执行相应操作并发送特征值可用的广播。
         */
        // 特征值改变时广播
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG, "onCharacteristicChanged: ")
            eventBusUpdate(BLEEnum.DATA_AVAILABLE, characteristic)
        }
    }

    private fun eventBusUpdate(action: BLEEnum){
        EMEventBus.post(BLEEvent(action))
    }

    /**
     * 更新事件总线，用于向应用程序的其他部分发送蓝牙相关事件和数据。
     *
     * @param action BLEEnum 枚举类型，表示蓝牙事件的类型。
     * @param characteristic BluetoothGattCharacteristic 对象，表示蓝牙特征值。
     */
    private fun eventBusUpdate(action: BLEEnum, characteristic: BluetoothGattCharacteristic){
        // 用于存储特征值数据的字符串
        var dataBLE = ""

        // 检查特征值的 UUID 是否为心率测量特征值
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            // 获取特征值的属性标志
            val flag = characteristic.properties
            // 定义格式变量
            var format = -1
            // 根据属性标志判断数据格式
            format = if (flag and 0x01 != 0) {
                BluetoothGattCharacteristic.FORMAT_UINT16
            } else {
                BluetoothGattCharacteristic.FORMAT_UINT8
            }
            // 从特征值中获取心率数据
            val heartRate = characteristic.getIntValue(format, 1)
            // 将心率数据转换为字符串
            dataBLE = heartRate.toString()
        } else {
            // 如果特征值不是心率测量特征值，将数据格式化为十六进制字符串
            val data = characteristic.value
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
                // 格式化数据字符串
                dataBLE = """
 ${String(data)}
 $stringBuilder
 """.trimIndent()
            }
        }
        // 向事件总线发布蓝牙事件和数据
        EMEventBus.post(BLEEvent(action,dataBLE))
    }

    // 广播
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        Log.d(TAG, "broadcastUpdate: ")
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind : 绑定")
        return mBinder
    }

    /**
     * 广播更新方法，用于发送带有特征值数据的广播。
     *
     * @param action 广播的操作动作。
     * @param characteristic BluetoothGattCharacteristic 对象，表示蓝牙特征值。
     */
    private fun broadcastUpdate(
        action: String,
        characteristic: BluetoothGattCharacteristic
    ) {
        // 创建一个意图对象，用于发送广播
        val intent = Intent(action)

        // 判断特征值的 UUID 是否为心率测量特征值
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            // 如果是心率测量特征值，进行特殊处理以解析数据
            val flag = characteristic.properties
            var format = -1
            // 根据属性标志判断数据格式
            if (flag and 0x01 != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                // 打印日志，表示数据格式为 UINT16
                Log.d(TAG, "Heart rate format UINT16.")
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                // 打印日志，表示数据格式为 UINT8
                Log.d(TAG, "Heart rate format UINT8.")
            }
            // 从特征值中获取心率数据
            val heartRate = characteristic.getIntValue(format, 1)
            // 打印日志，表示接收到的心率数据
            Log.d(TAG, String.format("Received heart rate: %d", heartRate))
            // 将心率数据作为额外数据放入意图中
            intent.putExtra(EXTRA_DATA, heartRate.toString())
        } else {
            // 如果特征值不是心率测量特征值，则将数据格式化为十六进制字符串
            val data = characteristic.value
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
                // 将数据作为额外数据放入意图中
                intent.putExtra(
                    EXTRA_DATA, """
 ${String(data)}
 $stringBuilder
 """.trimIndent()
                )
            }
        }
        // 发送广播
        sendBroadcast(intent)
    }

    /**
     * 当客户端与服务解绑时调用的方法。
     *
     * @param intent 解绑时传递的意图对象。
     * @return 返回一个布尔值，表示解绑操作是否成功。
     */
    override fun onUnbind(intent: Intent?): Boolean {
        // 在使用完特定设备后，应确保调用 BluetoothGatt.close() 方法，
        // 以便正确清理资源。在这个示例中，当 UI 与服务断开连接时调用 close() 方法。
        // close()
        // 打印日志，表示服务解绑操作
        Log.d("MeasureActivity", "onUnbind: ")
        // 调用父类的 onUnbind 方法
        return super.onUnbind(intent)
    }

    /**
     * 在服务启动时调用的方法。
     *
     * @param intent 接收到的意图对象，可能为 null。
     * @param flags 用于指定启动服务的行为。
     * @param startId 表示此次服务启动的唯一标识符。
     * @return 一个整数值，用于指定服务的行为。
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 打印日志，表示服务开始启动
        Log.d(TAG, "onStartCommand: onStartCommand")
        // 向事件总线发送 BLEServiceEvent 事件
        EMEventBus.post(BLEServiceEvent(mBinder))
        // 调用父类的 onStartCommand 方法，并返回其结果
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 初始化本地蓝牙适配器的引用。
     *
     * @return 如果初始化成功，则返回 true。
     */
    fun initialize(): Boolean {
        // 对于 API 等级 18 及以上版本，通过 BluetoothManager 获取对 BluetoothAdapter 的引用
        if (mBluetoothManager == null) {
            // 获取 BluetoothManager 对象
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                // 如果获取失败，则记录错误日志并返回 false
                Log.e(TAG, "无法初始化 BluetoothManager。")
                return false
            }
        }
        // 获取 BluetoothAdapter 对象
        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            // 如果获取失败，则记录错误日志并返回 false
            Log.e(TAG, "无法获取 BluetoothAdapter。")
            return false
        }
        // 记录日志，表示初始化成功
        Log.d(TAG, "初始化完成。")
        return true
    }

    /**
     * 连接到托管在蓝牙 LE 设备上的 GATT 服务器。
     *
     * @param address 目标设备的设备地址。
     *
     * @return 如果连接成功启动，则返回 true。连接结果通过异步方式通过
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * 回调报告。
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String?): Boolean {
        // 检查蓝牙适配器和设备地址是否已初始化
        if (mBluetoothAdapter == null || address == null) {
            // 如果蓝牙适配器未初始化或设备地址为空，则记录警告日志并返回 false
            Log.w(TAG, "BluetoothAdapter 未初始化或设备地址未指定。")
            return false
        }

        // 之前连接的设备。尝试重新连接。
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            // 如果之前已连接到设备，并且蓝牙 GATT 对象不为空，则尝试重用现有的 mBluetoothGatt 进行连接
            Log.d(TAG, "尝试使用现有的 mBluetoothGatt 进行连接。")
            return if (mBluetoothGatt!!.connect()) {
                // 如果连接成功，则更新连接状态为 STATE_CONNECTING 并返回 true
                mConnectionState = STATE_CONNECTING
                true
            } else {
                // 如果连接失败，则返回 false
                false
            }
        }
        // 记录日志，表示尝试连接到新设备
        Log.d(TAG, "connect: $address")
        // 获取远程蓝牙设备对象
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            // 如果获取设备失败，则记录警告日志并返回 false
            Log.w(TAG, "设备未找到。无法连接。")
            return false
        }
        // 我们希望直接连接到设备，因此将 autoConnect 参数设置为 false。
        // 连接到设备的 GATT 服务器，并设置回调接口为 mGattCallback
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        // 记录日志，表示正在尝试创建新连接
        Log.d(TAG, "尝试创建新连接。")
        // 更新当前设备的设备地址和连接状态为 STATE_CONNECTING，并返回 true
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * 断开现有连接或取消挂起连接。断开连接的结果通过异步方式通过
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * 回调报告。
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        // 检查蓝牙适配器和蓝牙 GATT 对象是否已初始化
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            // 如果蓝牙适配器或蓝牙 GATT 对象未初始化，则记录警告日志并返回
            Log.w(TAG, "BluetoothAdapter 未初始化")
            return
        }
        // 调用 BluetoothGatt 对象的 disconnect 方法断开连接
        mBluetoothGatt!!.disconnect()
    }

    /**
     * 在使用给定的 BLE 设备之后，应用程序必须调用此方法以确保资源正确释放。
     */
    @SuppressLint("MissingPermission")
    fun close() {
        // 检查蓝牙 GATT 对象是否为空
        if (mBluetoothGatt == null) {
            return
        }
        // 关闭蓝牙 GATT 对象，并将其设置为空
        Log.d(TAG, "close: -----------------------------")
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * 请求读取给定的 `BluetoothGattCharacteristic`。读取结果通过异步方式通过
     * `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * 回调报告。
     *
     * @param characteristic 要读取的特征值。
     */
    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        // 检查蓝牙适配器和蓝牙 GATT 对象是否已初始化
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            // 如果蓝牙适配器或蓝牙 GATT 对象未初始化，则记录警告日志并返回
            Log.w(TAG, "BluetoothAdapter 未初始化")
            return
        }
        // 调用 BluetoothGatt 对象的 readCharacteristic 方法读取特征值
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * 启用或禁用给定特征值的通知。
     *
     * @param characteristic 要操作的特征值。
     * @param enabled 如果为 true，则启用通知。否则为 false。
     */
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        // 检查蓝牙适配器和蓝牙 GATT 对象是否已初始化
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            // 如果蓝牙适配器或蓝牙 GATT 对象未初始化，则记录警告日志并返回
            Log.w(TAG, "BluetoothAdapter 未初始化")
            return
        }
        // 启用或禁用给定特征值的通知
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // 这是特定于心率测量的操作。
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGatt.CLIENT_CHARACTERISTIC_CONFIG)
            )
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    /**
     * 检索连接设备上支持的 GATT 服务的列表。这应该只在 `BluetoothGatt#discoverServices()` 成功完成后调用。
     *
     * @return 支持的服务的 `List`。
     */
    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return if (mBluetoothGatt == null) null else mBluetoothGatt!!.services
    }


    /**
     * LocalBinder 是一个内部类，它继承自 Binder 类，
     * 用于提供一个公共方法，使外部组件可以获取 BLEService 实例。
     */
    inner class LocalBinder : Binder() {
        fun getService():BLEService {
            return this@BLEService
        }
    }



}