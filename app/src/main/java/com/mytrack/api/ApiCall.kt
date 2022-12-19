package com.mytrack.api

import com.mytrack.model.response.WeatherResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiCall {

    /* q=coimbatore,IN&APPID=fede8c6f8a61cfd5c731ec3865d0294c lat={lat}&lon={lon} */
    @POST("weather?")
    fun getweather(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("APPID") APPID: String?
    ): Call<WeatherResponse>

}