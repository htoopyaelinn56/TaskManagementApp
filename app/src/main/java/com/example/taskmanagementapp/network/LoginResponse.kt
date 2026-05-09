package com.example.taskmanagementapp.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null
)
