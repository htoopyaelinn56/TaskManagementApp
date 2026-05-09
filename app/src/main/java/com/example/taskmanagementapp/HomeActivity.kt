package com.example.taskmanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import android.widget.Toast
import com.example.taskmanagementapp.model.ActivityEntry
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.model.Metric as ModelMetric
import com.example.taskmanagementapp.network.GoalResponse
import com.example.taskmanagementapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.taskmanagementapp.ui.ActivityAdapter
import com.example.taskmanagementapp.ui.GoalAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var recentGoalsList: RecyclerView
    private lateinit var recentActivitiesList: RecyclerView

    private val updatesReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            when (intent?.action) {
                "com.example.taskmanagementapp.ACTION_GOALS_UPDATED" -> {
                    if (::recentGoalsList.isInitialized) fetchGoals(recentGoalsList)
                }
                "com.example.taskmanagementapp.ACTION_ACTIVITIES_UPDATED" -> {
                    if (::recentActivitiesList.isInitialized) fetchActivities(recentActivitiesList)
                }
            }
        }
    }

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

        recentActivitiesList = findViewById<RecyclerView>(R.id.home_recent_list)
        recentActivitiesList.layoutManager = LinearLayoutManager(this)
        // start with empty adapter while we fetch real activities
        recentActivitiesList.adapter = ActivityAdapter(listOf())
        // fetch recent activities from API (or fallback to sample)
        fetchActivities(recentActivitiesList)

        recentGoalsList = findViewById<RecyclerView>(R.id.home_recent_goals_list)
        recentGoalsList.layoutManager = LinearLayoutManager(this)
        // initially empty adapter until data loads
        recentGoalsList.adapter = GoalAdapter(emptyList())

        // fetch goals from API (or fallback to sample if not logged in)
        fetchGoals(recentGoalsList)

        // receiver registration moved to onStart so it is active only while HomeActivity is visible

        findViewById<View>(R.id.home_recent_goals_show_all).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }
        findViewById<View>(R.id.home_recent_activities_show_all).setOnClickListener {
            startActivity(Intent(this, ActivitiesActivity::class.java))
        }

        val addFab = findViewById<FloatingActionButton>(R.id.home_add_fab)
        addFab.setOnClickListener {
            showQuickActionsDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // clear stored user id from SharedPreferences
                val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                sharedPref.edit().remove("user_id").apply()

                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun fetchActivities(recyclerView: RecyclerView) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId <= 0) {
            // not logged in - show sample activities
            val activities = listOf<ActivityEntry>()
            recyclerView.adapter = ActivityAdapter(activities.take(5))
            return
        }

        RetrofitClient.instance.getActivities(userId).enqueue(object : Callback<List<com.example.taskmanagementapp.network.ActivityResponse>> {
            override fun onResponse(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, response: Response<List<com.example.taskmanagementapp.network.ActivityResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val activities = body.map { ar ->
                        val metric = if (ar.metricValue != null) com.example.taskmanagementapp.model.Metric(ar.metricValue, ar.metricUnit ?: "") else null
                        com.example.taskmanagementapp.model.ActivityEntry(
                            id = ar.id,
                            type = ar.type,
                            durationMinutes = ar.durationMinutes,
                            metric = metric,
                            date = ar.date,
                            caloriesKcal = ar.caloriesKcal,
                            location = ar.location
                        )
                    }

                    setupWeeklyChart(activities)

                    recyclerView.adapter = ActivityAdapter(activities.take(5))
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load activities: ${response.code()}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = ActivityAdapter(listOf<ActivityEntry>().take(5))
                }
            }

            override fun onFailure(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                recyclerView.adapter = ActivityAdapter(listOf<ActivityEntry>().take(5))
            }
        })
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

    private fun fetchGoals(recyclerView: RecyclerView) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId <= 0) {
            // no logged in user - show sample goals
            val goals = listOf<Goal>()
            val recentGoals = goals.take(5)
            recyclerView.adapter = GoalAdapter(recentGoals, showDelete = false, onDelete = null)
            return
        }

        RetrofitClient.instance.getGoals(userId).enqueue(object : Callback<List<GoalResponse>> {
            override fun onResponse(call: Call<List<GoalResponse>>, response: Response<List<GoalResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val goals = body.map { gr ->
                        val metric = if (gr.targetValue != null) ModelMetric(gr.targetValue, gr.targetUnit ?: "") else null
                        Goal(
                            id = gr.id,
                            name = gr.name,
                            activityType = gr.activityType,
                            targetMetric = metric,
                            deadline = gr.deadline,
                            notes = gr.notes,
                            status = gr.status
                        )
                    }

                    val recent = goals.take(5)
                    recyclerView.adapter = GoalAdapter(recent, showDelete = false, onDelete = null)
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load goals: ${response.code()}", Toast.LENGTH_SHORT).show()
                    val goals = listOf<Goal>()
                    recyclerView.adapter = GoalAdapter(goals.take(5), showDelete = false, onDelete = null)
                }
            }

            override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                val goals = listOf<Goal>()
                recyclerView.adapter = GoalAdapter(goals.take(5), showDelete = false, onDelete = null)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updatesReceiver)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = android.content.IntentFilter().apply {
            addAction("com.example.taskmanagementapp.ACTION_GOALS_UPDATED")
            addAction("com.example.taskmanagementapp.ACTION_ACTIVITIES_UPDATED")
        }
        // register receiver while activity is visible; mark not exported for platform requirements
        registerReceiver(updatesReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        // ensure recent goals and activities are refreshed when activity becomes visible
        if (::recentGoalsList.isInitialized) fetchGoals(recentGoalsList)
        if (::recentActivitiesList.isInitialized) fetchActivities(recentActivitiesList)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(updatesReceiver)
        } catch (e: Exception) {
            // ignore
        }
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
