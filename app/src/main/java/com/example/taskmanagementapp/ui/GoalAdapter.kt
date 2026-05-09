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
    private val items: List<Goal>,
    private val showDelete: Boolean = false,
    private val onDelete: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_card, parent, false)
        return GoalViewHolder(view, showDelete, onDelete)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GoalViewHolder(itemView: View, private val showDelete: Boolean, private val onDelete: ((Int) -> Unit)?) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.goal_title)
        private val meta = itemView.findViewById<TextView>(R.id.goal_meta)
        private val status = itemView.findViewById<TextView>(R.id.goal_status)
        private val deleteBtn = itemView.findViewById<android.widget.ImageButton>(R.id.goal_delete_btn)

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
            val progressText = item.progress?.let { p ->
                val pct = (p * 100).toInt()
                " | Progress: $pct%"
            } ?: ""
            status.text = "Status: ${item.status}$progressText"

            if (showDelete && item.id != null) {
                deleteBtn.visibility = View.VISIBLE
                deleteBtn.setOnClickListener {
                    onDelete?.invoke(item.id)
                }
            } else {
                deleteBtn.visibility = View.GONE
                deleteBtn.setOnClickListener(null)
            }
        }
    }
}
