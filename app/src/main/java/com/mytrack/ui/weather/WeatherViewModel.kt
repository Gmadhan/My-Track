package com.mytrack.ui.weather

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytrack.api.RetrofitClientInstance
import com.mytrack.model.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class WeatherViewModel: ViewModel() {
    var weatherList: MutableLiveData<WeatherResponse>? = MutableLiveData<WeatherResponse>()
    var error: MutableLiveData<String>? = MutableLiveData<String>("")

    fun getWeatherList(lat: String, lng: String, appId: String) {
        viewModelScope.launch {
            RetrofitClientInstance.apiCall.getweather(lat,lng,appId).enqueue(object: retrofit2.Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if(response.isSuccessful)
                        weatherList!!.value = response.body()
                    else
                        error!!.value = if(response.message().isNotEmpty()) response.message().toString() else response.errorBody().toString()
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    error!!.value = t.message
                }
            })
        }
    }
}