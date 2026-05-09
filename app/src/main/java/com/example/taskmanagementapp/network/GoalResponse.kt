package com.example.taskmanagementapp.network

import com.google.gson.annotations.SerializedName

data class GoalResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("target_value") val targetValue: Double?,
    @SerializedName("target_unit") val targetUnit: String?,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)

