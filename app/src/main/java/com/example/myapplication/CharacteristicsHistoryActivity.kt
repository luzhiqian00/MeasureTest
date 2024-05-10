package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginlibrary.secure.SecureStorage
import com.example.myapplication.UI.OuterAdapter
import com.example.myapplication.databinding.ActivityCharacteristicsHistoryBinding
import com.example.myapplication.model.AppDatabase
import com.example.myapplication.model.MeasurementData
import com.example.myapplication.model.MeasurementsResponse
import com.example.myapplication.model.dataPointModel.DataPoint
import com.example.myapplication.model.measureResultModel.Measurement
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.util.concurrent.TimeUnit


class CharacteristicsHistoryActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CharacteristicsHistoryActivity"
    }
    
    private val binding by lazy { ActivityCharacteristicsHistoryBinding.inflate(layoutInflater) }
    private val outerRecyclerView by lazy { binding.outerRecyclerView }
    private val mToolbar by lazy { binding.characteristicHistoryToolbar }
    private val mActionButton by lazy { binding.actionButton }

    private var storedUserEmail : String? =null
    private var mMeasurementDatas = ArrayList<MeasurementData>()
    private var mOuterAdapter:OuterAdapter? =null
    private var refreshJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SecureStorage.init(this)
        storedUserEmail = SecureStorage.decrypt("userEmailKey")

        setSupportActionBar(mToolbar)
        mToolbar.title = "测量历史记录"


        GlobalScope.launch(Dispatchers.IO) {
            mMeasurementDatas = queryMeasurement(storedUserEmail!!)
            withContext(Dispatchers.Main) {
                mOuterAdapter = OuterAdapter(mMeasurementDatas)
                outerRecyclerView.layoutManager = LinearLayoutManager(this@CharacteristicsHistoryActivity)
                outerRecyclerView.adapter = mOuterAdapter
            }
        }

//        startRefreshCoroutine()

        mActionButton.setOnClickListener {
            val idList = mOuterAdapter?.getcheckIdList()
            when(selectedTask){
                "delete" -> {
                    if (!idList.isNullOrEmpty()){
                        GlobalScope.launch(Dispatchers.IO) {
                            val measurementDao = AppDatabase.getDatabase(this@CharacteristicsHistoryActivity).measurementDao()
                            measurementDao?.deleteMeasurementsByIds(idList)
                            // 更新测量数据
                            mMeasurementDatas = queryMeasurement(storedUserEmail!!)

                            // 在主线程中更新 UI
                            runOnUiThread {
                                // 恢复动作按钮的可见性
                                mActionButton.visibility = View.GONE
                                // 通知适配器数据已更改
                                mOuterAdapter?.setMeasurementDatas(mMeasurementDatas)
                                toggleCheckBoxVisibility(false)
                                mOuterAdapter?.notifyDataSetChanged()
                            }
                        }
                        Toast.makeText(this, "已经删除成功", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "没有任何一项被选中", Toast.LENGTH_SHORT).show()
                    }
                }
                "upload" -> {
                    if (!idList.isNullOrEmpty()){
                        uploadData()
                    }else{
                        Toast.makeText(this, "没有任何一项被选中", Toast.LENGTH_SHORT).show()
                    }
                    // 在主线程中更新 UI
                    runOnUiThread {
                        // 恢复动作按钮的可见性
                        mActionButton.visibility = View.GONE
                        // 通知适配器数据已更改
                        mOuterAdapter?.setMeasurementDatas(mMeasurementDatas)
                        toggleCheckBoxVisibility(false)
                        mOuterAdapter?.notifyDataSetChanged()
                    }
                }
                "download" ->{

                }
                else ->{
                    Toast.makeText(this, "请选择一个任务", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

//    private fun startRefreshCoroutine() {
//        refreshJob = GlobalScope.launch {
//            while (isActive) {
//                delay(1000) // 延迟1秒
//                withContext(Dispatchers.Main) {
//                    mOuterAdapter?.notifyDataSetChanged()
//                }
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        refreshJob?.cancel() // 在 Activity 销毁时取消刷新协程
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_action_menu,menu)
        return  true
    }

    var selectedTask: String = ""

    override fun onOptionsItemSelected(item: MenuItem):Boolean{
        when(item.itemId){
            R.id.delete_items->{
                mActionButton.visibility = View.VISIBLE
                toggleCheckBoxVisibility(true)
                selectedTask = "delete"
                return true
            }
            R.id.upload_items->{
                mActionButton.visibility = View.VISIBLE
                toggleCheckBoxVisibility(true)
                selectedTask = "upload"
                uploadData()
                return true
            }
            R.id.download_items->{
                selectedTask = "download"
                downloadData()  // 调用下载数据的方法
                return true
            }
        }
        return true
    }



    private fun toggleCheckBoxVisibility(showCheckBoxes: Boolean? = null) {
        // 如果 showCheckBoxes 参数没有指定，则自动取反 mOuterAdapter 的 showCheckBoxes 属性
        val newValue = showCheckBoxes ?: !(mOuterAdapter?.showCheckBoxes ?: false)

        mOuterAdapter?.showCheckBoxes = newValue

        mOuterAdapter?.notifyDataSetChanged() // Notify the RecyclerView about the change
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
                    it,userEmail
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

    private fun uploadData() {
        val idList = mOuterAdapter?.getcheckIdList()
        if (!idList.isNullOrEmpty()) {
            // 构建要发送的数据
            val sendData = constructSendData(idList)
            // 发送数据到后端
            DataSender().sendDataToBackend(sendData)
            Toast.makeText(this, "数据已上传", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有任何一项被选中", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadData() {
        // 确保 userEmail 不为空
        storedUserEmail?.let { email ->
            val url = "http://39.105.8.110:6666/download_data?userEmail=${Uri.encode(email)}" // 使用 Uri.encode 确保 URL 安全
            DataSender().fetchDataFromBackend(url) { downloadedData ->
                CoroutineScope(Dispatchers.IO).launch {
                    val database = AppDatabase.getDatabase(this@CharacteristicsHistoryActivity)
                    val measurementDao = database.measurementDao()
                    val dataPointDao = database.dataPointDao()

                    // 开始数据库事务
                    database.runInTransaction {
                        downloadedData.forEach { measurementData ->
                            // 查询数据库中是否已经存在相同的measurementId
                            val existingMeasurement = measurementDao?.doesMeasurementExist(measurementData.measurementId.toString(),measurementData.userEmail.toString())

                            // 如果测量数据不存在，则插入
                            if (existingMeasurement==false) {
                                // 构造 Measurement 对象
                                val measurement = Measurement().apply {
                                    measurementId = measurementData.measurementId.toString()
                                    userEmail = measurementData.userEmail.toString()
                                    timestamp = measurementData.timestamp!!
                                    description = measurementData.description
                                }

                                // 插入测量数据
                                measurementDao?.insertMeasurement(measurement)

                                measurementData.dataPoints?.forEach { dataPoint ->
                                    dataPointDao?.insertDataPoint(dataPoint) // 插入数据点
                                }
                            }
                        }
                    }

                    // 重新查询数据库以更新UI
                    mMeasurementDatas = queryMeasurement(email)
                    withContext(Dispatchers.Main) {
                        mOuterAdapter?.setMeasurementDatas(mMeasurementDatas)
                        mOuterAdapter?.notifyDataSetChanged()
                    }
                }
            }
        } ?: Toast.makeText(this, "用户未登录或邮箱未知", Toast.LENGTH_SHORT).show()
    }


    private fun constructSendData(idList: List<String>): List<MeasurementData> {
        val sendData = ArrayList<MeasurementData>()

        idList.forEach { id ->
            val measurement = mMeasurementDatas.find { it.measurementId == id }
            measurement?.let {
                sendData.add(it)
            }
        }
        return sendData
    }


    inner class DataSender {
        private val client = OkHttpClient()

        inner class BackendSender {
            fun sendData(data: Any) {
                val url = "http://39.105.8.110:6666/upload_data"
                val json = Gson().toJson(data)

                val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")
                            println(response.body?.string())
                        }
                    }
                })
            }
        }

        fun fetchDataFromBackend(url: String, onComplete: (List<MeasurementData>) -> Unit) {
            val request = Request.Builder().url(url).build()
            val client = if (BuildConfig.DEBUG) {
                OkHttpClient.Builder()
                    .connectTimeout(1000, TimeUnit.SECONDS)
                    .readTimeout(1000, TimeUnit.SECONDS)
                    .build()
            } else {
                OkHttpClient()
            }



            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) throw IOException("Unexpected code $response")

                        val jsonData = it.body?.string()
                        val gson = Gson()
                        val measurementsResponse: MeasurementsResponse = gson.fromJson(jsonData, MeasurementsResponse::class.java)

                        // 现在可以访问measurementsResponse.measurements来获取数据列表
                        val data: List<MeasurementData> = measurementsResponse.measurements
                        onComplete(data)
                    }
                }
            })
        }


        fun sendDataToBackend(data: Any) {
            val sender = BackendSender()
            sender.sendData(data)
        }
    }
}