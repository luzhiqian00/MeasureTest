package com.example.myapplication.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.MeasurementData
import com.example.myapplication.model.dataPointModel.DataPoint
import org.w3c.dom.Text

class OuterAdapter(private var outerData: List<MeasurementData>) : RecyclerView.Adapter<OuterAdapter.ViewHolder>() {

    var showCheckBoxes = false
    private var checkedIdList = ArrayList<Long>()

    fun getcheckIdList():ArrayList<Long>{
        return checkedIdList
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val measurementId:TextView = view.findViewById(R.id.measurementIdTextView)
        val innerRecyclerView: RecyclerView = view.findViewById(R.id.innerRecyclerView)
        val measurementDescription:TextView = view.findViewById(R.id.measurementDescription)
        val checkBox:CheckBox = view.findViewById(R.id.measurementSelectionCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.outer_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val measurement = outerData[position]
        holder.measurementId.text = measurement.measurementId.toString()
        holder.measurementDescription.text = measurement.description
        val innerDataList = measurement.dataPoints
        holder.innerRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.innerRecyclerView.adapter = InnerAdapter(innerDataList!!)

        holder.checkBox.visibility = if (showCheckBoxes) View.VISIBLE else View.GONE

        // 为 CheckBox 设置监听器
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 如果 CheckBox 被选中，并且 measurementId 尚未添加到 checkedIdList 中，则添加
                measurement.measurementId?.let {
                    if (!checkedIdList.contains(it)) {
                        checkedIdList.add(it)
                    }
                }
            } else {
                // 如果 CheckBox 被取消选中，则将当前项的 measurementId 从 checkedIdList 中移除
                checkedIdList.remove(measurement.measurementId)
            }
        }
    }


    override fun getItemCount() = outerData.size
    fun setMeasurementDatas(mMeasurementDatas: ArrayList<MeasurementData>) {
        outerData = mMeasurementDatas
    }

}



