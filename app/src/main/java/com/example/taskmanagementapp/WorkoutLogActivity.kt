package com.example.taskmanagementapp

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.taskmanagementapp.network.RetrofitClient
import com.example.taskmanagementapp.network.CreateDeleteResponse
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

class WorkoutLogActivity : AppCompatActivity() {
    // no external Play Services dependency: use Android LocationManager for last-known location
    // pending lambdas to execute submission depending on permission result
    private var pendingSubmission: (() -> Unit)? = null
    private var pendingNoLocationSubmission: (() -> Unit)? = null

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingSubmission?.invoke()
        } else {
            Toast.makeText(this, "Location permission denied; sending without location", Toast.LENGTH_SHORT).show()
            pendingNoLocationSubmission?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_log)
        // nothing to init here for LocationManager
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

        // submit new activity
        findViewById<View>(R.id.workout_log_submit).setOnClickListener {
            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)
            if (userId <= 0) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // collect fields now so they can be captured by lambdas
            val type = activityTypeField.text?.toString().orEmpty()
            val duration = findViewById<TextInputEditText>(R.id.workout_log_duration).text?.toString()?.toIntOrNull() ?: 0
            val metricValue = findViewById<TextInputEditText>(R.id.workout_log_metric_value).text?.toString()?.toDoubleOrNull()
            val metricUnit = metricUnitField.text?.toString().orEmpty().ifBlank { null }
            val dateText = dateField.text?.toString().orEmpty()
            val calories = findViewById<TextInputEditText>(R.id.workout_log_calories).text?.toString()?.toIntOrNull()

            // helper to perform the network call; location can be null
            fun performCreateActivityWithLocation(locationName: String?) {
                RetrofitClient.instance.createActivity(
                    userId,
                    type,
                    duration,
                    metricValue,
                    metricUnit,
                    dateText,
                    calories,
                    locationName
                ).enqueue(object : Callback<CreateDeleteResponse> {
                    override fun onResponse(call: Call<CreateDeleteResponse>, response: Response<CreateDeleteResponse>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.status == "success") {
                                Toast.makeText(this@WorkoutLogActivity, body.message, Toast.LENGTH_SHORT).show()
                                // notify other parts of app to refresh activities
                                val intent = android.content.Intent("com.example.taskmanagementapp.ACTION_ACTIVITIES_UPDATED")
                                intent.`package` = packageName
                                sendBroadcast(intent)
                                finish()
                            } else {
                                Toast.makeText(this@WorkoutLogActivity, "Failed to log activity: ${body?.message ?: "unknown"}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@WorkoutLogActivity, "Failed to log activity: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateDeleteResponse>, t: Throwable) {
                        Toast.makeText(this@WorkoutLogActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            // build two possible pending actions: with location (attempt to fetch + reverse geocode) and without location
            val withLocationAction: () -> Unit = {
                // try to fetch last known location from available providers
                try {
                    val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
                    val providers = locationManager.getProviders(true)
                    var lastLoc: android.location.Location? = null
                    for (p in providers) {
                        val l = try {
                            locationManager.getLastKnownLocation(p)
                        } catch (_: SecurityException) {
                            null
                        }
                        if (l != null) {
                            if (lastLoc == null || l.time > lastLoc.time) lastLoc = l
                        }
                    }
                    if (lastLoc != null) {
                        var locationName: String? = null
                        try {
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val results = geocoder.getFromLocation(lastLoc.latitude, lastLoc.longitude, 1)
                            if (!results.isNullOrEmpty()) {
                                val addr = results[0]
                                locationName = addr.locality ?: addr.thoroughfare ?: addr.subAdminArea ?: addr.adminArea
                            }
                        } catch (e: IOException) {
                            locationName = "${lastLoc.latitude},${lastLoc.longitude}"
                        }
                        performCreateActivityWithLocation(locationName)
                    } else {
                        performCreateActivityWithLocation(null)
                    }
                } catch (e: SecurityException) {
                    performCreateActivityWithLocation(null)
                }
            }

            val withoutLocationAction: () -> Unit = {
                performCreateActivityWithLocation(null)
            }

            pendingSubmission = withLocationAction
            pendingNoLocationSubmission = withoutLocationAction

            // check permission
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // already granted
                    pendingSubmission?.invoke()
                }
                else -> {
                    // request permission; the registered callback will call the appropriate pending action
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }
}
