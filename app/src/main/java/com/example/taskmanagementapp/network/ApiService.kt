package com.example.taskmanagementapp.network

import retrofit2.Call
import retrofit2.http.DELETE
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

    @FormUrlEncoded
    @DELETE("goals.php")
    fun deleteGoal(
        @Field("id") id: Int,
    ): Call<DeleteResponse>
}
