package com.mytrack.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {
    private val BASE_URL = "http://api.openweathermap.org/data/2.5/"

    val apiCall: ApiCall by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCall::class.java)
    }

}