package com.example.taskmanagementapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.taskmanagementapp.model.ActivityTypes
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.model.formatMetric
import com.example.taskmanagementapp.ui.GoalAdapter
import com.example.taskmanagementapp.network.GoalResponse
import com.example.taskmanagementapp.network.RetrofitClient
import com.example.taskmanagementapp.model.Metric as ModelMetric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.appbar.MaterialToolbar

class GoalsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goals)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goals_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.goals_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.goals_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val goalsList = findViewById<RecyclerView>(R.id.goals_list)
        goalsList.layoutManager = LinearLayoutManager(this)
        goalsList.adapter = GoalAdapter(emptyList())

        // fetch all goals for the logged-in user (or fallback to sample data)
        fetchGoals(goalsList)
    }

    private fun fetchGoals(recyclerView: RecyclerView) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId <= 0) {
            // no logged in user - show sample goals
            recyclerView.adapter = GoalAdapter(getGoals())
            return
        }

        RetrofitClient.instance.getGoals(userId).enqueue(object : Callback<List<GoalResponse>> {
            override fun onResponse(call: Call<List<GoalResponse>>, response: Response<List<GoalResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val goals = body.map { gr ->
                        val metric = if (gr.targetValue != null) ModelMetric(gr.targetValue, gr.targetUnit ?: "") else null
                        Goal(
                            name = gr.name,
                            activityType = gr.activityType,
                            targetMetric = metric,
                            deadline = gr.deadline,
                            notes = gr.notes,
                            status = gr.status
                        )
                    }

                    recyclerView.adapter = GoalAdapter(goals)
                } else {
                    Toast.makeText(this@GoalsActivity, "Failed to load goals: ${response.code()}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = GoalAdapter(getGoals())
                }
            }

            override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                Toast.makeText(this@GoalsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                recyclerView.adapter = GoalAdapter(getGoals())
            }
        })
    }

    private fun getGoals(): List<Goal> {
        return listOf(
            Goal(
                "Run 10 km",
                ActivityTypes.RUN,
                Metric(10.0, "km"),
                "2026-05-20",
                "Weekend target",
                "pending"
            ),
            Goal(
                "Yoga 5 sessions",
                ActivityTypes.YOGA,
                Metric(5.0, "sessions"),
                "2026-05-25",
                null,
                "pending"
            ),
            Goal(
                "Walk 30k steps",
                ActivityTypes.WALK,
                Metric(30000.0, "steps"),
                "2026-05-30",
                "Daily average",
                "pending"
            ),
            Goal("Swim 3 km", ActivityTypes.SWIM, Metric(3.0, "km"), "2026-06-01", null, "completed",),
            Goal(
                "Cycle 50 km",
                ActivityTypes.CYCLING,
                Metric(50.0, "km"),
                "2026-06-05",
                "Long ride",
                "pending"
            ),
            Goal(
                "Strength 12 sets",
                ActivityTypes.WEIGHTLIFTING,
                Metric(12.0, "sets"),
                "2026-06-10",
                null,
                "missed"
            )
        )
    }
}
