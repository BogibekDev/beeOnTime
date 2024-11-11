package com.jamesmobiledev.beeontime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.model.Attendance

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.VH>() {

    private val list: ArrayList<Attendance> = arrayListOf()
    fun submitList(newList: ArrayList<Attendance>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_attendance, parent, false
        )
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val attendance = list[position]
        holder.apply {
            tVDate.text = attendance.date
            tvIn.text = attendance.inTime
            tvOut.text = attendance.outTime
            tvLocation.text = attendance.branchName
            item.setOnClickListener {

            }

        }

    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.item)
        val tVDate = view.findViewById<TextView>(R.id.tvDate)
        val tvIn = view.findViewById<TextView>(R.id.tvIn)
        val tvOut = view.findViewById<TextView>(R.id.tvOut)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
    }
}