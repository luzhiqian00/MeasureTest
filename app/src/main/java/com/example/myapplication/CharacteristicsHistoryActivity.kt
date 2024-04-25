package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginlibrary.secure.SecureStorage
import com.example.myapplication.UI.OuterAdapter
import com.example.myapplication.databinding.ActivityCharacteristicsHistoryBinding
import com.example.myapplication.model.AppDatabase
import com.example.myapplication.model.DataPointData
import com.example.myapplication.model.MeasurementData
import com.example.myapplication.model.dataPointModel.DataPoint
import com.example.myapplication.model.measureResultModel.Measurement
import kotlinx.coroutines.*

class CharacteristicsHistoryActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CharacteristicsHistoryActivity"
    }
    
    private val binding by lazy { ActivityCharacteristicsHistoryBinding.inflate(layoutInflater) }
    private val outerRecyclerView by lazy { binding.outerRecyclerView }
    private var storedUserEmail : String? =null
    private var mMeasurementDatas = ArrayList<MeasurementData>()
    private var mOuterAdapter:OuterAdapter? =null
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SecureStorage.init(this)
        storedUserEmail = SecureStorage.decrypt("userEmailKey")


        GlobalScope.launch(Dispatchers.IO) {
            mMeasurementDatas = queryMeasurement(storedUserEmail!!)
            withContext(Dispatchers.Main) {
                mOuterAdapter = OuterAdapter(mMeasurementDatas)
                outerRecyclerView.layoutManager = LinearLayoutManager(this@CharacteristicsHistoryActivity)
                outerRecyclerView.adapter = mOuterAdapter
            }
        }

        startRefreshCoroutine()
    }

    private fun startRefreshCoroutine() {
        refreshJob = GlobalScope.launch {
            while (isActive) {
                delay(1000) // 延迟1秒
                withContext(Dispatchers.Main) {
                    mOuterAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshJob?.cancel() // 在 Activity 销毁时取消刷新协程
    }

    private fun queryMeasurement(userEmail: String) : ArrayList<MeasurementData> {
        val db = AppDatabase.getDatabase(MeasureApplication.context)
        val measurementDao = db.measurementDao()
        val dataPointDao = db.dataPointDao()
        var mMeasurementDatas = ArrayList<MeasurementData>()
        
        var measurements =  measurementDao?.getMeasurementsForUser(userEmail)
        measurements?.forEach { measurement ->

            // 根据 measurementId 获取该测量的数据点历史记录q
            val dataPoints = measurement?.measurementId?.let {
                dataPointDao?.getDataPointsForMeasurement(
                    it
                )
            } ?: emptyList()

            val dataPointDataList = ArrayList<DataPoint>()

            for (dataPoint in dataPoints) {
                // 创建 DataPointData 对象
                dataPointDataList.add(dataPoint!!)
            }
            val measureMentData = MeasurementData(
                measurement?.measurementId,
                measurement?.userEmail,
                measurement?.timestamp,
                measurement?.description,
                dataPointDataList)

            // 将 dataPointDataList 添加到 measurementData 中
            mMeasurementDatas.add(measureMentData)

        }
        return mMeasurementDatas

        
    }


}