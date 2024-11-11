package com.jamesmobiledev.beeontime.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import com.jamesmobiledev.beeontime.R
import java.util.Objects


class NoFilterArrayAdapter<T>(
    context: Context?,
    private var resource: Int,
    mItems: Array<T>,
    private val setItemText: (item: String) -> Unit
) :
    ArrayAdapter<T>(context!!, resource, mItems) {
    private val items: List<T>

    init {
        items = mItems.toList()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(resource, null)
        }

        val item = getItem(position)
        if (item != null) {
            val textView = view!!.findViewById<TextView>(R.id.text_view_item)
            textView.text = item.toString()

            textView.setOnClickListener {
                setItemText(item.toString())
            }
        }
        return view!!
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                results.values = items
                results.count = items.size
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                return resultValue.toString()
            }
        }
    }
}

