package com.example.taskmanagementapp.network

import com.google.gson.annotations.SerializedName

data class CreateDeleteResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

