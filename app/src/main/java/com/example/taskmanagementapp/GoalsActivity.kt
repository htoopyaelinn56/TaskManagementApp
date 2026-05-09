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
            // no logged in user - show sample goals (delete hidden for sample items because id == null)
            recyclerView.adapter = GoalAdapter(listOf(), showDelete = true, onDelete = null)
            return
        }

        Http.api.getGoals(userId).enqueue(object : Callback<List<GoalResponse>> {
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

                    recyclerView.adapter = GoalAdapter(goals, showDelete = true, onDelete = { id ->
                        deleteGoal(id, recyclerView)
                    })
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to load goals: ${response.code()}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = GoalAdapter(listOf(), showDelete = true, onDelete = null)
                }
            }

            override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                recyclerView.adapter = GoalAdapter(listOf(), showDelete = true, onDelete = null)
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
                        // notify other parts of app (HomeActivity) to refresh recent goals
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
