package com.jamesmobiledev.beeontime.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.model.LeaveRequest

class RequestAdapter : RecyclerView.Adapter<RequestAdapter.VH>() {
    private val list: ArrayList<LeaveRequest> = arrayListOf()

    var onClickAccept: ((Int) -> Unit)? = null
    var onClickReject: ((Int) -> Unit)? = null

    fun submitList(newList: ArrayList<LeaveRequest>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_leave_request, parent, false)
        )
    }

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val request = list[position]
        holder.apply {
            tvName.text = request.name
            tvFrom.text = request.from
            tvTo.text = request.to
            tvReason.text = "Reason : ${request.reason}"

            llAccept.setOnClickListener {
                onClickAccept?.invoke(position)
            }

            llReject.setOnClickListener {
                onClickReject?.invoke(position)
            }
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvFrom: TextView = view.findViewById(R.id.tvFrom)
        val tvTo: TextView = view.findViewById(R.id.tvTo)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val llAccept: LinearLayout = view.findViewById(R.id.llAccept)
        val llReject: LinearLayout = view.findViewById(R.id.llReject)
    }
}