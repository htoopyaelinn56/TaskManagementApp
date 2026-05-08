package com.example.taskmanagementapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagementapp.R
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.model.formatMetric

class GoalAdapter(
    private val items: List<Goal>
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_card, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.goal_title)
        private val meta = itemView.findViewById<TextView>(R.id.goal_meta)

        fun bind(item: Goal) {
            title.text = item.name
            meta.text = buildString {
                append(item.activityType)
                val metricText = formatMetric(item.targetMetric)
                if (!metricText.isNullOrBlank()) {
                    append(" | Target: ")
                    append(metricText)
                }
                if (item.deadline.isNotBlank()) {
                    append(" | Due: ")
                    append(item.deadline)
                }
            }
        }
    }
}

