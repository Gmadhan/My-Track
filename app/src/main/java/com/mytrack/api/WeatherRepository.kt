package com.mytrack.api

import com.mytrack.model.response.WeatherResponse

class WeatherRepository(private val weatherData: WeatherResponse) {
    private val loginUrl = "http://api.openweathermap.org/data/2.5/"

   /* fun makeLoginRequest(
        jsonBody: String
    ): Result<weatherResponse> {
        val url = URL(loginUrl)
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; utf-8")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            outputStream.write(jsonBody.toByteArray())
            return ApiResult.Success(outputStream as weatherResponse)
        }
        return ApiResult.Error(Exception("Cannot open HttpURLConnection"))
    }*/
}