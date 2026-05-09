package com.example.taskmanagementapp.network

open class NetworkClient(protected val apiService: ApiService) {
    open val api: ApiService
        get() = apiService
}

object RetrofitNetworkClient : NetworkClient(RetrofitClient.instance)


object Http {
    var client: NetworkClient = RetrofitNetworkClient
    val api: ApiService get() = client.api
}

