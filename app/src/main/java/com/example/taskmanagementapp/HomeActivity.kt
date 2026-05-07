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
import com.example.taskmanagementapp.model.ActivityEntry
import com.example.taskmanagementapp.model.ActivityTypes
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.model.formatMetric
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
            val totalMinutes = dayEntries.sumOf { it.durationMinutes }
            val totalCalories = dayEntries.sumOf { it.caloriesKcal }
            DailyStat(
                label = day.format(dayFormatter),
                totalMinutes = totalMinutes,
                totalCalories = totalCalories
            )
        }
    }

    private fun getActivities(): List<ActivityEntry> {
        return listOf(
            // Today - May 6, 2026
            ActivityEntry(ActivityTypes.RUN, 22, Metric(3.4, "km"), "2026-05-06", 210, "City Park"),
            ActivityEntry(ActivityTypes.CORE, 305, null, "2026-05-06", 100, "Home"),

            // May 5, 2026
            ActivityEntry(ActivityTypes.CYCLING, 38, Metric(12.1, "km"), "2026-05-05", 360, "Riverside Trail"),

            // May 4, 2026
            ActivityEntry(ActivityTypes.YOGA, 30, null, "2026-05-04", 120, "Studio A"),
            ActivityEntry(ActivityTypes.WALK, 28, Metric(2.2, "km"), "2026-05-04", 140, "Neighborhood"),
            ActivityEntry(ActivityTypes.WEIGHTLIFTING, 45, null, "2026-05-04", 280, "Iron Gym"),

            // May 3, 2026
            ActivityEntry(ActivityTypes.SWIM, 35, Metric(1.0, "km"), "2026-05-03", 300, "Community Pool"),
            ActivityEntry(ActivityTypes.HIKE, 62, Metric(5.6, "km"), "2026-05-03", 520, "Pine Trail"),

            // May 2, 2026
            ActivityEntry(ActivityTypes.ROWING, 25, Metric(4.0, "km"), "2026-05-02", 260, "River Dock"),
            ActivityEntry(ActivityTypes.HIIT, 20, null, "2026-05-02", 240, "Home"),
            ActivityEntry(ActivityTypes.STRETCHING, 10, null, "2026-05-02", 40, "Home"),

            // May 1, 2026
            ActivityEntry(ActivityTypes.PILATES, 40, null, "2026-05-01", 190, "Studio B"),
            ActivityEntry(ActivityTypes.ELLIPTICAL, 30, Metric(5.0, "km"), "2026-05-01", 280, "Fitness Center"),

            // April 30, 2026
            ActivityEntry(ActivityTypes.BASKETBALL, 50, null, "2026-04-30", 420, "Community Court"),
            ActivityEntry(ActivityTypes.SOCCER, 70, null, "2026-04-30", 560, "East Field"),

            // April 29, 2026
            ActivityEntry(ActivityTypes.TENNIS, 55, null, "2026-04-29", 410, "West Courts"),
            ActivityEntry(ActivityTypes.STAIR_CLIMB, 18, Metric(45.0, "floors"), "2026-04-29", 200, "Office Tower"),
            ActivityEntry(ActivityTypes.JUMP_ROPE, 10, null, "2026-04-29", 110, "Home")
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
