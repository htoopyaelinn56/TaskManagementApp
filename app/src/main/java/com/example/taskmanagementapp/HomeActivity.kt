package com.example.taskmanagementapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.home_toolbar)
        setSupportActionBar(toolbar)

        val recentList = findViewById<RecyclerView>(R.id.home_recent_list)
        recentList.layoutManager = LinearLayoutManager(this)
        recentList.adapter = ActivityAdapter(buildSampleActivities())
    }

    private fun buildSampleActivities(): List<ActivityEntry> {
        return listOf(
            ActivityEntry("Run", "22 min", "3.4 km", "2026-05-06", "210 kcal", "City Park"),
            ActivityEntry("Cycling", "38 min", "12.1 km", "2026-05-05", "360 kcal", "Riverside Trail"),
            ActivityEntry("Weightlifting", "45 min", null, "2026-05-05", "280 kcal", "Iron Gym"),
            ActivityEntry("Yoga", "30 min", null, "2026-05-04", "120 kcal", "Studio A"),
            ActivityEntry("Walk", "28 min", "2.2 km", "2026-05-04", "140 kcal", "Neighborhood"),
            ActivityEntry("Swim", "35 min", "1.0 km", "2026-05-03", "300 kcal", "Community Pool"),
            ActivityEntry("Hike", "62 min", "5.6 km", "2026-05-03", "520 kcal", "Pine Trail"),
            ActivityEntry("Rowing", "25 min", "4.0 km", "2026-05-02", "260 kcal", "River Dock"),
            ActivityEntry("HIIT", "20 min", null, "2026-05-02", "240 kcal", "Home"),
            ActivityEntry("Pilates", "40 min", null, "2026-05-01", "190 kcal", "Studio B"),
            ActivityEntry("Elliptical", "30 min", "5.0 km", "2026-05-01", "280 kcal", "Fitness Center"),
            ActivityEntry("Basketball", "50 min", null, "2026-04-30", "420 kcal", "Community Court"),
            ActivityEntry("Soccer", "70 min", null, "2026-04-30", "560 kcal", "East Field"),
            ActivityEntry("Tennis", "55 min", null, "2026-04-29", "410 kcal", "West Courts"),
            ActivityEntry("Stair Climb", "18 min", "45 floors", "2026-04-29", "200 kcal", "Office Tower"),
            ActivityEntry("Boxing", "40 min", null, "2026-04-28", "480 kcal", "Boxing Club"),
            ActivityEntry("Jump Rope", "15 min", null, "2026-04-28", "160 kcal", "Home"),
            ActivityEntry("Strength Training", "50 min", null, "2026-04-27", "340 kcal", "Iron Gym"),
            ActivityEntry("Core", "18 min", null, "2026-04-27", "110 kcal", "Home"),
            ActivityEntry("Stretching", "25 min", null, "2026-04-26", "90 kcal", "Home")
        )
    }
}

private data class ActivityEntry(
    val type: String,
    val duration: String,
    val metric: String?,
    val date: String,
    val calories: String,
    val location: String
)

private class ActivityAdapter(
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
                append(item.duration)
                if (!item.metric.isNullOrBlank()) {
                    append(" | Metric: ")
                    append(item.metric)
                }
                append(" | Calories: ")
                append(item.calories)
            }
            meta.text = "Date: ${item.date} | Location: ${item.location}"
        }
    }
}
