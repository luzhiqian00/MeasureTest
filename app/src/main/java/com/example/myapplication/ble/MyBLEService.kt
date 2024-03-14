package com.example.myapplication.ble

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


class MyBLEService : Service() {
    companion object {
        private const val TAG = "MyBLEService"
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE=
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"

        const val EXTRA_RAW_DATA = "com.example.bluetooth.le.EXTRA_RAW_DATA"
        const val EXTRA_HEX_DATA ="com.example.bluetooth.le.EXTRA_HEX_DATA"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
        const val EXTRA_UUID = "com.example.bluetooth.le.EXTRA_UUID"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }


    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : MyBLEService {
            return this@MyBLEService
        }
    }

    //检查蓝牙适配器是否在当前android设备上可用
    private var bluetoothAdapter: BluetoothAdapter? = null

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }




    private var mBluetoothGatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED


    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                broadcastUpdate(ACTION_GATT_CONNECTED)
                connectionState = STATE_CONNECTED
                Log.i(TAG,"Attempting to start service discovery:"
                        + mBluetoothGatt?.discoverServices())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                connectionState = STATE_DISCONNECTED
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.services?.forEach { gattService ->
                    val characteristics = gattService.characteristics
                    characteristics.forEach { characteristic ->
                        val success = gatt.setCharacteristicNotification(characteristic, true)
                        if (success) {
                            characteristic.descriptors?.forEach { descriptor ->
                                val b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                if (b1) {
                                    gatt.writeDescriptor(descriptor)
                                    Log.i(TAG,"descriptor set successfully")
                                }
                            }
                            Log.i(TAG,"setCharacteristicNotification successfully ")
                        }
                    }
                }

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.i(TAG, "onServicesDiscovered recall successfully")
            } else {
                Log.i(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG,"detect the change in characteristics")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        mBluetoothGatt?.let { gatt ->
            Log.i("TAG","open the Notification")
            gatt.setCharacteristicNotification(characteristic, enabled)
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
        }
    }



    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return mBluetoothGatt?.services
    }

    //connect到蓝牙设备
    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                mBluetoothGatt =device.connectGatt(this,false,bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.  Unable to connect.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect(){
        mBluetoothGatt?.let { gatt ->
            gatt.disconnect()
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    /**
     * 发送广播通知，将特征数据格式化为十六进制字符串后发送。
     *
     * @param action 广播动作
     * @param characteristic 蓝牙特征对象
     */
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        // 创建一个用于发送广播的 Intent 对象，指定动作为传入的 action
        val intent = Intent(action)

        // 从特征对象中获取数据
        val data: ByteArray? = characteristic.value

        // 检查数据是否非空
        if (data?.isNotEmpty() == true) {
            // 将字节数组格式化为十六进制字符串
            val hexString: String = data.joinToString(separator = " ") {
                String.format("%02X", it)
            }

            intent.putExtra(EXTRA_UUID,characteristic.uuid.toString())
//            // 将原始字节数组和十六进制字符串作为附加数据放入 Intent 中
//            intent.putExtra(EXTRA_DATA, "$data\n$hexString")
            // 将原始字节数组作为附加数据放入 Intent 中
            intent.putExtra(EXTRA_RAW_DATA, data)
            // 将十六进制字符串作为附加数据放入 Intent 中
            intent.putExtra(EXTRA_HEX_DATA, hexString)
        }

        // 发送广播通知，携带着格式化后的特征数据
        sendBroadcast(intent)
    }



    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    @SuppressLint("MissingPermission")
    private fun close() {
        mBluetoothGatt?.let { gatt ->
            gatt.disconnect()
            //不能增加gatt.close()，我也不知道为什么，可能增加了之后，就不会有状态的变化了。没有状态的变化，就会导致页面不能更新连接状态
        }
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        mBluetoothGatt?.let { gatt ->
            Log.d(TAG, "readCharacteristic operation...")
            gatt.readCharacteristic(characteristic)
        } ?: run {
            Log.w(TAG, "BluetoothGatt not initialized")
            return
        }
    }

}