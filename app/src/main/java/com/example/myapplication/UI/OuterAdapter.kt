package com.example.myapplication.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.MeasurementData
import com.example.myapplication.model.dataPointModel.DataPoint
import org.w3c.dom.Text

class OuterAdapter(private val outerData: List<MeasurementData>) : RecyclerView.Adapter<OuterAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val measurementId:TextView = view.findViewById(R.id.measurementIdTextView)
        val innerRecyclerView: RecyclerView = view.findViewById(R.id.innerRecyclerView)
        val measurementDescription:TextView = view.findViewById(R.id.measurementDescription)
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
    }

    override fun getItemCount() = outerData.size
}



