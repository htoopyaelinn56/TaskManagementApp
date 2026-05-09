package com.example.taskmanagementapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.taskmanagementapp.model.ActivityEntry
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.ui.ActivityAdapter
import com.example.taskmanagementapp.network.Http
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.appbar.MaterialToolbar

class ActivitiesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activities)
        applyEdgeToEdge(R.id.activities_root)
        setupToolbar(R.id.activities_toolbar, getString(R.string.activities_title), showBack = true)

        val activitiesList = findViewById<RecyclerView>(R.id.activities_list)
        activitiesList.layoutManager = LinearLayoutManager(this)
        // start with an empty adapter while we load real data
        activitiesList.adapter = ActivityAdapter(listOf())
        fetchActivities(activitiesList)
    }

    private fun fetchActivities(recyclerView: RecyclerView) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId <= 0) {
            // not logged in - show sample activities
            recyclerView.adapter = ActivityAdapter(listOf<ActivityEntry>())
            return
        }

        Http.api.getActivities(userId).enqueue(object : Callback<List<com.example.taskmanagementapp.network.ActivityResponse>> {
            override fun onResponse(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, response: Response<List<com.example.taskmanagementapp.network.ActivityResponse>>) {
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val activities = body.map { ar ->
                        val metric = if (ar.metricValue != null) Metric(ar.metricValue, ar.metricUnit ?: "") else null
                        ActivityEntry(
                            id = ar.id,
                            type = ar.type,
                            durationMinutes = ar.durationMinutes,
                            metric = metric,
                            date = ar.date,
                            caloriesKcal = ar.caloriesKcal,
                            location = ar.location
                        )
                    }

                    recyclerView.adapter = ActivityAdapter(activities, showDelete = true, onDelete = { id ->
                        deleteActivity(id, recyclerView)
                    })
                } else {
                    Toast.makeText(this@ActivitiesActivity, "Failed to load activities: ${response.code()}", Toast.LENGTH_SHORT).show()
                    recyclerView.adapter = ActivityAdapter(listOf())
                }
            }

            override fun onFailure(call: Call<List<com.example.taskmanagementapp.network.ActivityResponse>>, t: Throwable) {
                Toast.makeText(this@ActivitiesActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                recyclerView.adapter = ActivityAdapter(listOf())
            }
        })
    }

    private fun deleteActivity(activityId: Int, recyclerView: RecyclerView) {
        Http.api.deleteActivity(activityId).enqueue(object : Callback<com.example.taskmanagementapp.network.CreateDeleteResponse> {
            override fun onResponse(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, response: Response<com.example.taskmanagementapp.network.CreateDeleteResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        Toast.makeText(this@ActivitiesActivity, body.message, Toast.LENGTH_SHORT).show()
                        // refresh list
                        fetchActivities(recyclerView)
                        // notify HomeActivity to refresh recent activities
                        val intent = android.content.Intent("com.example.taskmanagementapp.ACTION_ACTIVITIES_UPDATED")
                        intent.`package` = packageName
                        sendBroadcast(intent)
                    } else {
                        Toast.makeText(this@ActivitiesActivity, "Failed to delete: ${body?.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ActivitiesActivity, "Failed to delete: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, t: Throwable) {
                Toast.makeText(this@ActivitiesActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}