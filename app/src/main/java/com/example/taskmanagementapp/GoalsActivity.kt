package com.example.taskmanagementapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.ui.GoalAdapter
import com.example.taskmanagementapp.network.GoalResponse
import com.example.taskmanagementapp.network.Http
import com.example.taskmanagementapp.model.Metric as ModelMetric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.appbar.MaterialToolbar
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList

class GoalsActivity : BaseActivity() {
    private lateinit var goalsList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        applyEdgeToEdge(R.id.goals_root)
        setupToolbar(R.id.goals_toolbar, getString(R.string.goals_title), showBack = true)

        goalsList = findViewById<RecyclerView>(R.id.goals_list)
        goalsList.layoutManager = LinearLayoutManager(this)
        goalsList.adapter = GoalAdapter(emptyList(), showDelete = true, onDelete = null)

        // fetch all goals for the logged-in user (or fallback to sample data)
        fetchGoals(goalsList)
    }

    override fun onStart() {
        super.onStart()
        if (::goalsList.isInitialized) {
            fetchGoals(goalsList)
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
        // we need activities to compute progress for goals -> fetch activities first
        Http.api.getActivities(userId).enqueue(object : Callback<List<com.example.taskmanagementapp.network.ActivityResponse>> {
            override fun onResponse(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, response: Response<List<com.example.taskmanagementapp.network.ActivityResponse>>) {
                val activitiesBody = if (response.isSuccessful) response.body() ?: kotlin.collections.emptyList() else kotlin.collections.emptyList()

                // now fetch goals and compute progress/status using activitiesBody
                Http.api.getGoals(userId).enqueue(object : Callback<List<GoalResponse>> {
                    override fun onResponse(call: Call<List<GoalResponse>>, response: Response<List<GoalResponse>>) {
                        if (response.isSuccessful) {
                            val body = response.body() ?: kotlin.collections.emptyList()
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val today = java.time.LocalDate.now()

                            val goals = body.map { gr ->
                                val metric = if (gr.targetValue != null) ModelMetric(gr.targetValue, gr.targetUnit ?: "") else null

                                // compute sum of matching activities
                                val sumForGoal = if (metric != null) {
                                    activitiesBody.filter { ar ->
                                        // match activity type
                                        ar.type == gr.activityType &&
                                                // match metric unit
                                                (ar.metricUnit ?: "") == (gr.targetUnit ?: "") &&
                                                // activity date must be on or before deadline if deadline provided
                                                runCatching {
                                                    if (gr.deadline.isBlank()) true
                                                    else {
                                                        val actDate = java.time.LocalDate.parse(ar.date, dateFormatter)
                                                        val dl = java.time.LocalDate.parse(gr.deadline, dateFormatter)
                                                        !actDate.isAfter(dl)
                                                    }
                                                }.getOrDefault(false)
                                    }.sumOf { it.metricValue ?: 0.0 }
                                } else 0.0

                                val progressFraction: Double? = if (metric != null && metric.value > 0.0) {
                                    val frac = (sumForGoal / metric.value).coerceAtMost(1.0)
                                    // normalize small negative rounding
                                    if (frac < 0.0) 0.0 else frac
                                } else null

                                // determine status
                                val status = when {
                                    progressFraction != null && progressFraction >= 1.0 -> "completed"
                                    gr.deadline.isNotBlank() -> {
                                        val dl = runCatching { java.time.LocalDate.parse(gr.deadline, dateFormatter) }.getOrNull()
                                        if (dl != null && today.isAfter(dl) && (progressFraction == null || progressFraction < 1.0)) {
                                            "missed"
                                        } else {
                                            "pending"
                                        }
                                    }
                                    else -> if (progressFraction != null && progressFraction >= 1.0) "completed" else "pending"
                                }

                                Goal(
                                    id = gr.id,
                                    name = gr.name,
                                    activityType = gr.activityType,
                                    targetMetric = metric,
                                    deadline = gr.deadline,
                                    notes = gr.notes,
                                    status = status,
                                    progress = progressFraction
                                )
                            }

                            val recent = goals.take(5)
                            recyclerView.adapter = GoalAdapter(recent, showDelete = false, onDelete = null)
                        } else {
                            Toast.makeText(this@GoalsActivity, "Failed to load goals: ${response.code()}", Toast.LENGTH_SHORT).show()
                            val goals = listOf<Goal>()
                            recyclerView.adapter = GoalAdapter(goals.take(5), showDelete = false, onDelete = null)
                        }
                    }

                    override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                        Toast.makeText(this@GoalsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                        val goals = listOf<Goal>()
                        recyclerView.adapter = GoalAdapter(goals.take(5), showDelete = false, onDelete = null)
                    }
                })
            }

            override fun onFailure(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Network error fetching activities: ${t.message}", Toast.LENGTH_SHORT).show()
                // fallback to fetching goals without progress
                Http.api.getGoals(userId).enqueue(object : Callback<List<GoalResponse>> {
                    override fun onResponse(call: Call<List<GoalResponse>>, response: Response<List<GoalResponse>>) {
                        if (response.isSuccessful) {
                            val body = response.body() ?: kotlin.collections.emptyList()
                            val goals = body.map { gr ->
                                val metric = if (gr.targetValue != null) ModelMetric(gr.targetValue, gr.targetUnit ?: "") else null
                                Goal(
                                    id = gr.id,
                                    name = gr.name,
                                    activityType = gr.activityType,
                                    targetMetric = metric,
                                    deadline = gr.deadline,
                                    notes = gr.notes,
                                    status = gr.status,
                                    progress = null
                                )
                            }
                            recyclerView.adapter = GoalAdapter(goals.take(5), showDelete = false, onDelete = null)
                        } else {
                            recyclerView.adapter = GoalAdapter(listOf<Goal>().take(5), showDelete = false, onDelete = null)
                        }
                    }

                    override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                        recyclerView.adapter = GoalAdapter(listOf<Goal>().take(5), showDelete = false, onDelete = null)
                    }
                })
            }
        })
    }

    private fun deleteGoal(goalId: Int, recyclerView: RecyclerView) {
        Http.api.deleteGoal(goalId).enqueue(object : Callback<com.example.taskmanagementapp.network.CreateDeleteResponse> {
            override fun onResponse(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, response: Response<com.example.taskmanagementapp.network.CreateDeleteResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        Toast.makeText(this@GoalsActivity, body.message, Toast.LENGTH_SHORT).show()
                        // refetch goals for this screen
                        fetchGoals(recyclerView)
                        // notify other parts of this@GoalsActivity) to refresh recent goals
                        val intent = android.content.Intent("com.example.taskmanagementapp.ACTION_GOALS_UPDATED")
                        // restrict broadcast to this app only to avoid any external components
                        intent.`package` = packageName
                        sendBroadcast(intent)
                    } else {
                        Toast.makeText(this@GoalsActivity, "Failed to delete: ${body?.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to delete: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
