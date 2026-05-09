package com.example.taskmanagementapp.ui

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VH : BaseViewHolder<T>>(protected var items: List<T>) : RecyclerView.Adapter<VH>() {
    override fun getItemCount(): Int = items.size

    open fun updateItems(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
}

