package com.jamesmobiledev.beeontime.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.extensions.isShow
import com.jamesmobiledev.beeontime.model.User

class StaffAdapter : RecyclerView.Adapter<StaffAdapter.VH>() {

    var onClickItem: ((Int) -> Unit)? = null

    private val list: ArrayList<User> = arrayListOf()
    fun submitList(newList: ArrayList<User>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return VH(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val staff = list[position]
        holder.apply {
            tvName.text = "${staff.name} ${staff.lastName}"
            ivIndicator.isShow(staff.isRequested)
            item.setOnClickListener {
                onClickItem?.invoke(position)
            }
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val ivIndicator = view.findViewById<ImageView>(R.id.ivIndicator)
        val item = view.findViewById<LinearLayout>(R.id.item)
    }

}