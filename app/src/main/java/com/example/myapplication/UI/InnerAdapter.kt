package com.example.myapplication.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.dataPointModel.DataPoint

class InnerAdapter(private val innerData: List<DataPoint>) : RecyclerView.Adapter<InnerAdapter.InnerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.inner_item_layout, parent, false)
        return InnerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        val dataPoint = innerData[position]
        holder.bind(dataPoint)
    }

    override fun getItemCount(): Int {
        return innerData.size
    }

    inner class InnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val valueTextView: TextView = itemView.findViewById(R.id.dataPointValueTextView)

        fun bind(dataPoint: DataPoint) {
            // 绑定数据到视图
            valueTextView.text = "Value: ${dataPoint.value}"
        }
    }
}