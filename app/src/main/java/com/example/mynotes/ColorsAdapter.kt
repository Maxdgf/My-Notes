package com.example.mynotes

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class ColorsAdapter(context: Context, private val dataList: ArrayList<ColorItemData>) : ArrayAdapter<ColorItemData>(context, 0, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return customView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return customView(position, convertView, parent)
    }

    private fun customView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.color_spinner_item, parent, false)

        val list = dataList[position]
        val description: TextView = view.findViewById(R.id.colorName)
        val colorImg: ImageView = view.findViewById(R.id.colorIcon)

        description.text = list.nameColor
        colorImg.setColorFilter(Color.parseColor(list.tintColor))

        return view
    }
}