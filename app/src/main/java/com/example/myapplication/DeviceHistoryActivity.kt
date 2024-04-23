package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.UI.CharacteristicDataAdapter
import com.example.myapplication.UI.DeviceHistoryAdapter
import com.example.myapplication.databinding.ActivityDeviceBinding
import com.example.myapplication.databinding.ActivityDeviceHistoryBinding
import com.example.myapplication.model.characteristicModel.AppDatabase
import com.example.myapplication.model.characteristicModel.Characteristic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class DeviceHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DeviceHistoryActivity"
    }

    private val binding by lazy { ActivityDeviceHistoryBinding.inflate(layoutInflater) }
    private val characteristicListView by lazy { binding.characteristicListView }
    private val deviceHistoryAdapter by lazy { DeviceHistoryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        characteristicListView.adapter = deviceHistoryAdapter
        displayCharacteristicHistory()
    }

    private fun displayCharacteristicHistory() {
        val userDao = AppDatabase.getDatabase(this).characteristicDao()

        thread {
            val characteristics = userDao.getAllCharacteristics()
            deviceHistoryAdapter.setCharacteristicList(characteristics as ArrayList<Characteristic>)
            deviceHistoryAdapter.notifyDataSetChanged() // 通知适配器数据已更新
        }

    }
}