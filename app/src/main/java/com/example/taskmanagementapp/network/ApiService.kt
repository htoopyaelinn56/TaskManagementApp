package com.example.taskmanagementapp.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("auth.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("action") action: String = "login"
    ): Call<LoginResponse>

    @GET("goals.php")
    fun getGoals(
        @Query("user_id") userId: Int
    ): Call<List<GoalResponse>>

    @GET("activities.php")
    fun getActivities(
        @Query("user_id") userId: Int
    ): Call<List<ActivityResponse>>

    @FormUrlEncoded
    @retrofit2.http.HTTP(method = "DELETE", path = "goals.php", hasBody = true)
    fun deleteGoal(
        @Field("id") id: Int
    ): Call<CreateDeleteResponse>

    @FormUrlEncoded
    @POST("goals.php")
    fun createGoal(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("activity_type") activityType: String,
        @Field("target_value") targetValue: Double?,
        @Field("target_unit") targetUnit: String?,
        @Field("deadline") deadline: String?,
        @Field("calories_kcal") caloriesKcal: Int?,
        @Field("notes") notes: String?
    ): Call<CreateDeleteResponse>
}
