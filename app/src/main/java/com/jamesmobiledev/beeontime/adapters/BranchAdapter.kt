package com.jamesmobiledev.beeontime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jamesmobiledev.beeontime.R
import com.jamesmobiledev.beeontime.model.Branch

class BranchAdapter : RecyclerView.Adapter<BranchAdapter.VH>() {

    private val list = arrayListOf<Branch>()

    var onItemClick: ((Int) -> Unit)? = null

    fun submitList(newList: ArrayList<Branch>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_branch, parent, false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val branch = list[position]
        holder.apply {
            tvName.text = branch.name
            tvOpen.text = branch.open
            tvClose.text = branch.close

            item.setOnClickListener {
                onItemClick?.invoke(position)
            }
        }
    }


    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.item)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvOpen = view.findViewById<TextView>(R.id.tvOpen)
        val tvClose = view.findViewById<TextView>(R.id.tvClose)
    }


}