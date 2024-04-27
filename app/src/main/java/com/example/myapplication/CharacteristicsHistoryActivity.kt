package com.example.myapplication

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
import com.example.myapplication.model.dataPointModel.DataPoint
import kotlinx.coroutines.*


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

    override fun onOptionsItemSelected(item: MenuItem):Boolean{
        when(item.itemId){
            R.id.delete_items->{
                mActionButton.visibility = View.VISIBLE
                toggleCheckBoxVisibility(true)
                return true
            }
            R.id.upload_items->{
                return true
            }
            R.id.download_items->{
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