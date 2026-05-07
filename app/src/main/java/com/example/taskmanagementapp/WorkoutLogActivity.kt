package com.example.taskmanagementapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagementapp.model.ActivityTypeList
import com.example.taskmanagementapp.model.MetricUnitList
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WorkoutLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.workout_log_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.workout_log_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.workout_log_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val activityTypeField = findViewById<MaterialAutoCompleteTextView>(R.id.workout_log_activity_type)
        val metricUnitField = findViewById<MaterialAutoCompleteTextView>(R.id.workout_log_metric_unit)

        val dropdownAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            ActivityTypeList
        )
        activityTypeField.setAdapter(dropdownAdapter)

        val metricUnitAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MetricUnitList
        )
        metricUnitField.setAdapter(metricUnitAdapter)

        val dateField = findViewById<TextInputEditText>(R.id.workout_log_date)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.workout_log_date_picker_title)
            .build()

        val openDatePicker = {
            if (!datePicker.isAdded) {
                datePicker.show(supportFragmentManager, "workout_log_date_picker")
            }
        }

        dateField.setOnClickListener { openDatePicker() }
        findViewById<View>(R.id.workout_log_date_layout).setOnClickListener { openDatePicker() }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(dateFormatter)
            dateField.setText(date)
        }
    }
}
