package com.example.taskmanagementapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagementapp.model.ActivityTypeList
import android.widget.Toast
import com.example.taskmanagementapp.network.Http
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.taskmanagementapp.model.MetricUnitList
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GoalSetActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_set)
        applyEdgeToEdge(R.id.goal_set_root)
        setupToolbar(R.id.goal_set_toolbar, getString(R.string.goal_set_title), showBack = true)

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
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)
            if (userId <= 0) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val targetValue = targetValueField.text?.toString()?.toDoubleOrNull()
            val targetUnit = metricUnitField.text?.toString().orEmpty().ifBlank { null }
            val deadlineText = dateField.text?.toString().orEmpty().ifBlank { null }
            val notesText = notesField.text?.toString().orEmpty().ifBlank { null }
            // calories input may not be present in the layout; leave null if not provided
            val calories: Int? = null

            Http.api.createGoal(
                userId,
                nameField.text?.toString().orEmpty(),
                activityTypeField.text?.toString().orEmpty(),
                targetValue,
                targetUnit,
                deadlineText,
                calories,
                notesText
            ).enqueue(object : Callback<com.example.taskmanagementapp.network.CreateDeleteResponse> {
                override fun onResponse(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, response: Response<com.example.taskmanagementapp.network.CreateDeleteResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.status == "success") {
                            Toast.makeText(this@GoalSetActivity, body.message, Toast.LENGTH_SHORT).show()
                            // notify other parts of the app and finish to return to GoalsActivity
                            val intent = android.content.Intent("com.example.taskmanagementapp.ACTION_GOALS_UPDATED")
                            intent.`package` = packageName
                            sendBroadcast(intent)
                            finish()
                        } else {
                            Toast.makeText(this@GoalSetActivity, "Failed to create goal: ${body?.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@GoalSetActivity, "Failed to create goal!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<com.example.taskmanagementapp.network.CreateDeleteResponse>, t: Throwable) {
                    Toast.makeText(this@GoalSetActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
