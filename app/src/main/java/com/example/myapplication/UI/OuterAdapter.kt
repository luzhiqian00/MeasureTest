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

class OuterAdapter(private var outerData: List<MeasurementData>) : RecyclerView.Adapter<OuterAdapter.ViewHolder>() {

    var showCheckBoxes = false
    private var checkedIdList = ArrayList<String>()

    fun getcheckIdList(): ArrayList<String> {
        return checkedIdList
    }

    fun clearSelection() {
        checkedIdList.clear()
        notifyDataSetChanged() // 通知适配器数据集已更改
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val measurementId: TextView = view.findViewById(R.id.measurementIdTextView)
        val innerRecyclerView: RecyclerView = view.findViewById(R.id.innerRecyclerView)
        val measurementDescription: TextView = view.findViewById(R.id.measurementDescription)
        val checkBox: CheckBox = view.findViewById(R.id.measurementSelectionCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.outer_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val measurement = outerData[position]
        holder.measurementId.text = measurement.measurementId
        holder.measurementDescription.text = measurement.description
        val innerDataList = measurement.dataPoints
        holder.innerRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.innerRecyclerView.adapter = InnerAdapter(innerDataList!!)

        holder.checkBox.visibility = if (showCheckBoxes) View.VISIBLE else View.GONE

        holder.checkBox.setOnCheckedChangeListener(null) // 避免重用时触发旧的监听器

        holder.checkBox.isChecked = checkedIdList.contains(measurement.measurementId)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                measurement.measurementId?.let {
                    if (!checkedIdList.contains(it)) {
                        checkedIdList.add(it)
                    }
                }
            } else {
                checkedIdList.remove(measurement.measurementId)
            }
        }
    }

    override fun getItemCount() = outerData.size

    fun setMeasurementDatas(mMeasurementDatas: ArrayList<MeasurementData>) {
        outerData = mMeasurementDatas
        notifyDataSetChanged()
    }
}
