package com.example.taskmanagementapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagementapp.R
import com.example.taskmanagementapp.model.ActivityEntry
import com.example.taskmanagementapp.model.formatMetric

class ActivityAdapter(
    private val items: List<ActivityEntry>
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_card, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.activity_title)
        private val metrics = itemView.findViewById<TextView>(R.id.activity_metrics)
        private val meta = itemView.findViewById<TextView>(R.id.activity_meta)

        fun bind(item: ActivityEntry) {
            title.text = item.type
            metrics.text = buildString {
                append("Duration: ")
                append(item.durationMinutes)
                append(" min")
                val metricText = formatMetric(item.metric)
                if (!metricText.isNullOrBlank()) {
                    append(" | Metric: ")
                    append(metricText)
                }
                append(" | Calories: ")
                append(item.caloriesKcal)
                append(" kcal")
            }
            meta.text = "Date: ${item.date} | Location: ${item.location}"
        }
    }
}
