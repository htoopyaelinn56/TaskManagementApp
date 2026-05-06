package com.example.taskmanagementapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
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
