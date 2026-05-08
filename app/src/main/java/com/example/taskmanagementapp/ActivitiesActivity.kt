package com.example.taskmanagementapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagementapp.model.sampleActivities
import com.example.taskmanagementapp.ui.ActivityAdapter
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
        activitiesList.adapter = ActivityAdapter(sampleActivities())
    }
}
