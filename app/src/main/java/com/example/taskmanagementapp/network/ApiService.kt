package com.example.taskmanagementapp.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("auth.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("action") action: String = "login"
    ): Call<LoginResponse>
}
