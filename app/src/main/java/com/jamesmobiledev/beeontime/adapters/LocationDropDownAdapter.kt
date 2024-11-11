package com.jamesmobiledev.beeontime.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.jamesmobiledev.beeontime.R


class LocationDropDownAdapter(
    private val mContext: Context,
    private val resourceLayout: Int,
    private val items: ArrayList<String>,
//    private val removeLocation: (location: String) -> Unit,
    private val selectLocation: (Int) -> Unit
) :
    ArrayAdapter<String>(mContext, resourceLayout, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = LayoutInflater.from(mContext)
            view = inflater.inflate(resourceLayout, null)
        }
        val item = getItem(position)
        if (item != null) {
            val textView = view!!.findViewById<TextView>(R.id.text_view_item)
            val deleteButton = view.findViewById<ImageButton>(R.id.button_delete)
            textView.text = item

            deleteButton.setOnClickListener {
//                remove(item)
//                removeLocation(item)
            }

            textView.setOnClickListener {
                selectLocation(position)
            }

        }
        return view!!
    }
}

