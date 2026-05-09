package com.example.taskmanagementapp.network

import com.google.gson.annotations.SerializedName

data class ActivityResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("metric_value") val metricValue: Double?,
    @SerializedName("metric_unit") val metricUnit: String?,
    @SerializedName("date") val date: String,
    @SerializedName("calories_kcal") val caloriesKcal: Int,
    @SerializedName("location") val location: String,
    @SerializedName("created_at") val createdAt: String
)

