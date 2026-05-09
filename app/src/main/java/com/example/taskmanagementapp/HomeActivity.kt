package com.example.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.model.sampleActivities
import com.example.taskmanagementapp.ui.ActivityAdapter
import com.example.taskmanagementapp.ui.GoalAdapter
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

        val activities = sampleActivities()
        val recentActivities = activities.take(5)
        val recentList = findViewById<RecyclerView>(R.id.home_recent_list)
        recentList.layoutManager = LinearLayoutManager(this)
        recentList.adapter = ActivityAdapter(recentActivities)

        val goals = getGoals()
        val recentGoals = goals.take(5)
        val recentGoalsList = findViewById<RecyclerView>(R.id.home_recent_goals_list)
        recentGoalsList.layoutManager = LinearLayoutManager(this)
        recentGoalsList.adapter = GoalAdapter(recentGoals)

        findViewById<View>(R.id.home_recent_goals_show_all).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }
        findViewById<View>(R.id.home_recent_activities_show_all).setOnClickListener {
            startActivity(Intent(this, ActivitiesActivity::class.java))
        }

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

    private fun getGoals(): List<Goal> {
        return listOf(
            Goal("Run 10 km", ActivityTypes.RUN, Metric(10.0, "km"), "2026-05-20", "Weekend target", "pending"),
            Goal("Yoga 5 sessions", ActivityTypes.YOGA, Metric(5.0, "sessions"), "2026-05-25", null, "missed"),
            Goal("Walk 30k steps", ActivityTypes.WALK, Metric(30000.0, "steps"), "2026-05-30", "Daily average", "pending"),
            Goal("Cycle 50 km", ActivityTypes.CYCLING, Metric(50.0, "km"), "2026-06-05", "Long ride", "completed")
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
