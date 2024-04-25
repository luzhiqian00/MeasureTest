package com.example.myapplication.UI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.model.characteristicModel.Characteristic

class CharacteristicActivityAdapter(
    private val context: Context,
    private val valueList: ArrayList<String>
) : BaseAdapter() {

    private val checkedPositions = HashSet<Int>()

    fun addItem(value: String) {
        valueList.add(value)
    }

    fun toggleItem(position: Int) {
        if (checkedPositions.contains(position)) {
            checkedPositions.remove(position)
        } else {
            checkedPositions.add(position)
        }
        notifyDataSetChanged()
    }

    fun getCheckedItems(): List<String> {
        val checkedItems = ArrayList<String>()
        for (position in checkedPositions) {
            checkedItems.add(valueList[position])
        }
        return checkedItems
    }

    override fun getCount(): Int {
        return valueList.size
    }

    override fun getItem(position: Int): Any {
        return valueList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.check_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.measurementValTextView.text = valueList[position]
        viewHolder.selectionCheckBox.isChecked = checkedPositions.contains(position)

        viewHolder.selectionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedPositions.add(position)
            } else {
                checkedPositions.remove(position)
            }
        }

        return view
    }

    private class ViewHolder(view: View) {
        val measurementValTextView: TextView = view.findViewById(R.id.measurementValTextView)
        val selectionCheckBox: CheckBox = view.findViewById(R.id.selectionCheckBox)
    }
}
