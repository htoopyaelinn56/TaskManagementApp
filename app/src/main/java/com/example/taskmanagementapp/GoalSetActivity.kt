package com.example.taskmanagementapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagementapp.model.ActivityTypeList
import com.example.taskmanagementapp.model.Goal
import com.example.taskmanagementapp.model.Metric
import com.example.taskmanagementapp.model.MetricUnitList
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GoalSetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goal_set)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goal_set_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.goal_set_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.goal_set_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val activityTypeField = findViewById<MaterialAutoCompleteTextView>(R.id.goal_set_activity_type)
        val metricUnitField = findViewById<MaterialAutoCompleteTextView>(R.id.goal_set_target_metric_unit)

        val activityTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            ActivityTypeList
        )
        activityTypeField.setAdapter(activityTypeAdapter)

        val metricUnitAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MetricUnitList
        )
        metricUnitField.setAdapter(metricUnitAdapter)

        val dateField = findViewById<TextInputEditText>(R.id.goal_set_deadline)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.goal_set_date_picker_title)
            .build()

        val openDatePicker = {
            if (!datePicker.isAdded) {
                datePicker.show(supportFragmentManager, "goal_set_date_picker")
            }
        }

        dateField.setOnClickListener { openDatePicker() }
        findViewById<View>(R.id.goal_set_deadline_layout).setOnClickListener { openDatePicker() }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(dateFormatter)
            dateField.setText(date)
        }

        val nameField = findViewById<TextInputEditText>(R.id.goal_set_name)
        val targetValueField = findViewById<TextInputEditText>(R.id.goal_set_target_value)
        val notesField = findViewById<TextInputEditText>(R.id.goal_set_notes)

        findViewById<View>(R.id.goal_set_submit).setOnClickListener {
            val targetValue = targetValueField.text?.toString()?.toDoubleOrNull()
            val targetUnit = metricUnitField.text?.toString().orEmpty()
            val metric = if (targetValue != null && targetUnit.isNotBlank()) {
                Metric(targetValue, targetUnit)
            } else {
                null
            }

            val goal = Goal(
                name = nameField.text?.toString().orEmpty(),
                activityType = activityTypeField.text?.toString().orEmpty(),
                targetMetric = metric,
                deadline = dateField.text?.toString().orEmpty(),
                notes = notesField.text?.toString().orEmpty().ifBlank { null },
                status = "pending"
            )
        }
    }
}
