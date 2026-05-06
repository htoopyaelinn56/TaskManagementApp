package com.example.taskmanagementapp

import android.content.Intent
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
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        val activities = getActivities()
        val recentList = findViewById<RecyclerView>(R.id.home_recent_list)
        recentList.layoutManager = LinearLayoutManager(this)
        recentList.adapter = ActivityAdapter(activities)

        setupWeeklyChart(activities)

        val addFab = findViewById<FloatingActionButton>(R.id.home_add_fab)
        addFab.setOnClickListener {
            showQuickActionsDialog()
        }
    }

    private fun showQuickActionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_home_quick_actions, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.home_dialog_set_goal).setOnClickListener {
            startActivity(Intent(this, GoalSetActivity::class.java))
            dialog.dismiss()
        }
        dialogView.findViewById<MaterialButton>(R.id.home_dialog_add_workout).setOnClickListener {
            startActivity(Intent(this, WorkoutLogActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupWeeklyChart(activities: List<ActivityEntry>) {
        val chart = findViewById<CombinedChart>(R.id.home_weekly_chart)
        val dailyStats = buildWeeklyStats(activities)
        val dayLabels = dailyStats.map { it.label }

        val timeEntries = dailyStats.mapIndexed { index, stat ->
            BarEntry(index.toFloat(), stat.totalMinutes.toFloat())
        }
        val caloriesEntries = dailyStats.mapIndexed { index, stat ->
            Entry(index.toFloat(), stat.totalCalories.toFloat())
        }

        val timeDataSet = BarDataSet(timeEntries, "Minutes")
        timeDataSet.color = getColor(R.color.primary)
        timeDataSet.valueTextSize = 10f
        timeDataSet.setDrawValues(false)

        val caloriesDataSet = LineDataSet(caloriesEntries, "Calories")
        caloriesDataSet.color = getColor(R.color.black)
        caloriesDataSet.circleRadius = 3.5f
        caloriesDataSet.setCircleColor(getColor(R.color.black))
        caloriesDataSet.valueTextSize = 10f
        caloriesDataSet.lineWidth = 2f

        val combinedData = CombinedData().apply {
            setData(BarData(timeDataSet).apply {
                barWidth = 0.6f
            })
            setData(LineData(caloriesDataSet))
        }

        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.granularity = 1f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = dailyStats.size - 0.5f
        chart.xAxis.valueFormatter = DayLabelFormatter(dayLabels)
        chart.legend.isEnabled = true
        chart.data = combinedData
        chart.invalidate()
    }

    private fun buildWeeklyStats(activities: List<ActivityEntry>): List<DailyStat> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val parsedDates = activities.mapNotNull { entry ->
            runCatching { LocalDate.parse(entry.date, dateFormatter) }.getOrNull()
        }
        val endDate = parsedDates.maxOrNull() ?: LocalDate.now()
        val startDate = endDate.minusDays(6)
        val range = (0..6).map { startDate.plusDays(it.toLong()) }

        return range.map { day ->
            val dayEntries = activities.filter { it.date == day.format(dateFormatter) }
            val totalMinutes = dayEntries.sumOf { parseMinutes(it.duration) }
            val totalCalories = dayEntries.sumOf { parseCalories(it.calories) }
            DailyStat(
                label = day.format(dayFormatter),
                totalMinutes = totalMinutes,
                totalCalories = totalCalories
            )
        }
    }

    private fun parseMinutes(duration: String): Int {
        return duration.substringBefore(" ").toIntOrNull() ?: 0
    }

    private fun parseCalories(calories: String): Int {
        return calories.substringBefore(" ").toIntOrNull() ?: 0
    }

    private fun getActivities(): List<ActivityEntry> {
        return listOf(
            // Today - May 6, 2026
            ActivityEntry("Run", "22 min", "3.4 km", "2026-05-06", "210 kcal", "City Park"),
            ActivityEntry("Core", "305 min", null, "2026-05-06", "100 kcal", "Home"),

            // May 5, 2026
            ActivityEntry("Cycling", "38 min", "12.1 km", "2026-05-05", "360 kcal", "Riverside Trail"),

            // May 4, 2026
            ActivityEntry("Yoga", "30 min", null, "2026-05-04", "120 kcal", "Studio A"),
            ActivityEntry("Walk", "28 min", "2.2 km", "2026-05-04", "140 kcal", "Neighborhood"),
            ActivityEntry("Weightlifting", "45 min", null, "2026-05-04", "280 kcal", "Iron Gym"),

            // May 3, 2026
            ActivityEntry("Swim", "35 min", "1.0 km", "2026-05-03", "300 kcal", "Community Pool"),
            ActivityEntry("Hike", "62 min", "5.6 km", "2026-05-03", "520 kcal", "Pine Trail"),

            // May 2, 2026
            ActivityEntry("Rowing", "25 min", "4.0 km", "2026-05-02", "260 kcal", "River Dock"),
            ActivityEntry("HIIT", "20 min", null, "2026-05-02", "240 kcal", "Home"),
            ActivityEntry("Stretching", "10 min", null, "2026-05-02", "40 kcal", "Home"),

            // May 1, 2026
            ActivityEntry("Pilates", "40 min", null, "2026-05-01", "190 kcal", "Studio B"),
            ActivityEntry("Elliptical", "30 min", "5.0 km", "2026-05-01", "280 kcal", "Fitness Center"),

            // April 30, 2026
            ActivityEntry("Basketball", "50 min", null, "2026-04-30", "420 kcal", "Community Court"),
            ActivityEntry("Soccer", "70 min", null, "2026-04-30", "560 kcal", "East Field"),

            // April 29, 2026
            ActivityEntry("Tennis", "55 min", null, "2026-04-29", "410 kcal", "West Courts"),
            ActivityEntry("Stair Climb", "18 min", "45 floors", "2026-04-29", "200 kcal", "Office Tower"),
            ActivityEntry("Jump Rope", "10 min", null, "2026-04-29", "110 kcal", "Home")
        )
    }
}

private data class DailyStat(
    val label: String,
    val totalMinutes: Int,
    val totalCalories: Int
)

private class DayLabelFormatter(
    private val labels: List<String>
) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = value.toInt()
        return labels.getOrNull(index) ?: ""
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
