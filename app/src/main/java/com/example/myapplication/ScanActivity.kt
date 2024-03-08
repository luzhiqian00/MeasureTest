package com.example.myapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.myapplication.UI.LeDeviceListAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityScanBinding

class ScanActivity : AppCompatActivity() {
    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "ScanActivity"

    }

    // 使用懒加载初始化绑定视图
    private val binding by lazy { ActivityScanBinding.inflate(layoutInflater) }
    private val deviceListView by lazy { binding.deviceListView }
    private val scanButton by lazy { binding.scanButton }
    private val leDeviceListAdapter by lazy { LeDeviceListAdapter() }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        deviceListView.adapter = leDeviceListAdapter
        // 设置按钮点击事件监听器

        scanButton.setOnClickListener {
            // 开始或停止扫描
            scanLeDevice()
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(result.device.toString(),"BLEMeasure")
            super.onScanResult(callbackType, result)
            leDeviceListAdapter.addDevice(result.device)
            leDeviceListAdapter.notifyDataSetChanged()
        }
    }

}