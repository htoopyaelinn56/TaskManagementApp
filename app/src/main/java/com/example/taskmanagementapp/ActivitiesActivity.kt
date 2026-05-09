package com.example.taskmanagementapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.taskmanagementapp.model.ActivityEntry
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.ui.ActivityAdapter
import com.example.taskmanagementapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.appbar.MaterialToolbar

class ActivitiesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activities)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activities_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.activities_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.activities_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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

        RetrofitClient.instance.getActivities(userId).enqueue(object : Callback<List<com.example.taskmanagementapp.network.ActivityResponse>> {
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

                    recyclerView.adapter = ActivityAdapter(activities)
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
}